package net.slate.builder

import scala.xml._
import scala.actors.Actor._

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, File }
import scala.io.Source

trait Builder {

  lazy val configuration = loadConfig

  def build

  protected def supportedExtension: String

  protected def findAllFiles(dir: String): List[String] = {
    var list = List[String]()

    new File(dir).list.foreach { f =>
      val file = dir + File.separator + f
      
      if (f.toLowerCase.endsWith(supportedExtension)) {
        list :::= List(file)
      } else if (new File(file).isDirectory) {
        list :::= findAllFiles(file)
      }
    }
    
    list
  }

  protected def execute(command: Array[String]) = {
    //
    //    val pb =
    //      new ProcessBuilder("C:\\scala-2.8.1.final\\bin\\scalac.bat", " -d " + "C:\\project\\github\\portal\\bin " + dir + "\\controllers\\PersonsController.scala" /*, "myArg1", "myArg2"*/ )
    //    pb.directory(new File("C:\\project\\github\\portal\\app"))
    //    val p = pb.start()
    //    actor {
    //      p.waitFor
    //      val source = Source.fromInputStream(p.getInputStream)
    //      val lines = source.mkString
    //      source.close()
    //      println(">>>>>>>>>>>>>   " + p.exitValue);
    //      println(lines);
    //    }

    val p = Runtime.getRuntime.exec(command)
    actor {
      p.waitFor
      val source = Source.fromInputStream(p.getErrorStream)
      val lines = source.mkString
      source.close()
      println(lines);
    println ("done")
    }
  }

  private def loadConfig = {
    val skin = XML.load(getClass.getClassLoader.getResourceAsStream("builders.xml"))

    var config = Map[String, String]()

    skin \\ "builder" foreach { builder =>
      val builderType = (builder \\ "@type").text
      val executablePath = (builder \\ "@executable_path").text
      config += (builderType -> executablePath)
    }

    config
  }
}