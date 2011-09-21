package net.slate.builder

/**
 * 
 *
 */
class Message(val projectName: String, val description: String, val file: String, val line: String, val problemType: String = "Error") {}

/**
 * 
 *
 */
object Message {
  import net.slate.ExecutionContext

  def parse(line: String) = {
    val projectName = ExecutionContext.currentProjectName
    val report = line.replace(projectName + java.io.File.separator, "").split(":")
    var msg = ""
    val name = projectName.substring(projectName.lastIndexOf(java.io.File.separator) + 1)
    for (i <- 3 to report.length - 1) msg += report(i)
    new Message(name, msg, report(0), report(1).trim)
  }
}