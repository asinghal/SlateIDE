package net.slate.builder

/**
 * 
 *
 */
class Message(val projectName: String, val description: String, val file: String, val line: String, val path: String, val problemType: String = "Error") {}

/**
 * 
 *
 */
object Message {
  import net.slate.ExecutionContext
  import java.io.File._

  def parse(line: String, messageStartPosition:Int = 3) = {
    val projectName = ExecutionContext.currentProjectName
    val report = line.replace(projectName + separator, "").split(":")
    var msg = ""
    val name = projectName.substring(projectName.lastIndexOf(separator) + 1)
    for (i <- messageStartPosition to report.length - 1) msg += report(i)
    new Message(name, msg, report(0).substring(report(0).lastIndexOf(separator) + 1), report(1).trim, projectName + separator + report(0))
  }
}