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

/**
 * Container for recording in flight operations.
 *
 * @author Aishwarya Singhal
 */
object ExecutionContext {

  /**
   * A list of all projects  that have been loaded into the context.
   */
  var loadedProjects = List[String]()

  /**
   * The process that is running at the moment. Null denotes no running processes.
   */
  var runningProcess: Process = null

  /**
   * Gets the current project based on the file open in editor.
   */
  def currentProjectName: String = {
    val file = Launch.currentScript.text.path
    currentProjectName(file)
  }

  /**
   * Gets the current project based on the supplied path.
   */
  def currentProjectName(selectedDir: String) = {
    val file = selectedDir
    val p = loadedProjects.sortWith { _ > _ }.filter { project =>
      file.contains(project)
    }

    if (p.isEmpty) loadedProjects(0) else p(0)
  }

  /**
   * Stops the running process.
   */
  def stop = {
    runningProcess.destroy
    runningProcess = null
  }
}