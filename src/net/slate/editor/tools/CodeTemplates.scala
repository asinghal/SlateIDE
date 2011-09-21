package net.slate.editor.tools

import scala.xml._

object CodeTemplates {

  lazy val all = load
  var map = Map[String ,String]()

  private def load = {
    val templates = XML.load(getClass.getClassLoader.getResourceAsStream("codetemplates.xml"))

    var t = List[(String, String, String)]()
    templates \\ "template" foreach { template =>
      val name = (template \\ "@name").text
      val description = (template \\ "@description").text
      val text = template.text.trim
      map += (name -> text)
      t :::= List((name, description, text))
    }
    
    t
  }
}