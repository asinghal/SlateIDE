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
package net.slate.gui.popupmenu

import java.awt.{ Color, Font }
import javax.swing.{ BorderFactory, JPopupMenu }
import javax.swing.border._
import scala.swing.{ Action, Menu, Component, MenuItem }
import scala.swing.SequentialContainer.Wrapper

import net.slate.Launch
import net.slate.Size

object PopupMenu {
  private[PopupMenu] trait JPopupMenuMixin { def popupMenuWrapper: PopupMenu }
}

class PopupMenu extends Component with Wrapper {

  override lazy val peer: JPopupMenu = new JPopupMenu with PopupMenu.JPopupMenuMixin with SuperMixin {
    def popupMenuWrapper = PopupMenu.this
  }

  def show(invoker: Component, x: Int, y: Int): Unit = peer.show(invoker.peer, x, y)

  /* Create any other peer methods here */
}

class EditorPopupMenu extends PopupMenu with MenuPainter {
  import net.slate.action._

  contents += new Menu("Source") with MenuFont {
    contents += new MenuItem("Format") with MenuFont { peer.addActionListener(new FormatFileAction) }
    contents += new MenuItem("Organise Imports") with MenuFont { peer.addActionListener(new OrganiseImportsAction) }
  }
}

trait MenuPainter {
  self: Component =>
}

trait MenuFont {
  self: Component =>
  border = BorderFactory.createEmptyBorder
}

trait CreateNewItemMenuItem {
  self: Component =>
  val itemSize = Size(150, 20)
  minimumSize = itemSize
  maximumSize = itemSize
}