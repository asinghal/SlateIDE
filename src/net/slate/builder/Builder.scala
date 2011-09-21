package net.slate.builder

import scala.xml._
import scala.actors.Actor._

import java.io.{ File, FileReader }
import scala.io.Source

trait Builder {

  import net.slate.util.FileUtils

  lazy val configuration = loadConfig

  def build: List[Message]

  /**
   *
   * @return
   */
  protected def supportedExtension: String

  /**
   *
   * @param dir
   * @return
   */
  protected def findAllFiles(dir: String): List[String] = FileUtils.findAllFiles(dir, supportedExtension)

  /**
   *
   * @param dir
   * @param command
   */
  protected def execute(dir: String, command: String*) = {

    val a = actor {
      val pb =
        new ProcessBuilder(command: _*)
      var env = pb.environment
      env.put("JAVA_OPTS", "-Xmx512M -Xms32M -Xss20M")
      pb.directory(new File(dir))
      val p = pb.start()
      p.waitFor
      println("done")
      println(read(p.getErrorStream))
      println(read(p.getInputStream))
      p.destroy
    }
  }

  /**
   * 
   * @param stream
   */
  private def read(stream: java.io.InputStream) = {
    val source = Source.fromInputStream(stream)
    val lines = source.mkString
    source.close()
  }

  /**
   * 
   * @return
   */
  private def loadConfig = {
    val xml = XML.load(getClass.getClassLoader.getResourceAsStream("builders.xml"))

    var config = Map[String, String]()

    xml \\ "builder" foreach { builder =>
      val builderType = (builder \\ "@type").text
      val executablePath = (builder \\ "@executable_path").text
      config += (builderType -> executablePath)
    }

    config
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

    val classpath = (xml \\ "classpath" \\ "@path").text
    (src, destdir, classpath)
  }
}