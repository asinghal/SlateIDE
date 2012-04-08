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
package net.slate

import java.awt.event.{ MouseEvent, MouseAdapter }
import java.awt.{ SystemTray, Toolkit }
import scala.util.Properties.isMac

object TrayIcon {
  val icon = Toolkit.getDefaultToolkit.createImage("images/ide.gif")

  val supported = SystemTray.isSupported && !isMac

  def init() {
    if (supported) {
      val trayIcon = new java.awt.TrayIcon(icon, "Scala Cafe")
      SystemTray.getSystemTray.add(trayIcon)
      trayIcon.addMouseListener(new MouseAdapter() {
        override def mouseClicked(e: MouseEvent) {
          if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount == 2) {
            val main = Launch.top
            main.visible = !main.visible
            if ((main.peer.getExtendedState & java.awt.Frame.ICONIFIED) != 0) main.uniconify()
          }
        }
      })
    }
  }
}