package edu.nyu.cs.cs2580;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;
import java.util.Comparator;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import edu.nyu.cs.cs2580.IndexerInvertedCompressed.Tuple;

/**
 * @CS2580: Implement this class for HW3 based on your {@code RankerFavorite}
 * from HW2. The new Ranker should now combine both term features and the
 * document-level features including the PageRank and the NumViews. 
 */
public class RankerComprehensive extends Ranker {

  public RankerComprehensive(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
	
	Vector<ScoredDocument> all = new Vector<ScoredDocument>();


    QueryPhrase qp=new QueryPhrase(query._raw);
    qp.processQuery();
	    
	    
    Document i = _indexer.nextDoc(query, -1);	    
    Double pos;
    int j;
	  
    while(i != null) {

        if(qp.phrase.size()>0) {

        	for(j=0;j<qp.phrase.size();j++) {
        		pos=_indexer.NextPhrase(qp.phrase.get(j), i._docid, -1);
				if (pos != Double.POSITIVE_INFINITY)
	  				System.out.println( "Position: " + pos+ " Docid: " + i._docid + " Docname: " + i.getTitle() );
	  			 if(pos==Double.POSITIVE_INFINITY)
	  				 break;
	  		}

	  		if(j==qp.phrase.size())
	  			all.add(scoreDocument(query, i));
	  	}
	  	else {
	  		System.out.println(  " Docid: " + i._docid + " Docname: " + i.getTitle() );	
	        all.add(scoreDocument(query, i));	
	    }
	    
	    i = _indexer.nextDoc(query,i._docid);
	}

	Collections.sort(all, Collections.reverseOrder());
    rerank(all);
    Collections.sort(all, Collections.reverseOrder());

    Vector<ScoredDocument> results = new Vector<ScoredDocument>();    
    for (int j1 = 0; j1 < all.size() && j1 < numResults; ++j1)
      results.add(all.get(j1));

	return results;
  }


  private void rerank(Vector<ScoredDocument> orig_ranks) {

    ArrayList<Tuple<ScoredDocument, Double>> pagerank_tuples = new ArrayList<Tuple<ScoredDocument, Double>>();
    ArrayList<Tuple<ScoredDocument, Double>> numviews_tuples = new ArrayList<Tuple<ScoredDocument, Double>>();


    // rerank the top 50 documents
    for (int i = 0; i < orig_ranks.size() && i < 55; i++) {
        ScoredDocument sdoc = orig_ranks.get(i);
        pagerank_tuples.add(new Tuple<ScoredDocument, Double>(sdoc, sdoc.get_doc().getPageRank()));
        numviews_tuples.add(new Tuple<ScoredDocument, Double>(sdoc, (double) sdoc.get_doc().getNumViews()));
    }
    
    Comparator< Tuple<ScoredDocument, Double>> comparator = new Comparator<Tuple<ScoredDocument, Double>>() {
      public int compare(Tuple<ScoredDocument, Double>tupleA, Tuple<ScoredDocument, Double> tupleB) {
        // tupleB then tuple A to do descending order
        return tupleB.getSecond().compareTo(tupleA.getSecond());
      }
    };
    Collections.sort(pagerank_tuples, comparator);
    Collections.sort(numviews_tuples, comparator);

    for (int i = 0; i < pagerank_tuples.size(); i++) {
      ScoredDocument sdoc1 = pagerank_tuples.get(i).getFirst();
      ScoredDocument sdoc2 = numviews_tuples.get(i).getFirst();

      double score;
      if (isBetween(i, 0, 9)){
        score = 1.0;
      } else if (isBetween(i, 10, 19)) {
        score = 0.8;        
      } else if (isBetween(i, 20, 29)) {
        score = 0.6;        
      } else if (isBetween(i, 30, 39)) {
        score = 0.4;        
      } else if (isBetween(i, 40, 49)) {
        score = 0.2;        
      } else {
        score = 0.1;
      }

      sdoc1.updateScore(score);      
      sdoc2.updateScore(score);
    }

}

  private boolean isBetween(int x, int lower, int upper) {
    return lower <= x && x <= upper;
  }




	private ScoredDocument scoreDocument(Query query, Document document) {
		double title_score = runquery_title(query, document);
	    double cosine_score = runquery_cosine(query, document);

	    double score = title_score + cosine_score;

	    return new ScoredDocument(document, score);
	}

	private double runquery_title(Query query, Document doc) {
	    String title = ((DocumentIndexed) doc).getTitle();
	    Vector<String> titleTokens = new Vector<String>( Arrays.asList(title.split(" ")) );    

	    double size = (double) query._tokens.size();
	    titleTokens.retainAll(query._tokens); 
	    double score = titleTokens.size() / size;

	    return score;
	  }

	  private double runquery_cosine(Query query, Document doc) {
	    double score = 0.0;

	    if (_options._indexerType.equals("inverted-doconly")) {
		for (String queryToken : query._tokens) {
		    int idx = ((IndexerInvertedDoconly) _indexer).getTerm(queryToken);
		    if (idx >= 0 ) 
			score += ((DocumentIndexed) doc).getTFIDF(idx);
		}
	    } else {
		// total number of docs, from indexer
		int num_docs = _indexer.numDocs();
		for (String queryToken : query._tokens){
		    
		    // number of occurrences of this term, from postings list
		    int tf = _indexer.documentTermFrequency(queryToken, Integer.toString(doc._docid) );
		    // number of docs word is in, from indexer
		    int df = _indexer.corpusDocFrequencyByTerm(queryToken);
		    
		    double idf = ( 1 + Math.log( (double) num_docs/df ) / Math.log(2) );
		    score += tf * idf;
		    
		   // System.out.println(queryToken + ' ' + tf + ' ' + df + ' ' + idf + ' ' + score);
		}
		score = Math.log(score);
	    }   
	    return score;
	  }

	

  }

