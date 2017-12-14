package Retriever_Clusterer;

import Crawler.FileManager;
import ParserIndexer.ParserIndexer;
import ParserIndexer.Stemmer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Retriever_Clusterer
{
	
	private static TreeMap<Integer, Double> vectorLengths = new TreeMap<Integer, Double>();
	private static TreeMap<Integer, TreeMap<Integer, TreeMap<String, Double>>> clusters = null;//new TreeMap<Integer, TreeMap<Integer, TreeMap<String, Double>>>();
	
	private static TreeMap<Integer, TreeMap<String, Double>> centroids = null;
	
	public static TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> documents = null;
	public static TreeMap<Integer, String> dIDLink = null;
	
	
	private static boolean KMeansRan = false;

	
	//each integer (centroid) maps to a cluster of c
	
		
	//K Means has to be passed document vectors of the same dimensionality
	public static /*TreeMap<Integer, TreeMap<String, Double>>*/ void K_Means(int K, int iterations, TreeMap<Integer, TreeMap<String, Double>> documents)
	{
		clusters = new TreeMap<Integer, TreeMap<Integer, TreeMap<String, Double>>>();
		
		//Create centroids map to hold K centroids
		//each integer maps to a centroid/vector
		TreeMap<Integer, TreeMap<String, Double>> tempCentroids = new TreeMap<Integer, TreeMap<String, Double>>();

		//select K random seeds
		//Assign seeds to centroids
        //Assign K Seed Vectors to uk centroids
		int count = 0;
		while (count != K)
		{
			int randomDocID = (int) ((Math.random() * (documents.size() - 1)) + 1);
			
			if (!tempCentroids.containsKey(randomDocID))
			{
				tempCentroids.put(randomDocID, documents.get(randomDocID)); //centroid is equal to seed
				count++;
			}
			
		}
		
		centroids = new TreeMap<Integer, TreeMap<String, Double>>();
		Iterator tempCentroidIterator = tempCentroids.entrySet().iterator();
		Entry tempCentroidsEntry = null;
		
		count = 1;
		while (tempCentroidIterator.hasNext())
		{
			tempCentroidsEntry = (Entry) tempCentroidIterator.next();
			int docID = (Integer) tempCentroidsEntry.getKey();
			TreeMap<String, Double> document = (TreeMap<String, Double>) tempCentroidsEntry.getValue();
			
			centroids.put(count, document);
			count++;
			
		}
		
	    //As long as we're not satisfied = we haven't stopped
		count = 0;
	    while (count != iterations)
	    {
	    	//System.out.println("Iteration: " + count);
	        //Create new empty cluster k for all seeds k (or not formed yet clusters)
	        //Create K clusters
	    	
	    	//clusters.clear();
	    	
	    	for (int j = 1; j != K + 1; j++)
	        {
	            //create cluster j
	        	TreeMap<Integer, TreeMap<String, Double>> cluster = new TreeMap<Integer, TreeMap<String, Double>>();
	        	clusters.put(j, cluster);
	        }
	        
	        //For all document vectors
	        for (int n = 1; n != documents.size() + 1; n++)
	        {
	            //Find the closest centroid to document vector n
	        	int minCentroidIndex = 9999;
	        	double minCentroidScore = 9999;
	        		        	
	        	Iterator centroidIt = centroids.entrySet().iterator();
	        	Entry centroidEntry = null;
	        	while (centroidIt.hasNext())
	        	{	        		
	        		centroidEntry = (Entry) centroidIt.next();
	        		
	        		int centroidNumber = (Integer) centroidEntry.getKey();
	        			        		
	        		TreeMap<String, Double> centroidVector = (TreeMap<String, Double>) centroidEntry.getValue();
	        		
	        		if (!centroidVector.isEmpty())
	        		{
	        			double newCentroidScore = Retriever_Clusterer.euclideanDistance(centroidVector, documents.get(n));

	        			if (newCentroidScore <= minCentroidScore)
	        			{
	        				minCentroidIndex = centroidNumber;
	        				minCentroidScore = newCentroidScore;
	        			}
	        		}
	        		//else
	        			//System.out.println("Centroid Empty");
	        		
	        		
	        	}
	        	
	        	
	        	int j = minCentroidIndex;
	            
	            	        
	            //Get the cluster associated with that centroid
	            TreeMap<Integer, TreeMap<String, Double>> clusterJ = clusters.get(j);
	            
	            //add documentVector to cluster j
	            TreeMap<String, Double> doc = documents.get(n);
	            clusterJ.put(n, doc);
	            
	            
	        }
	        
	        //For all K clusters, recompute the new centroid
	        for (int j = 1; j != K + 1; j++)
	        {
	        	//recomputation of centroids
	        	TreeMap<String, Double> centroidJ = Retriever_Clusterer.computeCentroidForCluster(clusters.get(j));
	        	centroids.put(j, centroidJ);
	        	
	        }
	        
	        
	        count++;
	        
	    }
	    
	    //return the set of centroids
	    //return centroids;
		
	}
	
	
	
	
	public static Map<Integer, Double> processQueryBM25F(String query, int K, int iterations, TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> finalIndex, int docsWanted, int titleWeight, int headerWeight, int paragraphWeight, double k, double b) 
	{
		Map<Integer, Double> toReturn = null;
		
		System.out.println("Processing Query Terms");
		
		TreeMap<Integer, Double> docsScores = new TreeMap<Integer, Double>();
		
		query = (ParserIndexer.whiteSpace.matcher(ParserIndexer.nonwords.matcher((ParserIndexer.numbers.matcher(ParserIndexer.dash.matcher(query).replaceAll("")).replaceAll(""))).replaceAll(" ")).replaceAll(" ")).toLowerCase();
		
		ArrayList<String> terms = new ArrayList<String>();
		ArrayList<String> termsForBM = new ArrayList<String>();


		StringTokenizer str = new StringTokenizer(query);

		while(str.hasMoreTokens())
		{
			String word = str.nextToken();

			Stemmer stem = new Stemmer();
			stem.add(word.toCharArray(), word.length());
			stem.stem();

			word = stem.toString();
			
			//System.out.println(ParserIndexer.vocabulary);
			//System.out.println(word);
			
			//System.exit(-1);
			
			if ((!ParserIndexer.stopWords.contains(word)) && (ParserIndexer.vocabulary.contains(word)))
			{
				terms.add(word);
				termsForBM.add(word);
			}

		}
		
		System.out.println("Done Processing Query Terms");
		
		//System.out.println(ParserIndexer.vocabulary);
		
		//Create/Get Query Vector  &   Length Normalize
		//System.out.println(terms);
		TreeMap<String, Double> queryVector = Retriever_Clusterer.generateDocumentVectorForQuery(terms);
		//queryVector = Retriever_Clusterer.generateLengthNormalizedDocumentVectorForQuery(queryVector);
		
		//System.out.println("ZABRE");
		
		//documentVector = Retriever_Clusterer.generateLengthNormalizedDocumentVectors(documentVector);
		
		
		

		if (KMeansRan == false)
		{
			//System.out.println("Running K Means for the first time ... Sit back ... It should only take 3 mins");
			System.out.println("Running K Means");
			//Create/Get Document Vector  &   Length Normalize
			TreeMap<Integer, TreeMap<String, Double>> documentVector = Retriever_Clusterer.generateDocumentVector(finalIndex, titleWeight, headerWeight, paragraphWeight);
			Retriever_Clusterer.K_Means(K, iterations, documentVector);
			System.out.println("Done Running K Means");
			KMeansRan = true;
			
			//Retriever_Clusterer.printClustersToFile();
			
			
		}
		
		//Retrieve all Centroids
		
		System.out.println("Finding Closest Centroid");
		
		//Find the closest Centroid to query vector
		int minCentroidIndex = 9999;
		double minCentroidScore = 9999;

		Iterator centroidIt = centroids.entrySet().iterator();
		Entry centroidEntry = null;
		while (centroidIt.hasNext())
		{
			//System.out.println("Finding Closest Centroid");
			centroidEntry = (Entry) centroidIt.next();

			int centroidNumber = (Integer) centroidEntry.getKey();

			TreeMap<String, Double> centroidVector = (TreeMap<String, Double>) centroidEntry.getValue();
    		if (!centroidVector.isEmpty())
    		{
    			double newCentroidScore = Retriever_Clusterer.euclideanDistance(centroidVector, queryVector);
    			//System.out.println(centroidVector);

    			//System.out.println("Printing centroidVector: " + centroidVector);
    			//System.out.println(Retriever_Clusterer.computeVectorLength(centroidVector));
    			//System.out.println(newCentroidScore);



    			if (newCentroidScore < minCentroidScore)
    			{
    				minCentroidIndex = centroidNumber;
    				minCentroidScore = newCentroidScore;
    			}
    			
    		}

		}
		
		int j = minCentroidIndex;
		
		
		
		System.out.println("Cluster is: " + j);
		System.out.println("Retrieving Cluster Associated with that Centroid");
		
		//Get the Cluster Documents associated with that centroid
		TreeMap<Integer, TreeMap<String, Double>> clusterJ = clusters.get(j);
		
		//System.out.println(clusterJ.size());
		System.gc();
		//System.out.println(Runtime.getRuntime().freeMemory() / 1048576);
		//System.out.println(clusterJ);
		
		TreeMap<String, TreeMap<Integer, Double>> clusteredToIndexed = null;
		if (clusterJ != null)
		{
			System.out.println("Converting Cluster into an Index");
			clusteredToIndexed = Retriever_Clusterer.convertClusterToIndex(clusterJ);
		}
		
		//TreeMap<String, TreeMap<Integer, Double>> adjustedCollectionIndex = Retriever_Clusterer.generateAdjustedFrequencyIndex(finalIndex, titleWeight, headerWeight, paragraphWeight);
		
		
		
		Iterator docs_TokensAmtIterator = ParserIndexer.getDocs_TokensAmt().entrySet().iterator();
		Entry docs_TokensAmtEntry = null;

		//double docs = 0;
		double tokensAmt = 0;
		double tokensAmtPerDoc = 0;
		double totalDocs = 0;
		while (docs_TokensAmtIterator.hasNext())
		{
			docs_TokensAmtEntry = (Entry) docs_TokensAmtIterator.next();

			
			int docID = (Integer) docs_TokensAmtEntry.getKey();
			
			if (clusterJ.containsKey(docID))
			{
				tokensAmtPerDoc = (Integer) docs_TokensAmtEntry.getValue();
				tokensAmt += tokensAmtPerDoc;

				totalDocs++;
			}
		}

		double AvgDocsTokens = tokensAmt / totalDocs;
		
		
		

		if (clusteredToIndexed != null)
		{
			//System.out.println("HERE");

			System.out.println("Running BM25F on cluster documents");
			
			//For each term in query
			Iterator termsIterator = termsForBM.iterator();
			while (termsIterator.hasNext())			
			{
				String word = (String) termsIterator.next();

				//For each document
				//TreeMap<String, TreeMap<Integer, Double>> finalIndex = null;
				//term, docID, frequency
				//docID, frequency
				//A document may not have a certain word appearing in the query and vocabulary
				System.out.println("word is: " + word);

				if (clusteredToIndexed.containsKey(word))
				{
					//System.out.println("Calculating BM25");
					
					TreeMap<Integer, Double> docIDs = clusteredToIndexed.get(word);
					
					//TreeMap<Integer, Double> collectionDocIDs = adjustedCollectionIndex.get(word);
					
					Iterator dIDsIterator = docIDs.entrySet().iterator();
					Entry dIDEntry = null;

					while (dIDsIterator.hasNext())
					{
						
						//System.out.println("Calculating Score for 1 instance");
						
						dIDEntry = (Entry) dIDsIterator.next();

						int dID = (Integer) dIDEntry.getKey();

						
						double frequency = (Double) dIDEntry.getValue();
						//System.out.println(frequency);
						double part1 = ((clusterJ.size() - docIDs.size() + 0.5) / (docIDs.size() + 0.5));
						double logPart1 = Math.log(part1) / Math.log(2);
						double lengthOfDocument = (Integer) ParserIndexer.getDocs_TokensAmt().get(dID);
						double part2 = (frequency * (k + 1)) / (frequency + k*(1 - b + ((b * lengthOfDocument) / AvgDocsTokens) ));

						double finalResult = logPart1 * part2;


						if (!docsScores.containsKey(dID))
						{
							docsScores.put(dID, finalResult);
						}
						else
						{
							double oldResult = docsScores.get(dID);
							docsScores.put(dID, oldResult + finalResult);
						}
						
					}
					
				}
				
			}


			System.out.println("Sorting Scores");
			toReturn = new LinkedHashMap<Integer, Double>();

			TreeMap<Double, ArrayList<Integer>> temp = new TreeMap<Double, ArrayList<Integer>>(Collections.reverseOrder());

			Iterator docsFsIterator = docsScores.entrySet().iterator();
			Entry docsFsEntry = null;

			while (docsFsIterator.hasNext())
			{
				docsFsEntry = (Entry) docsFsIterator.next();

				double score = (Double) docsFsEntry.getValue();
				int documentID = (Integer) docsFsEntry.getKey();

				if (temp.containsKey(score))
				{
					ArrayList<Integer> documents = temp.get(score);
					documents.add(documentID);
				}
				else
				{
					ArrayList<Integer> documents = new ArrayList<Integer>();
					documents.add(documentID);
					temp.put(score, documents);
				}



			}

			
			System.out.println("Matching Documents Are: ");
			Iterator tempIterator = temp.entrySet().iterator();
			Entry tempEntry = null;

			int count = 0;

			while (tempIterator.hasNext())
			{
				tempEntry = (Entry) tempIterator.next();

				double score = (Double) tempEntry.getKey();
				ArrayList<Integer> documents = (ArrayList<Integer>) tempEntry.getValue();

				Iterator documentsIterator = documents.iterator();


				while ((documentsIterator.hasNext()) && (count != docsWanted))
				{
					int docID = (Integer) documentsIterator.next();
					toReturn.put(docID, score);
					count++;
				}

			}
			
		}

		System.out.println("Returning Results");

		return toReturn;
	}
	
	
	
	

	
	//Input: Index of term, docID, section, TF
	//Output: AdjustedIndex of term, docID, TF
	public static TreeMap<String, TreeMap<Integer, Double>> generateAdjustedFrequencyIndex(TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> finalIndex, int titleWeight, int headerWeight, int paragraphWeight)
	{
		TreeMap<String, TreeMap<Integer, Double>> toReturn = new TreeMap<String, TreeMap<Integer, Double>>();
		
		
		Iterator finalIndexIt = finalIndex.entrySet().iterator();
		Entry finalIndexEntry = null;
		while (finalIndexIt.hasNext())
		{
			finalIndexEntry = (Entry) finalIndexIt.next();
			String term = (String) finalIndexEntry.getKey();
			
			TreeMap<Integer, TreeMap<String, Integer>> documentsSections = (TreeMap<Integer, TreeMap<String, Integer>>) finalIndexEntry.getValue();
			
			//result += term + ":\n";
			
			
			Iterator documentsSectionsIt = documentsSections.entrySet().iterator();
			Entry documentsSectionsEntry = null;
			while (documentsSectionsIt.hasNext())
			{
				documentsSectionsEntry = (Entry) documentsSectionsIt.next();
				int docID = (Integer) documentsSectionsEntry.getKey();
				TreeMap<String, Integer> sectionsTF = (TreeMap<String, Integer>) documentsSectionsEntry.getValue();
				
				//result += "docID: " + docID + ":\n";
				
				int adjustedFrequency = 0;
				Iterator sectionsTFIt = sectionsTF.entrySet().iterator();
				Entry sectionsTFEntry = null;
				while (sectionsTFIt.hasNext())
				{
					sectionsTFEntry = (Entry) sectionsTFIt.next();
					String section = (String) sectionsTFEntry.getKey();
					int frequency = (Integer) sectionsTFEntry.getValue();
					
					//result += "Section: " + section + " Frequency: " + frequency + "\n";
					if (section.equalsIgnoreCase("title"))
						adjustedFrequency += titleWeight * frequency;
					else
						if (section.equalsIgnoreCase("headers"))
							adjustedFrequency += headerWeight * frequency;
						else
							if (section.equalsIgnoreCase("paragraphs"))
								adjustedFrequency += paragraphWeight * frequency;
								
				}
				
				TreeMap<Integer, Double> doc_Frequency = null;
				if (!toReturn.containsKey(term))
				{
					doc_Frequency = new TreeMap<Integer, Double>();
					doc_Frequency.put(docID, (double) adjustedFrequency);
					toReturn.put(term, doc_Frequency);
				}
				else
				{
					doc_Frequency = toReturn.get(term);
					
					if (!doc_Frequency.containsKey(docID))
					{
						doc_Frequency.put(docID, (double) adjustedFrequency);
					}
					
				}
				
			}
			//result += "\n";
		}
		
		return toReturn;
		
	}
	
	
	
	
	//Input: finalIndex term, docID, section, TF ---> AdjustedIndex of term, docID, TF
	//Ouput: DocID, term, TF
	private static TreeMap<Integer, TreeMap<String, Double>> generateDocumentTF(TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> finalIndex, int titleWeight, int headerWeight, int paragraphWeight)
	{
		TreeMap<String, TreeMap<Integer, Double>> adjustedIndex = Retriever_Clusterer.generateAdjustedFrequencyIndex(finalIndex, titleWeight, headerWeight, paragraphWeight);
		
		
		//docID, term, frequency
		TreeMap<Integer, TreeMap<String, Double>> toReturn = new TreeMap<Integer, TreeMap<String, Double>>();
		
		Iterator adjIterator = adjustedIndex.entrySet().iterator();
		Entry adjEntry = null;
		
		while (adjIterator.hasNext())
		{
			adjEntry = (Entry) adjIterator.next();
			
			String term = (String) adjEntry.getKey();
			TreeMap<Integer, Double> docFrequency = (TreeMap<Integer, Double>) adjEntry.getValue();
			
			Iterator docFrequencyIterator = docFrequency.entrySet().iterator();
			Entry dfEntry = null;
			while (docFrequencyIterator.hasNext())
			{
				dfEntry = (Entry) docFrequencyIterator.next();
				
				int docID = (Integer) dfEntry.getKey();
				double frequency = (Double) dfEntry.getValue();
				
				TreeMap<String, Double> tf = null;
				
				if (!toReturn.containsKey(docID))
				{
					tf = new TreeMap<String, Double>();
					toReturn.put(docID, tf);
				}
				else
				{
					tf = toReturn.get(docID);
				}
				
				tf.put(term, frequency);
				
			}
			
			
		}
		
		
		return toReturn;
		
	}
	
	
	//Input: finalIndex --- > AdjustedIndex of term, docID, tf ---> DocumentTF (docID, term, TF)
	//Output: document vector (docID, term, TF) that contains all words in the vocabulary
	public static TreeMap<Integer, TreeMap<String, Double>> generateDocumentVector(TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> finalIndex, int titleWeight, int headerWeight, int paragraphWeight)
	{
		TreeMap<Integer, TreeMap<String, Double>> toReturn = Retriever_Clusterer.generateDocumentTF(finalIndex, titleWeight, headerWeight, paragraphWeight);
		
		
		Iterator docVectorIt = toReturn.entrySet().iterator();
		Entry docVectorEntry = null;
		
		while (docVectorIt.hasNext())
		{
			docVectorEntry = (Entry) docVectorIt.next();
			int docID = (Integer) docVectorEntry.getKey();
			
			//get the term frequency for that document
			TreeMap<String, Double> TF = (TreeMap<String, Double>) docVectorEntry.getValue();
			
			//loop over vocabulary words and add them to vector if not there
			Iterator wordsIt = ParserIndexer.vocabulary.iterator();
			while (wordsIt.hasNext())
			{
				String word = (String) wordsIt.next();
				
				if (!TF.containsKey(word))
					TF.put(word, 0.0);
				
			}
		}
		
		
		
		return toReturn;
		
	}
	
	
	public static TreeMap<Integer, TreeMap<String, Double>> generateLengthNormalizedDocumentVectors(TreeMap<Integer, TreeMap<String, Double>> documentVector)
	{
		//TreeMap<Integer, Double> vectorLengths = Retriever_Clusterer.computeVectorLength(documentVector);
		
		Retriever_Clusterer.computeVectorsLengths(documentVector);
		
		TreeMap<Integer, TreeMap<String, Double>> toReturn = documentVector;
		
		
		
		Iterator docVectorIt = toReturn.entrySet().iterator();
		Entry docVectorEntry = null;
		
		while (docVectorIt.hasNext())
		{
			docVectorEntry = (Entry) docVectorIt.next();
			int docID = (Integer) docVectorEntry.getKey();
			
			//get the term frequencies for that document
			TreeMap<String, Double> TF = (TreeMap<String, Double>) docVectorEntry.getValue();
			Iterator TFIt = TF.entrySet().iterator();
			Entry TFEntry = null;
			
			double length = (Double) vectorLengths.get(docID);
			
			while (TFIt.hasNext())
			{
				TFEntry = (Entry) TFIt.next();
				String word = (String) TFEntry.getKey();
				
				double oldFrequency = (Double) TFEntry.getValue();
				double newFrequency = oldFrequency / length;
				
				TF.put(word, newFrequency);
				
			}
			
		}
		
		
		return toReturn;
		
	}
	
	private static void computeVectorsLengths(TreeMap<Integer, TreeMap<String, Double>> documentVector)
	{
		//for each document
			//calculate vector length
		
		Retriever_Clusterer.vectorLengths.clear();
		
		TreeMap<Integer, Double> toReturn = Retriever_Clusterer.vectorLengths;//new TreeMap<Integer, Double>();
		
		
		Iterator docVectorIt = documentVector.entrySet().iterator();
		Entry docVectorEntry = null;
		
		while (docVectorIt.hasNext())
		{
			docVectorEntry = (Entry) docVectorIt.next();
			int docID = (Integer) docVectorEntry.getKey();
			
			//get the term frequencies for that document
			TreeMap<String, Double> TF = (TreeMap<String, Double>) docVectorEntry.getValue();
			
			Iterator frequenciesIt = TF.entrySet().iterator();
			Entry frequenciesEntry = null;
			
			double length = 0;
			while (frequenciesIt.hasNext())
			{
				frequenciesEntry = (Entry) frequenciesIt.next();
				//System.out.println(frequenciesEntry.getValue());
				double frequency = (Double) frequenciesEntry.getValue();
				length += (frequency * frequency);
			}
			
			length = Math.sqrt(length);
			
			toReturn.put(docID, length);
			
			
		}
		
		
		
		//return toReturn;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static TreeMap<String, Double> generateDocumentVectorForQuery(ArrayList<String> queryTerms)
	{
		TreeMap<String, Double> toReturn = new TreeMap<String, Double>();
		
		Iterator querywordsIterator = queryTerms.iterator();
		while (querywordsIterator.hasNext())
		{
			String word = (String) querywordsIterator.next();
			
			//System.out.println(word);
			
			if (!toReturn.containsKey(word))
			{
				toReturn.put(word, 1.0);
			}
			else
				toReturn.put(word, toReturn.get(word) + 1.0);
			
		}
		
		//get the term frequency for that document

		//loop over vocabulary words and add them to vector if not there
		Iterator wordsIt = ParserIndexer.vocabulary.iterator();
		while (wordsIt.hasNext())
		{
			String word = (String) wordsIt.next();

			if (!toReturn.containsKey(word))
				toReturn.put(word, 0.0);

		}
		
		return toReturn;
		
	}
	
	
	//Generate length Normalized Document Vector for the query to be able to calculate the Euclidean Distance with Centroid
	public static TreeMap<String, Double> generateLengthNormalizedDocumentVectorForQuery(TreeMap<String, Double> documentVector)
	{
		//TreeMap<Integer, Double> vectorLengths = Retriever_Clusterer.computeVectorLength(documentVector);
		

		TreeMap<String, Double> toReturn = documentVector;

		Iterator TFIt = toReturn.entrySet().iterator();
		Entry TFEntry = null;
		
		double length = Retriever_Clusterer.computeVectorLength(documentVector);
		if (length !=0)
		{
			while (TFIt.hasNext())
			{
				TFEntry = (Entry) TFIt.next();
				String word = (String) TFEntry.getKey();

				double oldFrequency = (Double) TFEntry.getValue();
				double newFrequency = oldFrequency / length;

				toReturn.put(word, newFrequency);
			}
		}
		else
			System.err.println("None of the query words exist in the vocabulary\nLengtNormalizedVector --> NaN");
		return toReturn;
		
	}
	
	
	
	
	
	
	
	private static double computeVectorLength(TreeMap<String, Double> documentVector)
	{
		//for each document
			//calculate vector length
		
		
		//get the frequencies for that documen
		Iterator frequenciesIt = documentVector.entrySet().iterator();
		Entry frequenciesEntry = null;
		
		double length = 0;
		while (frequenciesIt.hasNext())
		{
			frequenciesEntry = (Entry) frequenciesIt.next();
			double frequency = (Double) frequenciesEntry.getValue();
			length += (frequency * frequency);
		}
		
		length = Math.sqrt(length);
		
		return length;
		
	}
	
	
	//For the query, insert all vocab words into query with a frequency of 0
	public static double cosSimilarityBetweenNormalizedDocs(TreeMap<String, Double> doc1, TreeMap<String, Double> doc2)
	{
		//TreeMap<String, Double> vector1 = doc1.getValue();
		//TreeMap<String, Double> vector2 = doc2.getValue();
		
		Iterator i1 = doc1.entrySet().iterator();
		Iterator i2 = doc2.entrySet().iterator();
		
		Entry i1Entry = null;
		Entry i2Entry = null;
		
		double similarity = 0;
		
		while ((i1.hasNext()) && (i2.hasNext()))
		{
			i1Entry = (Entry) i1.next();
			i2Entry = (Entry) i2.next();
			
			String term1 = (String) i1Entry.getKey();
			String term2 = (String) i2Entry.getKey();
			
			double doc1F = (Double) i1Entry.getValue();
			double doc2F = (Double) i2Entry.getValue();
			
			similarity += doc1F * doc2F;
		}
		
		return similarity;
	}
	
	
	//Euclidean Distance between normalized or unnormalized doc-doc or query-doc that contain all words in the vocabulary
	//For the document, pass in a vector that contains all words in the vocabulary
	//For the query, insert all vocab words into query with a frequency of 0
	public static double euclideanDistance(TreeMap<String, Double> doc1, TreeMap<String, Double> doc2)
	{
		//TreeMap<String, Double> vector1 = doc1.getValue();
		//TreeMap<String, Double> vector2 = doc2.getValue();
		
		Iterator i1 = doc1.entrySet().iterator();
		Iterator i2 = doc2.entrySet().iterator();
		
		Entry i1Entry = null;
		Entry i2Entry = null;
		
		double similarity = 0;
		
		TreeMap<String, Double> temporaryVector = new TreeMap<String, Double>();
		
		while ((i1.hasNext()) && (i2.hasNext()))
		{
			i1Entry = (Entry) i1.next();
			i2Entry = (Entry) i2.next();
			
			String term1 = (String) i1Entry.getKey();
			String term2 = (String) i2Entry.getKey();
			
			double doc1F = (Double) i1Entry.getValue();
			double doc2F = (Double) i2Entry.getValue();
			
			double newValue = doc1F - doc2F;
			
			temporaryVector.put(term1, newValue);
			
		}
		
		double distance = Retriever_Clusterer.computeVectorLength(temporaryVector);
		
		return distance;
		
	}
	
	
	private static TreeMap<String, Double> computeCentroidForCluster(TreeMap<Integer, TreeMap<String, Double>> cluster)
	{
		TreeMap<String, Double> toReturn = new TreeMap<String, Double>();
		
		
		Iterator clusterIterator = cluster.entrySet().iterator();
		Entry clusterEntry = null;
		
		while (clusterIterator.hasNext())
		{
			clusterEntry = (Entry) clusterIterator.next();
			
			int docID = (Integer) clusterEntry.getKey();
			TreeMap<String, Double> document = (TreeMap<String, Double>) clusterEntry.getValue();
			
			Iterator docIterator = document.entrySet().iterator();
			
			Entry docEntry = null;
			
			while (docIterator.hasNext())
			{
				docEntry = (Entry) docIterator.next();
				
				String term = (String) docEntry.getKey();
				double frequency = (Double) docEntry.getValue();
				
				if (!toReturn.containsKey(term))
				{
					toReturn.put(term, frequency);
				}
				else
					toReturn.put(term, toReturn.get(term) + frequency);
				
			}
			
		}
		
		
		Iterator toReturnIterator = cluster.entrySet().iterator();
		Entry toReturnEntry = null;
		
		while (toReturnIterator.hasNext())
		{
			toReturnEntry = (Entry) toReturnIterator.next();
			
			int docID = (Integer) toReturnEntry.getKey();
			TreeMap<String, Double> document = (TreeMap<String, Double>) toReturnEntry.getValue();
			
			Iterator docIterator = document.entrySet().iterator();
			
			Entry docEntry = null;
			
			while (docIterator.hasNext())
			{
				docEntry = (Entry) docIterator.next();
				
				String term = (String) docEntry.getKey();
				double frequency = (Double) docEntry.getValue();
				
				toReturn.put(term, toReturn.get(term) / cluster.size());
				
			}
			
		}
		
		return toReturn;
	}
	
	
	
	
	
	
	//Input: finalIndex term, docID, section, TF ---> AdjustedIndex of term, docID, TF
	//Ouput: DocID, term, TF
	private static TreeMap<String, TreeMap<Integer, Double>> convertClusterToIndex(TreeMap<Integer, TreeMap<String, Double>> cluster)
	{
		//term, docID, frequency
		TreeMap<String, TreeMap<Integer, Double>> toReturn = new TreeMap<String, TreeMap<Integer, Double>>();

		Iterator clusterIterator = cluster.entrySet().iterator();
		Entry clusterEntry = null;

		while (clusterIterator.hasNext())
		{
			clusterEntry = (Entry) clusterIterator.next();


			int docID = (Integer) clusterEntry.getKey();
			TreeMap<String, Double> termFrequency = (TreeMap<String, Double>) clusterEntry.getValue();


			Iterator termFrequencyIterator = termFrequency.entrySet().iterator();
			Entry dfEntry = null;
			while (termFrequencyIterator.hasNext())
			{
				dfEntry = (Entry) termFrequencyIterator.next();

				String term = (String) dfEntry.getKey();
				double frequency = (Double) dfEntry.getValue();
				
				if (frequency != 0.0)
				{
					TreeMap<Integer, Double> docFrequency = null;

					if (!toReturn.containsKey(term))
					{
						docFrequency = new TreeMap<Integer, Double>();
						toReturn.put(term, docFrequency);
					}
					else
					{
						docFrequency = toReturn.get(term);
					}

					docFrequency.put(docID, frequency);
					
				}

			}
		}

		return toReturn;
		
	}
	
	
	public static ArrayList<Integer> printCluster(TreeMap<Integer, TreeMap<String, Double>> cluster)
	{
		ArrayList<Integer> IDs = new ArrayList<Integer>();
		
		Iterator clusterIterator = cluster.entrySet().iterator();
		Entry clusterEntry = null;

		while (clusterIterator.hasNext())
		{
			clusterEntry = (Entry) clusterIterator.next();


			int docID = (Integer) clusterEntry.getKey();
			IDs.add(docID);
			
		}

		return IDs;
	}
	
	
	public static void printClustersToFile()
	{
		//Print to file
		PrintWriter out = null;
		try
		{
			out = new PrintWriter("clusters.txt");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Iterator clustersIterator = clusters.entrySet().iterator();
		Entry clusterEntry = null; 
		
		int count = 1;
		while (clustersIterator.hasNext())
		{	
			out.println("Cluster " + count + ":\n");
			clusterEntry = (Entry) clustersIterator.next();
			TreeMap<Integer, TreeMap<String, Double>> cluster = (TreeMap<Integer, TreeMap<String, Double>>) clusterEntry.getValue();
			
			ArrayList<Integer> documentsInCluster = printCluster(cluster);
			out.println(retrieveDocumentsWithIDsForCluster(documentsInCluster));
			
			
			
			count++;
		}
		
		out.close();
	}
	
	
	public static String retrieveDocumentsWithIDsForCluster(ArrayList<Integer> idList)
	{
		String toReturn = "";
		
		Iterator docsIterator = idList.iterator();
		Entry docsEntry = null;
		
		while (docsIterator.hasNext())
		{
			int id = (Integer) docsIterator.next();
			
			//Find the relevant document file
			String link = dIDLink.get(id);
			
			toReturn += "\n" + link;
			
			toReturn += "\nDocument ID: " + id + "\n";
			
			//Get Body of html link
			//toReturn += "Title: " + ParserIndexer.getTitle(link) + "\n";
			//toReturn += "Headers: " + ParserIndexer.getHeader(link) + "\n";
			//toReturn += "Body: " +  ParserIndexer.getBody(link) + "\n\n";
			toReturn += ParserIndexer.getContent(link) + "\n\n";
			
		}
		
		toReturn += "\n\n";
		
		return toReturn;
		
	}
	
	
	//Passed as argument, the list of document ids that match our query. This method prints the documents to screen
		public static String retrieveDocumentsWithIDs(Map<Integer, Double> idList)
		{
			String toReturn = "";
			
			Iterator docsIterator = idList.entrySet().iterator();
			Entry docsEntry = null;
			
			while (docsIterator.hasNext())
			{
				docsEntry = (Entry) docsIterator.next();
				
				int id = (Integer) docsEntry.getKey();
				
				//Find the relevant document file
				String link = dIDLink.get(id);
				
				toReturn += "\n" + link;
				
				toReturn += "\nDocument ID: " + id + "\tScore:" + (Double) docsEntry.getValue() + ":\n";
				
				//Get Body of html link
				//toReturn += "Title: " + ParserIndexer.getTitle(link) + "\n";
				//toReturn += "Headers: " + ParserIndexer.getHeader(link) + "\n";
				//toReturn += "Body: " +  ParserIndexer.getBody(link) + "\n\n";
				toReturn += ParserIndexer.getContent(link) + "\n\n";
				
			}
			
			toReturn += "\n\n";
			
			return toReturn;
			
		}
		
		
}


















//if (minCentroidScore == 9999)
		//System.err.println("NaN");

	//System.out.println(clusters.get(1));
	//System.out.println(clusters.get(2));
	//System.out.println(clusters.get(3));
	//System.out.println(clusters.get(4));
	//System.out.println(clusters.get(5));

		
		
		//System.gc();
		
		
		
		
		
		//System.out.println("\nClusters: ");
		//System.out.println(clusters);
		//System.out.println("\n");
		
		//System.out.println("J is: " + j);
		//System.out.println("Cluster J: ");
		//System.out.println(clusterJ);
		
		
		//Convert Documents back into a term, docID, frequency    TreeMap
		//System.out.println(minCentroidScore);
		
		/*
		if (clusterJ == null)
			System.out.println("Cluster is Null");
		else
			System.out.println("Cluster is not Null");
		*/



/*
//Print Cluster
Iterator i1 = clusterJ.entrySet().iterator();
Entry i1Entry = null;

PrintWriter out = null;
try
{
	out = new PrintWriter("clusterDocs.txt");
}
catch (FileNotFoundException e)
{
	// TODO Auto-generated catch block
	e.printStackTrace();
}

while (i1.hasNext())
{
	i1Entry = (Entry) i1.next();

	out.println(i1Entry.getKey());
	out.println(i1Entry.getValue());
}

out.close();
*/


/*
//Print Cluster
//Print to file
Iterator i1 = clusteredToIndexed.entrySet().iterator();
Entry i1Entry = null;

//int postings = 0;
PrintWriter out = null;

try
{
	out = new PrintWriter("clusterDocs.txt");
}
catch (FileNotFoundException e)
{
	// TODO Auto-generated catch block
	e.printStackTrace();
}

while (i1.hasNext())
{
	i1Entry = (Entry) i1.next();

	out.println(i1Entry.getKey());
	out.println(i1Entry.getValue());
}

out.close();
*/



























//Check Centroids calculation

/*
if (count == 1)
{
	if (j == 5)
	{
		System.out.println(j);
		System.out.println(centroidJ);
		System.exit(-11);
	}
	
}
*/


//SO THE BUG IS THAT ONLY THE FIRST CLUSTER IS BEING FILLED
//AND IT IS ONLY FUCKING UP WHEN ITERATION = 2 or more.
//cluster is being added to cluster

/*
 * 
System.out.println("j is: " + j);

if (count == 1)
{
	if (j == 2)
	{
		//2,3,4,5 not working
		System.out.println("Printing Clusters.get(2)");
		System.out.println(clusters.get(j));
		
		//System.out.println("Printing Centroid J:" );
		//System.out.println(centroidJ);
	
		System.exit(-1);
	}
	
}

*/