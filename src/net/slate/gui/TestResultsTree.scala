package net.slate.gui

import javax.swing.{ ImageIcon, JTree }
import javax.swing.tree.{ DefaultMutableTreeNode, DefaultTreeCellRenderer, DefaultTreeModel }
import scala.swing._

class TestResultsTree extends ScrollPane {
  val top = new DefaultMutableTreeNode("Test Results")

  val tree = new JTree(top)
  tree.setCellRenderer(new TestResultsCellRenderer)
  peer.getViewport().add(tree)

  def addResult(className: String, results: Map[String, (Boolean, String)], failed: Boolean) = {
    def addNewNode(parent: DefaultMutableTreeNode, name: String, isMethod: Boolean, hasPassed: Boolean): DefaultMutableTreeNode = {
      val node = new DefaultMutableTreeNode(new TestResultsNode(name, isMethod, hasPassed))
      tree.getModel.asInstanceOf[DefaultTreeModel].insertNodeInto(node, parent, parent.getChildCount())
      node
    }

    val parent = addNewNode(top, className, false, !failed)

    results.keys.foreach { key =>
      val m = addNewNode(parent, key.replace("*** FAILED ***", ""), true, results(key)._1)
      if (!results(key)._1) addNewNode(m, results(key)._2, false, false)
    }
  }

  def clear = {
    val model = tree.getModel.asInstanceOf[DefaultTreeModel]
    val count = model.getChildCount(top)

    for (i <- 0 to (count - 1)) {
      model.removeNodeFromParent(model.getChild(top, 0).asInstanceOf[DefaultMutableTreeNode])
    }
  }

  class TestResultsNode(val name: String, val isMethod: Boolean, val hasPassed: Boolean) {
    override def toString = { name }
  }

  class TestResultsCellRenderer extends DefaultTreeCellRenderer {

    import java.awt.Color

    val failedIcon = new ImageIcon("images/testerr.gif")
    val passedIcon = new ImageIcon("images/testok.gif")

    override def getTreeCellRendererComponent(tree: JTree, value: AnyRef, selected: Boolean,
      expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) = {
      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
      setForeground(Color.decode("0x000000"))

      value match {
        case v: DefaultMutableTreeNode =>
          v.getUserObject match {
            case result: TestResultsNode =>
              if (result.hasPassed) {
                setForeground(Color.decode("0x22B14C"))
                setIcon(passedIcon)
              } else {
                setForeground(Color.decode("0xEE3333"))
                setIcon(failedIcon)
              }
            case _ =>
          }
      }

      this
    }

  }

}