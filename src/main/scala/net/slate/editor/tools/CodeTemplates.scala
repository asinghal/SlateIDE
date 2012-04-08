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
package net.slate.editor.tools

import scala.xml._

object CodeTemplates {

  lazy val all = load
  var map = Map[String ,String]()

  private def load = {
    val templates = XML.load(getClass.getClassLoader.getResourceAsStream("codetemplates.xml"))

    var t = List[(String, String, String)]()
    templates \\ "template" foreach { template =>
      val name = (template \\ "@name").text
      val description = (template \\ "@description").text
      val text = template.text.trim
      map += (name -> text)
      t :::= List((name, description, text))
    }
    
    t
  }
}