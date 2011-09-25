package net.slate.builder

import java.io.{ File, FileReader }
import scala.xml._

import net.slate.Launch._

object TestCaseMessage {

  def parse(rootDir: String, program: String, line: String) = {
    program match {
      case "scala" => parseForScalaTest(line)
      case "java" =>
      case "play" => parseForPlayAutoTest(rootDir)
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

  def parseForPlayAutoTest(rootDir: String) = {
    val dir = new File(rootDir + File.separator + "test-result")
    if (dir.exists) {
      dir.list.filter { file => file.startsWith("TEST-") && file.endsWith(".xml") }.foreach { file =>
        val xml = XML.load(new FileReader(dir.getAbsolutePath + File.separator + file))
        val className = (xml \\ "testsuite" \\ "@name")(0).text
        var failed = (((xml \\ "testsuite" \\ "@errors").text != "0") || ((xml \\ "testsuite" \\ "@failures").text != "0"))

        var testcases = Map[String, (Boolean, String)]()
        xml \\ "testsuite" \\ "testcase" foreach { testcase =>
          val testName = (testcase \\ "@name").text
          val hasError = (testcase \\ "error") != null
          val message = if ((testcase \\ "error") != null) (testcase \\ "error" \\ "@message").text else ""
          testcases = testcases.update(testName, (hasError, message))
        }
        bottomTabPane.testResults.addResult(className, testcases, failed)
      }
      bottomTabPane.selection.index = 2
    }
  }

}