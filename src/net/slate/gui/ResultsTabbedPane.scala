package net.slate.gui

import scala.swing.{ ScrollPane, TabbedPane, Table }
import net.slate.Launch._
import net.slate.formatting.Configuration

/**
 *
 */
class ResultsTabbedPane extends TabbedPane {
  val outputPane = new Console
  val problems = new ProblemsTab
  val testResults = new TestResultsTree

  focusable = false
  pages += new TabbedPane.Page("Console", outputPane)
  pages += new TabbedPane.Page("Problems", problems)
  pages += new TabbedPane.Page("Test Results", testResults)

  selection.index = 0
}

/**
 *
 */
class Console extends ScrollPane {
  val pane = new TextPane {
    font = displayFont
    background = (Configuration.editorBackground)
    foreground = (Configuration.editorForeground)
    peer.setCaretColor(Configuration.editorCursorColor)
  }

  viewportView = pane
}

/**
 *
 */
class ProblemsTab extends ScrollPane {
  import java.awt.event.{ MouseAdapter, MouseEvent }
  import javax.swing.table.DefaultTableModel

  import net.slate.util.FileUtils

  val columnNames: Array[AnyRef] = Array("Description", "File", "Line", "Project", "Type", "Location")

  val tableModel = new DefaultTableModel(Array[Array[AnyRef]](), columnNames)

  viewportView = new Table {
    font = displayFont
    model = tableModel

    peer.addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent) = {
        //        if (e.getButton == java.awt.event.MouseEvent.BUTTON3/* && e.getClickCount() == 2*/) {
        val row = peer.getSelectedRow()
        val path = peer.getValueAt(row, 5).asInstanceOf[String]
        FileUtils.open(path.substring(path.lastIndexOf(java.io.File.separator) + 1), path)
        //        }
      }
    })
  }

  /**
   * add a row to the table
   * @param description
   * @param file
   * @param line
   * @param project
   * @param path
   * @param problemType
   */
  def add(description: String, file: String, line: String, project: String, path: String, problemType: String = "Error") = {
    tableModel.insertRow(tableModel.getRowCount, Array[AnyRef](description, file, line, project, problemType, path))
  }

  /**
   * clears the table
   */
  def clear = {
    for (r <- 0 to tableModel.getRowCount - 1) {
      tableModel.removeRow(0)
    }
  }
}