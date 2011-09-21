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

  focusable = false
  pages += new TabbedPane.Page("Console", outputPane)
  pages += new TabbedPane.Page("Problems", problems)

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
  import javax.swing.table.DefaultTableModel

  val columnNames: Array[AnyRef] = Array("Description", "File", "Line", "Project", "Type")

  val tableModel = new DefaultTableModel(Array[Array[AnyRef]](), columnNames)

  viewportView = new Table {
    font = displayFont
    model = tableModel
  }

  /**
   * add a row to the table
   * @param description
   * @param file
   * @param line
   * @param problemType
   */
  def add(description: String, file: String, line: String, project: String, problemType: String = "Error") = {
    tableModel.insertRow(tableModel.getRowCount, Array[AnyRef](description, file, line, project, problemType))
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