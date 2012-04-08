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
 *  Created on: 28th February 2012
 */
package net.slate.util

import java.awt.event.{ InputEvent, KeyEvent }
import javax.swing.KeyStroke
import scala.util.Properties.isMac

object KeyStrokeHelper {

  def apply(keyEvent: Int, inputEvent: Int ) = {
    val input = if (isMac && inputEvent == InputEvent.CTRL_DOWN_MASK) InputEvent.META_DOWN_MASK else inputEvent
    
    KeyStroke.getKeyStroke(keyEvent, input)
  }
  
  def apply(k: String) = {
    val keys = if (isMac) k.replace("control", "meta") else k
    
    KeyStroke.getKeyStroke(keys)
  }
}