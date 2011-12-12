package net.slate.editor.tools

import scala.io.Source
import net.slate.action.LineParser
import net.slate.editor.tools.CodeAssist._

object ScalaAPILookup extends LineParser {
  import net.slate.Launch._

  private[this] val url = "http://api.scalex.org/?q="

  def lookup = {
    import actors.Actor._
    if (isApplicable) {
      var a = actor {

        val urlConnection = new java.net.URL(url + getWord._2).openConnection()
        urlConnection.setUseCaches(true)
        val stream = urlConnection.getInputStream

        val source = Source.fromInputStream(stream)
        val lines = source.mkString
        source.close()
        parse(lines);
      }
    }
  }

  private[this] def isApplicable = {
    
    var applicable = false
    if (currentScript.text.path.endsWith(".scala")) {
      val textPane = currentScript.text.peer
      val caret = textPane.getCaretPosition
      val l = line(textPane, caret)
      
      // TODO need to ignore comments too
      
      applicable = (!l.trim.startsWith("import"))
    }

    applicable
  }

  private[this] def parse(json: String) = {
    import com.google.gson._

    val gson = new Gson()
    val docs = gson.fromJson(json, classOf[APIDocContainer])
    var list = List[String]()
    docs.results.foreach { x => list ::= getText(x) }

    if (!list.isEmpty) apiLookupDialog.display(docs.query, list) else apiLookupDialog.hide
  }

  private[this] def getText(x: APIDoc) = {
    var text = (x.name + " @@@@@@ " + x.signature + " @@@@@@ ")
    if (x.comment != null && x.comment.body != null) {
      text += (x.comment.body.txt)
    }

    text
  }

  class APIDocContainer {
    var query: String = ""
    var results: Array[APIDoc] = Array()
  }

  class APIDoc {
    var name: String = ""
    var qualifiedName: String = ""
    var typeParams: String = ""
    var resultType: String = ""
    var valueParams: String = ""
    var signature: String = ""
    var comment: APIComment = null
  }

  class APIComment {
    var body: APICommentBody = null
  }

  class APICommentBody {
    var html: String = ""
    var txt: String = ""
  }
}