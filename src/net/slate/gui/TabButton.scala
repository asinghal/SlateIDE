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

import swing.Button
import net.slate.Launch._

/**
 * This is the 'close' button that appears on each tab of the editor.
 */
class TabButton(name: String) extends Button {
  import java.awt.{ BasicStroke, Color, Dimension, Graphics2D }
  import javax.swing.BorderFactory
  import javax.swing.plaf.basic.BasicButtonUI
  import swing.event.MouseClicked

  val buttonSize = 18
  preferredSize = new Dimension(buttonSize, buttonSize)

  tooltip = "Close"
  peer.setUI(new BasicButtonUI)
  contentAreaFilled = false
  focusable = false
  border = BorderFactory.createEtchedBorder()
  borderPainted = false
  rolloverEnabled = true

  peer.addMouseListener(new java.awt.event.MouseAdapter {
    override def mousePressed(e: java.awt.event.MouseEvent) {
      closeTab(name)
    }
  })

  override protected def paintComponent(g: Graphics2D) {
    super.paintComponent(g)
    g.setStroke(new BasicStroke(2));
    g.setColor(Color.decode("0xAAAAAA"));
    if (peer.getModel().isRollover()) {
      g.setColor(Color.decode("0xFFC4E1"));
    }
    val delta = 6;
    g.drawLine(delta, delta, peer.getWidth() - delta - 1, peer.getHeight() - delta - 1);
    g.drawLine(peer.getWidth() - delta - 1, delta, delta, peer.getHeight() - delta - 1);

    g.dispose()
  }
}