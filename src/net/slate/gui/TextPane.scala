package net.slate.gui
import java.awt.event.{ InputEvent, KeyEvent }

import swing.{ Button, Component, TextComponent }
import javax.swing.{ Action, KeyStroke, JTextPane }
import java.awt.{ FlowLayout, Font, LayoutManager, Point }
import net.slate.Launch._
import net.slate.Actions

/**
 * 
 * @author Aishwarya Singhal
 *
 */
class TextPane extends TextComponent {
  override lazy val peer: JTextPane = new JTextPane() with SuperMixin

  def viewToModel(p: Point) = peer.viewToModel(p)
  def setLayout(layout: LayoutManager) = peer.setLayout(layout)
  def modelToView(pos: Int) = peer.modelToView(pos)
  def addActionforKeyStroke(keyevent: Int, action: Action) = peer.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(keyevent, 0), action)
  def addActionforKeyStroke(keystroke: KeyStroke, action: Action) = peer.getKeymap.addActionForKeyStroke(keystroke, action)
}

/**
 * 
 * @author Aishwarya Singhal
 *
 */
class EditorTabbedPane(tabName: String, val path: String) extends TextPane {
  peerText =>

  import java.awt.{ Cursor, Dimension, Graphics2D }
  import javax.swing.event.{ DocumentEvent, DocumentListener }
  import javax.swing.text.DefaultStyledDocument
  import scala.swing.Panel
  import scala.swing.event._
  import javax.swing.AbstractAction
  import net.slate.gui._
  import net.slate.action._
  import net.slate.editor._
  import net.slate.formatting.{ Configuration, FilterFactory }

  var doc: DefaultStyledDocument = new DefaultStyledDocument
  val filter = FilterFactory.build(tabName, doc)
  background_=(Configuration.editorBackground)
  foreground_=(Configuration.editorForeground)

  addActionforKeyStroke(KeyEvent.VK_ENTER, new AutoIndentAction(peer))
  addActionforKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK), new CommentCodeAction(peer))
  addActionforKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), new javax.swing.AbstractAction {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      findDialog.display
    }
  })

  addActionforKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), new javax.swing.AbstractAction {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      currentScript.size = (new Dimension(1024, 1024))
    }
  })

  peer.setCaretColor(Configuration.editorCursorColor)

  val popupMenu = new EditorPopupMenu

  addActionforKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK), new javax.swing.AbstractAction {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      val pane = currentScript
      val point = pane.text.peer.getCaret.getMagicCaretPosition
      val x = if (point != null) (point.getX.asInstanceOf[Int] + 50) else 50
      val y = if (point != null) (point.getY.asInstanceOf[Int] + 10) else 10
      CodeSuggestionPopupMenu.show(pane, x, y)
    }
  })

  listenTo(mouse.clicks)
  reactions += {
    case e: MouseClicked =>

      if (e.peer.getButton == java.awt.event.MouseEvent.BUTTON3) {
        popupMenu.show(e.source, e.point.x, e.point.y);
      }
  }

  val undoManager = new TextUndoManager
  undoManager.addPropertyChangeListener(Actions.UndoAction)
  undoManager.addPropertyChangeListener(Actions.RedoAction)

  var documentChangedSinceLastRepaint = false;

  font = displayFont
  cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
  doc.setDocumentFilter(filter)
  doc.addUndoableEditListener(undoManager)
  doc.addDocumentListener(Actions.UndoAction)
  doc.addDocumentListener(Actions.RedoAction)
  doc.addDocumentListener(new DocumentListener {
    def changedUpdate(e: DocumentEvent) {
      documentChangedSinceLastRepaint = true
    }

    def removeUpdate(e: DocumentEvent) {
      documentChangedSinceLastRepaint = true
    }

    def insertUpdate(e: DocumentEvent) {
      documentChangedSinceLastRepaint = true
    }
  })
  peer.setStyledDocument(doc)

  override protected def paintComponent(g: Graphics2D) {
    super.paintComponent(g)
    if (documentChangedSinceLastRepaint) {
      numbersPane.repaint()
      documentChangedSinceLastRepaint = false
    }
  }

  peer.setFocusTraversalKeysEnabled(false)
  listenTo(peerText.keys)

  reactions += {
    case KeyPressed(_, Key.Tab, Key.Modifier.Control, _) =>
      Actions.switchTabAction.apply()
  }

  // show line numbers
  val numbersPane = new Panel {
    val fontSizeChanged = { () =>
      val initialSize = 3 * displayFont.getSize
      minimumSize = new Dimension(initialSize, initialSize)
      preferredSize = new Dimension(initialSize, initialSize)
    }

    fontSizeChanged()

    override protected def paintComponent(g: Graphics2D) {
      super.paintComponent(g)
      val start = peerText.viewToModel(currentScript.peer.getViewport.getViewPosition)
      val end = peerText.viewToModel(new Point(10, currentScript.peer.getViewport.getViewPosition.y + peerText.peer.getVisibleRect.getHeight.toInt))
      // translate offsets to lines
      val doc = peerText.peer.getDocument
      val startline = doc.getDefaultRootElement.getElementIndex(start) + 1
      val endline = doc.getDefaultRootElement.getElementIndex(end) + 1
      val fontHeight = g.getFontMetrics(displayFont).getHeight
      val fontDesc = g.getFontMetrics(displayFont).getDescent
      val startingY = peerText.modelToView(start).y + fontHeight - fontDesc
      g.setColor(java.awt.Color.decode("0xAAAAAA"))
      g.setFont(displayFont)
      var line = startline
      var y = startingY
      while (line <= endline) {
        g.drawString("% 4d".format(line), 0, y)
        y += fontHeight
        line += 1
      }
    }
  }
}