package net.slate.util

import java.io.File
import net.slate.Launch._

/**
 *
 * @author Aishwarya Singhal
 *
 */
object FileUtils {

  /**
   * Find all files in a directory (recursively).
   *
   * @param dir
   * @param supportedExtension
   *
   * @return List
   */
  def findAllFiles(dir: String, supportedExtension: String): List[String] = {
    var list = List[String]()

    if (new File(dir).exists) {
      new File(dir).list.foreach { f =>
        val file = dir + File.separator + f

        val isDirectory = new File(file).isDirectory

        if (!isDirectory && (supportedExtension == null || f.toLowerCase.endsWith(supportedExtension))) {
          list :::= List(file)
        } else if (isDirectory) {
          list :::= findAllFiles(file, supportedExtension)
        }
      }
    }

    list
  }

  def getSimpleName(file: String) = {
    file.substring(file.lastIndexOf(File.separator) + 1)
  }

  def open(name: String, path: String) = {
    val opened = if (addTab(name, path)) {
      updateStatusBar("Loading " + path)
      val source = scala.io.Source.fromFile(path)
      val lines = source.mkString
      source.close()
      currentScript.text.text = lines
      currentScript.text.peer.setSelectionStart(0)
      currentScript.text.peer.setSelectionEnd(0)
      currentScript.text.undoManager.discardAllEdits
      updateStatusBar("Ready")
      true
    } else false

    opened
  }
}