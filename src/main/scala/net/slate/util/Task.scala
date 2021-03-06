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
 *  Created on: 26th October 2011
 */
package net.slate.util

import java.io.Serializable

/**
 *
 * @author Aishwarya Singhal
 */
object Task {

  import TaskDetailsSerializer._

  def create(title: String, description: String, project: String, status: Int) = {
    val id = nextId
    add(new Task(id, title, description, project, status))
    id
  }

  def list = read

  def delete(id: Long) {
    remove(t => t.id == id)
  }

  def update(t: Task) {
    remove(p => p.id == t.id)
    add(t)
  }
}

/**
 *
 * @author Aishwarya Singhal
 */
class Task(var id: Long, var title: String, var description: String, var project: String, var status: Int) extends Serializable {
  val serialVersionUID = 1L
  def isOpen = status == TaskStatus.OPEN
}

/**
 *
 */
object TaskDetailsSerializer extends ObjectSerializer[Task] {
  override lazy val storeName = ".metadata" + java.io.File.separator + "taskDetails.ser"
  private var taskId = System.currentTimeMillis

  def nextId = {
    taskId = taskId + 1
    taskId
  }
}

/**
 *
 * @author Aishwarya Singhal
 */
// I wish I could get enums to work!!
object TaskStatus {
  val OPEN = 1
  val CLOSED = 2

  def getName(t: Int) = {
    t match {
      case OPEN => "Open"
      case CLOSED => "Closed"
      case _ => "Unknown"
    }
  }

  def getByName(t: String) = {
    t match {
      case "Open" => OPEN
      case "Closed" => CLOSED
      case _ => 0
    }
  }
}
