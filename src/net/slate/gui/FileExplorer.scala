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

import java.io.File
import javax.swing.{ BorderFactory, ImageIcon, JTree }
import javax.swing.event.{ TreeSelectionEvent, TreeSelectionListener }
import javax.swing.tree.{ DefaultMutableTreeNode, DefaultTreeCellRenderer, DefaultTreeModel, TreeCellRenderer }
import scala.swing._
import scala.swing.event._

import net.slate.{ ExecutionContext, Launch }
import net.slate.builder.ProjectConfigurator
import net.slate.gui.popupmenu.ProjectTreeMenu
import net.slate.util.{ FileUtils, ObjectSerializer }

/**
 * Provides the Project explorer on the left hand side of the IDE.
 *
 * @author Aishwarya Singhal
 */
class FileExplorer(dir: File) extends ScrollPane {
  import Launch._
  import net.slate.editor.tools.TypeIndexer

  private val customLeafIcon = new ImageIcon("images/file.png")
  private val projectRootIcon = new ImageIcon("images/project.png")
  private val srcDirIcon = new ImageIcon("images/src.jpg")
  private val destDirIcon = new ImageIcon("images/destDir.png")
  private val hiddenDirIcon = new ImageIcon("images/hidden.png")
  private val viewsDirIcon = new ImageIcon("images/views.png")

  private var allProjectSettings = Map[String, (List[String], String)]()

  private val top = new DefaultMutableTreeNode(new File("."))

  private val tree = new JTree(addNodes(top, dir))

  private val projectTreeMenu = ProjectTreeMenu

  // Add a listener
  tree.addMouseListener(new java.awt.event.MouseAdapter {
    override def mousePressed(e: java.awt.event.MouseEvent) {
      val treePath = tree.getPathForLocation(e.getX(), e.getY())
      if (treePath != null) {
        if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
          val node = treePath.getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]

          node.getUserObject match {
            case fileNode: FileNode =>
              if (!fileNode.isDirectory && FileUtils.open(fileNode.name, fileNode.path)) {
              }
            case _ =>
          }
        } else if (e.getButton == java.awt.event.MouseEvent.BUTTON3) {

          val node = tree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]
          val row = tree.getRowForLocation(e.getX(), e.getY())

          node.getUserObject match {
            case fileNode: FileNode =>
              if (fileNode.isDirectory) {
                projectTreeMenu.path = fileNode.path
                projectTreeMenu.nodeRow = row
                projectTreeMenu.show(Launch.top.fileExplorer, e.getX(), e.getY())
              }
            case _ =>
          }
        }
      }
    }
  })

  tree.setRootVisible(false)

  val renderer = new ProjectTreeCellRenderer();
  renderer.setLeafIcon(customLeafIcon);

  tree.setCellRenderer(renderer)
  tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
  peer.getViewport().add(tree)

  openProjects

  def addNodes(curTop: DefaultMutableTreeNode, dir: File): DefaultMutableTreeNode = {
    if (dir != null) {
      val curPath = dir.getPath()
      var pathname = curPath
      if (curPath.contains(File.separator)) {
        pathname = pathname.substring(pathname.lastIndexOf(File.separator) + 1)
      }
      val curDir = new DefaultMutableTreeNode(new FileNode(pathname, curPath, true))
      if (curTop != null) { // should only be null at root
        curTop.add(curDir);
      }

      val ol = List(dir.list(): _*).sort(_.toUpperCase < _.toUpperCase)

      var files = List[FileNode]()
      // Make two passes, one for Dirs and one for Files. This is #1.
      ol foreach { file =>
        if (file != ".slate") {
          val newPath = if (curPath.equals(".")) file else (curPath + File.separator + file)
          val f = new File(newPath)

          if (f.isDirectory()) addNodes(curDir, f) else files :::= List(new FileNode(file, newPath))
        }
      }

      // Pass two: for files.
      files.sortWith(_.compareTo(_) < 0).foreach { file => curDir.add(new DefaultMutableTreeNode(file)) }

      if (curTop != null) curTop else curDir
    } else {
      curTop
    }
  }

  def addNewNode(targetNodeRow: Int, name: String, path: String, isDirectory: Boolean) {
    val node = new DefaultMutableTreeNode(new FileNode(name, path, isDirectory))
    val parent = tree.getPathForRow(targetNodeRow).getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]
    tree.getModel.asInstanceOf[DefaultTreeModel].insertNodeInto(node, parent, parent.getChildCount())
  }

  def removeNode(targetNodeRow: Int) {
    val node = tree.getPathForRow(targetNodeRow).getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]
    val file = new File(node.getUserObject.asInstanceOf[FileNode].path)
    FileUtils.delete(file)

    tree.getModel.asInstanceOf[DefaultTreeModel].removeNodeFromParent(node)
  }

  def openProject(project: File, persist: Boolean = true) = {
    if (project != null && project.exists) {
      ExecutionContext.loadedProjects :::= List(project.getPath)
      projectSettings(project.getPath)

      tree.setModel(new DefaultTreeModel(addNodes(top, project)))
      ProjectConfigurator.init(project)
      new TypeIndexer(project.getAbsolutePath).index

      if (persist)
        ProjectDetailsSerializer.add(new ProjectDetails(project.getPath, true))
    } else {
      ProjectDetailsSerializer.remove(p => p.path == project.getPath)
    }
  }

  def openProjects = {
    ProjectDetailsSerializer.read.foreach { details =>
      if (details.open) openProject(new File(details.path), false)
    }
  }

  private def projectSettings(project: String) = {
    import java.io.FileReader
    import scala.xml.XML
    val xml = XML.load(new FileReader(project + File.separator + ".slate" + File.separator + "settings.xml"))

    var config = Map[String, String]()

    var src = List[String]()

    xml \\ "srcdirs" \\ "dir" foreach { srcdir =>
      src :::= List(project + File.separator + (srcdir \\ "@path").text)
    }
    val destdir = project + File.separator + (xml \\ "destdir" \\ "@path").text

    allProjectSettings = allProjectSettings.updated(project, (src, destdir))
  }

  case class FileNode(val name: String, val path: String, val isDirectory: Boolean = false) {

    override def toString = { name }

    def compareTo(other: FileNode) = { name.toUpperCase.compareTo(other.name.toUpperCase) }
  }

  class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

    override def getTreeCellRendererComponent(tree: JTree, value: AnyRef, selected: Boolean,
      expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) = {
      var isLeaf = leaf

      var isRoot = false
      var srcDir = false
      var destDir = false
      var hidden = false

      def identifyDirType(dir: String) {
        hidden = dir.startsWith(".") || dir.contains(File.separator + ".")
        val project = ExecutionContext.currentProjectName(dir)
        isRoot = (dir == project)
        val settings = allProjectSettings(project)
        srcDir = settings._1.contains(dir)
        destDir = (dir == settings._2)
      }

      value match {
        case v: DefaultMutableTreeNode =>
          v.getUserObject match {
            case file: FileNode =>
              if (new File(file.path).isDirectory) {
                isLeaf = false;
                identifyDirType(file.path)
              }
            case file: File =>
              if (file.isDirectory) {
                isLeaf = false;
                identifyDirType(file.getAbsolutePath)
              }
            case _ =>
          }
      }
      val c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, hasFocus)
      if (hidden) { setIcon(hiddenDirIcon); setForeground(java.awt.Color.decode("0x888888")) }
      if (isRoot) { setIcon(projectRootIcon) }
      if (srcDir) { setIcon(srcDirIcon) }
      if (destDir) { setIcon(destDirIcon) }

      c
    }

  }
}

@scala.serializable
class ProjectDetails(val path: String, val open: Boolean) {
  val serialVersionUID = 1L
}

object ProjectDetailsSerializer extends ObjectSerializer[ProjectDetails] {
  override lazy val storeName = "projectDetails.ser"
}
