package net.slate.formatting

import java.awt.Color
import javax.swing.text.{ StyleConstants, StyleContext, DefaultStyledDocument }

class ScalaFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import ScalaFilter._
  import Configuration._

  implicit def customStyle2Style(c: CustomStyle) = c.self

  (SLASH_STAR_COMMENT -> styles('multilineComment)) :: (SLASH_SLASH_COMMENT -> styles('inlineComment)) ::
    (QUOTES -> styles('quotes)) :: (DIGITS -> styles('digits)) :: (OPERATION -> styles('operation)) ::
    (SYMBOL -> styles('symbols)) ::
    (IDENT -> styles('ident)) :: Nil foreach { kv =>
      lexer.putStyle(kv._1, kv._2)
    }
//  lexer.putChild(OPERATION, new LexerNode().putStyle(LEFT_PARENS, styles('leftParen)))
  lexer.putChild(IDENT, new LexerNode().putStyle(keywords, styles('reserved)).putStyle(typeDefs, styles('typeDefs)))
}

object ScalaFilter {
  val COMMENT_COLOR = Color.LIGHT_GRAY.darker.darker
  val SLASH_STAR_COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)"
  val SLASH_SLASH_COMMENT = "//.*"
  val SYMBOL = "'[\\D][\\w]* "
  val QUOTES = "'[\\w]'|(?ms:\"{3}(?!\\\"{1,3}).*?(?:\"{3}|\\z))|(?:\"{1}(?!\\\").*?(?:\"|\\Z))"
  val IDENT = "[\\w\\$&&[\\D]][\\w\\$]*"
  val OPERATION = "[\\w\\$&&[\\D]][\\w\\$]* *\\("
  val LEFT_PARENS = "\\("
  val DIGITS = "\\d+?[efld]?"
}

object Configuration {
  import scala.xml._

  var styles = scala.collection.mutable.Map[Symbol, CustomStyle]()
  val sc = StyleContext.getDefaultStyleContext
  val defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE)

  var editorBackground: Color = Color.WHITE
  var editorForeground: Color = Color.BLACK
  var editorCursorColor: Color = Color.BLACK

  var keywords = List[String]()
  var typeDefs = List[String]()
  var predefinedTypes = List[String]()

  loadStyles
  loadKeywords

  class CustomStyle(val name: Symbol) {

    val self = sc.addStyle(name.name, defaultStyle)

    def italics(i: Boolean) {
      StyleConstants.setItalic(self, i)
    }

    def foreground(color: Color) {
      StyleConstants.setForeground(self, color)
    }

    def background(color: Color) {
      StyleConstants.setBackground(self, color)
    }

    def bold(i: Boolean) {
      StyleConstants.setBold(self, i)
    }
  }

  def loadStyles = {
    val skin = XML.load(getClass.getClassLoader.getResourceAsStream("skin.xml"))

    val background = skin \\ "background" \\ "@color"
    if (background != null && background.text != "") editorBackground = Color.decode(background.text)

    val foreground = skin \\ "foreground" \\ "@color"
    if (foreground != null && foreground.text != "") editorForeground = Color.decode(foreground.text)

    val cursor = skin \\ "cursor" \\ "@color"
    if (cursor != null && cursor.text != "") editorCursorColor = Color.decode(cursor.text)

    skin \\ "element" foreach { e =>
      val style = buildStyle(e)
      styles(style.name) = style
    }
  }

  def loadKeywords = {
    val keywordsData = XML.load(getClass.getClassLoader.getResourceAsStream("keywords.xml"))

    keywordsData \\ "reserved" foreach { e =>
      keywords :::= List("\\b" + e.text + "\\b")
    }

    keywordsData \\ "type" foreach { e =>
      typeDefs :::= List("\\b" + e.text + "\\b")
    }

    keywordsData \\ "predefinedType" foreach { e =>
      typeDefs :::= List("\\b" + e.text + "\\b")
    }
  }

  def buildStyle(element: Node) = {
    val name = (element \\ "@type").text
    val background = element \\ "@background"
    val foreground = element \\ "@foreground"
    val bold = element \\ "@bold"
    val italics = element \\ "@italics"
    val style = new CustomStyle(Symbol(name))
    if (background != null && background.text != "") style.background(Color.decode(background.text))
    if (foreground != null && foreground.text != "") style.foreground(Color.decode(foreground.text))
    if (bold != null && bold.text != "") style.bold(bold.text.toBoolean)
    if (italics != null && italics.text != "") style.italics(italics.text.toBoolean)

    style
  }
}