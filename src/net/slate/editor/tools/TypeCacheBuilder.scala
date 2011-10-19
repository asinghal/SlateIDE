/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 20th September 2011
 */
package net.slate.editor.tools

import java.io.{ File, FileInputStream }
import java.util.jar.{ JarEntry, JarInputStream }

import net.slate.builder.ScalaBuilder

/**
 *
 * @author Aishwarya Singhal
 */
object TypeCacheBuilder {
  private lazy val pathSeparator = System.getProperty("path.separator")

  def getAllClasses(project: String) = {
    var classes = List[String]()

    var classpath = System.getProperty("sun.boot.class.path")
    if (classpath != "") classpath += pathSeparator
    classpath += ScalaBuilder.getClassPath(project)
    
    val entries = classpath.split(pathSeparator).foreach { entry =>
      if (entry.toLowerCase().endsWith(".jar")) { classes :::= getAllClassesFromJar(entry) }
      if (!entry.toLowerCase().endsWith(".jar")) { classes :::= findClasses(new File(entry), null) }
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
  def findScalaTestCaseClasses(project: String): List[String] = {
    import java.net.{ URL, URLClassLoader }

    val settings = ScalaBuilder.getClassPathURLs(project)

    var classLoader = URLClassLoader.newInstance(settings._1)

    val directory = new File(settings._2)
    
    def loadClass(name: String) = try { findClass(classLoader, name) } catch { case _ => Class.forName(name, true, classLoader) }

    val scalaSuite = Class.forName("org.scalatest.Suite", true, classLoader)
    var classes = findClasses(directory, null) map { loadClass } filter { scalaSuite.isAssignableFrom } map { _.getName }

    return classes;
  }

  /**
   *
   */
  def findClass(project: String, file: String): Class[_] = {
    findClass(getClassLoader(project), file)
  }

  /**
   *
   */
  def getClassLoader(project: String) = {
    import java.net.URLClassLoader

    val settings = ScalaBuilder.getClassPathURLs(project)

    URLClassLoader.newInstance(settings._1)
  }

  /**
   *
   */
  def findClass(classLoader: ClassLoader, file: String): Class[_] = {
    import java.net.URLClassLoader
    val method = classOf[URLClassLoader].getDeclaredMethod("findClass", classOf[String]);
    method.setAccessible(true)
    method.invoke(classLoader, file).asInstanceOf[Class[_]]
  }

  /**
   *
   */
  private def getAllClassesFromJar(jarName: String) = {
    var classes = List[String]()

    try {
      val jarFile = new JarInputStream(new FileInputStream(
        jarName))
      
      var continue = true
      while (continue) {
        val jarEntry = jarFile.getNextJarEntry
        if (jarEntry != null && jarEntry.getName.endsWith(".class")) {
          val name = jarEntry.getName.replace("/", ".").substring(0, jarEntry.getName().length() - 6)
          classes ::= name
        }
        
        if (jarEntry == null) {
          continue = false
        }
      }
    } catch {
      case e: Exception =>
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
      } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
        val name = packagePrefix + file.getName().substring(0, file.getName().length() - 6)
        classes ::= name
      }
    }

    return classes;
  }
}