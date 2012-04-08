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
 *  Created on: 11th October 2011
 */
package net.slate.builder

/**
 * Enables execution of heroku related commands.
 *
 * @author Aishwarya Singhal
 */
class HerokuRunner(project: String) extends Runner {

  /**
   * create a new app on heroku.
   *
   * @param name
   */
  def create(name: String) {
    createProcFile
    run("heroku", "create", name, "--stack", "cedar")
  }

  /**
   * deploy code to heroku.
   *
   */
  def deploy {
    executeCommand(List(GitRunner.git, "push", "heroku", "master"), project, "git", false)
  }

  /**
   * run the given command on heroku console.
   *
   * @param command
   */
  def console(command: String) {
    run("run:console", command)
  }

  /**
   * show logs of the application.
   */
  def logs {
    run("logs")
  }

  /**
   * restart this application.
   */
  def restart {
    run("restart")
  }

  /**
   * run the given commands.
   *
   * @param command
   */
  private def run(command: String*) {
    executeCommand((List(List("heroku"), List(command: _*))).flatten, project, "heroku", false)
  }

  /**
   * creates the Procfile.
   */
  private def createProcFile {
    import java.io.{ BufferedWriter, File, FileWriter }
    val writer = new BufferedWriter(new FileWriter(project + File.separator + "Procfile"))
    writer.write("web:    play run --http.port=$PORT $PLAY_OPTS\n")
    writer.close()
  }
}