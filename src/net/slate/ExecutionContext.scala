package net.slate

object ExecutionContext {

  var loadedProjects = List[String]()

  def currentProjectName = {
    val file = Launch.currentScript.text.path
    val p = loadedProjects.sort { _ > _ }.filter { project =>
      file.contains(project)
    }

    if (p.isEmpty) null else p(0)
  }
}