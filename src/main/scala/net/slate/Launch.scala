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

import scala.swing._
import scala.swing.event._
import scala.actors.Actor._
import java.awt.{ Color, Cursor, Font, Frame, SplashScreen, Toolkit }
import java.io.{ PipedInputStream, PipedOutputStream, PrintStream }
import javax.swing.event._
import javax.swing.{ SwingUtilities, UIManager }
import javax.swing.text.DefaultStyledDocument

import net.slate.gui._

/**
 *
 * @author Aishwarya Singhal
 *
 */
object Launch extends SimpleSwingApplication {
  var lastFileOperationDirectory: Option[String] = None

  var displayFont = Font.decode(System.getProperty("font", "Monospaced-12"))

  val outputIs = new PipedInputStream(4096)
  val replOs = new PipedOutputStream(outputIs)

  var tabs = Map[String, Int]()

  val dir = new java.io.File(".metadata")
  if (!dir.exists) {
    dir.mkdir
  }

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

  private val splash = new MainFrame {
    iconImage = TrayIcon.icon
    title = "Slate"
    preferredSize = Size(400, 150)
    peer.setUndecorated(true)
    contents = new BorderPanel {
      import javax.swing.BorderFactory

      val color = Color.decode("0xFFFFFF")
      val largeFont = new Font("Dialog", 1, 30)

      border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
      background = Color.decode("0x7092BE")
      add(new Label("Slate - An IDE for Scala") { foreground = color; font = largeFont }, BorderPanel.Position.North)
      add(new Label("Loading Slate...") { foreground = color }, BorderPanel.Position.Center)
      add(new Label("Version 0.2 | Released October 2011 | Written By Aishwarya Singhal") { foreground = color }, BorderPanel.Position.South)
    }
    centerOnScreen()
    visible = true
  }

  lazy val top = new MainFrame {
    iconImage = TrayIcon.icon
    title = "Slate"
    menuBar = MainMenuBar
    val outputFrame = new OutputFrame
    outputFrame.setVisible(true)
    outputFrame.toBack

    TrayIcon.init
    
    val editorSplitPane = new SplitPane(Orientation.Vertical) {
      topComponent = tabPane
      bottomComponent = apiLookupDialog
      dividerLocation = Size.adjustWidth(1200)
      resizeWeight = 1.0
      dividerSize = 1
    }
    
    val searchField = SearchField("search...")
    
    val glasspane = new FlowPanel(FlowPanel.Alignment.Right)(searchField) {
      opaque = false
    }

    // editor panel that houses editor + console
    val splitPane = new SplitPane(Orientation.Horizontal) {
      topComponent = editorSplitPane

      bottomComponent = bottomTabPane

      dividerLocation = Size.adjustHeight(550)
      resizeWeight = 1.0
      dividerSize = 1
      var maximized = false

      /**
       * maximize the editor.
       */
      def maximize = {
        // resizeWeight = 1d
        dividerLocation = Size.adjustHeight(800)
        maximized = true
      }

      /**
       * restore the editor.
       */
      def restore = {
        // resizeWeight = 0.8d
        dividerLocation = Size.adjustHeight(550)
        maximized = false
      }
    }

    val toolbar = new NavigationToolBar

    val fileExplorer = new FileExplorer(null)

    // main panel that houses the file explorer as well as the editor + console
    val mainPanel = new SplitPane(Orientation.Vertical) {
      var maximized = false

      topComponent = new BorderPanel {
        add(toolbar, BorderPanel.Position.North)
        add(fileExplorer, BorderPanel.Position.Center)
      }

      bottomComponent = splitPane

      dividerLocation = Size.adjustWidth(200)
      resizeWeight = 0.0
      dividerSize = 1

      /**
       * maximize the editor.
       */
      def maximize = {
        resizeWeight = 1d
        dividerLocation = 10
        maximized = true
      }

      /**
       * restore the editor.
       */
      def restore = {
        resizeWeight = 0.0
        dividerLocation = Size.adjustWidth(200)
        maximized = false
      }
    }

    contents = new BorderPanel {
      add(mainPanel, BorderPanel.Position.Center)
      add(statusBarPane, BorderPanel.Position.South)
    }

    peer.setExtendedState(peer.getExtendedState() | Frame.MAXIMIZED_BOTH)
    peer.setGlassPane(glasspane.peer)
    peer.getGlassPane.setVisible(true)

    Actions.newTabAction.apply()

    listenTo(this)
    reactions += {
      case WindowIconified(w) =>
        if (TrayIcon.supported) w.visible = true
    }
    centerOnScreen()
    updateStatusBar("Ready")
    currentScript.text.requestFocus()

    // redirect all output and errors henceforth to the console in Slate
    System.setOut(sysOutErr)
    System.setErr(sysOutErr)
    splash.visible = false
    splash.dispose
  }

  lazy val findDialog = new FindDialog(top)
  lazy val lookUpDialog = new LookupResourceDialog(top)
  lazy val codeAssistDialog = new CodeAssistDialog(top)
  lazy val apiLookupDialog = new APILookupDialog
  lazy val runDialog = new RunDialog(top)

  def textPane(name: String, path: String) = new EditorTabbedPane(name, path)

  val tabPane: TabbedPane = new TabbedPane {
    focusable = false
  }

  val bottomTabPane = new ResultsTabbedPane

  val outputPane = bottomTabPane.outputPane

  def currentScript = tabPane.selection.page.content.asInstanceOf[ScriptScrollPane]

  def addTab(name: String, path: String): Boolean = {

    if (!tabs.contains(path)) {
      tabPane.pages += new TabbedPane.Page(name, new ScriptScrollPane(name, path)) { tip = path }

      val index = tabPane.pages.length - 1

      tabPane.selection.index = index
      tabs += (path -> index)

      val pnl = new BorderPanel {
        import BorderPanel.Position

        add(new Label(tabPane.peer.getTitleAt(index)), Position.West)
        add(new TabButton(path), Position.East)
      }

      pnl.opaque = false

      tabPane.peer.setTabComponentAt(index, pnl.peer)

      true
    } else {
      tabPane.selection.index = tabs(path)
      false
    }
  }

  def closeTab(name: String = currentScript.text.path): Unit = {
    if (tabs.contains(name)) {
      tabs -= (name)

      (0 until tabPane.peer.getTabCount).find { i => (tabPane.peer.getToolTipTextAt(i) == name) } foreach (tabPane.pages.remove)
    }
  }

  class ScriptScrollPane(tabName: String, path: String) extends ScrollPane {
    val text = textPane(tabName, path: String)
    viewportView = new BorderPanel {
      import BorderPanel.Position

      add(text.numbersPane, Position.West)
      add(text, Position.Center)
    }
  }

  val progressBar = new ProgressBar {
    min = 0
    max = 100
    value = 0
    indeterminate = true
    visible = false
  }

  val statusBar = new Label {
    xAlignment = Alignment.Left
  }

  lazy val statusBarPane = new BorderPanel {
    add(statusBar, BorderPanel.Position.West)
    add(progressBar, BorderPanel.Position.East)
  }

  def updateStatusBar(text: String) {
    statusBar.text = "  " + text
  }
}