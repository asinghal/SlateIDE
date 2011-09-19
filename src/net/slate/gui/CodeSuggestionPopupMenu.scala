package net.slate.gui

import scala.swing.{ Action, Component }

import net.slate.Launch._

/**
 * 
 * @author Aishwarya Singhal
 *
 */
object CodeSuggestionPopupMenu {
  import java.awt.event.KeyEvent
  import javax.swing.{ DefaultListCellRenderer, ImageIcon, JList, JScrollPane, KeyStroke, Popup, PopupFactory }
  import net.slate.editor.tools.{ CodeAssist, TypeIndexer }

  var popup: Popup = null

  def show(owner: Component, x: Int, y: Int) {
    val factory = PopupFactory.getSharedInstance()
    val word = CodeAssist.getWord
    val list = new TypeIndexer("").find(word._2)
    val contents = new JList(list)
    contents.setCellRenderer(new CodeSuggestionRenderer)
    val scrollpane = new JScrollPane(contents)
    if (popup != null) popup.hide

    popup = factory.getPopup(owner.peer, scrollpane, 200 + x, 100 + y)
    contents.addMouseListener(new java.awt.event.MouseAdapter {
      override def mouseClicked(e: java.awt.event.MouseEvent) {
        if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
          val index = contents.locationToIndex(e.getPoint());
          val pane = currentScript.text
          list(index) match {
            case x: String =>
              val text = x.substring(0, x.indexOf("-")).trim
              pane.doc.remove(word._1, pane.caret.position - word._1)

              pane.doc.insertString(pane.caret.position, text, null)
          }
          popup.hide
        }
      }

    })

    contents.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeCodeAssist");
    contents.getActionMap().put("closeCodeAssist", new javax.swing.AbstractAction {
      def actionPerformed(e: java.awt.event.ActionEvent) {
        restoreFocus
      }
    });
    popup.show()
  }

  def insertTextAction(text: String) = new Action(text) {
    def apply() {
      val pane = currentScript.text
      pane.doc.insertString(pane.caret.position, text, null)
      restoreFocus
    }
  }

  def restoreFocus = {
    javax.swing.SwingUtilities.invokeLater(new Runnable {
      def run = {
        popup.hide
        currentScript.text.peer.requestFocus
      }
    })
  }

  /**
   * cell renderer for showing code options.
   */
  class CodeSuggestionRenderer extends DefaultListCellRenderer {
    val icon = new ImageIcon("images/img_bullet_blue.png");

    /* This is the only method defined by ListCellRenderer.  We just
     * reconfigure the Jlabel each time we're called.
     */
    override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, iss: Boolean, chf: Boolean) = {
      /* The DefaultListCellRenderer class will take care of
         * the JLabels text property, it's foreground and background
         * colors, and so on.
         */
      super.getListCellRendererComponent(list, value, index, iss, chf);

      /* We additionally set the JLabels icon property here.
         */
      setIcon(icon);

      this
    }
  }
}
