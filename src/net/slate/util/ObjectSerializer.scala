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
 *  Created on: 27th October 2011
 */
package net.slate.util

/**
 * Provides APIs to help serialize/ deserialize a list of objects to/ from
 * the filesystem.
 *
 * @author Aishwarya Singhal
 */
trait ObjectSerializer[T] {
  import java.io._

  lazy val storeName = "objectDetails.ser"

  /**
   * Serialize data to file system.
   * @param data
   */
  def write(data: List[T]) {
    val file = new File(storeName)
    if (file.exists) { file.delete }
    val store = new ObjectOutputStream(new FileOutputStream(storeName))
    store.writeObject(data)
    store.close
  }

  /**
   * Read serialized data from the file system.
   *
   * @return
   */
  def read = {
    if (new File(storeName).exists) {
      val in = new ObjectInputStream(new FileInputStream(storeName))
      val details = in.readObject().asInstanceOf[List[T]]
      in.close()
      details
    } else {
      List[T]()
    }
  }

  /**
   * Adds an object to the serialized data.
   *
   * @param o
   */
  def add(o: T) {
    write(read ::: List(o))
  }

  /**
   * Removes objects matching the given criteria from the serialized data.
   *
   * @param fn
   */
  def remove(fn: (T) => Boolean) {
    write(read.remove(fn))
  }
}