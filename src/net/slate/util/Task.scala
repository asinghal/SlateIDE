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

/**
 *
 * @author Aishwarya Singhal
 */
object Task {

  import TaskDetailsSerializer._

  def create(title: String, description: String, status: TaskStatus) {
    add(new Task(nextId, title, description, status))
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
@scala.serializable
class Task(var id: Long, var title: String, var description: String, var status: TaskStatus) {
  val serialVersionUID = 1L
  def isOpen = status == TaskStatus.OPEN
}

/**
 *
 */
object TaskDetailsSerializer extends ObjectSerializer[Task] {
  override lazy val storeName = "taskDetails.ser"
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
trait TaskStatus {}

/**
 *
 * @author Aishwarya Singhal
 */
object TaskStatus extends Enumeration with TaskStatus {
  type TaskStatus = Value
  val OPEN, CLOSED = Value
}