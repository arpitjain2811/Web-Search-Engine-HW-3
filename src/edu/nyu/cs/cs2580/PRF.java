package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import com.google.common.collect.HashBiMap;

public class PRF {
	
		
	public static Vector<ScoredTerms> Relevance(Vector<ScoredDocument> scoredDocs,int numdocs, int numTerms, HashBiMap<String,Integer> dict,int max_terms){
		int i;
		int Total=0;
		HashMap<Integer, Integer> WordMap = new HashMap<Integer, Integer>();
		numdocs= Math.min(numdocs, scoredDocs.size());
		for (i=0; i<numdocs; i++){
			ScoredDocument docum = scoredDocs.get(i);
			Document d =  docum.get_doc();
			HashMap<Integer, Integer> wordHash = ((DocumentIndexed) d).getTopWords(max_terms);
			
			
			for (int j:wordHash.keySet())
			{
				if (WordMap.containsKey(j))
				{
					
					WordMap.put(j, wordHash.get(j)+WordMap.get(j));
				}
				else
				{
					WordMap.put(j, wordHash.get(j));
				}
			}
		}
			
		
		
		Vector<ScoredTerms> scoreTerms = new Vector<ScoredTerms>();
		
		
		
		for (int keys:WordMap.keySet())
		{
			
			String name = dict.inverse().get(keys);
			double scor = ((double) WordMap.get(keys));
			ScoredTerms scoreTs = new ScoredTerms(new Terms(name), scor);
			scoreTerms.add(scoreTs);
			
	
		}
		
		
			
		Collections.sort(scoreTerms, Collections.reverseOrder());
	
		for(i=0;i<scoreTerms.size();i++)
			Total+= scoreTerms.get(i).get_score();
		
		for(i=0;i<scoreTerms.size();i++)
			scoreTerms.get(i).set_score(scoreTerms.get(i).get_score()/Total);
		
		System.out.println(Total);
		Vector<ScoredTerms> scoreTerms_ret = new Vector<ScoredTerms>();
		
		int tot=0;
		for(i=0;i<numTerms;i++)
			tot+= scoreTerms.get(i).get_score();
		
		System.out.println(tot);
		for(i=0;i<numTerms;i++)
		{
			scoreTerms.get(i).set_score(scoreTerms.get(i).get_score()/tot);
			scoreTerms_ret.add(scoreTerms.get(i));
		}
		return scoreTerms_ret;
		
	}
	

}
