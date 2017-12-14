package ParserIndexer;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Crawler.FileManager;
import Retriever_Clusterer.Retriever_Clusterer;

import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.analysis.*;




public class ParserIndexer
{
	public static Pattern numbers = Pattern.compile("\\d*"); //matches any number //"[0-9]");
	public static Pattern nonwords = Pattern.compile("\\W"); //matches any non-word
	public static Pattern whiteSpace = Pattern.compile("\\s+"); //matches any 1 or more whitespaces
	public static Pattern dash = Pattern.compile("-");

	public static ArrayList<String> stopWords = loadStopWords();
	
	public static ArrayList<String> vocabulary = new ArrayList<String>();
	
	
	
	private static TreeMap<Integer, Integer> docs_TokensAmt = new TreeMap<Integer, Integer>();
	
	
	private static double totalDocs = 0;
	private static double AvgDocsTokens = 0;
	
	
	public static TreeMap<Integer, Integer> getDocs_TokensAmt()
	{
		return docs_TokensAmt;
	}

	public static void setDocs_TokensAmt(TreeMap<Integer, Integer> docs_TokensAmt)
	{
		ParserIndexer.docs_TokensAmt = docs_TokensAmt;
	}
	
	
	
	public static double getAvgDocsTokens()
	{
		return AvgDocsTokens;
	}

	public static void setAvgDocsTokens(double avgDocsTokens)
	{
		AvgDocsTokens = avgDocsTokens;
	}
	

	public static double getTotalDocs()
	{
		return totalDocs;
	}

	public static void setTotalDocs(double totalDocs)
	{
		ParserIndexer.totalDocs = totalDocs;
	}
	
	

	public static ArrayList<String> loadStopWords()
	{

		ArrayList<String> list = new ArrayList<String>();

		try 
		{
			BufferedReader in = new BufferedReader(new FileReader("stopwords.txt"));

			String word = null;

			while ((word = in.readLine()) != null)
				list.add(word);

		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		};



		return list;
	}

	/**
This method takes a blob file (archived docID-Link file) as input and returns a {Term, {docID, {Section, TF}}}
	 */
	public static TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> parseDocuments(File docs)
	{

		FileManager fm = new FileManager();
		TreeMap<Integer, String> documentsToBeParsed = fm.loadFromFiledocumentID_Link(docs);;

		TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> toReturn = new TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>>();

		Document onlinedoc = null;
		Iterator docsIterator = documentsToBeParsed.entrySet().iterator();
		Entry docsEntry = null;

		while (docsIterator.hasNext())
		{
			docsEntry = (Entry) docsIterator.next();

			int docID = (Integer) docsEntry.getKey();
			String documentURL = (String) docsEntry.getValue();
			
			boolean success = false;
			while (success != true)
			{
				try
				{
					//System.out.print("Reading: ");
					//System.out.println(documentURL);
					onlinedoc = Jsoup.connect(documentURL).userAgent("COMP 479 Student - Project 3 - Crawling Concordia").get();//.timeout(6000).get();
					success = true;
					break;
				}
				catch (SocketTimeoutException e)
				{
					System.err.println("Socket Timed Out, Retrying");
					e.printStackTrace();
				}
				catch (HttpStatusException e)
				{
					System.err.println("HttpStatusException:");//, Waiting a couple of secs
					System.err.print("Could not grab:");
					System.err.println(documentURL);
					
					//e.printStackTrace();
					
					System.err.println("Waiting 10 secs");
					try
					{
						Thread.sleep(10000);
					}
					catch (InterruptedException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					System.err.println("Retrying");
					
				}
				catch (IOException e)
				{
					System.err.println("IO Exception, Retrying");
					e.printStackTrace();
		        }         
				catch (Exception e)
				{
					System.err.println("General Exception, Retrying");
					e.printStackTrace();
				}
			}
							
			

			String text = "";
			String title = "";
			String header = "";

			Elements paragraphs = onlinedoc.select("p");
			for(Element p : paragraphs)
				text += p.text() + " ";

			title = onlinedoc.title();
			//System.out.println(title);


			Elements headers = onlinedoc.select("h1");
			for(Element h : headers)
				header += h.text() + " ";


			headers = onlinedoc.select("h2");
			for(Element h : headers)
				header += h.text() + " ";

			headers = onlinedoc.select("h3");
			for(Element h : headers)
				header += h.text() + " ";

			headers = onlinedoc.select("h4");
			for(Element h : headers)
				header += h.text() + " ";

			headers = onlinedoc.select("h5");
			for(Element h : headers)
				header += h.text() + " ";

			headers = onlinedoc.select("h6");
			for(Element h : headers)
				header += h.text() + " ";

			int textCount = 0;
			
			{
				text = (whiteSpace.matcher(nonwords.matcher((numbers.matcher(dash.matcher(text).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

				StringTokenizer str = new StringTokenizer(text);
				

				while(str.hasMoreTokens())
				{
					//{Term, {docID, {Section, TF}}
					//for each term
						//get the TreeMap of documentIDs and sections-TFs
							//get the TreeMap of section-TFs for that docID
								//add word with TF

					String word = str.nextToken();

					Stemmer stem = new Stemmer();
					stem.add(word.toCharArray(), word.length());
					stem.stem();
					
					word = stem.toString();
					
					if(!stopWords.contains(word))
					{
						TreeMap<Integer, TreeMap<String, Integer>> documentSections = null;
						TreeMap<String, Integer> sectionTFs = null;
						
						if (!toReturn.containsKey(word))
						{
							sectionTFs = new TreeMap<String, Integer>();
							sectionTFs.put("paragraphs", 0);
							documentSections = new TreeMap<Integer, TreeMap<String, Integer>>();
							documentSections.put(docID, sectionTFs);
							
							
							toReturn.put(word, documentSections);
						}
						else
						{	
							documentSections = toReturn.get(word);
							if (!documentSections.containsKey(docID))
							{
								sectionTFs = new TreeMap<String, Integer>();
								documentSections.put(docID, sectionTFs);
							}
							else
								sectionTFs = documentSections.get(docID);
						}
						
						if(sectionTFs != null) 
						{
							if (!sectionTFs.containsKey("paragraphs"))
							{
								sectionTFs.put("paragraphs", 1);
							}
							else
							{
								int oldvalue = sectionTFs.get("paragraphs");
								sectionTFs.put("paragraphs", oldvalue + 1);
							}
							
						}
						
						
						if (!vocabulary.contains(word))
							vocabulary.add(word);
						
						
					}
					textCount++;
				}

			}
			
			int titleCount = 0;
			{
				title = (whiteSpace.matcher(nonwords.matcher((numbers.matcher(dash.matcher(title).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

				StringTokenizer str = new StringTokenizer(title);
				

				while(str.hasMoreTokens())
				{
					//{Term, {docID, {Section, TF}}
					//for each term
						//get the TreeMap of documentIDs and sections-TFs
							//get the TreeMap of section-TFs for that docID
								//add word with TF

					String word = str.nextToken();

					Stemmer stem = new Stemmer();
					stem.add(word.toCharArray(), word.length());
					stem.stem();
					
					word = stem.toString();
					
					if(!stopWords.contains(word))
					{
						TreeMap<Integer, TreeMap<String, Integer>> documentSections = null;
						TreeMap<String, Integer> sectionTFs = null;
						
						if (!toReturn.containsKey(word))
						{
							sectionTFs = new TreeMap<String, Integer>();
							sectionTFs.put("title", 0);
							documentSections = new TreeMap<Integer, TreeMap<String, Integer>>();
							documentSections.put(docID, sectionTFs);
							
							
							toReturn.put(word, documentSections);
						}
						else
						{	
							documentSections = toReturn.get(word);
							if (!documentSections.containsKey(docID))
							{
								sectionTFs = new TreeMap<String, Integer>();
								documentSections.put(docID, sectionTFs);
							}
							else
								sectionTFs = documentSections.get(docID);
						}
						
						if(sectionTFs != null) 
						{
							if (!sectionTFs.containsKey("title"))
							{
								sectionTFs.put("title", 1);
							}
							else
							{
								int oldvalue = sectionTFs.get("title");
								sectionTFs.put("title", oldvalue + 1);
							}
							
						}
						
						
						if (!vocabulary.contains(word))
							vocabulary.add(word);
						
					}
					titleCount++;
				}

			}
			
			int headerCount = 0;
			{
				header = (whiteSpace.matcher(nonwords.matcher((numbers.matcher(dash.matcher(header).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

				StringTokenizer str = new StringTokenizer(header);
				

				while(str.hasMoreTokens())
				{
					//{Term, {docID, {Section, TF}}
					//for each term
						//get the TreeMap of documentIDs and sections-TFs
							//get the TreeMap of section-TFs for that docID
								//add word with TF

					String word = str.nextToken();

					Stemmer stem = new Stemmer();
					stem.add(word.toCharArray(), word.length());
					stem.stem();
					
					word = stem.toString();
					
					if(!stopWords.contains(word))
					{
						TreeMap<Integer, TreeMap<String, Integer>> documentSections = null;
						TreeMap<String, Integer> sectionTFs = null;
						
						if (!toReturn.containsKey(word))
						{
							sectionTFs = new TreeMap<String, Integer>();
							sectionTFs.put("headers", 0);
							documentSections = new TreeMap<Integer, TreeMap<String, Integer>>();
							documentSections.put(docID, sectionTFs);
							
							
							toReturn.put(word, documentSections);
						}
						else
						{	
							documentSections = toReturn.get(word);
							if (!documentSections.containsKey(docID))
							{
								sectionTFs = new TreeMap<String, Integer>();
								documentSections.put(docID, sectionTFs);
							}
							else
								sectionTFs = documentSections.get(docID);
						}
						
						if(sectionTFs != null) 
						{
							if (!sectionTFs.containsKey("headers"))
							{
								sectionTFs.put("headers", 1);
							}
							else
							{
								int oldvalue = sectionTFs.get("headers");
								sectionTFs.put("headers", oldvalue + 1);
							}
							
						}
						
						
						if (!vocabulary.contains(word))
							vocabulary.add(word);
						
						
						
					}
					headerCount++;
				}

			}

			ParserIndexer.getDocs_TokensAmt().put(docID, headerCount + titleCount + textCount);



}
		ParserIndexer.calculateStaticVariables();
	

	return toReturn;
}
	
	public static void printMap(TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> map)
	{
		//String result = "";
		
		Iterator i = map.entrySet().iterator();
		Entry iEntry = null;
		while (i.hasNext())
		{
			iEntry = (Entry) i.next();
			String term = (String) iEntry.getKey();
			
			TreeMap<Integer, TreeMap<String, Integer>> documentsSections = (TreeMap<Integer, TreeMap<String, Integer>>) iEntry.getValue();
			
			//result += term + ":\n";
			System.out.print(term); System.out.println(":"); 
			
			
			Iterator documentsSectionsIt = documentsSections.entrySet().iterator();
			Entry documentsSectionsEntry = null;
			while (documentsSectionsIt.hasNext())
			{
				documentsSectionsEntry = (Entry) documentsSectionsIt.next();
				int docID = (Integer) documentsSectionsEntry.getKey();
				TreeMap<String, Integer> sectionsTF = (TreeMap<String, Integer>) documentsSectionsEntry.getValue();
				
				//result += "docID: " + docID + ":\n";
				System.out.print(docID); System.out.print(": "); System.out.print(docID); System.out.println(":"); 
				
				Iterator sectionsTFIt = sectionsTF.entrySet().iterator();
				Entry sectionsTFEntry = null;
				while (sectionsTFIt.hasNext())
				{
					sectionsTFEntry = (Entry) sectionsTFIt.next();
					String section = (String) sectionsTFEntry.getKey();
					int frequency = (Integer) sectionsTFEntry.getValue();
					
					//result += "Section: " + section + " Frequency: " + frequency + "\n";
					System.out.print("Section: "); System.out.print(section); System.out.print(" Frequency: "); System.out.println(frequency);
					
				}
				
			}
			//result += "\n";
			System.out.println();
		}
		
		//return result;
		
	}
	
	
	
	private static void calculateStaticVariables()
	{
		Iterator docs_TokensAmtIterator = docs_TokensAmt.entrySet().iterator();
		Entry docs_TokensAmtEntry = null;
		
		//double docs = 0;
		double tokensAmt = 0;
		double tokensAmtPerDoc = 0;
		
		while (docs_TokensAmtIterator.hasNext())
		{
			docs_TokensAmtEntry = (Entry) docs_TokensAmtIterator.next();
			tokensAmtPerDoc = (Integer) docs_TokensAmtEntry.getValue();
			tokensAmt += tokensAmtPerDoc;
			
			totalDocs++;
		}
		
		AvgDocsTokens = tokensAmt / totalDocs;
		
	}
	
	
	
	
	
	public static String getContent(String link)
	{
		
		String toReturn = "";
		String text = "";
		String title = "";
		String headers = "";
		

		boolean success = false;
		while (success != true)
		{
			try
			{
				Document onlinedoc = null;
				onlinedoc = Jsoup.connect(link).get();
				success = true;

				Elements paragraphs = onlinedoc.select("p");
				for(Element p : paragraphs)
					text += p.text() + " ";
				
				
				Elements titleElement = onlinedoc.select("title");
				for(Element t : titleElement)
					title += t.text() + " ";
				
				
				Elements headerElement = onlinedoc.select("h1");
				for(Element h : headerElement)
					headers += h.text() + " ";


				headerElement = onlinedoc.select("h2");
				for(Element h : headerElement)
					headers += h.text() + " ";

				headerElement = onlinedoc.select("h3");
				for(Element h : headerElement)
					headers += h.text() + " ";

				headerElement = onlinedoc.select("h4");
				for(Element h : headerElement)
					headers += h.text() + " ";

				headerElement = onlinedoc.select("h5");
				for(Element h : headerElement)
					headers += h.text() + " ";

				headerElement = onlinedoc.select("h6");
				for(Element h : headerElement)
					headers += h.text() + " ";
				
				
				
				toReturn += "Title: " + title + "\n";
				toReturn += "Headers: " + headers + "\n";
				toReturn += "Body: " +  text + "\n\n";

				break;

			}
			catch(IOException e)
			{
				/*
				System.err.println("HttpStatusException:");//, Waiting a couple of secs
				System.err.print("Could not grab body of:");
				System.err.println(link);

				//e.printStackTrace();

				System.err.println("Waiting 10 secs");
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				System.err.println("Retrying");

				//e.printStackTrace();
				*/
			}
			
		}

		return toReturn;

	}
	
	
	
	
	
	
	
	
	
	public static String getBody(String link)
	{
		
		String text = "";

		boolean success = false;
		while (success != true)
		{
			try
			{
				Document onlinedoc = null;
				onlinedoc = Jsoup.connect(link).get();
				success = true;

				Elements paragraphs = onlinedoc.select("p");
				for(Element p : paragraphs)
					text += p.text() + " ";

				break;

			}
			catch(IOException e)
			{
				/*
				System.err.println("HttpStatusException:");//, Waiting a couple of secs
				System.err.print("Could not grab body of:");
				System.err.println(link);

				//e.printStackTrace();

				System.err.println("Waiting 10 secs");
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				System.err.println("Retrying");

				//e.printStackTrace();
				*/
			}
			
		}

		return text;

	}
	
	public static String getTitle(String link)
	{
		
		String text = "";

		boolean success = false;
		while (success != true)
		{
			try
			{
				Document onlinedoc = null;
				onlinedoc = Jsoup.connect(link).get();
				success = true;

				Elements title = onlinedoc.select("title");
				for(Element t : title)
					text += t.text() + " ";

				break;

			}
			catch(IOException e)
			{
				/*
				System.err.println("HttpStatusException:");//, Waiting a couple of secs
				System.err.print("Could not grab title of:");
				System.err.println(link);

				//e.printStackTrace();

				System.err.println("Waiting 10 secs");
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				System.err.println("Retrying");

				//e.printStackTrace();
				*/
			}
			
		}

		return text;

	}
	
	
	public static String getHeader(String link)
	{
		String text = "";
		
		boolean success = false;
		
		while (success != true)
		{
			try
			{
				Document onlinedoc = null;
				onlinedoc = Jsoup.connect(link).get();
				success = true;


				Elements headers = onlinedoc.select("h1");
				for(Element h : headers)
					text += h.text() + " ";


				headers = onlinedoc.select("h2");
				for(Element h : headers)
					text += h.text() + " ";

				headers = onlinedoc.select("h3");
				for(Element h : headers)
					text += h.text() + " ";

				headers = onlinedoc.select("h4");
				for(Element h : headers)
					text += h.text() + " ";

				headers = onlinedoc.select("h5");
				for(Element h : headers)
					text += h.text() + " ";

				headers = onlinedoc.select("h6");
				for(Element h : headers)
					text += h.text() + " ";

				break;

			}
			catch(IOException e)
			{
				/*
				System.err.println("HttpStatusException:");//, Waiting a couple of secs
				System.err.print("Could not grab header of:");
				System.err.println(link);

				//e.printStackTrace();

				System.err.println("Waiting 10 secs");
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				System.err.println("Retrying");
				*/
				
			}
		}
		return text;

	}
	
}