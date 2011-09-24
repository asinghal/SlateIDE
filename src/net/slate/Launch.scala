package net.slate

import scala.swing._
import scala.swing.event._
import scala.actors.Actor._
import java.awt.{ Color, Cursor, Font, Frame, Toolkit }
import java.io.{ PipedInputStream, PipedOutputStream, PrintStream }
import javax.swing.event._
import javax.swing.{ SwingUtilities, UIManager }
import javax.swing.text.DefaultStyledDocument

import net.slate.gui._
import net.slate.formatting.Configuration

/**
 *
 * @author Aishwarya Singhal
 *
 */
object Launch extends SimpleGUIApplication {
  var lastFileOperationDirectory: Option[String] = None

  var displayFont = Font.decode(System.getProperty("font", "Monospaced-12"))

  val outputIs = new PipedInputStream(4096)
  val replOs = new PipedOutputStream(outputIs)

  var tabs = Map[String, Int]()

  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  } catch {
    case e: Exception =>
      e.printStackTrace
  }

  val sysOutErr = new PrintStream(replOs) {
    override def write(buf: Array[Byte], off: Int, len: Int) {
      val str = new String(buf, off, len)
      SwingUtilities.invokeLater(new Runnable() {
        def run() {
          bottomTabPane.selection.index = 0
          replOs.write(str.getBytes)
          replOs.flush()
        }
      })
    }
  }
  System.setOut(sysOutErr)
  System.setErr(sysOutErr)

  lazy val top = new MainFrame {
    iconImage = TrayIcon.icon
    title = "Scala Cafe"
    menuBar = MainMenuBar

    TrayIcon.init

    val splitPane = new SplitPane(Orientation.Horizontal) {
      topComponent = tabPane

      bottomComponent = bottomTabPane

      preferredSize = new Dimension(1024, 1024)

      dividerLocation = 900
      resizeWeight = 1.0
      dividerSize = 1
    }

    val toolbar = new NavigationToolBar

    val fileExplorer = new FileExplorer(null)
    contents = new BorderPanel {
      add(fileExplorer, BorderPanel.Position.West)
      add(toolbar, BorderPanel.Position.North)
      add(splitPane, BorderPanel.Position.Center)
      add(statusBar, BorderPanel.Position.South)
    }

    peer.setExtendedState(peer.getExtendedState() | Frame.MAXIMIZED_BOTH)

    Actions.newTabAction.apply()

    listenTo(this)
    reactions += {
      case WindowIconified(w) =>
        if (TrayIcon.supported) w.visible = true
    }
    centerOnScreen()
    updateStatusBar("Ready")
    currentScript.text.requestFocus()
  }

  lazy val findDialog = new FindDialog(top)
  lazy val lookUpDialog = new LookupResourceDialog(top)
  lazy val runDialog = new RunDialog(top)

  def textPane(name: String, path: String) = new EditorTabbedPane(name, path)

  val tabPane: TabbedPane = new TabbedPane {
    focusable = false
  }

  val bottomTabPane = new ResultsTabbedPane

  val outputPane = bottomTabPane.outputPane

  def currentScript = tabPane.selection.page.content.asInstanceOf[ScriptScrollPane]

  def addTab(name: String, path: String): Boolean = {
    if (!tabs.contains(name)) {
      tabPane.pages += new TabbedPane.Page(name, new ScriptScrollPane(name, path))

      val index = tabPane.pages.length - 1

      tabPane.selection.index = index
      tabs += (name -> index)

      //      val pnl = new javax.swing.JPanel();
      //      pnl.setOpaque(false);
      //      pnl.add(new Label(tabPane.peer.getTitleAt(index)).peer)
      //      pnl.add(new TabButton().peer)
      //      
      //      tabPane.peer.setTabComponentAt(index, pnl)

      true
    } else {
      tabPane.selection.index = tabs(name)
      false
    }
  }

  def closeTab = {
    val name = tabPane.selection.page.title
    if (tabs.contains(name)) {
      tabs -= (name)
      tabPane.pages.remove(tabPane.selection.index)
    }
  }

  class ScriptScrollPane(tabName: String, path: String) extends ScrollPane {
    val text = textPane(tabName, path: String)
    viewportView = new BorderPanel {
      import BorderPanel.Position

      add(text.numbersPane, Position.West)
      add(text, Position.Center)
      add(new BorderPanel { add(new TabButton(), Position.East) }, Position.North)
    }
  }

  val statusBar = new Label {
    xAlignment = Alignment.Left
  }

  def updateStatusBar(text: String) {
    statusBar.text = "  " + text
  }
}