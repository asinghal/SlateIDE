package net.slate.formatting

import javax.swing.text.DefaultStyledDocument

class PropertiesFileFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import Configuration._

  val COMMENT = "[ \\t]*#.*"

  implicit def customStyle2Style(c: CustomStyle) = c.self

  lexer.putStyle(COMMENT, styles('multilineComment))
}