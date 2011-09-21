package net.slate.builder

import java.io.File
import net.slate.ExecutionContext._
import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.ConsoleReporter

object ScalaBuilder extends Builder {

  def build: List[Message] = {

    val projectSettings = settings(currentProjectName)
    var sourceFiles = List[File]()

    val destDir = projectSettings._2
    val classpath = projectSettings._3

    projectSettings._1.foreach { dir =>
      findAllFiles(dir).filter { isModified(_) }.foreach { x => sourceFiles :::= List(new File(x)) }
    }

    var errors = List[Message]()

    if (!sourceFiles.isEmpty) {
      val settings = new Settings
      settings.outdir.value = destDir
      settings.classpath.value = classpath
      settings.unchecked.value = true

      val reporter = new ConsoleReporter(settings) {
        override def printMessage(msg: String) {
          //          println(msg + "\n")
          if (msg.indexOf(": error:") != -1) { errors :::= List(Message.parse(msg)) }
        }
      }
      val compiler = new Global(settings, reporter) // compiles the actual code

      try new compiler.Run compile (sourceFiles map (_.toString))
      catch {
        case ex: Throwable =>
          ex.printStackTrace()
          val msg = if (ex.getMessage == null) "no error message provided" else ex.getMessage
          error("Compile failed because of an internal compiler error (" + msg + "); see the error output for details.")
      }

      reporter.printSummary()
    }

    errors
  }

  protected def supportedExtension: String = ".scala"

  private def isModified(src: String) = {
    val bytecode = src.replace(supportedExtension, ".class")
    new File(src).lastModified > new File(bytecode).lastModified
  }
}