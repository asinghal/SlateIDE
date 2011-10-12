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
object Launch extends SimpleSwingApplication {
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

  lazy val top = new MainFrame {
    iconImage = TrayIcon.icon
    title = "Slate"
    menuBar = MainMenuBar
    val outputFrame = new OutputFrame
    outputFrame.setVisible(true)

    TrayIcon.init

    // editor panel that houses editor + console
    private val splitPane = new SplitPane(Orientation.Horizontal) {
      topComponent = tabPane

      bottomComponent = bottomTabPane

      dividerLocation = 900
      resizeWeight = 1.0
      dividerSize = 1
    }

    val toolbar = new NavigationToolBar

    val fileExplorer = new FileExplorer(null)

    // main panel that houses the file explorer as well as the editor + console
    val mainPanel = new SplitPane(Orientation.Vertical) {
      var maximized = false

      topComponent = fileExplorer

      bottomComponent = splitPane

      dividerLocation = 200
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
        dividerLocation = 200
        maximized = false
      }
    }

    contents = new BorderPanel {
      add(toolbar, BorderPanel.Position.North)
      add(mainPanel, BorderPanel.Position.Center)
      add(statusBarPane, BorderPanel.Position.South)
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
    
    // redirect all output and errors henceforth to the console in Slate
    System.setOut(sysOutErr)
    System.setErr(sysOutErr)
  }

  lazy val findDialog = new FindDialog(top)
  lazy val lookUpDialog = new LookupResourceDialog(top)
  lazy val codeAssistDialog = new CodeAssistDialog(top)
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
      tabPane.pages += new TabbedPane.Page(name, new ScriptScrollPane(name, path))

      val index = tabPane.pages.length - 1

      tabPane.selection.index = index
      tabs += (path -> index)

      //      val pnl = new javax.swing.JPanel();
      //      pnl.setOpaque(false);
      //      pnl.add(new Label(tabPane.peer.getTitleAt(index)).peer)
      //      pnl.add(new TabButton().peer)
      //      
      //      tabPane.peer.setTabComponentAt(index, pnl)

      true
    } else {
      tabPane.selection.index = tabs(path)
      false
    }
  }

  def closeTab = {
    //    val name = tabPane.selection.page.title
    val name = currentScript.text.path
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