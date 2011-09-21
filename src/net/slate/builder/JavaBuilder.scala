package net.slate.builder

import net.slate.ExecutionContext._

object JavaBuilder extends Builder {

  def build: List[Message] = {
    val dir = currentProjectName
    execute(dir, configuration("javac"), "-classpath bin\\ ", dir + "\\controllers\\PersonsController.scala")
    List()
  }

  protected def supportedExtension: String = ".java"
}