package net.slate.builder

import net.slate.ExecutionContext._

object JavaBuilder extends Builder {

  def build = {
    val dir = currentProjectName
    execute(Array(configuration("javac"), "-classpath bin\\ ", dir + "\\controllers\\PersonsController.scala"))
  }
  
  protected def supportedExtension: String = ".java"
}