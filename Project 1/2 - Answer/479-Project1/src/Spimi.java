import java.io.*;
import java.util.*;
import java.util.Map.Entry;

//import javax.swing.text.html.HTMLDocument.Iterator;


public class Spimi
{
	private static TreeMap<String, ArrayList<Integer>> finalIndex = null;
	
	public static void SPIMIalgo(long limit)
	{
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
		
		LinkedHashMap<String, ArrayList<String>> list = new LinkedHashMap<String, ArrayList<String>>();
		
		
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
				if ((currentMemory > 0.8 * initialMemory ) || (list.isEmpty()))
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
	
	/*
	public static void readIndex()
	{
		finalIndex = SpimiHelper.retrieveBlockFrom(new File("blocks/block0&1.blk"));
	}
	*/
	
	
	
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
				{	ArrayList<Integer> docIDs = finalIndex.get(key);
					Iterator dIDsIterator = docIDs.iterator();
					while (dIDsIterator.hasNext())
					{
						int dID = (Integer) dIDsIterator.next();
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
					ArrayList<Integer> docIDs = finalIndex.get(key);
					docs = intersectPostingsLists(docs, docIDs);
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
					{	ArrayList<Integer> docIDs = finalIndex.get(key);
						Iterator dIDsIterator = docIDs.iterator();
						while (dIDsIterator.hasNext())
						{
							int dID = (Integer) dIDsIterator.next();
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
	
	
	//Passed as argument, the list of document ids that match our query. This method prints the documents to screen
	public static void retrieveDocumentsWithIDs(ArrayList<Integer> idList)
	{
		
		String fileTemplate = "reuters21578/reut2-0";
		
		Iterator docsIterator = idList.iterator();
		
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

			
			
			/*
			StringBuilder entireText = new StringBuilder();
			
			try 
			{
				entireText.append((new Scanner(new File(fileName)).useDelimiter("\\A").next()));
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			
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
	
}