package edu.nyu.cs.cs2580;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Spearman {
	

	static String _pagerank_idx=null;
	static String _numviews_idx=null;
	private static HashMap<String, Integer> _numViews = new HashMap<String, Integer>();
	private static HashMap<String, Integer> _numViews_transformed = new LinkedHashMap<String, Integer>();
	private static HashMap<String, Double> _ranked_docs = new HashMap<String, Double>();
	private static HashMap<String, Double> _ranked_docs_transformed = new LinkedHashMap<String, Double>();
	
	
	private static void parse_CommandLine(String[] args)
	{
		if(args.length==0)
		{
		_pagerank_idx= "pageranks.idx";
		_numviews_idx= "numviews.idx";
		}
		else
		{
		_pagerank_idx= args[0] + "pageranks.idx";
		_numviews_idx= args[1] + "numviews.idx";
		}
	}
	
	private static void load_Numviews() throws FileNotFoundException, IOException, ClassNotFoundException
	{
	
	System.out.println("Load Numviews from: " + _numviews_idx);
	ObjectInputStream reader = new ObjectInputStream(new FileInputStream(_numviews_idx));
	LogMinerNumviews loaded = (LogMinerNumviews) reader.readObject();
	_numViews=loaded.get_numViews();
	loaded=null;
	reader.close();
	}


	private static void load_Pageranks() throws FileNotFoundException, IOException, ClassNotFoundException {
	System.out.println("Load Pageranks from: " + _pagerank_idx);
	ObjectInputStream reader = new ObjectInputStream(new FileInputStream(_pagerank_idx));
	CorpusAnalyzerPagerank loaded = (CorpusAnalyzerPagerank) reader.readObject();
	_ranked_docs=loaded.get_ranked_docs();
	loaded=null;
	reader.close();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	
	private static void transform_Pageranks()
	{
		List list = new LinkedList(_ranked_docs.entrySet());
		
		Collections.sort(list, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
               return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
       });

       Double rank=0.0;
       Double prev=null;
       int stride=1;
       
       
       for (Iterator it = list.iterator(); it.hasNext();)
       {
              Map.Entry<String, Double> entry = (Entry<String, Double>) it.next();
              if(entry.getValue()==prev)
              {
            	  _ranked_docs_transformed.put(entry.getKey(), rank);
            	  stride++;
              }
              else
              {
              _ranked_docs_transformed.put(entry.getKey(), rank+stride);
              rank=rank+stride;
              stride=1;
              }
              
              prev=entry.getValue();
       }
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void transform_Numviews()
	{
	List list = new LinkedList(_numViews.entrySet());
	
       Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2)
			{
               return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
       });

       Integer rank=0;
       int stride=1;
       Integer prev = null;
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
              
              if(entry.getValue()==prev)
              {
            	  _numViews_transformed.put(entry.getKey(), rank);
            	  stride++;
              }
              else
              {
              _numViews_transformed.put(entry.getKey(), rank+stride);
              rank=rank+stride;
              stride=1;
              }
              prev=entry.getValue();
       }
	}
	
	private static Double calculate_Spearman_coff()
	{
		Double n = (double) _numViews.size();
		
		Double z=0.0;

		for(Map.Entry<String, Double> entry : _ranked_docs_transformed.entrySet())
		{
			z += _ranked_docs_transformed.get(entry.getKey());
		}
		
		z=z/n;
		Double f1 = 0.0,f2=0.0,f3=0.0;

		for(Map.Entry<String, Double> entry : _ranked_docs_transformed.entrySet())
		{
	
			f1 +=((_ranked_docs_transformed.get(entry.getKey())-z)*(_numViews_transformed.get(entry.getKey().toLowerCase())-z)); 
			f2 += ((_ranked_docs_transformed.get(entry.getKey())-z)*(_ranked_docs_transformed.get(entry.getKey())-z));
			f3 += ((double)((_numViews_transformed.get(entry.getKey().toLowerCase())-z)*(_numViews_transformed.get(entry.getKey().toLowerCase())-z)));

		}
		return f1/(Math.sqrt(f2*f3));
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException
	{
		parse_CommandLine(args);
		load_Pageranks();
		load_Numviews();
		
		transform_Pageranks();
		transform_Numviews();
		
		Double correlation=calculate_Spearman_coff();
		System.out.println("Spearman rank correlation coefficient between each of the PageRank computations and the number of views: "+correlation);
	}







}
