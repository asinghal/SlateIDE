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
package net.slate.gui

import java.awt.{ Color, Dimension, Font }
import javax.swing.{ BorderFactory, JPopupMenu }
import javax.swing.border._
import scala.swing.{ Action, Menu, Component, MenuItem }
import scala.swing.SequentialContainer.Wrapper

import net.slate.Launch

object PopupMenu {
  private[PopupMenu] trait JPopupMenuMixin { def popupMenuWrapper: PopupMenu }
}

class PopupMenu extends Component with Wrapper {

  override lazy val peer: JPopupMenu = new JPopupMenu with PopupMenu.JPopupMenuMixin with SuperMixin {
    def popupMenuWrapper = PopupMenu.this
  }

  def show(invoker: Component, x: Int, y: Int): Unit = peer.show(invoker.peer, x, y)

  /* Create any other peer methods here */
}

class EditorPopupMenu extends PopupMenu with MenuPainter {
  import net.slate.action._

  contents += new Menu("Source") with MenuFont {
    contents += new MenuItem("Format") with MenuFont { peer.addActionListener(new FormatFileAction) }
    contents += new MenuItem("Organise Imports") with MenuFont { peer.addActionListener(new OrganiseImportsAction) }
  }
  //  contents += new Menu("Execute") with MenuFont {
  //    contents += new MenuItem("Run") with MenuFont
  //    contents += new MenuItem("Debug") with MenuFont
  //  }
}

class ProjectTreeMenu extends PopupMenu with MenuPainter {
  var path = ""
  lazy val form = new NewItemDetailsDialog(Launch.top)
  var nodeRow = -1

  preferredSize = new Dimension(100, 100)

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
          null)
        if (null != remote) new GitRunner(path).init(remote.toString)
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
          null)
        val git = new GitRunner(path)
        if (git ?) git.commit(comment.toString)
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

  def createNewItemAction(text: String, templateName: String, iconName: String) = new Action(text) {
    icon = new javax.swing.ImageIcon("images/" + iconName + ".gif")

    def apply() {
      form.display(templateName, path, nodeRow)
    }
  }
}

trait MenuPainter {
  self: Component =>
}

trait MenuFont {
  self: Component =>
  border = BorderFactory.createEmptyBorder
}

trait CreateNewItemMenuItem {
  self: Component =>
  val itemSize = new Dimension(150, 20)
  minimumSize = itemSize
  maximumSize = itemSize
}