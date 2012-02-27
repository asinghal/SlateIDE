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

import java.awt.{ BorderLayout, Color, FlowLayout }
import java.awt.event.KeyEvent
import javax.swing.{ BorderFactory, BoxLayout, JRadioButton, JButton, JTextField, JCheckBox, JDialog, JPanel }
import scala.swing._
import scala.swing.event._
import net.slate.Launch._
import net.slate.Size
import net.slate.editor.search._

class FindDialog(frame: MainFrame) extends Dialog(frame.owner) {
  title = "Find"
  val SPACING = 5

  var curFinder: Finder = null

  //  com.sun.awt.AWTUtilities.setWindowOpacity(peer, 0.8f)

  val pane = new JPanel(new BorderLayout(SPACING, SPACING));
  pane.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING))
  peer.getContentPane.add(pane)

  val pnlCenter = new JPanel(new BorderLayout(SPACING, SPACING))
  val pnlTextBoxes = new JPanel(new BorderLayout(SPACING, SPACING))
  val pnlRight = new JPanel()
  pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS))
  val pnlBottom = new JPanel(new BorderLayout(SPACING, SPACING))
  val pnlOptions = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))
  val pnlDirection = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))
  pane.add(pnlCenter, BorderLayout.CENTER)
  pane.add(pnlRight, BorderLayout.EAST)
  pnlCenter.add(pnlBottom, BorderLayout.SOUTH)
  pnlBottom.add(pnlOptions, BorderLayout.WEST)
  pnlBottom.add(pnlDirection, BorderLayout.EAST)
  pnlOptions.setBorder(BorderFactory.createTitledBorder("Options"))
  pnlDirection.setBorder(BorderFactory.createTitledBorder("Direction"))

  val chkCase = new JCheckBox("Case sensitive")
  pnlOptions.add(chkCase)
  chkCase.setMnemonic('c')

  val radUp = new RadioButton("Up") { mnemonic = Key('u') }
  val radDown = new RadioButton("Down") {
    mnemonic = Key('d')
    selected = true
  }
  pnlDirection.add(radUp.peer)
  pnlDirection.add(radDown.peer)

  val txtFind = new JTextField()
  pnlCenter.add(pnlTextBoxes, BorderLayout.NORTH)
  pnlTextBoxes.add(txtFind, BorderLayout.NORTH)
  val txtReplace = new JTextField()
  pnlTextBoxes.add(txtReplace, BorderLayout.SOUTH)

  val butFind = new Button("Find Next")
  val butReplace = new Button("Replace All")
  val butCancel = new Button("Cancel")
  pnlRight.add(butFind.peer)
  pnlRight.add(butReplace.peer)
  pnlRight.add(butCancel.peer)

  listenTo(butFind, butReplace, butCancel)
  reactions += {
    case ButtonClicked(`butFind`) =>
      TextSearch.findNext(txtFind.getText, chkCase.isSelected, radDown.selected)
    case ButtonClicked(`butReplace`) =>
      TextSearch.replace(txtFind.getText, txtReplace.getText(), chkCase.isSelected, radDown.selected)
    case ButtonClicked(`butCancel`) =>
      peer.setVisible(false)
  }

  preferredSize = Size(330, 150)
  resizable = false

  def display() {
    pack()
    txtFind.requestFocus()
    txtFind.selectAll()
    //    peer.setLocationRelativeTo(frame.peer)
    peer.setLocation(950, 100);

    peer.setVisible(true)
  }
}
