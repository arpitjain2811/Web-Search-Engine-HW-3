package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Vector;

public class Bhattacharyya {
	
	static String _prf_file=null;
	static String _output_file=null;
	static Vector<String> queries=new Vector<String>();
	static Vector<String> paths=new Vector<String>();
	
	
	
	private static void parse_CommandLine(String[] args)
	{
		if(args.length==0)
		{
			_prf_file= "";
			_output_file= "";
		}
		else
		{
			_prf_file=args[0];
			_output_file=args[1];
			
		}
	}
	@SuppressWarnings("resource")
	private static void query_similarity() throws IOException
	{
		
		BufferedReader br1= new BufferedReader(new FileReader(_prf_file));
		String line=null;

		
		while((line=br1.readLine()) != null)
		{
			Scanner s= new Scanner(line).useDelimiter(":");
			queries.add(s.next());
			paths.add(s.next());
			s.close();
		}
		br1.close();
		
		Double similarity;
		
		PrintWriter writer = new PrintWriter("./"+_output_file,"UTF-8");
		
		for(int i=0; i<paths.size();i++)
		{
			for(int j=i+1;j<paths.size(); j++)
			{
				similarity=calculate_Bhattacharyya_coff(paths.get(i),paths.get(j));
				writer.printf("%s\t%s\t%f\n",queries.get(i),queries.get(j),similarity);
				System.out.printf("%s\t%s\t%f\n",queries.get(i),queries.get(j),similarity);
			}
			
		}
		writer.close();
		
	}




	@SuppressWarnings("resource")
	private static Double calculate_Bhattacharyya_coff(String f1,String f2) throws IOException
	{
		Double beta=0.0;
		BufferedReader br1= new BufferedReader(new FileReader(f1));
		BufferedReader br2= new BufferedReader(new FileReader(f2));
		
		
		String line1=null;
		String line2=null;
		
		Scanner s1 = null;
		Scanner s2 = null;
		
		
		Vector<String>terms1=new Vector<String>();
		Vector<String>terms2=new Vector<String>();
		Vector<Double>probs1=new Vector<Double>();
		Vector<Double>probs2=new Vector<Double>();
		
		while(((line1=br1.readLine())!=null)&&(line2=br2.readLine())!=null)
		{
			
			s1= new Scanner(line1).useDelimiter("\t");
			s2= new Scanner(line2).useDelimiter("\t");
			
			
			terms1.add(s1.next());
			probs1.add(s1.nextDouble());
			
			terms2.add(s2.next());
			probs2.add(s2.nextDouble());
			
		}
		for(int i=0;i<terms1.size();i++)
		{
			for(int j=0;j<terms2.size();j++)
			{
				if(terms1.get(i).equals(terms2.get(j)))
				{
					beta += Math.sqrt(probs1.get(i)*probs2.get(j));
				}
			}
			
		}
		
		s1.close();
		s2.close();
		br1.close();
		br2.close();
		return beta;	
	
		
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		parse_CommandLine(args);
		System.out.println("Calculating Bhattacharyya coefficient");
		query_similarity();
		
	}



}
