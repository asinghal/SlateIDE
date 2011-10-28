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
package net.slate.gui

import javax.swing.JTable

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
  val tasks = new TasksTab
  tasks load

  focusable = false
  pages += new TabbedPane.Page("Console", outputPane)
  pages += new TabbedPane.Page("Problems", problems)
  pages += new TabbedPane.Page("Test Results", testResults)
  pages += new TabbedPane.Page("Tasks", tasks)

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

abstract class TabularTabPane extends ScrollPane {
  import java.awt.event.{ MouseAdapter, MouseEvent }
  import javax.swing.table.{ DefaultTableModel, DefaultTableCellRenderer }

  lazy val columnNames: Array[AnyRef] = Array("")
  lazy val hiddenColumns = Array("")

  lazy val tableModel = new DefaultTableModel(Array[Array[AnyRef]](), columnNames)
  lazy val cellRenderer: Option[DefaultTableCellRenderer] = None
  protected def mouseListener(table: JTable): MouseAdapter

  val table = new Table {
    font = displayFont
    model = tableModel
    // this should have worked but doesn't --> cellRenderer foreach peer.getColumnModel().getColumn(0).setCellRenderer

    var hidden = 0
    (0 until peer.getColumnModel.getColumnCount) foreach { c => peer.getColumnModel().getColumn(c - hidden).getHeaderValue match { case x: String => if (hiddenColumns.contains(x)) { peer.getColumnModel().removeColumn(peer.getColumnModel().getColumn(c - hidden)); hidden = hidden + 1 } case _ => } }

    peer.addMouseListener(mouseListener(peer))

    cellRenderer foreach { c => peer.setDefaultRenderer(classOf[String], c) }
  }

  viewportView = table

  /**
   * clears the table
   */
  def clear = {
    for (r <- 0 to tableModel.getRowCount - 1) {
      tableModel.removeRow(0)
    }
  }
}

/**
 *
 */
class ProblemsTab extends TabularTabPane {
  import java.awt.event.{ MouseAdapter, MouseEvent }
  import javax.swing.ImageIcon
  import javax.swing.table.DefaultTableCellRenderer

  import net.slate.util.FileUtils

  override lazy val columnNames: Array[AnyRef] = Array("Description", "File", "Line", "Project", "Type", "Location")
  override lazy val cellRenderer: Option[DefaultTableCellRenderer] = Some(new ErrorMessageCellRenderer)
  protected def mouseListener(peer: JTable): MouseAdapter = new MouseAdapter() {
    import net.slate.editor.ErrorMarker

    override def mouseClicked(e: MouseEvent) = {
      //        if (e.getButton == java.awt.event.MouseEvent.BUTTON3/* && e.getClickCount() == 2*/) {
      val row = peer.getSelectedRow()
      val path = peer.getValueAt(row, 5).asInstanceOf[String]
      FileUtils.open(path.substring(path.lastIndexOf(java.io.File.separator) + 1), path)
      ErrorMarker.mark
      //        }
    }
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
   * Key cell renderer to display error/ warning icons on the problems tab
   *
   * @author Aishwarya Singhal
   */
  class ErrorMessageCellRenderer extends DefaultTableCellRenderer {
    import javax.swing.JTable

    val errorIcon = new ImageIcon("images/error_tsk.gif")
    val warnIcon = new ImageIcon("images/warn_tsk.gif")

    override def getTableCellRendererComponent(table: JTable, value: AnyRef, selected: Boolean, hasFocus: Boolean, row: Int, column: Int): java.awt.Component = {
      super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column)

      column match {
        case 0 => table.getValueAt(row, 4) match {
          case "Error" => setIcon(errorIcon)
          case "Warning" => setIcon(warnIcon)
        }
        case _ => setIcon(null)
      }

      this
    }
  }
}

/**
 *
 */
class TasksTab extends TabularTabPane {
  import java.awt.event.{ MouseAdapter, MouseEvent }
  import javax.swing.table.DefaultTableCellRenderer
  import net.slate.util._
  import net.slate.util.TaskStatus._

  override lazy val columnNames: Array[AnyRef] = Array("Id", "Title", "Description", "Project", "Status")
  override lazy val hiddenColumns = Array("Id")
  protected def mouseListener(peer: JTable): MouseAdapter = new MouseAdapter() {
    override def mouseClicked(e: MouseEvent) = {
      //        if (e.getButton == java.awt.event.MouseEvent.BUTTON3/* && e.getClickCount() == 2*/) {
      //        }
    }
  }

  /**
   * add a row to the table
   * @param title
   * @param description
   * @param project
   * @param status
   */
  def add(title: String, description: String, project: String, status: Int = OPEN) = {
    val id = Task.create(title, description, project, status)
    insertRow(String.valueOf(id), title, description, project, getName(status))
  }

  /**
   * Inserts a row into the table.
   *
   * @param id
   * @param title
   * @param description
   * @param project
   * @param status
   */
  private def insertRow(id: String, title: String, description: String, project: String, status: String) {
    tableModel.insertRow(tableModel.getRowCount, Array[AnyRef](id, title, description, project, status))
  }

  /**
   * Load all existing tasks into the table.
   */
  def load {
    Task.list foreach { t => insertRow(String.valueOf(t.id), t.title, t.description, t.project, getName(t.status)) }
  }

  /**
   * Delete the currently selected task.
   */
  def delete {
    if (table.peer.getSelectedRow > -1) {
      Task.delete(tableModel.getValueAt(table.peer.getSelectedRow, 0).toString.toLong)
      tableModel.removeRow(table.peer.getSelectedRow)
    }
  }
}