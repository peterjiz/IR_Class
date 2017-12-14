package ParserIndexer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;


public class SpimiHelper
{

	//input.replaceAll("\\W", "");
	//In Java's handling of regular expressions, [^\w] matches a non-word character, so the above regular expression 
	//will match and remove all non-word characters.

	public static Pattern numbers = Pattern.compile("\\d*"); //matches any number //"[0-9]");
	public static Pattern nonwords = Pattern.compile("\\W"); //matches any non-word
	public static Pattern whiteSpace = Pattern.compile("\\s+"); //matches any 1 or more whitespaces
	public static Pattern dash = Pattern.compile("-");
	
	public static ArrayList<String> stopWords = loadStopWords();
	
	//private static TreeMap<Integer, Integer> docs_TokensAmt = null;
	
	private static long limit;
	
	
	public static LinkedHashMap<String, TreeMap<String, Integer>> parseFile(String file)
	{
		//read file
		//if (docs_TokensAmt == null)
			//docs_TokensAmt = new TreeMap<Integer, Integer>();

		LinkedHashMap<String, TreeMap<String, Integer>> hash = new LinkedHashMap<String, TreeMap<String, Integer>>();

		/*
		//StringBuilder entireText = new StringBuilder();
		String entireText = null;

		try 
		{
			entireText = (new Scanner(new File(file)).useDelimiter("\\A").next());
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		
		StringBuilder entireText = new StringBuilder();
		//String entireText = null;

		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
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
		
		int reutersStartIndex = entireText.indexOf("<REUTERS");

		while (reutersStartIndex >= 0) //there still exists a document inside our file	
		{
			//NEWID="
			int idStartIndex = entireText.indexOf("NEWID=\"", reutersStartIndex) + 7;
			int idEndIndex = entireText.indexOf("\">", idStartIndex);

			boolean normal = true;
			
			{
				int tempReutersEndIndex = entireText.indexOf("</REUTERS>", reutersStartIndex) + 10;
				int tempReutersStartIndex = entireText.indexOf("<REUTERS", tempReutersEndIndex);
				
				//if body is only after the next reuters
				//if body is after the next newid or the body is -1, exit
				int bodyBeforeReuters = entireText.indexOf("<BODY>", idEndIndex);
				int bodyAfterReuters = entireText.indexOf("<BODY>", tempReutersStartIndex);
				
				if ((bodyBeforeReuters == bodyAfterReuters) || (bodyBeforeReuters == -1))
					normal = false;
			}
			

			if (normal)
			{
				int bodyStartIndex = entireText.indexOf("<BODY>", idEndIndex) + 6;
				int bodyEndIndex = entireText.indexOf("</BODY>", bodyStartIndex);

				String id = entireText.substring(idStartIndex, idEndIndex);

				hash.put(id, new TreeMap<String, Integer>());


				String bodyText = null;

				if (bodyEndIndex - bodyStartIndex > 0)
					bodyText = entireText.substring(bodyStartIndex, bodyEndIndex);//bodyText.append(entireText.substring(bodyStartIndex, bodyEndIndex));

				//Modify bodyText
				//get rid of punctuation, white spaces, numbers, and change case to lower case.
				bodyText = (whiteSpace.matcher(nonwords.matcher((numbers.matcher(dash.matcher(bodyText).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();

				//for each word in bodyText
				StringTokenizer str = new StringTokenizer(bodyText);
				int count = 0;
				//int arrCount = 0;

				while(str.hasMoreTokens())
				{				
					TreeMap<String, Integer> list = hash.get(id);

					String word = str.nextToken();

					Stemmer stem = new Stemmer();
					stem.add(word.toCharArray(), word.length());
					stem.stem();

					word = stem.toString();

					if (list.containsKey(word))
						list.put(word, list.get(word) + 1);
					else
						if ( (!list.containsKey(word)) && (!stopWords.contains(word)) )
							list.put(word, 1);

					count++;
				}
				bodyText = "";

				Spimi.getDocs_TokensAmt().put(Integer.parseInt(id), count);


				int reutersEndIndex = entireText.indexOf("</REUTERS>", reutersStartIndex) + 10;
				reutersStartIndex = entireText.indexOf("<REUTERS", reutersEndIndex);
			}
			else
			{
				int reutersEndIndex = entireText.indexOf("</REUTERS>", reutersStartIndex) + 10;
				reutersStartIndex = entireText.indexOf("<REUTERS", reutersEndIndex);
			}
		}


		return hash;

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




	public static String[] getFiles(String directory, String startswith, String endswith) //No Compression Method
	{

		LinkedHashMap<String, ArrayList<String>> hash = new LinkedHashMap<String, ArrayList<String>>();

		//get list of files		
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		String file = null;


		ArrayList<String> list = new ArrayList<String>();

		//loop through files
		for (int i = 0; i < listOfFiles.length; i++) 
		{


			if ( (listOfFiles[i].isFile()) && (listOfFiles[i].getAbsolutePath().endsWith(endswith)) && (listOfFiles[i].getAbsolutePath().startsWith(startswith))) 
			{
				file = listOfFiles[i].getAbsolutePath();
				list.add(file);

			}

		}

		String[] files = new String[list.size()];
		files = list.toArray(files);

		return files;

	}

	
	//input: document - list of terms
	public static LinkedHashMap<String, TreeMap<String, Integer>> SPIMIConstruction(LinkedHashMap<String, TreeMap<String, Integer>> list, File outFile) //list of term-docID pairs
	{
		//File outFile;
		HashMap<String, TreeMap<Integer, Integer>> outHash = new HashMap<String, TreeMap<Integer, Integer>>(); //<word, <document, tf of that word in that document> outHash;
		
		LinkedHashMap<String, TreeMap<String, Integer>> tempList = (LinkedHashMap<String, TreeMap<String, Integer>>) list.clone();
				
		long initialMemory = Runtime.getRuntime().freeMemory();
		
		if (initialMemory > limit)
			initialMemory = limit;
		
		long currentMemory = initialMemory;
		
		//long currentMemory = Runtime.getRuntime().freeMemory();
		
		Iterator mapIt = tempList.entrySet().iterator();
		while ( (mapIt.hasNext()) && (currentMemory > 0))
		{
			Map.Entry entry = (Entry) mapIt.next();

			String dID = (String) entry.getKey();
			TreeMap<String, Integer> listOfWords = (TreeMap<String, Integer>) entry.getValue();

			//list of words contains the word and the term frequency for that word
			//Iterator listOfWordsIterator = listOfWords.iterator();
			
			
			
			Iterator listOfWordsIterator = listOfWords.entrySet().iterator();
			
			Entry listOfWordsEntry = null;
			
			while (listOfWordsIterator.hasNext())
			{
				listOfWordsEntry = (Entry) listOfWordsIterator.next();
				
				String word = (String) listOfWordsEntry.getKey();//listOfWordsIterator.next();
				Integer termFrequency = (Integer) listOfWordsEntry.getValue();

				if (!outHash.containsKey(word)) //new term
				{
					//postings_list = ADDTODICTIONARY(dictionary, term(token));

					//create postings list for that term
					//ArrayList<String> postings = new ArrayList<String>();
					TreeMap<Integer, Integer> postings = new TreeMap<Integer, Integer>();
					
					//add docID to list
					//postings.add(Integer.parseInt(dID));//.addElement(Integer.parseInt(dID));
					postings.put(Integer.parseInt(dID), termFrequency);
					
					//add term/list to dictionary
					outHash.put(word, postings);
				}
				else //old term
				{
					//postings_list = GETPOSTINGSLIST(dictionary,term(token));

					//get postings list for that term
					//PostingList postings = outHash.get(word);
					TreeMap<Integer, Integer> postings = outHash.get(word);

					//check size + expand (if necessary)


					//add docID to list if it isn't there
					if (!postings.containsKey(Integer.parseInt(dID)))
						postings.put(Integer.parseInt(dID), termFrequency);//.addElement(Integer.parseInt(dID));
					
				}
				
			}
			
			
			//remove processed element from the list
			list.remove(dID);
			
			currentMemory = Runtime.getRuntime().freeMemory();
			if (currentMemory > initialMemory)
				currentMemory = initialMemory;
			
		}
		
			

			//sort lexicographically
			TreeMap<String, TreeMap<Integer, Integer>> sortedTerms = new TreeMap<String, TreeMap<Integer, Integer>>();
			sortedTerms.putAll(outHash);
			
			outHash = null;
			
			//write sortedTerms to disk
			
			try
			{
				FileOutputStream fileOutStream = new FileOutputStream(outFile);
				ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
				objectOutStream.writeObject(sortedTerms);
				//System.out.println("HERE");
				objectOutStream.close();
				fileOutStream.close();

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
			}
			
			
			 //will return modified list so that subsequent calls to SPIMIConstruction processes the rest of the elements

		return list;

	}


	public static void mergeTwoBlocks(File file1, File file2, File outFile)
	{
		//Load file 1 into Tree Map
		TreeMap<String, TreeMap<Integer, Integer>> file1List = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(file1);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			file1List = (TreeMap<String, TreeMap<Integer, Integer>>) objectInStream.readObject();
			objectInStream.close();
			fileInStream.close();

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
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//Load file 2 into Tree Map
		TreeMap<String, TreeMap<Integer, Integer>> file2List = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(file2);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			file2List = (TreeMap<String, TreeMap<Integer, Integer>>) objectInStream.readObject();
			objectInStream.close();
			fileInStream.close();

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
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		//Merge Code		
		Iterator i1 = file1List.entrySet().iterator();
		Iterator i2 = file2List.entrySet().iterator();
		
		
		Entry i1Entry = null;
		Entry i2Entry = null;
		
		if ((i1.hasNext()) && (i2.hasNext()))
		{
			i1Entry = (Entry) i1.next();
			i2Entry = (Entry) i2.next();
		}
		
		TreeMap<String, TreeMap<Integer, Integer>> temporary = new TreeMap<String, TreeMap<Integer, Integer>>();

		
		while ((i1.hasNext()) && (i2.hasNext()))
		{
			//i1 term
			String i1Term = (String) i1Entry.getKey();//i1Entry.getKey();

			//i2 term
			String i2Term = (String) i2Entry.getKey();
			
			//System.out.println("i1: " + i1Term);
			//System.out.println("i2: " + i2Term);
			
			
			
			if (i1Term.equalsIgnoreCase(i2Term)) //equal terms
			{				
				//Merge Postings List

				//i1 documents
				TreeMap<Integer, Integer> i1Postings = (TreeMap<Integer, Integer>) i1Entry.getValue();

				//i2 documents
				TreeMap<Integer, Integer> i2Postings = (TreeMap<Integer, Integer>) i2Entry.getValue();


				//add i2's documents to i1 (i2's document ids > i1's) in natural ascending order.
				Iterator p = i2Postings.entrySet().iterator();
				Entry pEntry = null;
				
				while (p.hasNext())
				{
					pEntry = (Entry) p.next();
					
					Integer docValue = (Integer) pEntry.getKey();//.next();
					Integer termFrequency = (Integer) pEntry.getValue();
					if (!i1Postings.containsKey(docValue))
						i1Postings.put(docValue, termFrequency);
				}
				
				i1Entry = (Entry) i1.next();
				i2Entry = (Entry) i2.next();

			}
			else
				if (i1Term.compareTo(i2Term) < 0) //i1Term < i2Term
				{
					//advance i1
					i1Entry = (Entry) i1.next();

					//no need to add anything, because the element exists in the i1Postings

					//i2 stays the same
				}
				else
				{

					TreeMap<Integer, Integer> i1Postings = (TreeMap<Integer, Integer>) i1Entry.getValue();
					TreeMap<Integer, Integer> i2Postings = (TreeMap<Integer, Integer>) i2Entry.getValue();

					//add i2
					//file1List.put(i2Term, (ArrayList<Integer>) i2Entry.getValue());
					temporary.put(i2Term, (TreeMap<Integer, Integer>) i2Entry.getValue());
					
					//advance i2
					i2Entry = (Entry) i2.next();

					//i1 stays the same
				}
				
		}
		
		/*
		Iterator tempIterator = file1List.entrySet().iterator();
		Entry tempEntry = null;

		
		while (tempIterator.hasNext())
		{
			tempEntry = (Entry) tempIterator.next();
			
			String tempTerm = (String) tempEntry.getKey();
			ArrayList<Integer> tempPostings = (ArrayList<Integer>) tempEntry.getValue();
			file1List.put(tempTerm, tempPostings);

		}
		*/
		
		//Add elements that we couldn't add, because we couldn't modify our ds while iterating
		file1List.putAll(temporary);
		
		
		while (i2.hasNext())
		{
			
			TreeMap<Integer, Integer> i2Postings = (TreeMap<Integer, Integer>) i2Entry.getValue();
			String i2Term = (String) i2Entry.getKey();
			
			if (!file1List.containsKey(i2Term))
			{
				Iterator p = i2Postings.entrySet().iterator();
				TreeMap<Integer, Integer> postings = new TreeMap<Integer, Integer>();
				Entry pEntry = null;
				
				while (p.hasNext())
				{
					pEntry = (Entry) p.next();
					
					Integer docValue = (Integer) pEntry.getKey();
					Integer termFrequency = (Integer) pEntry.getValue();
					postings.put(docValue, termFrequency);
				}
				
				file1List.put(i2Term, postings);
				
			}
			else
			{
				Iterator p = i2Postings.entrySet().iterator();
				Entry pEntry = null;
				TreeMap<Integer, Integer> postings = file1List.get(i2Term);
				
				while (p.hasNext())
				{
					pEntry = (Entry) p.next();
					
					Integer docValue = (Integer) pEntry.getKey();
					Integer termFrequency = (Integer) pEntry.getValue();
					postings.put(docValue, termFrequency);
					
				}
				
				file1List.put(i2Term, postings);
				
			}
			
			
			
			//file1List.put((String) i2Entry.getKey(), (ArrayList<Integer>) i2Entry.getValue());
			i2Entry = (Entry) i2.next();
			
		}
		
		//Save to disk
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(file1List);
			objectOutStream.close();
			fileOutStream.close();
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
		}
		

	}


	public static TreeMap<String, TreeMap<Integer, Integer>> retrieveBlockFrom(File file) 
	{
		TreeMap<String, TreeMap<Integer, Integer>> fileList = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(file);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			fileList = (TreeMap<String, TreeMap<Integer, Integer>>) objectInStream.readObject();
			objectInStream.close();
			fileInStream.close();

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
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fileList;
	}
	
	
	
	
	
	/*
	public static LinkedHashMap<String, ArrayList<String>> parseFileNoCompression(String file)
	{
		LinkedHashMap<String, ArrayList<String>> hash = new LinkedHashMap<String, ArrayList<String>>();
		String entireText = null;

		try 
		{
			entireText = (new Scanner(new File(file)).useDelimiter("\\A").next());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		int reutersStartIndex = entireText.indexOf("<REUTERS");

		while (reutersStartIndex >= 0)
		{
			int idStartIndex = entireText.indexOf("NEWID=\"", reutersStartIndex) + 7;
			int idEndIndex = entireText.indexOf("\">", idStartIndex);

			int bodyStartIndex = entireText.indexOf("<BODY>", idEndIndex) + 6;
			int bodyEndIndex = entireText.indexOf("</BODY>", bodyStartIndex);

			String id = entireText.substring(idStartIndex, idEndIndex);

			hash.put(id, new ArrayList<String>());

			String bodyText = null;

			if (bodyEndIndex - bodyStartIndex > 0)
				bodyText = entireText.substring(bodyStartIndex, bodyEndIndex);
			
			//for each word in bodyText
			StringTokenizer str = new StringTokenizer(bodyText);
			int count = 0;
			int arrCount = 0;

			while(str.hasMoreTokens())
			{				

				ArrayList<String> list = hash.get(id);

				String word = str.nextToken();

				if (!list.contains(word))
					list.add(word);
				
			}
			bodyText = "";
			
			int reutersEndIndex = entireText.indexOf("</REUTERS>", reutersStartIndex) + 10;
			reutersStartIndex = entireText.indexOf("<REUTERS", reutersEndIndex);
			
		}
		
		return hash;

	}
	*/
	
	
	public static LinkedHashMap<String, ArrayList<String>> parseFileNoCompression(String file)
	{
		//read file

		LinkedHashMap<String, ArrayList<String>> hash = new LinkedHashMap<String, ArrayList<String>>();

		//StringBuilder entireText = new StringBuilder();
		String entireText = null;

		try 
		{
			entireText = (new Scanner(new File(file)).useDelimiter("\\A").next());
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int reutersStartIndex = entireText.indexOf("<REUTERS");

		while (reutersStartIndex >= 0) //there still exists a document inside our file	
		{
			//NEWID="
			int idStartIndex = entireText.indexOf("NEWID=\"", reutersStartIndex) + 7;
			int idEndIndex = entireText.indexOf("\">", idStartIndex);

			int bodyStartIndex = entireText.indexOf("<BODY>", idEndIndex) + 6;
			int bodyEndIndex = entireText.indexOf("</BODY>", bodyStartIndex);

			String id = entireText.substring(idStartIndex, idEndIndex);

			hash.put(id, new ArrayList<String>());


			String bodyText = null;

			if (bodyEndIndex - bodyStartIndex > 0)
				bodyText = entireText.substring(bodyStartIndex, bodyEndIndex);//bodyText.append(entireText.substring(bodyStartIndex, bodyEndIndex));

			//Modify bodyText
			//get rid of punctuation, white spaces, numbers, and change case to lower case.
			//bodyText = (whiteSpace.matcher(nonwords.matcher((numbers.matcher(dash.matcher(bodyText).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();
			
			//for each word in bodyText
			StringTokenizer str = new StringTokenizer(bodyText);
			int count = 0;
			int arrCount = 0;

			while(str.hasMoreTokens())
			{				
				ArrayList<String> list = hash.get(id);

				String word = str.nextToken();
				
				//Stemmer stem = new Stemmer();
				//stem.add(word.toCharArray(), word.length());
				//stem.stem();
				
				//word = stem.toString();
				
				if ( (!list.contains(word)))// && (!stopWords.contains(word)) )
					list.add(word);
			}
			bodyText = "";
			

			int reutersEndIndex = entireText.indexOf("</REUTERS>", reutersStartIndex) + 10;
			reutersStartIndex = entireText.indexOf("<REUTERS", reutersEndIndex);

		}


		return hash;

	}
	
	

	public static long getLimit() {
		return limit;
	}

	public static void setLimit(long limit) {
		SpimiHelper.limit = limit;
	}
	
}


class ValueComparator implements Comparator<String>
{

	Map<String, ArrayList<String>> base;

	public ValueComparator(Map<String, ArrayList<String>> outHash) 
	{
		this.base = outHash;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b)
	{
		if (a.compareToIgnoreCase(b) >= 0) //(base.get(a) >= base.get(b))
		{
			return -1;
		} 
		else
		{
			return 1;
		} // returning 0 would merge keys

	}

}














/*
try
{
	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();


	System.out.println(file);


	Document doc = docBuilder.parse (new File(file));

	doc.getDocumentElement ().normalize ();

	NodeList ids = doc.getElementsByTagName("REUTERS");
	NodeList bodies = doc.getElementsByTagName("BODY");


	for(int s=0; s < bodies.getLength() ; s++)
	{
		Node body = bodies.item(s);
		Node id = ids.item(s);

		String idText = id.getAttributes().getNamedItem("NEWID").getNodeValue();

		Element bodyElement = (Element) body;
		NodeList textBodyList = bodyElement.getChildNodes();
		String bodyText = ((Node)textBodyList.item(0)).getNodeValue();

		System.out.println("ID: " + idText);
		System.out.println("Body:\n" + bodyText + "\n");

	}

}
catch(SAXParseException err)
{
	System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	System.out.println(" " + err.getMessage ());
}
catch (SAXException e)
{
	Exception x = e.getException ();
	((x == null) ? e : x).printStackTrace ();
} 
catch (Throwable t)
{
	t.printStackTrace ();
}
 */

/*
String entireText = null;

try 
{
	entireText = new Scanner(new File(file)).useDelimiter("\\A").next();
}
catch (FileNotFoundException e)
{
	// TODO Auto-generated catch block
	e.printStackTrace();
}

entireText.replace("[\\x00-\\x1F]", "");

System.out.println(entireText);
 */