package net.slate.builder

/**
 * Runner to run play framework projects.
 *
 * @author Aishwarya Singhal
 */
object PlayRunner extends Builder {

  /**
   * Not used
   */
  def build: List[Message] = {
    // we don't really need this method
    List()
  }

  /**
   * Run play server for this project.
   */
  def run(project: String, programArgs: String = "") = {
    // we can use the following way, but it would overload the IDE. May be its safer to just call the executable
    //    val play = net.slate.editor.tools.TypeCacheBuilder.findClass(project, "play.Play")
    //    val init = play.getDeclaredMethod("init", classOf[File], classOf[String])
    //    init.invoke(play, new File(project), "abc123123123")

    val executable = configuration("play")

    executeCommand(List(executable, "run", "-Xmx256M", "-Xms256M", "-Xss32M"), project, executable, false)
  }

  /**
   * Update Play dependencies this project.
   */
  def updateDeps(project: String) = {
    val executable = configuration("play")
    executeCommand(List(executable, "deps"), project, executable, false)
  }

  /**
   * Run tests on play server for this project.
   */
  def runTests(project: String, className: String = "", programArgs: String = "") = {
    val executable = configuration("play")

    executeCommand(List(executable, "autotest", "-Xmx256M", "-Xms256M", "-Xss32M"), project, executable, true)
  }

  /**
   * Not used
   */
  protected def supportedExtension: String = ".scala"
}