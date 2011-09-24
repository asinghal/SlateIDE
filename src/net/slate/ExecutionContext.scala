package net.slate

object ExecutionContext {

  var loadedProjects = List[String]()

  var runningProcess: Process = null

  def currentProjectName = {
    val file = Launch.currentScript.text.path
    val p = loadedProjects.sort { _ > _ }.filter { project =>
      file.contains(project)
    }

    if (p.isEmpty) loadedProjects(0) else p(0)
  }

  def stop = {
    runningProcess.destroy
    runningProcess = null
  }
}