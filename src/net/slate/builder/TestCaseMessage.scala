package net.slate.builder

import net.slate.Launch._

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
    var testcases = Map[String, (Boolean, String)]()
    var failed = false
    var testName = ""

    line.split("\n").foreach { x =>
      val l = x.replace("[0m", "").trim

      if (l.charAt(4) == '-') {
        testName = l.substring(6)
        if (l.endsWith("*** FAILED ***")) failed = true
        testcases = testcases.update(testName, (!l.endsWith("*** FAILED ***"), ""))
      } else if (l.startsWith("[32m") && className == "") {
        className = l.substring(4, l.indexOf(":"))
      } else if (l.startsWith("[31m ")) {
        val status = testcases(testName)
        val trace = status._2 + l.substring(5)
        testcases = testcases.update(testName, (status._1, trace + "\n"))
      }
    }
    
    bottomTabPane.testResults.addResult(className, testcases, failed)
    bottomTabPane.selection.index = 2
  }

}