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
package net.slate.editor.tools

import java.io.File
import java.io.IOException

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryParser.ParseException
import org.apache.lucene.queryParser.{ MultiFieldQueryParser, QueryParser }
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version

import scala.actors.Actor._

import net.slate.util.FileUtils

class TypeIndexer(project: String) {
  val INDEX_DIR = new File(project + File.separator + ".slate" + File.separator + "index")
  var ready = false
  val analyzer = new StandardAnalyzer(Version.LUCENE_33)
  val index_ = FSDirectory.open(INDEX_DIR)

  def index = {
    val indexer = actor {
      val config = new IndexWriterConfig(Version.LUCENE_33,
        analyzer)

      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE)

      val w = new IndexWriter(index_, config)

      // index all classes in the classpath
      TypeCacheBuilder.getAllClasses(project).foreach { name =>

        if (!name.contains("$$anonfun$") && !name.substring(name.lastIndexOf("$") + 1).matches("[\\d]*")) {
          val simpleName = name.substring(name.lastIndexOf(".") + 1).replace("\\$", ".")
          val entryType = "class"

          addDoc(w, simpleName, name, entryType)
        }
      }

      // index all code templates
      CodeTemplates.all.foreach { template =>
        val name = template._1
        val description = template._2
        val text = template._3

        addDoc(w, name, description, "template", text)
      }

      ready = true
      w.close()

      TypeIndexer.recordTimeStamp(project)
    }
  }

  def find(name: String, annotationsOnly: Boolean = false, exact: Boolean = false) = {
    val keyword = if (!exact) name + "*" else name
    val query = if (!annotationsOnly) keyword else "type:\"annotation\" " + keyword

    val q = new QueryParser(Version.LUCENE_33, "name", analyzer)
      .parse(query)
    search(q, name, exact)
  }

  def findByFullName(name: String) = {
    val keyword = name + "*"
    val query = "fullName:\"" + keyword + "\" OR \"" + keyword + "\""

    val q = new QueryParser(Version.LUCENE_33, "name", analyzer)
      .parse(query)
    search(q, name)
  }

  private def search(q: Query, name: String, exact: Boolean = false) = {
    try {
      val hitsPerPage = 100
      val searcher = new IndexSearcher(index_, true)
      val collector = TopScoreDocCollector.create(
        hitsPerPage, true)
      searcher.search(q, collector)
      val hits = collector.topDocs().scoreDocs

      val results = new Array[AnyRef](hits.length)

      var skipped = 0

      // prepare results
      for (i <- 0 to (hits.length - 1)) {
        val docId = hits(i).doc
        val d = searcher.doc(docId)

        if ((exact && d.get("name") == name) || !exact) {
          val fullName = d.get("fullName").replace("." + d.get("name"), "").replace(".class", "")
          results(i - skipped) = d.get("name") + " - " + fullName
        } else {
          skipped += 1
        }
      }

      // searcher can only be closed when there
      // is no need to access the documents any more.
      searcher.close()

      if (results.length == 0) {
        Array[AnyRef]("No match")
      } else {
        results
      }
    } catch {
      case e: Throwable => Array[AnyRef]("No match")
    }
  }

  private def addDoc(w: IndexWriter, name: String, fullName: String, entryType: String, details: String = "") = {
    val doc = new Document()
    doc.add(new Field("name", name, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("type", entryType, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("fullName", fullName, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("details", details, Field.Store.YES, Field.Index.NOT_ANALYZED))
    w.addDocument(doc)
  }

}

object TypeIndexer {
  var timestamps = Map[String, Long]()
  val age = 5 * 60 * 1000L

  /**
   * Re-index the given project if the index is too old. The default age is 5 mins.
   *
   * @param project
   */
  def reindex(project: String) = {
    if ((System.currentTimeMillis - timestamps(project.trim)) > age) {
      // too old an index, lets refresh it
      new TypeIndexer(project).index
    }
  }

  def recordTimeStamp(project: String) = {
    timestamps = timestamps.updated(project, System.currentTimeMillis)
  }
}