package net.slate

import javax.swing.{ Icon, ImageIcon, JOptionPane, KeyStroke, SwingUtilities, JFileChooser }
import javax.swing.event.{ DocumentListener, DocumentEvent }
import javax.swing.filechooser.FileFilter
import java.awt.{ BorderLayout, Font }
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import java.io._
import scala.actors.Actor._
import scala.util.Properties._
import scala.tools.nsc.{ Interpreter, Settings, InterpreterResults }
import swing._
import net.slate.builder.ScalaBuilder

object Actions {

  import Launch._

  var writeToRepl = actor {

    def handleError(res: String) {
      println("Error: " + res)
    }

    def isToReader(is: InputStream) = new BufferedReader(new InputStreamReader(is))

    def osToWriter(is: OutputStream) = new PrintWriter(new OutputStreamWriter(is))

    val replIs = new PipedInputStream(4096)
    val writer = new StringWriter()

    val settings = new Settings(handleError _)
    val interp = new Interpreter(settings, new PrintWriter(writer))

    val readFromRepl = actor {
      val reader = isToReader(outputIs)
      loop {
        val line = reader.readLine()
        if (line != null) {
          outputPane.text += (line + "\n")
          outputPane.caret.position = outputPane.text.length
        } else exit()
      }
    }

    loop {
      receive {
        case ('Normal, script: String) =>
          println(script)

          interp.interpret(script)

          println(writer.getBuffer);
          if (script == ":q") exit()
      }
    }
  }

  def registerAction(title: String, acce: String = "", ico: Icon = null)(action: => Unit) = new Action(title) {
    val shortcut = if (isMac && acce.contains("control"))
      acce.replace("control", "meta")
    else acce

    accelerator = Some(KeyStroke.getKeyStroke(shortcut))

    icon = ico

    def apply() {
      action
    }
  }

  def classPathAction(configure: JFileChooser => Unit) {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))
    configure(fileChooser)
    fileChooser.showOpenDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        //        ClasspathManager.addExtraClasspathTo(currentScalaVersion, fileChooser.getSelectedFiles)
        //        ClassLoaderManager.reset(currentScalaVersion)
        lastFileOperationDirectory = Some(fileChooser.getSelectedFiles.apply(0).getParent)
        resetReplAction.apply()
        updateStatusBar("Classpath changed. Repl reset.")
      case _ =>
    }
  }

  val addArtifactsAction = registerAction("Add Artifacts ...", "control I") {}

  val showClassPathAction = registerAction("Show Added ClassPath ...", "control K") {}

  val addJarsAction = registerAction("Add Jars ...", "control J")(classPathAction { fileChooser =>
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    fileChooser.setFileHidingEnabled(false)
    fileChooser.setMultiSelectionEnabled(true)
    fileChooser.setFileFilter(new FileFilter() {
      def accept(pathname: File) = pathname.isDirectory || pathname.getName.endsWith(".jar")

      def getDescription = "*.jar"
    })
  })
  val addClassesAction = registerAction("Add Classes ...", "control d")(classPathAction { fileChooser =>
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    fileChooser.setFileHidingEnabled(false)
    fileChooser.setMultiSelectionEnabled(true)
  })

  val openAction = registerAction("Open", "control O", new ImageIcon("images/open.gif")) {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))

    fileChooser.showOpenDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        val file = fileChooser.getSelectedFile
        val reader = new BufferedReader(new FileReader(file))
        currentScript.text.text = ""
        var eof = false
        while (!eof) {
          val line = reader.readLine()
          if (line == null) eof = true
          else currentScript.text.text = currentScript.text.text + line + "\n"
        }
        reader.close()
        lastFileOperationDirectory = Some(file.getParent)
      case _ =>
    }

  }

  val saveAction = registerAction("Save", "control S", new ImageIcon("images/save_edit.gif")) {
    val writer = new BufferedWriter(new FileWriter(currentScript.text.path))
    writer.write(currentScript.text.text)
    writer.close()
    
    ScalaBuilder.build
  }

  val saveAsAction = registerAction("Save As", "F5", new ImageIcon("images/saveas_edit.gif")) {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))
    fileChooser.showSaveDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        val file = fileChooser.getSelectedFile
        val writer = new BufferedWriter(new FileWriter(file))
        writer.write(currentScript.text.text)
        writer.close()
        lastFileOperationDirectory = Some(file.getParent)
      case _ =>
    }
  }

  def runScript(mode: Symbol, selectedOnly: Boolean = false) {
    val toRun = if (selectedOnly) {
      Option(currentScript.text.selected) getOrElse ""
    } else currentScript.text.text
    if (toRun.nonEmpty)
      writeToRepl ! (mode, toRun)
  }

  val runAction = registerAction("Run", "control R") { runScript('Normal) }
  val runSelectedAction = registerAction("Run selected", "control shift R") { runScript('Normal, true) }
  val clearScriptAction = registerAction("Clear", "control L") {
    currentScript.text.text = ""
  }
  val clearOutputAction = registerAction("Clear", "control E") {
    outputPane.text = ""
  }
  val resetReplAction = registerAction("Reset", "control shift E") {}
  val newTabAction = registerAction("New Tab", "control T") {
	  val name = "Script" + (tabPane.pages.length + 1) + ".scala";
    addTab(name, "." + File.separator + name)
  }
  val closeTabAction = registerAction("Close Tab", "control F4") {
    if (tabPane.pages.length > 1)
      closeTab
    else updateStatusBar("At lease 1 tab needed.")
  }

  // See key reaction definition in textPane. shortcut defined here has no effect.
  val switchTabAction = registerAction("Switch Tab", "control TAB") {
    tabPane.selection.index = tabPane.selection.index match {
      case c if c == tabPane.pages.length - 1 => 0
      case current => current + 1
    }
  }

  object UndoAction extends UpdateCaretListener("Undo") with PropertyChangeListener {
    accelerator = Some(KeyStroke.getKeyStroke("control Z"))
    enabled = false

    override def apply() {
      val undoManager = currentScript.text.undoManager
      undoManager.undo()
      enabled = undoManager.canUndo
      RedoAction.enabled = undoManager.canRedo
      super.apply()
    }

    override def propertyChange(evt: PropertyChangeEvent) {
      val undoManager = currentScript.text.undoManager
      enabled = undoManager.canUndo
    }
  }

  object RedoAction extends UpdateCaretListener("Redo") with PropertyChangeListener {
    title = "Redo"
    accelerator = Some(KeyStroke.getKeyStroke("control Y"))
    enabled = false

    override def apply() {
      val undoManager = currentScript.text.undoManager
      undoManager.redo()
      enabled = undoManager.canRedo
      UndoAction.enabled = undoManager.canUndo
      super.apply()
    }

    override def propertyChange(evt: PropertyChangeEvent) {
      val undoManager = currentScript.text.undoManager
      enabled = undoManager.canRedo
    }
  }

  class UpdateCaretListener(title: String) extends Action(title) with DocumentListener {
    var lastUpdate: Int = _

    def apply() {
      currentScript.text.caret.position = lastUpdate
    }

    def changedUpdate(e: DocumentEvent) {}

    def removeUpdate(e: DocumentEvent) {
      lastUpdate = e.getOffset
    }

    def insertUpdate(e: DocumentEvent) {
      lastUpdate = e.getOffset + e.getLength
    }
  }
}

object MainMenuBar extends MenuBar {
  peer.setLayout(new BorderLayout)
  peer.add(new MenuBar {

    import Actions._

    contents += new Menu("File") {
      contents += new MenuItem(openAction)
      contents += new MenuItem(saveAction)
      contents += new MenuItem(saveAsAction)
      contents += new MenuItem(clearScriptAction)
      contents += new MenuItem(newTabAction)
      contents += new MenuItem(closeTabAction)
    }

    contents += new Menu("Script") {
      contents += new MenuItem(runAction)
      contents += new MenuItem(runSelectedAction)
      contents += new Separator
      contents += new MenuItem(clearOutputAction)
      contents += new MenuItem(resetReplAction)
      contents += new Separator
      contents += new Menu("Add To Classpath") {
        contents += new MenuItem(addJarsAction)
        contents += new MenuItem(addClassesAction)
        contents += new MenuItem(addArtifactsAction)
      }
      contents += new MenuItem(showClassPathAction)
    }
    contents += new Menu("Edit") {
      contents += new MenuItem(UndoAction)
      contents += new MenuItem(RedoAction)
    }
  }.peer, BorderLayout.CENTER)
}
