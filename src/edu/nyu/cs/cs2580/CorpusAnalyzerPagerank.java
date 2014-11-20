package edu.nyu.cs.cs2580;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.HashSet;
import java.util.Map;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer implements Serializable {

  private static final long serialVersionUID = 2698138733115785548L;

  private HashMap<Integer, HashSet<Integer> > _linkGraph = new HashMap<Integer, HashSet<Integer>>();
  private HashMap<String, Integer> _linkHash = new HashMap<String, Integer>();
  private HashMap<String, Float> _ranked_docs = new HashMap<String, Float>();

  public CorpusAnalyzerPagerank(Options options) {
    super(options);
  }

  /**
   * This function processes the corpus as specified inside {@link _options}
   * and extracts the "internal" graph structure from the pages inside the
   * corpus. Internal means we only store links between two pages that are both
   * inside the corpus.
   * 
   * Note that you will not be implementing a real crawler. Instead, the corpus
   * you are processing can be simply read from the disk. All you need to do is
   * reading the files one by one, parsing them, extracting the links for them,
   * and computing the graph composed of all and only links that connect two
   * pages that are both in the corpus.
   * 
   * Note that you will need to design the data structure for storing the
   * resulting graph, which will be used by the {@link compute} function. Since
   * the graph may be large, it may be necessary to store partial graphs to
   * disk before producing the final graph.
   *
   * @throws IOException
   */
  @Override
  public void prepare() throws IOException, NoSuchAlgorithmException  {
    System.out.println("Preparing " + this.getClass().getName());
    
    String corpusDir = _options._corpusPrefix;
    final File Dir = new File(corpusDir);
    Document.HeuristicDocumentChecker Checker = new Document.HeuristicDocumentChecker();
     
    String link_name;
    String corresponding_links;
    HashMap<String, HashSet<String> > linksource = new HashMap<String, HashSet<String>>();
    int num_docs = 0;
    System.out.println("Extracting Links");
    for (final File fileEntry : Dir.listFiles()) {
      
      if ( !fileEntry.isDirectory() ) {
        
        // dont read hidden files
        if(fileEntry.isHidden())
		      continue;
	    
	    // Create Extract link object
	    HeuristicLinkExtractor f = new HeuristicLinkExtractor(fileEntry);
	    
	    // Get Main source page link
	    link_name= f.getLinkSource();
	    
	    ArrayList<String> linkList = new ArrayList<String>();
	    // Get all links (Page names) present in the source page
	    while ( (corresponding_links = f.getNextInCorpusLinkTarget()) != null)
        linkList.add(corresponding_links);
	    
	    // Put the array list of Strings (Links in source page into a hash map)
	    HashSet<String> linkSet = new HashSet<String>(linkList);
	    linksource.put(link_name, linkSet);
	    _linkHash.put(link_name, num_docs);

      Checker.addDoc(link_name);

//       Document doc = new Document(_ranked_docs.size());
//       doc.setTitle(link_name);
// //      doc.setUrl(link_name);
//       _ranked_docs.add(doc);

    }
    num_docs++;
    if (num_docs >100)
      break;
  }

  // Create a local map variable (efficient to iterate over)
  System.out.println("Creating Graph");
  // Iterate over Map keys
  for (String key : linksource.keySet()) {
    HashSet<String> links = new HashSet<String>();
    HashSet<Integer> linkAdjSet = new HashSet<Integer>();

    // Store Link Set of a particular key
    links = linksource.get(key);

    //Iterate over the links in the set
    for (String link_values : links) {
      //Add to the adjacency list  (HashSet) if present in corpus
	    if (linksource.containsKey(link_values))
        linkAdjSet.add(_linkHash.get(link_values));
    }
    _linkGraph.put(_linkHash.get(key), linkAdjSet);
  }

  return;
} 

    /**
   * This function computes the PageRank based on the internal graph generated
   * by the {@link prepare} function, and stores the PageRank to be used for
   * ranking.
   * 
   * Note that you will have to store the computed PageRank with each document
   * the same way you do the indexing for HW2. I.e., the PageRank information
   * becomes part of the index and can be used for ranking in serve mode. Thus,
   * you should store the whatever is needed inside the same directory as
   * specified by _indexPrefix inside {@link _options}.
   *
   * @throws IOException
   */
  @Override
  public void compute() throws IOException {
    System.out.println("Computing using " + this.getClass().getName());
    
    // total number of pages included in graph
    int nnodes = _linkGraph.keySet().size();
    // initial value for beginning of each iteration
    float init = (float) (1 - _options._lambda)/nnodes;
    // initialize pagerank of all pages to 0.5 maybe go bigger?
    ArrayList<Float> ranks = new ArrayList<Float>( Collections.nCopies(nnodes, (float) .5) );
    // array to track pageranks as we update 
    ArrayList<Float> new_ranks = new ArrayList<Float>( Collections.nCopies(nnodes, init) ); 

    for (int iters = 0; iters < _options._iterations; iters++) {
      // reinitialize if its not the first iteration
      for (int i = 0; i < nnodes && iters > 0; i++) 
        new_ranks.set(i, init);

      // go through every webpage in the graph
      for (Integer node : _linkGraph.keySet()) {
        HashSet<Integer> links = _linkGraph.get(node);
        float distribute_rank = (float) _options._lambda * (ranks.get(node)) / links.size();
        // increase the pagerank of every page this one points to by the above amount
        for (Integer link : links) {
          float tmp = new_ranks.get(link);
          new_ranks.set(link, tmp + distribute_rank);	
        }
      }
      // update the pageranks and repeat
      ranks = new_ranks;
      //System.out.println(ranks);
    }

    for(String page : _linkHash.keySet())
      _ranked_docs.put(page, ranks.get(_linkHash.get(page)));
     	
    new_ranks = null;
    ranks = null;
    _linkHash = null;
    _linkGraph = null;

    //String indexFile = _options._indexPrefix + "/pageranks.idx";
    String indexFile = "pageranks.idx";
    System.out.println("Store PageRanks to: " + indexFile);
    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));

    try {
      writer.writeObject(this);
      writer.close();
    }
    catch(Exception e) {
      System.out.println(e.toString());
    }
   
    return;
  }

  /**
   * During indexing mode, this function loads the PageRank values computed
   * during mining mode to be used by the indexer.
   *
   * @throws IOException
   */
  @Override
  public Object load() throws IOException {
    System.out.println("Loading using " + this.getClass().getName());
    return null;
  }
}
