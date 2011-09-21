package net.slate.formatting

import javax.swing.text.DefaultStyledDocument

class CSSFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)"
  val ATTRIBUTE = "[\\w\\-]*\\:"

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(COMMENT, styles('multilineComment)).putStyle(ATTRIBUTE, styles('symbols))
}