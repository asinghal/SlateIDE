package net.slate.formatting

import javax.swing.text.DefaultStyledDocument

class JSFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val SLASH_STAR_COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)"
  val SLASH_SLASH_COMMENT = "//.*"
  val IDENT = "[\\w\\$&&[\\D]][\\w\\$]*"

  val KEYWORDS = Seq("function", "do", "else", "false", "for", "if", "new", "null", "object", "return",
    "this", "true", "var", "while").map("\\b" + _ + "\\b")

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(SLASH_STAR_COMMENT, styles('multilineComment)).putStyle(SLASH_SLASH_COMMENT, styles('inlineComment))
//  lexer.putChild(IDENT, new LexerNode().putStyle(KEYWORDS, styles('reserved)))
}