package net.slate.builder

object TestCaseMessage {

  def parse(program: String, line: String) = {
    program match {
      case "scala" => parseForScalaTest(line)
      case "java" =>
      case _ =>
    }
  }

  private def parseForScalaTest(line: String) = {
    var className = ""

    var testcases = new java.util.HashMap[String, (Boolean, String)]()

    var testName = ""

    line.split("\n").foreach { x =>
      val l = x.replace("[0m", "").trim

      if (l.charAt(4) == '-') {
        testName = l.substring(6)
        testcases.put(testName, (!l.endsWith("*** FAILED ***"), ""))
      } else if (l.startsWith("[32m") && className == "") {
        className = l.substring(4, l.indexOf(":"))
      } else if (l.startsWith("[31m ")) {
        val status = testcases.get(testName)
        val trace = status._2 + l.substring(5)
        testcases.put(testName, (status._1, trace + "\n"))
      }
    }

    println(className)
    println(testcases)
  }

}