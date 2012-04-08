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

import scala.swing.Dimension
import java.awt.{ Toolkit }

object Size {

  val toolkit = Toolkit.getDefaultToolkit
  val screen = toolkit.getScreenSize
  
  val w = screen.width.toDouble / 1280
  val h = screen.height.toDouble / 800

  def apply(x: Int, y: Int) = {
    new Dimension((x * w).toInt, (y * h).toInt)
  }
  
  def adjustWidth(x: Int) = (x * w).toInt
  def adjustHeight(y: Int) = (y * h).toInt
}