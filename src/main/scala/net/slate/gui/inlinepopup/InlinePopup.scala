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
 *  Created on: 19th October 2011
 */
package net.slate.gui

import javax.swing.Popup
import net.slate.Launch._

/**
 * A generic outline for all inline popups (used for making code suggestions).
 *
 * @author Aishwarya Singhal
 */
trait InlinePopup {
  import scala.actors._

  var processor: Actor = null

  var popup: Popup = null

  def isOpen = popup != null

  def hide {
    if (popup != null) {
      popup.hide
      popup = null
      processor = null
    }
  }

  def restoreFocus = {
    javax.swing.SwingUtilities.invokeLater(new Runnable {
      def run = {
        hide
        currentScript.text.peer.requestFocus
      }
    })
  }
}