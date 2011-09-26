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
package net.slate.editor

import javax.swing.undo.{ UndoableEdit, CompoundEdit, UndoManager }
import java.lang.Boolean
import java.beans.PropertyChangeListener
import javax.swing.event.{ UndoableEditEvent, SwingPropertyChangeSupport }

import net.slate.Launch._

class TextUndoManager extends UndoManager {
  def addPropertyChangeListener(listener: PropertyChangeListener) {
    propChangeSupport.addPropertyChangeListener(listener)
  }
  def removePropertyChangeListener(listener: PropertyChangeListener) {
    propChangeSupport.removePropertyChangeListener(listener)
  }

  val propChangeSupport = new SwingPropertyChangeSupport(this)
  var compoundEdit = new StructuredEdit
  var firstModified = 0L
  var modificationMarker = editToBeUndone()

  var hasChangedSinceLastSave = false
  var titlePrefix = ""

  override def die() {
    val undoable = canUndo
    super.die()
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
  }

  override def discardAllEdits() {
    val undoable = canUndo
    val redoable = canRedo
    super.discardAllEdits()
    modificationMarker = editToBeUndone()
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
    firePropertyChangeEvent('Redo.name, redoable, canRedo)
    titlePrefix = ""
    save
  }

  def save = {
    hasChangedSinceLastSave = false
    if (tabPane.selection.page.title.startsWith("* ")) {
      tabPane.selection.page.title = tabPane.selection.page.title.substring(2)
    }
  }

  def hasChanged = modificationMarker != editToBeUndone()

  override def redo() {
    compoundEdit.end()
    if (firstModified == 0L) {
      firstModified = editToBeRedone().asInstanceOf[StructuredEdit].editedTime
    }
    val undoable = canUndo
    super.redo()
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
  }

  override def redoTo(edit: UndoableEdit) {
    compoundEdit.end()
    if (firstModified == 0L) {
      firstModified = editToBeRedone().asInstanceOf[StructuredEdit].editedTime
    }
    val undoable = canUndo
    super.redoTo(edit)
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
  }

  def reset() {
    if (modificationMarker != editToBeUndone()) {
      modificationMarker = editToBeUndone()
    }
  }

  override def trimEdits(from: Int, to: Int) {
    val undoable = canUndo
    val redoable = canRedo
    super.trimEdits(from, to)
    firePropertyChangeEvent('Undo.name, undoable, canUndo)
    firePropertyChangeEvent('Redo.name, redoable, canRedo)
  }

  override def undo() {
    compoundEdit.end()
    if (firstModified == editToBeUndone.asInstanceOf[StructuredEdit].editedTime) {
      firstModified = 0
    } else if (firstModified == 0) {
      firstModified = editToBeUndone().asInstanceOf[StructuredEdit].editedTime
    }
    val redoable = canRedo
    super.undo()
    firePropertyChangeEvent('Redo.name, redoable, canRedo)
  }

  override def undoableEditHappened(e: UndoableEditEvent) {
    val edit = e.getEdit
    val undoable = canUndo
    var editTime = System.currentTimeMillis()
    if (firstModified == 0 || editTime - compoundEdit.editedTime > 700) {
      compoundEdit.end()
      compoundEdit = new StructuredEdit
    }
    compoundEdit.addEdit(edit)
    if (firstModified == 0) {
      firstModified = compoundEdit.editedTime
    }
    if (lastEdit() != compoundEdit) {
      addEdit(compoundEdit)
      firePropertyChangeEvent('Undo.name, undoable, canUndo)
    }

    hasChangedSinceLastSave = true
    if (titlePrefix == "") {
      titlePrefix = "* "
      tabPane.selection.page.title = titlePrefix + tabPane.selection.page.title
    }
  }

  protected def firePropertyChangeEvent(name: String, oldValue: Boolean, newValue: Boolean) {
    propChangeSupport.firePropertyChange(name, oldValue, newValue)
  }

  class StructuredEdit extends CompoundEdit {
    var editedTime = 0L

    override def addEdit(anEdit: UndoableEdit) = {
      val result = super.addEdit(anEdit)
      if (result && editedTime == 0L) {
        editedTime = System.currentTimeMillis
      }
      result
    }

    override def isInProgress = false

    override def canUndo = edits.size > 0
  }
}

