package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.HashSet;
import java.util.Map;
import edu.nyu.cs.cs2580.CorpusAnalyzer.HeuristicLinkExtractor;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
	private HashMap<Integer, HashSet<Integer> > _linkGraph = new HashMap<Integer, HashSet<Integer>>();
	private HashMap<String, Integer> _linkHash = new HashMap<String, Integer>();
	private HashMap<Integer, Double> _pgRank = new HashMap<Integer, Double>();
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
  public void prepare() throws IOException {
    System.out.println("Preparing " + this.getClass().getName());
    
    ReadCorpus DocReader = new ReadCorpus();
    String corpusDir = _options._corpusPrefix;
    final File Dir = new File(corpusDir);
     
    String link_name;
    String corresponding_links;
    HashMap<String, HashSet<String> > linksource = new HashMap<String, HashSet<String>>();
    System.out.println("Extracting Links");
    int num_docs = 0;
    for (final File fileEntry : Dir.listFiles()) 
    {	
    	num_docs += 1;		
	if ( !fileEntry.isDirectory() ) 
	{
	      
	      	// dont read hidden files
	      	if(fileEntry.isHidden())
			continue;
	    
	      	// special case for testing with corpus.tsv
	      	//System.out.println("IncomingFile");
	      	//System.out.println(fileEntry.getName());
	      	// Create Extract link object
	      	HeuristicLinkExtractor f = new HeuristicLinkExtractor(fileEntry);
		// Get Main source page link
		link_name= f.getLinkSource();
		ArrayList<String> linkList = new ArrayList<String>();
		// Get all links (Page names) present in the source page
		while (true)
		{
			// Get next link in Page until it returns null
			corresponding_links = f.getNextInCorpusLinkTarget();
			if (corresponding_links != null)
			{
				linkList.add(corresponding_links);
			}
			else
			{
				break;
			}		  
		}
		HashSet<String> linkSet = new HashSet<String>(linkList);
		// Put the array list of Strings (Links in source page into a hash map)
		linksource.put(link_name, linkSet);
		//System.out.println(linkSet);	
		_linkHash.put(link_name, num_docs);
		
	  }
	if (num_docs >100)
		break;
	}

	// Create a local map variable (efficient to iterate over)
	Map<String, HashSet<String>> linkMap = new HashMap<String, HashSet<String> >(linksource);
	System.out.println("Creating Graph");
	// Iterate over Map keys
	for (String key: linkMap.keySet())
	    {
	    	HashSet<String> links = new HashSet<String>();
	    	HashSet<Integer> linkAdjSet = new HashSet<Integer>();
		
		// Store Link Set of a particular key
		links = linkMap.get(key);
		
	    	//Iterate over the links in the set
	    	for (String link_values: links)
		{	
	    		//Add to the adjacency list  (HashSet) if present in corpus
	    		if (linkMap.containsKey(link_values))
	    		{
	    			linkAdjSet.add(_linkHash.get(link_values));
	    		}
		}	 
		_linkGraph.put(_linkHash.get(key), linkAdjSet);
	    }
	System.out.println(_linkGraph);

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
    Map<Integer, HashSet<Integer>> Graph = new HashMap<Integer, HashSet<Integer> >(_linkGraph);
    for (Integer keys: Graph.keySet())
    {
    	int no_links = (Graph.get(keys).size());
    	int total_links = (Graph.size());
    	double alpha = 0.7;
	double PageRank = (1.0 - alpha)*((total_links - no_links)*1.0)*(1/(total_links*1.0));
	System.out.println(PageRank);
	if (no_links >0)
	{
		PageRank += no_links*(alpha/(no_links*1.0) + (1.0 - alpha)*(1/(total_links*1.0)));
	}
	
    	_pgRank.put(keys, PageRank);   	
    }
    System.out.println(_pgRank);
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
