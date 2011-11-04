/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 20th September 2011
 */
package net.slate.gui.popupmenu

import scala.swing.{ Action, Menu, Component, MenuItem }
import net.slate.Launch
import net.slate.gui.NewItemDetailsDialog

/**
 * Project tree menu that appears on the file explorer when mouse is right clicked.
 *
 * @author Aishwarya Singhal
 */
object ProjectTreeMenu extends PopupMenu with MenuPainter {
  var path = ""
  lazy val form = new NewItemDetailsDialog(Launch.top)
  var nodeRow = -1
  
  contents += new Menu("New") with CreateNewItemMenuItem {

    // template names need to start with Scala/ Java/ General as the dialog box logic depends on it
    contents += new Menu("Scala") with CreateNewItemMenuItem {
      contents += new MenuItem(createNewItemAction("Application", "ScalaApplication", "newapplication")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Class", "ScalaClass", "newclass")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Object", "ScalaObject", "newobject")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Trait", "ScalaTrait", "newtrait")) with CreateNewItemMenuItem
    }

    contents += new Menu("Java") with CreateNewItemMenuItem {
      contents += new MenuItem(createNewItemAction("Annotation", "JavaAnnotation", "newannotation")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Class", "JavaClass", "newclass")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Enum", "JavaEnum", "newenum")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Interface", "JavaInterface", "newint")) with CreateNewItemMenuItem
    }

    contents += new Menu("General") with CreateNewItemMenuItem {
      contents += new MenuItem(createNewItemAction("File", "GeneralFile", "newfile")) with CreateNewItemMenuItem
      contents += new MenuItem(createNewItemAction("Folder", "GeneralFolder", "newfolder")) with CreateNewItemMenuItem
    }
  }

  contents += new MenuItem(new Action("Delete") {
    import net.slate.Launch._

    def apply() {
      top.fileExplorer.removeNode(nodeRow)
    }
  }) with CreateNewItemMenuItem

  contents += new Menu("Run") with CreateNewItemMenuItem {

    contents += new Menu("Scala") with CreateNewItemMenuItem {
      contents += new MenuItem(new Action("Test Cases") {
        import net.slate.ExecutionContext
        import net.slate.editor.tools.TypeCacheBuilder
        import net.slate.builder.ScalaBuilder
        import net.slate.Launch._

        def apply() {
          bottomTabPane.testResults.clear

          val project = ExecutionContext.currentProjectName(path)
          val classes = TypeCacheBuilder.findScalaTestCaseClasses(project)
          classes.foreach { c => ScalaBuilder.runTests(project, c) }
        }
      }) with CreateNewItemMenuItem
    }

    contents += new Menu("Play") with CreateNewItemMenuItem {
      icon = new javax.swing.ImageIcon("images/play.png")

      contents += new MenuItem(new Action("Server") {
        import net.slate.ExecutionContext._
        import net.slate.builder.PlayRunner

        def apply() {
          PlayRunner.run(currentProjectName(path))
        }
      }) with CreateNewItemMenuItem

      contents += new MenuItem(new Action("Test Cases") {
        import net.slate.ExecutionContext._
        import net.slate.builder.PlayRunner
        import net.slate.Launch._

        def apply() {
          bottomTabPane.testResults.clear

          PlayRunner.runTests(currentProjectName(path))
        }
      }) with CreateNewItemMenuItem

      contents += new MenuItem(new Action("Update Dependencies") {
        import net.slate.ExecutionContext._
        import net.slate.builder.PlayRunner

        def apply() {
          PlayRunner.updateDeps(currentProjectName(path))
        }
      }) with CreateNewItemMenuItem
    }
  }

  contents += new Menu("Git") with CreateNewItemMenuItem {
    import javax.swing.JOptionPane
    import net.slate.Launch._

    import net.slate.builder.GitRunner
    contents += new MenuItem(new Action("Init") {

      def apply() {
        val remote = JOptionPane.showInputDialog(
          top.peer,
          "Please enter the path of remote Git",
          "Git Init",
          JOptionPane.PLAIN_MESSAGE,
          null,
          null,
          null) match { case x: String => x  case _ => null}
        if (null != remote) new GitRunner(path).init(remote)
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Add") {

      def apply() {
        new GitRunner(path) +
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Commit") {
      import net.slate.ExecutionContext._
      import net.slate.builder.PlayRunner

      def apply() {
        val comment = JOptionPane.showInputDialog(
          top.peer,
          "Please enter a comment for commit",
          "Git Commit",
          JOptionPane.PLAIN_MESSAGE,
          null,
          null,
          null) match { case x: String => x  case _ => null}
        val git = new GitRunner(path)
        if (git ?) git.commit(comment)
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Checkout") {

      def apply() {
        new GitRunner(path).checkout
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Push") {

      def apply() {
        new GitRunner(path).push
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Pull") {
      import net.slate.ExecutionContext._
      import net.slate.builder.PlayRunner

      def apply() {
        new GitRunner(path).pull
      }
    }) with CreateNewItemMenuItem
  }

  contents += new Menu("Heroku") with CreateNewItemMenuItem {
    import javax.swing.JOptionPane
    import net.slate.ExecutionContext._
    import net.slate.Launch._

    import net.slate.builder.HerokuRunner

    contents += new MenuItem(new Action("Create") {

      def apply() {
        val name = JOptionPane.showInputDialog(
          top.peer,
          "Please enter the name of application for Heroku.\n The app name will appear in the url.",
          "Heroku",
          JOptionPane.PLAIN_MESSAGE,
          null,
          null,
          null) match { case x: String => x  case _ => null}
        val project = currentProjectName(path)
        if (null != name) new HerokuRunner(project).create(name)
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Deploy") {

      def apply() {
        val project = currentProjectName(path)
        new HerokuRunner(project).deploy
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Console") {
      import net.slate.ExecutionContext._
      import net.slate.builder.PlayRunner

      def apply() {
        val comment = JOptionPane.showInputDialog(
          top.peer,
          "Please enter any command",
          "Heroku Console",
          JOptionPane.PLAIN_MESSAGE,
          null,
          null,
          null) match { case x: String => x  case _ => null}
        val project = currentProjectName(path)
        new HerokuRunner(project).console(if (comment != null) comment else "")
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Logs") {

      def apply() {
        val project = currentProjectName(path)
        new HerokuRunner(project).logs
      }
    }) with CreateNewItemMenuItem

    contents += new MenuItem(new Action("Restart") {

      def apply() {
        val project = currentProjectName(path)
        new HerokuRunner(project).restart
      }
    }) with CreateNewItemMenuItem
  }

  def createNewItemAction(text: String, templateName: String, iconName: String) = new Action(text) {
    icon = new javax.swing.ImageIcon("images/" + iconName + ".gif")

    def apply() {
      form.display(templateName, path, nodeRow)
    }
  }
}