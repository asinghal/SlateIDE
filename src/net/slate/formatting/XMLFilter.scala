package net.slate.formatting

import javax.swing.text.DefaultStyledDocument

class XMLFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val COMMENT = "<!--(?s:.)*?(?:-->|\\z)"
  val NODE_START = "<[\\?\\w]*"
  val NODE_CLOSE = "[\\?/]?>"
  val NODE_END = "</[\\w]*>"
  val QUOTES = "(?ms:\"{3}(?!\\\"{1,3}).*?(?:\"{3}|\\z))|(?:\"{1}(?!\\\").*?(?:\"|\\Z))"

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(COMMENT, styles('multilineComment)).putStyle(NODE_START, styles('symbols)).putStyle(NODE_CLOSE, styles('symbols)).putStyle(NODE_END, styles('symbols)).putStyle(QUOTES, styles('quotes))
}