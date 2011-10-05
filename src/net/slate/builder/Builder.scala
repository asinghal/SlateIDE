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

import scala.xml._
import scala.actors.Actor._

import java.io.{ File, FileReader }
import scala.io.Source

trait Builder {

  import net.slate.ExecutionContext._
  import net.slate.Launch._
  import net.slate.util.FileUtils
  lazy val configuration = loadConfig
  var alacsJar: String = null

  private lazy val pathSeparator = System.getProperty("path.separator")

  var buildInProgress = false

  def build: List[Message]

  /**
   *
   * @return
   */
  protected def supportedExtension: String

  def getClassPath(project: String) = {
    settings(project)._3
  }

  def getClassPathURLs(project: String) = {
    val path = System.getProperty("sun.boot.class.path") + pathSeparator + settings(project)._3
    var urls = List[java.net.URL]()
    path.split(pathSeparator).foreach { p => urls :::= List(new File(p).toURL) }

    (urls.toArray, settings(project)._2)
  }
  /**
   *
   * @param dir
   * @return
   */
  protected def findAllFiles(dir: String): List[String] = FileUtils.findAllFiles(dir, supportedExtension)

  protected def execute(project: String, program: String, className: String, test: Boolean = false,
    programArgs: Array[String] = Array(), vmArgs: Array[String] = Array()) = {

    val projectSettings = settings(project)

    val executable = configuration(program)
    val dir = projectSettings._2
    val classpath = dir + pathSeparator + projectSettings._3

    var command = List[String]()

    command :::= List.fromArray(programArgs)
    command :::= List(className)
    command :::= List.fromArray(vmArgs)
    command :::= List(executable, "-classpath", classpath)

    executeCommand(command, dir, program, test)
  }

  protected def executeCommand(command: List[String], dir: String, program: String, test: Boolean) = {
    progressBar.visible = true
    var commandList = java.util.Arrays.asList(command.toArray: _*)

    val pb =
      new ProcessBuilder(commandList)
    pb.directory(new File(dir))
    pb.redirectErrorStream(true)

    System.getenv().keySet.toArray.foreach { key =>
      println(key)
      pb.environment.put(key.toString, System.getenv().get(key))
    }

    pb.environment.put("JAVA_OPTS", "-Xmx256M -Xms32M -Xss32M")
    val p = pb.start()
    runningProcess = p

    actor {
      val error = read(p.getErrorStream)
      println(error)
      val output = read(p.getInputStream)
      println(output)
      p.waitFor
      p.destroy
      println("done")
      val netOutput = error + output
      if (test) TestCaseMessage.parse(dir, program, netOutput)
      progressBar.visible = false
      runningProcess = null
    }
  }

  private def read(stream: java.io.InputStream) = {
    val source = Source.fromInputStream(stream)
    val lines = source.mkString
    source.close()
    lines
  }

  /**
   *
   * @param project
   * @return
   */
  protected def settings(project: String) = {
    val xml = XML.load(new FileReader(project + File.separator + ".slate" + File.separator + "settings.xml"))

    var config = Map[String, String]()

    var src = List[String]()

    xml \\ "srcdirs" \\ "dir" foreach { srcdir =>
      src :::= List(project + File.separator + (srcdir \\ "@path").text)
    }
    val destdir = project + File.separator + (xml \\ "destdir" \\ "@path").text

    if (!new File(destdir).exists) {
      new File(destdir).mkdir
    }

    val enableAlacs = if ((xml \\ "enableAlacs").text != "") (xml \\ "enableAlacs").text.toBoolean else false

    val classpath = (xml \\ "classpath" \\ "@path").text
    (src, destdir, qualifyClasspath(classpath, project), enableAlacs)
  }

  /**
   *
   * @param src
   * @return
   */
  protected def isModified(src: String, srcDir: String, destDir: String) = {
    val bytecode = src.replace(supportedExtension, ".class").replace(srcDir, destDir)
    new File(src).lastModified > new File(bytecode).lastModified
  }

  /**
   *
   * @return
   */
  private def loadConfig = {
    val skin = XML.load(getClass.getClassLoader.getResourceAsStream("builders.xml"))

    var config = Map[String, String]()

    skin \\ "builder" foreach { builder =>
      val builderType = (builder \\ "@type").text
      val executablePath = (builder \\ "@executable_path").text
      config += (builderType -> executablePath)
    }

    alacsJar = (skin \\ "alacs" \\ "@jar").text

    config
  }

  private def qualifyClasspath(classpath: String, project: String) = {
    var cp = ""
    classpath.split(pathSeparator).foreach { p =>
      if (p.endsWith("*.jar") || p.endsWith("*.zip")) {
        val dir = p.replace("*.jar", "").replace("*.zip", "")
        new File(dir).list.filter { lib => lib.endsWith(".jar") || lib.endsWith(".zip") }.foreach { lib =>
          cp += (dir + lib + pathSeparator)
        }
      } else if (p.startsWith(".\\")) {
        cp += (project + File.separator + p.substring(2))
      } else {
        cp += (p + pathSeparator)
      }
    }

    cp
  }
}