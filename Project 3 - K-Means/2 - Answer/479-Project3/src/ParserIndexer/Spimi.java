package ParserIndexer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.swing.text.html.HTMLDocument.Iterator;


public class Spimi
{
	private static TreeMap<String, TreeMap<Integer, Integer>> finalIndex = null;
	private static TreeMap<Integer, Integer> docs_TokensAmt = null;
	private static int totalDocs = 0;
	private static double AvgDocsTokens = 0;
	
	
	public static TreeMap<Integer, Integer> getDocs_TokensAmt()
	{
		return docs_TokensAmt;
	}

	public static void setDocs_TokensAmt(TreeMap<Integer, Integer> docs_TokensAmt)
	{
		Spimi.docs_TokensAmt = docs_TokensAmt;
	}
	
	
	
	public static void SPIMIalgo(long limit)
	{
		if (getDocs_TokensAmt() == null)
			docs_TokensAmt = new TreeMap<Integer, Integer>();//setDocs_TokensAmt(new TreeMap<Integer, Integer>());
		
		if ((limit / 1048576) < 30)
		{
			System.err.println("Limit too low, please enter any value > 30 MB");
			System.exit(0);
		}
		/* 
to save memory space, I could encode it as a list of documents, each document contains words
*/
		long initialMemoryUnchanged = Runtime.getRuntime().freeMemory();
		
		long initialMemory = initialMemoryUnchanged;
		
		//MEMORY STUFF
		//long limit = initialMemory;//52428800; //artificial memory size;
		SpimiHelper.setLimit(limit);
				
		if (initialMemory > limit)
			initialMemory = limit;
		
		
		System.out.println("Initial Free Memory: " + initialMemory / 1048756);
		
		//Gets all files available in the reuters directory that end with "sgm"
		String[] files = SpimiHelper.getFiles("reuters21578", "", "sgm");
		
		LinkedHashMap<String, TreeMap<String, Integer>> list = new LinkedHashMap<String, TreeMap<String, Integer>>();
		
		
		int count = 0;
		
		//This will be our naming template for the block files
		String template = "blocks/block";
		
		
		System.out.println("Parsing Reuters Directory");
		
		for (int i = 0; i < files.length; i++)
		{
			
			long currentMemory = Runtime.getRuntime().freeMemory();
			
			if (currentMemory > initialMemory)
				currentMemory = initialMemory;
						
			if (limit == initialMemoryUnchanged) // use percentage (memory not set)
			{
				if ((currentMemory > 0.7 * initialMemory ) || (list.isEmpty()))
				{
					System.out.println("Parsing file: " + files[i]);
					list.putAll(SpimiHelper.parseFile(files[i])); //adds elements to doc - words list
				}
				else
				{
					
					String file = template + count + ".blk";
					File outFile = new File(file);

					while (!list.isEmpty())
					{
						list = SpimiHelper.SPIMIConstruction(list, outFile); //saves blocks to disk
						count++;
					}
					
					i--;
					
				}
			}
			else
			{
				if ((currentMemory > 57153001.6 ) || (list.isEmpty()))
				{
					System.out.println("Parsing file: " + files[i]);
					list.putAll(SpimiHelper.parseFile(files[i])); //adds elements to doc - words list
				}
				else
				{
					
					String file = template + count + ".blk";
					File outFile = new File(file);

					while (!list.isEmpty())
					{
						list = SpimiHelper.SPIMIConstruction(list, outFile); //saves blocks to disk
						count++;
					}
					
					i--;
					
				}
			}
			
			
			
			
		}
		
		//As long as not elements have been processed
		while (!list.isEmpty())
		{
			String file = template + count + ".blk";
			File outFile = new File(file);

			list = SpimiHelper.SPIMIConstruction(list, outFile); //saves blocks to disk
			
			count++;
			
		}
		
		
		//Clean Up:
		files = null;
		list = null;
		System.gc();
		
		//System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory() / 1048756);
		
		//Merge Blocks
		//System.out.println("\nMerging Blocks:");
		System.out.println("Parsing Blocks Directory");
		
		
		files = SpimiHelper.getFiles("blocks/", "", "blk");
		
		String file1Name = null;
		String file2Name = null;
		String outFileName = null;
		
		String outFileAbsPath = null;
		
		for (int i = 0; i < files.length - 1; i = i + 1)
		{
						
			if (i == 0)
			{
				file1Name = template + i + ".blk";
				file2Name = template + (i + 1) + ".blk";
				outFileName = template + i + "&" + (i + 1);
				
			}
			else
			{
				file1Name = outFileName + ".blk";
				file2Name = template + (i + 1) + ".blk";
				
				outFileName = outFileName + "&" + (i + 1);
			}
			
			outFileAbsPath = outFileName + ".blk";
			
			
			//Files to merge, output file to store result in
			System.out.println("Merging \n" + file1Name + "\n & \n" + file2Name + "\n into \n" + outFileName + ".blk");
			
			File file1 = new File(file1Name);
			File file2 = new File(file2Name);
			File outFile = new File(outFileAbsPath);
			
			SpimiHelper.mergeTwoBlocks(file1, file2, outFile);
			System.gc();
			
		}
		
		System.gc();
		
		finalIndex = SpimiHelper.retrieveBlockFrom(new File(outFileAbsPath));
		
		
		
		Iterator docs_TokensAmtIterator = docs_TokensAmt.entrySet().iterator();
		Entry docs_TokensAmtEntry = null;
		
		//double docs = 0;
		double tokensAmt = 0;
		double tokensAmtPerDoc = 0;
		
		while (docs_TokensAmtIterator.hasNext())
		{
			docs_TokensAmtEntry = (Entry) docs_TokensAmtIterator.next();
			//docs = (Integer) docs_TokensAmtEntry.getKey();
			tokensAmtPerDoc = (Integer) docs_TokensAmtEntry.getValue();
			tokensAmt += tokensAmtPerDoc;
			
			//System.out.println("Doc ID: " + docs);
			//System.out.println("Tokens Amt: " + tokensAmtPerDoc);
			
			//docs++;
			totalDocs++;
		}
		
		//totalDocs = (int) docs;
		
		AvgDocsTokens = tokensAmt / totalDocs;

		System.out.println(tokensAmt + " / " + totalDocs + " = " + AvgDocsTokens);
		
		
		//System.out.println(AvgDocsTokens);
		
		
		
		/*
		//Print to screen
		Iterator i1 = finalIndex.entrySet().iterator();
		Entry i1Entry = null;
				
		while (i1.hasNext())
		{
			i1Entry = (Entry) i1.next();
			
			//if (i1Entry.getKey().equals("zurach"))
			{
				System.out.println(i1Entry.getKey());
				System.out.println(i1Entry.getValue());
			}
		}
		*/
		
		
		//System.out.println("HERE");
		
		//Print to file
		Iterator i1 = finalIndex.entrySet().iterator();
		Entry i1Entry = null;
		
		//int postings = 0;
		PrintWriter out = null;
		PrintWriter out2 = null;
		try {
			out = new PrintWriter("index.txt");
			out2 = new PrintWriter("words.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		while (i1.hasNext())
		{
			i1Entry = (Entry) i1.next();
			
				out.println(i1Entry.getKey());
				out2.println(i1Entry.getKey());
				out.println(i1Entry.getValue());
				//postings += ((ArrayList<Integer>) i1Entry.getValue()).size();
		}
		out.close();
		out2.close();
		
		//System.out.println("Postings: " + postings);
		
		
	}
	
	
	
	
	
	
	
	
	
	
	//Passed as argument, the list of document ids that match our query. This method prints the documents to screen
	public static void retrieveDocumentsWithIDs(Map<Integer, Double> idList)
	{
		
		String fileTemplate = "reuters21578/reut2-0";
		
		Iterator docsIterator = idList.entrySet().iterator();
		Entry docsEntry = null;
		
		while (docsIterator.hasNext())
		{
			docsEntry = (Entry) docsIterator.next();
			
			int id = (Integer) docsEntry.getKey();
			
			System.out.println("\nDocument ID: " + id + "\tScore:" + (Double) docsEntry.getValue());
			
			//Find the relevant document file
			int fileID = (id - 1);
			
			if (fileID >= 1000)
				fileID = fileID / 1000;
			else
				fileID = fileID / 100;
			
			//Names the file accordingly
			String fileName = null;
			
			if (fileID < 10)
				fileName = fileTemplate + "0" + fileID + ".sgm";
			else
				fileName = fileTemplate + fileID + ".sgm";
			
			StringBuilder entireText = new StringBuilder();
			
			FileInputStream fs = null;
			try {
				fs = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//new InputStreamReader(new File(file));
			InputStreamReader isr = new InputStreamReader(fs);
			BufferedReader br = new BufferedReader(isr);//( new InputStreamReader( source ) );    
			
			//String line;
			//while ( (line = br.readLine()) != null)
			try {
				for ( String line; (line = br.readLine()) != null; )
					entireText.append(line + "\n" );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			String idText = "NEWID=\"" + id;
			
			/*
			//Deletes everything preceding our document
			int startDelete = entireText.indexOf(idText);
			entireText.delete(0, startDelete);
			
			//Deletes everything preceding the body of our document
			int bodyStart = entireText.indexOf("<BODY>");
			entireText.delete(0, bodyStart-1);
			
			//Fetches the body index
			bodyStart = entireText.indexOf("<BODY>");
			int bodyEnd = entireText.indexOf("&#3");
			
			
			//Prints everything (in between <BODY> and &#3)
			System.out.println(entireText.substring(bodyStart + 6, bodyEnd));
			*/
			
			int idStartIndex = entireText.indexOf(idText);
			//int idEndIndex = entireText.indexOf("\">", idStartIndex);

			int bodyStartIndex = entireText.indexOf("<BODY>", idStartIndex) + 6;
			int bodyEndIndex = entireText.indexOf("</BODY>", bodyStartIndex) - 4;

			System.out.println(entireText.substring(bodyStartIndex, bodyEndIndex));
			
			
			entireText = null;
			System.gc();
			
		}
		
	
	}
	
	public static void retrieveDocumentsWithIDs(Map<Integer, Double> idList, String query)
	{
		
		query = (SpimiHelper.whiteSpace.matcher(SpimiHelper.nonwords.matcher((SpimiHelper.numbers.matcher(SpimiHelper.dash.matcher(query).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

		ArrayList<String> terms = new ArrayList<String>();

		StringTokenizer str = new StringTokenizer(query);

		while(str.hasMoreTokens())
		{
			String word = str.nextToken();

			Stemmer stem = new Stemmer();
			stem.add(word.toCharArray(), word.length());
			stem.stem();

			word = stem.toString();

			if (!SpimiHelper.stopWords.contains(word)) 
				terms.add(word);

		}
		
		String fileTemplate = "reuters21578/reut2-0";
		
		Iterator docsIterator = idList.entrySet().iterator();
		Entry docsEntry = null;
		
		while (docsIterator.hasNext())
		{
			docsEntry = (Entry) docsIterator.next();
			
			int id = (Integer) docsEntry.getKey();
			
			System.out.println("\nDocument ID: " + id + "\tScore:" + (Double) docsEntry.getValue());
			
			//Find the relevant document file
			int fileID = (id - 1);
			
			if (fileID >= 1000)
				fileID = fileID / 1000;
			else
				fileID = fileID / 100;
			
			//Names the file accordingly
			String fileName = null;
			
			if (fileID < 10)
				fileName = fileTemplate + "0" + fileID + ".sgm";
			else
				fileName = fileTemplate + fileID + ".sgm";
			
			StringBuilder entireText = new StringBuilder();
			
			FileInputStream fs = null;
			try {
				fs = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//new InputStreamReader(new File(file));
			InputStreamReader isr = new InputStreamReader(fs);
			BufferedReader br = new BufferedReader(isr);//( new InputStreamReader( source ) );    
			
			//String line;
			//while ( (line = br.readLine()) != null)
			try {
				for ( String line; (line = br.readLine()) != null; )
					entireText.append(line + "\n" );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			String idText = "NEWID=\"" + id;
						
			int idStartIndex = entireText.indexOf(idText);
			//int idEndIndex = entireText.indexOf("\">", idStartIndex);

			int bodyStartIndex = entireText.indexOf("<BODY>", idStartIndex) + 6;
			int bodyEndIndex = entireText.indexOf("</BODY>", bodyStartIndex) - 4;

			
			String body = entireText.substring(bodyStartIndex, bodyEndIndex);
			
			//For each term in query
			Iterator termsIterator = terms.iterator();
			while (termsIterator.hasNext())			
			{
				String word = (String) termsIterator.next();
				int count = 0;
				
				Pattern p = Pattern.compile(word, Pattern.CASE_INSENSITIVE);  // insert your pattern here
				Matcher m = p.matcher(body);
				while (m.find())
				{
					count++;
				}
				
				System.out.print(word +": " + count + "\t");
				
			}
			
			System.out.println("\n" + body);
			
			//System.out.println(entireText.substring(bodyStartIndex, bodyEndIndex));
			
			
			entireText = null;
			System.gc();
			
		}
	}











	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Process Query: Gets the query + type, filters the query, stores terms in list, loops through list, get postings lists for terms from index
		//get document ids, intersect or union, remove duplicates, return list
		
		public static ArrayList<Integer> processQuery(String query, String type)
		{
			if ( (!type.equalsIgnoreCase("AND")) && (!type.equalsIgnoreCase("OR")) )
				return null;
			
			query = (SpimiHelper.whiteSpace.matcher(SpimiHelper.nonwords.matcher((SpimiHelper.numbers.matcher(SpimiHelper.dash.matcher(query).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();
			
			ArrayList<String> terms = new ArrayList<String>();
			
			StringTokenizer str = new StringTokenizer(query);

			while(str.hasMoreTokens())
			{
				String word = str.nextToken();
				
				Stemmer stem = new Stemmer();
				stem.add(word.toCharArray(), word.length());
				stem.stem();
				
				word = stem.toString();
				
				if (!SpimiHelper.stopWords.contains(word)) 
					terms.add(word);
				
			}
			
			
			ArrayList<Integer> docs = new ArrayList<Integer>();
			
			if (type.equalsIgnoreCase("AND"))
			{
				Iterator termsIterator = terms.iterator();
				if (termsIterator.hasNext())
				{
					String key = (String) termsIterator.next();
					
					if (finalIndex.containsKey(key))
					{	
						TreeMap<Integer, Integer> docIDs = finalIndex.get(key);
						
						Iterator dIDsIterator = docIDs.entrySet().iterator();
						Entry dIDEntry = null;
						while (dIDsIterator.hasNext())
						{
							dIDEntry = (Entry) dIDsIterator.next();
							
							int dID = (Integer) dIDEntry.getKey();//dIDsIterator.next();
							if (!docs.contains(dID))
								docs.add(dID);
						}
					}
				}
				
				
				while (termsIterator.hasNext())
				{
					String key = (String) termsIterator.next();
					
					if (finalIndex.containsKey(key))
					{
						TreeMap<Integer, Integer> docIDs = finalIndex.get(key);
						//ArrayList<Integer> temp =  docIDs.keySet();
						//docs = intersectPostingsLists(docs, temp);
					}
					else
					{
						docs = new ArrayList<Integer>();
						return docs;
					}
				}
				
				
			}
			else
				if (type.equalsIgnoreCase("OR"))
				{
					Iterator termsIterator = terms.iterator();
					while (termsIterator.hasNext())
					{
						String key = (String) termsIterator.next();
						
						//System.out.println(finalIndex);
						
						if (finalIndex.containsKey(key))
						{	
							TreeMap<Integer, Integer> docIDs = finalIndex.get(key);
							Iterator dIDsIterator = docIDs.entrySet().iterator();
							
							Entry dIDEntry = null;
							
							while (dIDsIterator.hasNext())
							{
								dIDEntry = (Entry) dIDsIterator.next();
								
								int dID = (Integer) dIDEntry.getKey();
								if (!docs.contains(dID))
									docs.add(dID);
							}
						}
					}
				}
			
			Collections.sort(docs);
			
			return docs;
			
			
		}
		
		
		//Intersects Postings Lists and returns a new list with just the documents that intersect (documents available in both lists)
		public static ArrayList<Integer> intersectPostingsLists(ArrayList<Integer> l1, ArrayList<Integer> l2)
		{
			ArrayList<Integer> newList = new ArrayList<Integer>();
			
			Iterator l1Iterator = l1.iterator();
			Iterator l2Iterator = l2.iterator();
			
			int l1ID = 0;
			if (l1Iterator.hasNext())
				l1ID = (Integer) l1Iterator.next();
			
			int l2ID = 0;
			if (l2Iterator.hasNext())
				l2ID = (Integer) l2Iterator.next();
			
			while ((l1Iterator.hasNext()) && (l2Iterator.hasNext()))
			{
				
				if (l1ID == l2ID)
				{
					newList.add(l1ID);
					l1ID = (Integer) l1Iterator.next();
					l2ID = (Integer) l2Iterator.next();
				}
				else
				{
					if (l1ID < l2ID)
						l1ID = (Integer) l1Iterator.next();
					else
						l2ID = (Integer) l2Iterator.next();
				}
			}
			
			return newList;
		}

		
		
		
		
		
		
		
		
		
		public static /*LinkedHashMap<Integer, Integer>*/ Map<Integer, Double> processQueryTF(String query)
		{
			TreeMap<Integer, Integer> docsFrequencies = new TreeMap<Integer, Integer>();
			
			
			query = (SpimiHelper.whiteSpace.matcher(SpimiHelper.nonwords.matcher((SpimiHelper.numbers.matcher(SpimiHelper.dash.matcher(query).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

			ArrayList<String> terms = new ArrayList<String>();
			//LinkedHashMap<String, Integer> terms = new LinkedHashMap<String, Integer>();
			
			StringTokenizer str = new StringTokenizer(query);

			while(str.hasMoreTokens())
			{
				String word = str.nextToken();

				Stemmer stem = new Stemmer();
				stem.add(word.toCharArray(), word.length());
				stem.stem();

				word = stem.toString();

				if (!SpimiHelper.stopWords.contains(word)) 
					terms.add(word);
				/*	
				if (terms.containsKey(word))
						terms.put(word, ((Integer) terms.get(word)) + 1);
					else
						terms.put(word, 1);
				*/
				
			}
			
			
			//For each term in query
			Iterator termsIterator = terms.iterator();
			while (termsIterator.hasNext())			
			{
				String word = (String) termsIterator.next();
				
				//System.out.println(word);
				
				//For each document
				//private static TreeMap<String, TreeMap<Integer, Integer>> finalIndex = null;

				TreeMap<Integer, Integer> docIDs = finalIndex.get(word);
				Iterator dIDsIterator = docIDs.entrySet().iterator();
				Entry dIDEntry = null;
				
				while (dIDsIterator.hasNext())
				{
					//System.out.println("HERE");
					dIDEntry = (Entry) dIDsIterator.next();
					
					int dID = (Integer) dIDEntry.getKey();
					int frequency = (Integer) dIDEntry.getValue();
					
					//score[document] += term frequency
					
					if (!docsFrequencies.containsKey(dID))
					{
						//System.out.print("ZABRE");
						docsFrequencies.put(dID, frequency);
					}
					else
					{
						//System.out.println("ZABARINO");
						int oldFrequency = docsFrequencies.get(dID);
						docsFrequencies.put(dID, frequency + oldFrequency);
					}
					
				}
				
				
			}
			
			//docsFrequencies = null;
			//System.gc();
			
			Map<Integer, Double> toReturn = new LinkedHashMap<Integer, Double>();
			
			
			//frequency --> documents
			TreeMap<Integer, ArrayList<Integer>> temp = new TreeMap<Integer, ArrayList<Integer>>(Collections.reverseOrder());
			
			Iterator docsFsIterator = docsFrequencies.entrySet().iterator();
			Entry docsFsEntry = null;
			
			while (docsFsIterator.hasNext())
			{
				docsFsEntry = (Entry) docsFsIterator.next();
				
				int frequency = (Integer) docsFsEntry.getValue();
				int documentID = (Integer) docsFsEntry.getKey();
				
				//System.out.println("Frequency: " + frequency);
				//System.out.println("Document ID: " + documentID);
				
				if (temp.containsKey(frequency))
				{
					//System.out.println("HERE");
					ArrayList<Integer> documents = temp.get(frequency);
					documents.add(documentID);
					//System.out.println(documents);
				}
				else
				{
					ArrayList<Integer> documents = new ArrayList<Integer>();
					documents.add(documentID);
					temp.put(frequency, documents);
					//System.out.println(documents);
				}
				
				
				
			}
			
			//docsFrequencies = null;
			//System.gc();
			
			
			//LinkedHashMap<Integer, Integer> toReturn = new LinkedHashMap<Integer, Integer>();
			
			System.out.println("Matching Documents Are: ");
			Iterator tempIterator = temp.entrySet().iterator();
			Entry tempEntry = null;
			
			int count = 0;
			
			while (tempIterator.hasNext())
			{
				tempEntry = (Entry) tempIterator.next();
				
				int frequency = (Integer) tempEntry.getKey();
				ArrayList<Integer> documents = (ArrayList<Integer>) tempEntry.getValue();
				
				//System.out.println("FREQUENCY: " + frequency);
				
				Iterator documentsIterator = documents.iterator();
				
				
				while ((documentsIterator.hasNext()) && (count != 5))
				{
					int docID = (Integer) documentsIterator.next();
					//toReturn.put(docID, frequency);
					//System.out.println("docID:" + docID + "\tfrequency: " + frequency);
					toReturn.put(docID, (double) frequency);
					count++;
				}
				
			}
			//temp = null;
			
			
			
			return toReturn;
			
		}

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		public static Map<Integer, Double> processQueryBM25(String query, double k, double b) 
		{
			TreeMap<Integer, Double> docsScores = new TreeMap<Integer, Double>();
			
			query = (SpimiHelper.whiteSpace.matcher(SpimiHelper.nonwords.matcher((SpimiHelper.numbers.matcher(SpimiHelper.dash.matcher(query).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

			ArrayList<String> terms = new ArrayList<String>();

			StringTokenizer str = new StringTokenizer(query);

			while(str.hasMoreTokens())
			{
				String word = str.nextToken();

				Stemmer stem = new Stemmer();
				stem.add(word.toCharArray(), word.length());
				stem.stem();

				word = stem.toString();

				if (!SpimiHelper.stopWords.contains(word)) 
					terms.add(word);

			}
			
			
			//For each term in query
			Iterator termsIterator = terms.iterator();
			while (termsIterator.hasNext())			
			{
				String word = (String) termsIterator.next();
				
				//System.out.println(word);
				
				//For each document
				TreeMap<Integer, Integer> docIDs = finalIndex.get(word);
				Iterator dIDsIterator = docIDs.entrySet().iterator();
				Entry dIDEntry = null;
				
				while (dIDsIterator.hasNext())
				{
					//System.out.println("HERE");
					dIDEntry = (Entry) dIDsIterator.next();
					
					int dID = (Integer) dIDEntry.getKey();
					
					
					double frequency = (Integer) dIDEntry.getValue();
					double part1 = ((totalDocs - docIDs.size() + 0.5) / (docIDs.size() + 0.5));
					double logPart1 = Math.log(part1) / Math.log(2);
					double lengthOfDocument = (Integer) docs_TokensAmt.get(dID);
					double part2 = (frequency * (k + 1)) / (frequency + k*(1 - b + ((b * lengthOfDocument) / AvgDocsTokens) ));
					
					
					double finalResult = logPart1 * part2;
					
					//score[document] += term frequency
					
					if (!docsScores.containsKey(dID))
					{
						//System.out.print("ZABRE");
						docsScores.put(dID, finalResult);
					}
					else
					{
						//System.out.println("ZABARINO");
						double oldResult = docsScores.get(dID);
						docsScores.put(dID, oldResult + finalResult);
					}
					
					
					
				}
				
				
			}
			
			//docsFrequencies = null;
			//System.gc();
			
			Map<Integer, Double> toReturn = new LinkedHashMap<Integer, Double>();
			
			
			//frequency --> documents
			TreeMap<Double, ArrayList<Integer>> temp = new TreeMap<Double, ArrayList<Integer>>(Collections.reverseOrder());
			
			Iterator docsFsIterator = docsScores.entrySet().iterator();
			Entry docsFsEntry = null;
			
			while (docsFsIterator.hasNext())
			{
				docsFsEntry = (Entry) docsFsIterator.next();
				
				double score = (Double) docsFsEntry.getValue();
				int documentID = (Integer) docsFsEntry.getKey();
				
				//System.out.println("Frequency: " + frequency);
				//System.out.println("Document ID: " + documentID);
				
				if (temp.containsKey(score))
				{
					//System.out.println("HERE");
					ArrayList<Integer> documents = temp.get(score);
					documents.add(documentID);
					//System.out.println(documents);
				}
				else
				{
					ArrayList<Integer> documents = new ArrayList<Integer>();
					documents.add(documentID);
					temp.put(score, documents);
					//System.out.println(documents);
				}
				
				
				
			}
			
			//docsFrequencies = null;
			//System.gc();
			
			
			//LinkedHashMap<Integer, Integer> toReturn = new LinkedHashMap<Integer, Integer>();
			
			System.out.println("Matching Documents Are: ");
			Iterator tempIterator = temp.entrySet().iterator();
			Entry tempEntry = null;
			
			int count = 0;
			
			while (tempIterator.hasNext())
			{
				tempEntry = (Entry) tempIterator.next();
				
				double score = (Double) tempEntry.getKey();
				ArrayList<Integer> documents = (ArrayList<Integer>) tempEntry.getValue();
				
				//System.out.println("FREQUENCY: " + frequency);
				
				Iterator documentsIterator = documents.iterator();
				
				
				while ((documentsIterator.hasNext()) && (count != 5))
				{
					int docID = (Integer) documentsIterator.next();
					//toReturn.put(docID, frequency);
					//System.out.println("docID:" + docID + "\tscore: " + score);
					toReturn.put(docID, score);//.add(docID);
					count++;
				}
				
			}
			//temp = null;
			
			
			
			return toReturn;
		}

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		public static void retrieveDocumentsWithIDsAndTFs(LinkedHashMap<Integer, Integer> idList)
		{
			
			String fileTemplate = "reuters21578/reut2-0";
			
			Iterator docsIterator = idList.entrySet().iterator();
			
			while (docsIterator.hasNext())
			{
				
				int id = (Integer) docsIterator.next();
				
				System.out.println("\nDocument ID: " + id);
				
				//Find the relevant document file
				int fileID = (id - 1);
				
				if (fileID >= 1000)
					fileID = fileID / 1000;
				else
					fileID = fileID / 100;
				
				//Names the file accordingly
				String fileName = null;
				
				if (fileID < 10)
					fileName = fileTemplate + "0" + fileID + ".sgm";
				else
					fileName = fileTemplate + fileID + ".sgm";
				
				StringBuilder entireText = new StringBuilder();
				
				FileInputStream fs = null;
				try {
					fs = new FileInputStream(fileName);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//new InputStreamReader(new File(file));
				InputStreamReader isr = new InputStreamReader(fs);
				BufferedReader br = new BufferedReader(isr);//( new InputStreamReader( source ) );    
				
				//String line;
				//while ( (line = br.readLine()) != null)
				try {
					for ( String line; (line = br.readLine()) != null; )
						entireText.append(line + "\n" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				String idText = "NEWID=\"" + id;
				
				//Deletes everything preceding our document
				int startDelete = entireText.indexOf(idText);
				entireText.delete(0, startDelete);
				
				//Deletes everything preceding the body of our document
				int bodyStart = entireText.indexOf("<BODY>");
				entireText.delete(0, bodyStart-1);
				
				//Fetches the body index
				bodyStart = entireText.indexOf("<BODY>");
				int bodyEnd = entireText.indexOf("&#3");
				
				
				//Prints everything (in between <BODY> and &#3)
				System.out.println(entireText.substring(bodyStart + 6, bodyEnd));

				entireText = null;
				System.gc();
				
			}
			
		
		}
		*/
	
	
	
	
	
	
	
	
	
	
	
	
	
}