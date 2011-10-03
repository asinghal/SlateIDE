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
package net.slate.util

import java.io.File
import net.slate.Launch._

/**
 *
 * @author Aishwarya Singhal
 *
 */
object FileUtils {

  /**
   * Find all files in a directory (recursively).
   *
   * @param dir
   * @param supportedExtension
   *
   * @return List
   */
  def findAllFiles(dir: String, supportedExtension: String, ignoredExtension: String = ".class"): List[String] = {
    var list = List[String]()

    if (new File(dir).exists) {
      new File(dir).list.foreach { f =>
        val file = dir + File.separator + f

        val isDirectory = new File(file).isDirectory

        if (!isDirectory && (supportedExtension == null || f.toLowerCase.endsWith(supportedExtension))
          && (ignoredExtension == null || !f.toLowerCase.endsWith(ignoredExtension))) {
          list :::= List(file)
        } else if (isDirectory) {
          list :::= findAllFiles(file, supportedExtension, ignoredExtension)
        }
      }
    }

    list
  }

  def getSimpleName(file: String) = {
    file.substring(file.lastIndexOf(File.separator) + 1)
  }

  def open(name: String, path: String) = {
    val opened = if (addTab(name, path)) {
      updateStatusBar("Loading " + path)
      val source = scala.io.Source.fromFile(path)
      val lines = source.mkString
      source.close()
      currentScript.text.text = lines
      currentScript.text.peer.setSelectionStart(0)
      currentScript.text.peer.setSelectionEnd(0)
      currentScript.text.undoManager.discardAllEdits
      currentScript.text.onload
      updateStatusBar("Ready")
      true
    } else false

    opened
  }

  /**
   * Delete a file/ directory recursively.
   */
  def delete(f: File): Boolean = {
    if (f.isDirectory()) {
      for (c <- f.listFiles) {
        delete(c)
      }
    }

    f.delete
  }
}