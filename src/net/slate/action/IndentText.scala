package net.slate.action

trait IndentText {

  protected def indentLine(line: String, enableExtra: Boolean) = {
    val spacesCount = countWhitespace(line)

    var indentation = 0

    var trimmed = line.trim

    if (trimmed.endsWith("{")) indentation += 2
    if (trimmed.endsWith("}")) indentation -= 0
    if (trimmed.endsWith("*/")) indentation -= 1

    val space = whitespace(spacesCount + indentation)

    val extra = if (!enableExtra) ""
    else if (line.endsWith("/**") || line.endsWith("/*")) " * "
    else if (trimmed.startsWith("* ") || trimmed == "*") "* "
    else if (trimmed.endsWith("{")) ('\n' + space.substring(2) + "}")
    else ""

    ('\n' + space + extra, space.length)
  }

  protected def countWhitespace(line: String): Int = {
    var count = 0
    for (i <- 0 to line.length - 1) {
      val c = line.charAt(i)
      c match {
        case ' ' => count += 1
        case '\t' => count += 1
        case _ => return count
      }
    }

    count
  }

  protected def whitespace(count: Int) = {

    var space = ""
    for (i <- 1 to count) {
      space += " "
    }

    space
  }

}