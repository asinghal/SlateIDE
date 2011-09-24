package net.slate.builder

import java.io.{ File, PrintWriter, StringWriter }
import net.slate.ExecutionContext._

/**
 *
 */
object JavaBuilder extends Builder {

  /**
   *
   */
  def build: List[Message] = {
    val projectSettings = settings(currentProjectName)
    var sourceFiles = List[String]()

    val destDir = projectSettings._2
    val classpath = projectSettings._3

    projectSettings._1.foreach { dir =>
      findAllFiles(dir).filter { isModified(_, dir, destDir) }.foreach { x => sourceFiles :::= List(x) }
    }

    var errors = List[Message]()

    if (!sourceFiles.isEmpty && !buildInProgress) {
      buildInProgress = true
      val args = List("-classpath") ::: List(classpath) ::: List("-d") ::: List(destDir) ::: sourceFiles
      val files = args.toArray.asInstanceOf[Array[String]]

      try {
        val result = new StringWriter
        val printWriter = new PrintWriter(result)
        val c = Class.forName("com.sun.tools.javac.Main");
        val compiler = c.newInstance();
        val compile = c.getMethod("compile",
          classOf[Array[String]], classOf[PrintWriter])
        compile.invoke(compiler, files, printWriter)

        val lines = result.toString.split("\n").foreach { msg =>
          if (msg.indexOf(".java:") != -1) { errors :::= List(Message.parse(msg, 2)) }
        }

      } catch {
        case ex: Exception =>
          ex.printStackTrace()
      }
    }

    buildInProgress = false

    errors
  }

  def run(project: String, className: String) = {
    execute(project, "java", className)
  }

  protected def supportedExtension: String = ".java"
}