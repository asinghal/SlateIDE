package net.slate

object ExecutionContext {

  var loadedProjects = List[String]()

  var runningProcess: Process = null

  def currentProjectName: String = {
    val file = Launch.currentScript.text.path
    currentProjectName(file)
  }

  def currentProjectName(selectedDir : String) = {
    val file = selectedDir
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