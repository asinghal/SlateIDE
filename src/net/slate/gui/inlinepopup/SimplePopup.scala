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
 *  Created on: 28th December 2011
 */
package net.slate.gui

import scala.swing.{ Action, Component }
import net.slate.Launch._
import net.slate.util.FileUtils._
import net.slate.Launch._

/**
 *
 */
object SimplePopup extends CommonPopup {

  def show(list: Array[AnyRef]) {
    showPopup(list) { insert }
  }

  def insert(index: Int, list: Array[AnyRef]) {
    list(index) match {
      case text: String =>
        val path = text.substring(0, text.lastIndexOf(":")).trim
        val position = text.substring(text.lastIndexOf(":") + 1).split("-")(1).trim
        val name = path.substring(path.lastIndexOf(java.io.File.separator) + 1)
        open(name, path)
        currentScript.text.peer.setCaretPosition(position.toInt);
    }
    restoreFocus
  }
}