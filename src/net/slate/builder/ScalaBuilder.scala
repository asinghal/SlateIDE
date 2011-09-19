package net.slate.builder

import net.slate.ExecutionContext._

object ScalaBuilder extends Builder {

  def build = {
    val dir = currentProjectName
    val files = findAllFiles(dir).mkString(" ").replace(dir + java.io.File.separator, "")
    println(files);
    execute(Array(configuration("scalac"), files))
  }

  protected def supportedExtension: String = ".scala"
}