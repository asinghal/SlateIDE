package net.slate.editor.tools

import java.io.{ File, FileInputStream }
import java.util.jar.{ JarEntry, JarInputStream }

/**
 *
 * @author Aishwarya Singhal
 */
object TypeCacheBuilder {

  def getAllClassNames = {
    var classes = List[String]()

    var classpath = System.getProperty("sun.boot.class.path")
    if (classpath != "") classpath += ";"
    classpath += System.getProperty("java.class.path")
    
    val entries = classpath.split(";").foreach { entry =>
      if (entry.toLowerCase().endsWith(".jar")) { classes :::= getAllClassNamesFromJar(entry) }
      if (!entry.toLowerCase().endsWith(".jar")) { classes :::= findClasses(new File(entry), null) }
    }

    classes
  }

  /**
   *
   */
  private def getAllClassNamesFromJar(jarName: String) = {
    var classes = List[String]()

    try {
      val jarFile = new JarInputStream(new FileInputStream(
        jarName))

      var continue = true
      while (continue) {
        val jarEntry = jarFile.getNextJarEntry
        if (jarEntry != null && jarEntry.getName.endsWith(".class")) {
          classes :::= List(jarEntry.getName.replaceAll("/", "\\."))
        }

        if (jarEntry == null) {
          continue = false
        }
      }
    } catch {
      case e: Exception =>
//        e.printStackTrace
    }

    classes
  }

  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory   The base directory
   * @param packageName The package name for classes found inside the base directory
   * @return The classes
   */
  private def findClasses(directory: File, packageName: String): List[String] = {
    var classes = List[String]()
    if (!directory.exists()) {
      return classes
    }

    directory.listFiles.foreach { file =>
      val packagePrefix = if (packageName != null) (packageName + ".") else ""
      if (file.isDirectory) {
        classes :::= findClasses(file, packagePrefix + file.getName)
      } else if (file.getName().endsWith(".class")) {
        classes :::= List(packagePrefix + file.getName().substring(0, file.getName().length() - 6))
      }
    }

    return classes;
  }

}