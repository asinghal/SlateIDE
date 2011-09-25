package net.slate.gui

import java.awt.FlowLayout
import java.lang.reflect._
import java.io.File
import javax.swing.{ BoxLayout, DefaultListModel, DefaultListCellRenderer, ImageIcon, JList, JPanel, JScrollPane, JTextField }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import scala.swing._
import scala.swing.event.{ ButtonClicked, KeyReleased }

import net.slate.ExecutionContext
import net.slate.editor.tools.{ TypeCacheBuilder, TypeIndexer }

class CodeAssistDialog(frame: MainFrame) extends Dialog(frame.owner) {
  title = "Open"
  val SPACING = 5
  val pane = new JPanel
  pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS))
  peer.getContentPane.add(pane)
  val txtFind = new TextField(30)
  pane.add(txtFind.peer)

  val listModel = new DefaultListModel
  val results = new JList(listModel)
  results.setCellRenderer(new CodeAssistRenderer)

  val resultsPane = new JScrollPane(results)
  resultsPane.setPreferredSize(new Dimension(400, 400))
  pane.add(resultsPane)

  results.addMouseListener(new java.awt.event.MouseAdapter {
    override def mouseClicked(e: java.awt.event.MouseEvent) {
      if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
        val index = results.locationToIndex(e.getPoint());
        select(index)
      }
    }
  })

  results.addListSelectionListener(new ListSelectionListener {
    override def valueChanged(evt: ListSelectionEvent) = {
      if (!evt.getValueIsAdjusting()) {
        val index = evt.getLastIndex
        select(index)
      }
    }
  })

  val label = new Label
  pane.add(label.peer)
  val buttonPanel = new JPanel(new FlowLayout)
  pane.add(buttonPanel)
  val btnOpen = new Button("Open")
  val btnCancel = new Button("Cancel")
  buttonPanel.add(btnOpen.peer)
  buttonPanel.add(btnCancel.peer)

  listenTo(txtFind.keys, btnOpen, btnCancel)
  reactions += {
    case KeyReleased(`txtFind`, _, _, _) =>
      listModel.clear

      if (txtFind.text.trim != "") {

        var list = new TypeIndexer(ExecutionContext.currentProjectName).find(txtFind.text.trim).map { s => s.asInstanceOf[String] }.filter { s => !s.contains("$") }
        list = if (txtFind.text.contains(".")) list.filter { s => s.contains(txtFind.text) } else list.filter { s => s.toLowerCase.startsWith(txtFind.text.toLowerCase) }
        
        if (list.size > 1) {
          list = list.filter { s => s.toLowerCase.contains(txtFind.text.toLowerCase.substring(txtFind.text.lastIndexOf(".") + 1) + " ") }
        }

        if (list.size == 1) {
          val packageName = list(0).substring(list(0).indexOf("-") + 1).trim
          val typeName = list(0).substring(0, list(0).indexOf("-") - 1).trim
          val className = if (typeName == packageName) typeName else (packageName + "." + typeName)
          val l = getAllMembers(className.replace(".class.", "."))
          l.foreach { listModel.addElement(_) }
        }
      }

    case ButtonClicked(`btnOpen`) =>
      val index = results.getSelectedIndex()
      if (index != -1) {
        //        open(index)
      }
    case ButtonClicked(`btnCancel`) =>
      peer.setVisible(false)
  }

  def display() {
    pack()
    txtFind.text = ""
    txtFind.requestFocus()
    txtFind.selectAll()
    peer.setLocationRelativeTo(frame.peer)

    peer.setVisible(true)
  }

  private def getAllMembers(className: String) = {
    label.text = className
    var members = List[String]()
    try {
      val c = findClass(className)
      c.getDeclaredFields.foreach { f => members ::= getFieldSignature(f) }
      c.getDeclaredMethods.foreach { m => members ::= getMethodSignature(m) }

    } catch {
      case e: Throwable => // ignore
    }
    members
  }

  private def findClass(className: String) = {
    var c: Class[_] = null
    try {
      c = TypeCacheBuilder.findClass(ExecutionContext.currentProjectName, className)
    } catch {
      case e: Throwable =>
        try {
          c = Class.forName(className)
        } catch {
          case e: Throwable => throw e
        }
    }

    c
  }

  private def getFieldSignature(field: Field) = {
    val m = access(Modifier.toString(field.getModifiers()))
    m + field.getName + " : " + field.getType.getSimpleName
  }

  private def getMethodSignature(method: Method) = {
    val m = access(Modifier.toString(method.getModifiers()))
    def getParameters = {
      List.fromArray(method.getParameterTypes).map { t => t.getSimpleName }.mkString(", ")
    }
    m + method.getName + "(" + getParameters + ")" + " : " + method.getReturnType.getSimpleName
  }

  private def access(m: String) = {
    if (m.contains("public")) "__PUBLIC__" else if (m.contains("private")) "__PRIVATE__" else if (m.contains("protected")) "__PROTECTED__" else ""
  }

  private def select(index: Int) = {
    listModel.getElementAt(index) match {
      case x: String =>
        val list = new TypeIndexer(ExecutionContext.currentProjectName).find(x)
    }
  }

  /**
   * cell renderer for showing code options.
   */
  class CodeAssistRenderer extends DefaultListCellRenderer {
    val privateIcon = new ImageIcon("images/img_bullet_red.png");
    val protectedIcon = new ImageIcon("images/img_bullet_blue.png");
    val publicIcon = new ImageIcon("images/img_bullet_green.png");

    /* This is the only method defined by ListCellRenderer.  We just
     * reconfigure the Jlabel each time we're called.
     */
    override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, iss: Boolean, chf: Boolean) = {
      /* The DefaultListCellRenderer class will take care of
         * the JLabels text property, it's foreground and background
         * colors, and so on.
         */
      var text = value.asInstanceOf[String]
      text = text.substring(text.lastIndexOf(File.separator) + 1)

      var icon = protectedIcon

      if (text.startsWith("__PUBLIC__")) {
        icon = publicIcon
        text = text.replace("__PUBLIC__", "").trim
      } else if (text.startsWith("__PRIVATE__")) {
        icon = privateIcon
        text = text.replace("__PRIVATE__", "").trim
      } else {
        icon = protectedIcon
        text = text.replace("__PROTECTED__", "").trim
      }

      super.getListCellRendererComponent(list, text, index, iss, chf);

      setIcon(icon)

      this
    }
  }
}