package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Vector;

public class Bhattacharyya {
	
	static String _prf_path=null;
	static String _output_path=null;
	
	
	
	private static void parse_CommandLine(String[] args)
	{
		if(args.length==0)
		{
			_prf_path= "";
			_output_path= "";
		}
		else
		{
			_prf_path=args[0];
			_output_path=args[1];
			
		}
	}
	
	
	
	
	public static void main(String[] args) throws IOException
	{
		parse_CommandLine(args);
		System.out.println("Calculating Bhattacharyya coefficient");
		query_similarity();
		
	}




	private static void query_similarity() throws IOException
	{
		Vector<String> file_names=new Vector<String>();
		final File Dir = new File(_prf_path);
		for (final File fileEntry : Dir.listFiles()) {
			if(fileEntry.isHidden())
				continue;
			
			if(fileEntry.getName().endsWith(".tsv"))
			{
				file_names.add(fileEntry.getName());
			}
			
		}
		
		Double similarity;
		
		
		PrintWriter writer = new PrintWriter(_output_path+"Bhattacharyya.tsv", "UTF-8");
		for(int i=0; i<file_names.size();i++)
		{
			for(int j=i+1;j<file_names.size(); j++)
			{
				
				similarity=calculate_Bhattacharyya_coff(file_names.get(i),file_names.get(j));
				writer.printf("%s\t%s\t%f\n",file_names.get(i),file_names.get(j),similarity);
				
			}
			
		}
		writer.close();
		
	}




	private static Double calculate_Bhattacharyya_coff(String f1,String f2) throws IOException
	{
		Double beta=0.0;
		BufferedReader br1= new BufferedReader(new FileReader(_prf_path+f1));
		BufferedReader br2= new BufferedReader(new FileReader(_prf_path+f2));
		
		String term1=null;
		Double prob1=null;
		String term2=null;
		Double prob2=null;
		
		String line1=null;
		String line2=null;
		
		Scanner s1 = null;
		Scanner s2 = null;
		
		
		while(((line1=br1.readLine())!=null)&&(line2=br2.readLine())!=null)
		{
			
			s1= new Scanner(line1).useDelimiter("\t");
			s2= new Scanner(line2).useDelimiter("\t");
			
			
			term1=s1.next();
			prob1=s1.nextDouble();
			
			term2=s2.next();
			prob2=s2.nextDouble();
			
			beta= beta + Math.sqrt(prob1*prob2);
			
		}
		
		s1.close();
		s2.close();
		br1.close();
		br2.close();

		System.out.printf("%s\t%s\t%f\n",f1,f2,beta);
		return beta;	
	
		
	}



}
