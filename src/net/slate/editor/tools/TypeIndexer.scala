package net.slate.editor.tools

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import scala.actors.Actor._

class TypeIndexer(project: String) {
  val INDEX_DIR = new File("index")
  var ready = false
  val analyzer = new StandardAnalyzer(Version.LUCENE_33)
  val index_ = FSDirectory.open(INDEX_DIR)

  def index = {
    val indexer = actor {
      val config = new IndexWriterConfig(Version.LUCENE_33,
        analyzer);

      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

      val w = new IndexWriter(index_, config)

      TypeCacheBuilder.getAllClassNames.foreach { fullName =>
        val name = fullName.replace(".class", "")
        val simpleName = name.substring(name.lastIndexOf(".") + 1).replace("\\$", ".")
        
        addDoc(w, simpleName, fullName);
      }

      ready = true

      println("done indexing");

      w.close()
    }
  }

  def find(name: String) = {
    val q = new QueryParser(Version.LUCENE_33, "name", analyzer)
      .parse(name + "*")
    val hitsPerPage = 100;
    val searcher = new IndexSearcher(index_, true);
    val collector = TopScoreDocCollector.create(
      hitsPerPage, true);
    searcher.search(q, collector);
    val hits = collector.topDocs().scoreDocs;

    val results = new Array[AnyRef](hits.length)
    
    // prepare results
    for (i <- 0 to (hits.length - 1)) {
      val docId = hits(i).doc;
      val d = searcher.doc(docId);
      val fullName = d.get("fullName").replace("." + d.get("name"),"")
      results(i) = d.get("name") + " - " + fullName
    }

    // searcher can only be closed when there
    // is no need to access the documents any more.
    searcher.close();
    
    if (results.length == 0) {
    	Array[AnyRef]("No match")
    } else {
    	results
    }
  }

  private def addDoc(w: IndexWriter, name: String, fullName: String) = {
    val doc = new Document();
    doc.add(new Field("name", name, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("fullName", fullName, Field.Store.YES, Field.Index.NOT_ANALYZED));
    w.addDocument(doc)
  }

}