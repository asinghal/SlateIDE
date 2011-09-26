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
package net.slate.builder

import java.io._

object ProjectConfigurator {

  def init(project: File, src: String = "src", test:String = "test", output: String = "classes", classpath: String = "") = {
    val dir = new File(project.getAbsolutePath + File.separator + ".slate")
    if (!dir.exists) {
      dir.mkdir
      val source = scala.io.Source.fromFile("templates/settings.xml")
      var text = source.mkString
      source.close()

      text = text.replace("${src}", src).replace("${test}", test).replace("${classes}", output).replace("${classpath}", classpath)
      val file = new File(dir.getAbsolutePath + File.separator + "settings.xml")
      val fstream = new FileWriter(file);
      val out = new BufferedWriter(fstream);
      out.write(text);
      //Close the output stream
      out.close();
      println("Please define project settings in " + file.getAbsolutePath)
    } else if (!dir.isDirectory) {
      println("critical error - " + project + ".slate exists but is not a directory.")
    }
  }
}