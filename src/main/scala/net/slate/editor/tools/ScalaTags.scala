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
 *  Created on: 11th January 2012
 */
package net.slate.editor.tools

import scala.actors.Actor._
import net.slate.ExecutionContext._

object ScalaTags {

  import scala.io.Source
  import util.matching.Regex
  import net.slate.util.FileUtils

  private lazy val regex = Array("def", "class", "object", "trait").map { "\\b" + _ + "\\b[\\s]*" }.foldLeft("") { (x, y) => x + "|" + y }.substring(1)
  private lazy val overall: Regex = ("(" + regex + ")([^\\s\\:\\;\\(\\[\\{\\=]*)").r

  private var running = false

  def tagAll(project: String) = {
    FileUtils.findAllFiles(project, ".scala").foreach(tag)
  }

  def tag(path: String) {
    val source = Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val indexer = new TagsIndexer(net.slate.ExecutionContext.currentProjectName(path))

    var pos = 0

    val a = actor {
      val matcher = overall.pattern.matcher(lines)
      while (matcher.find()) {
        val cType = matcher.group(1).trim
        val tag = matcher.group(2).trim
        val position = matcher.start
        try {
          if (tag != "" && !indexer.exists(cType, tag, path)) {
            indexer.addTag(cType, tag, path, position)
          }
        } catch {
          case e: Exception => //println("could not tag " + tag + " of type " + cType + " in file" + path) 
        }
      }
    }
  }

  var cache = Map[String, TagsIndexer]()

  def lookup(tag: String, path: String) = {
    indexer(currentProjectName(path)).find(tag)
  }

  private def indexer(project: String) = {
    cache.getOrElse(project, {
      val _indexer = new TagsIndexer(project)
      cache += (project -> _indexer)
      _indexer
    })
  }
}

class TagsIndexer(project: String) {
  import java.io.File
  import java.io.IOException
  import org.apache.lucene.analysis.standard.StandardAnalyzer
  import org.apache.lucene.document._
  import org.apache.lucene.index._
  import org.apache.lucene.queryParser._
  import org.apache.lucene.search._
  import org.apache.lucene.store._
  import org.apache.lucene.util.Version

  val INDEX_DIR = new File(project + File.separator + ".slate_tags" + File.separator + "index")
  var ready = false
  val analyzer = new StandardAnalyzer(Version.LUCENE_33)
  val index_ = FSDirectory.open(INDEX_DIR)

  def addTag(cType: String, tag: String, path: String, position: Int) = {
    val config = new IndexWriterConfig(Version.LUCENE_33,
      analyzer)

    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)

    val w = new IndexWriter(index_, config)

    addDoc(w, cType, tag, path, position)

    ready = true
    w.close()
  }

  def find(tag: String) = findByTag(tag, tag)

  def exists(cType: String, tag: String, path: String) = {
    val keyword = tag
    val query = "cType:\"" + cType + "\" AND path:\"" + path + "\" AND \"" + keyword + "\""

    findByTag(tag, query).length != 0
  }

  private def findByTag(tag: String, query: String) = {
    if (!tag.isEmpty()) {
      val q = new QueryParser(Version.LUCENE_33, "tag", analyzer)
        .parse(query)
      search(q, tag)
    } else {
      returnOnFault
    }
  }

  private def search(q: Query, tag: String) = {
    val searcher = new IndexSearcher(index_, true)
    try {
      val hitsPerPage = 100
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

        if (d.get("tag") == tag) {
          results(i - skipped) = d.get("path") + ": " + d.get("cType") + "- " + d.get("position")
        } else {
          skipped += 1
        }
      }

      results
    } catch {
      case e: Throwable => returnOnFault
    } finally {
      // searcher can only be closed when there
      // is no need to access the documents any more.
      searcher.close()

    }
  }

  private def returnOnFault = Array[AnyRef]()

  private def addDoc(w: IndexWriter, cType: String, tag: String, path: String, position: Int) = {
    val doc = new Document()
    doc.add(new Field("cType", cType, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("tag", tag, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("path", path, Field.Store.YES, Field.Index.ANALYZED))
    doc.add(new Field("position", String.valueOf(position), Field.Store.YES, Field.Index.NOT_ANALYZED))
    w.addDocument(doc)
  }
}