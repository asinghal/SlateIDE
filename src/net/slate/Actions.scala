/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 20th September 2011
 */
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
import net.slate.builder.{ JavaBuilder, ScalaBuilder }

/**
 * Defines the main menu actions and Scala interpreter usage.
 *
 * @author Aishwarya Singhal
 */
object Actions {

  import Launch._

  /**
   * Actor that enables interaction with the Scala interpreter. It listens to messages that might
   * come in from user, runs them on the interpreter and prints the output to the console
   * (tab on the bottom of the editor).
   */
  var writeToRepl = actor {

    // handle any errors - simply put them on the console.
    def handleError(res: String) {
      println("Error: " + res)
    }

    // build a stream reader
    def isToReader(is: InputStream) = new BufferedReader(new InputStreamReader(is))

    // build a stream writer
    def osToWriter(is: OutputStream) = new PrintWriter(new OutputStreamWriter(is))

    // writer that will put received output to the console. 
    val writer = new StringWriter()

    val settings = new Settings(handleError _)
    // create an instance of the interpreter
    val interp = new Interpreter(settings, new PrintWriter(writer))

    // actor that interpreter output
    val readFromRepl = actor {
      val reader = isToReader(outputIs)
      loop {
        val line = reader.readLine()
        if (line != null) {
          outputPane.pane.text += (line + "\n")
          outputPane.pane.caret.position = outputPane.pane.text.length
        } else exit()
      }
    }

    // start the listener and wait for messages to arrive
    loop {
      receive {
        case ('Normal, script: String) =>
          // dump the user input to the console
          println(script)

          // run the interpreter
          interp.interpret(script)

          // now print the received responses
          println(writer.getBuffer);
          if (script == ":q") exit()
      }
    }
  }

  /**
   * Register an action and build a Scala swing Action object.
   *
   *  @param title
   *  @param accelerator
   *  @param icon
   *  @param action body
   */
  private def registerAction(title: String, acce: String = "", ico: Icon = null)(action: => Unit) = new Action(title) {
    val shortcut = if (isMac && acce.contains("control"))
      acce.replace("control", "meta")
    else acce

    accelerator = Some(KeyStroke.getKeyStroke(shortcut))

    icon = ico

    def apply() {
      action
    }
  }

  /**
   * Register the open file action.
   */
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

  /**
   * Register the save file action. Saves the file and runs Java and Scala builds. Also reports any
   * error on the problems tab.
   */
  val saveAction = registerAction("Save", "control S", new ImageIcon("images/save_edit.gif")) {
    if (currentScript.text.undoManager.hasChangedSinceLastSave) {
      saveFile
      bottomTabPane.problems.clear
      actor {
        JavaBuilder.build.foreach { msg => bottomTabPane.problems.add(msg.description, msg.file, msg.line, msg.projectName, msg.path, msg.problemType) }
        ScalaBuilder.build.foreach { msg => bottomTabPane.problems.add(msg.description, msg.file, msg.line, msg.projectName, msg.path, msg.problemType) }
        bottomTabPane.selection.index = 1
      }
    }
  }

  /**
   * Save the file to the disk.
   */
  def saveFile = {
    val writer = new BufferedWriter(new FileWriter(currentScript.text.path))
    writer.write(currentScript.text.text)
    writer.close()
    currentScript.text.undoManager.save
  }

  /**
   * Save "file as" action. 
   */
  val saveAsAction = registerAction("Save As", "F5", new ImageIcon("images/saveas_edit.gif")) {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))
    fileChooser.showSaveDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        val file = fileChooser.getSelectedFile
        val writer = new BufferedWriter(new FileWriter(file))
        writer.write(currentScript.text.text)
        writer.close()
        currentScript.text.undoManager.discardAllEdits
        lastFileOperationDirectory = Some(file.getParent)
      case _ =>
    }
  }

  /**
   * Runs the current script on the interpreter.
   */
  def runScript(mode: Symbol, selectedOnly: Boolean = false) {
    val toRun = if (selectedOnly) {
      Option(currentScript.text.selected) getOrElse ""
    } else currentScript.text.text
    if (toRun.nonEmpty)
      writeToRepl ! (mode, toRun)
  }


  /**
   * Register the run script action. 
   */  
  val runAction = registerAction("Run on Interpreter", "control R") { runScript('Normal) }

  /**
   * Register the run "selected" text as script action. 
   */
  val runSelectedAction = registerAction("Run selected on Interpreter", "control shift R") { runScript('Normal, true) }

  /**
   * Register the clear console action. 
   */
  val clearOutputAction = registerAction("Clear", "control E") {
    outputPane.pane.text = ""
  }

  /**
   * Register the new tab action. 
   */
  val newTabAction = registerAction("New Tab", "control T") {
    val name = "Script" + (tabPane.pages.length + 1) + ".scala";
    addTab(name, "." + File.separator + name)
  }

  /**
   * Register the close tab action. 
   */
  val closeTabAction = registerAction("Close Tab", "control F4") {
    if (tabPane.pages.length > 1)
      closeTab
    else updateStatusBar("At lease 1 tab needed.")
  }

  /**
   * Register the switch tab action. 
   */
  // See key reaction definition in textPane. shortcut defined here has no effect.
  val switchTabAction = registerAction("Switch Tab", "control TAB") {
    tabPane.selection.index = tabPane.selection.index match {
      case c if c == tabPane.pages.length - 1 => 0
      case current => current + 1
    }
  }

  /**
   * Initialize the Help dialog. 
   */
  lazy val helpDialog = new net.slate.gui.HelpDialog(Launch.top)

  /**
   * Initialize the About dialog. 
   */
  lazy val aboutDialog = new net.slate.gui.AboutDialog(Launch.top)

  /**
   * Register the "help" action. 
   */
  val helpAction = registerAction("Help", "F1") {
    helpDialog.display
  }

  /**
   * Register the "about" action. 
   */
  val aboutAction = registerAction("About", "alt F1") {
    aboutDialog.display
  }

  /**
   * Enables "undo" on the text pane. 
   */
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

  /**
   * Enables "redo" on the text pane. 
   */
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

  /**
   * Basic text pane listener. 
   */
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

/**
 * Defines the Main menu bar that appear on the top.
 */
object MainMenuBar extends MenuBar {
  peer.setLayout(new BorderLayout)
  peer.add(new MenuBar {

    import Actions._

    // "File" menu
    contents += new Menu("File") {
      contents += new MenuItem(openAction)
      contents += new MenuItem(saveAction)
      contents += new MenuItem(saveAsAction)
      contents += new MenuItem(newTabAction)
      contents += new MenuItem(closeTabAction)
    }

    // "Edit" menu
    contents += new Menu("Edit") {
      contents += new MenuItem(UndoAction)
      contents += new MenuItem(RedoAction)
    }

    // "Scala" menu
    contents += new Menu("Scala") {
      contents += new MenuItem(runAction)
      contents += new MenuItem(runSelectedAction)
    }

    // "Help" menu
    contents += new Menu("Help") {
      contents += new MenuItem(helpAction)
      contents += new MenuItem(aboutAction)
    }
  }.peer, BorderLayout.CENTER)
}
