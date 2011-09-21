package net.slate.builder

import java.io._

object ProjectConfigurator {

  def init(project: File) = {
    val dir = new File(project.getAbsolutePath + File.separator + ".slate")
    if (!dir.exists) {
      dir.mkdir
      val file = new File(dir.getAbsolutePath + File.separator + "settings.xml")
      val fstream = new FileWriter(file);
      val out = new BufferedWriter(fstream);
      out.write("<settings>\n<srcdirs>\n<dir path=\"src\"/>\n</srcdirs>\n<destdir path=\"classes\"/>\n<classpath path=\"\"/>\n</settings>");
      //Close the output stream
      out.close();
      println("Please define project settings in " + file.getAbsolutePath)
    } else if (!dir.isDirectory) {
      println("critical error - " + project + ".slate exists but is not a directory.")
    }
  }
}