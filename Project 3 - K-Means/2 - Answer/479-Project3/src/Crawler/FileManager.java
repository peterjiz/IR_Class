package Crawler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;


public class FileManager
{
	public void saveToFiledocumentID_Link(File outFile, TreeMap<Integer, String> docID_Link)
	{		
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(docID_Link);
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
	}
	
	public TreeMap<Integer, String> loadFromFiledocumentID_Link(File inFile)
	{		
		//Load file 1 into Tree Map
		TreeMap<Integer, String> toReturn = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(inFile);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			toReturn = (TreeMap<Integer, String>) objectInStream.readObject();
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
		
		return toReturn;

	}
	
	
	
	public void saveIndexToFile(File outFile, TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> index)
	{		
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(index);
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
	}
	
	public TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> loadIndexFromFile(File inFile)
	{		
		//Load file 1 into Tree Map
		TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> toReturn = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(inFile);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			toReturn = (TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>>) objectInStream.readObject();
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
		
		return toReturn;

	}
	
	
	
	
	
	public void saveVocabularyToFile(File outFile, ArrayList<String> vocabulary)
	{		
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(vocabulary);
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
	}
	
	
	
	public ArrayList<String> loadVocabularyFromFile(File inFile)
	{		
		//Load file 1 into Tree Map
		ArrayList<String> toReturn = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(inFile);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			toReturn = (ArrayList<String>) objectInStream.readObject();
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
		
		return toReturn;

	}

	public TreeMap<Integer, Integer> loadDocsTokensAmtFromFile(File inFile)
	{
		//Load file 1 into Tree Map
		TreeMap<Integer, Integer> toReturn = null;

		try
		{
			FileInputStream fileInStream = new FileInputStream(inFile);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			toReturn = (TreeMap<Integer, Integer>) objectInStream.readObject();
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

		return toReturn;
	}

	public double loadTotalDocsFromFile(File inFile)
	{
		//Load file 1 into Tree Map
		double toReturn = 0;

		try
		{
			FileInputStream fileInStream = new FileInputStream(inFile);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			toReturn = (Double) objectInStream.readObject();
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

		return toReturn;
		
	}

	public double loadAvgDocsTokens(File inFile)
	{
		double toReturn = 0;

		try
		{
			FileInputStream fileInStream = new FileInputStream(inFile);
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			toReturn = (Double) objectInStream.readObject();
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

		return toReturn;
		
	}

	public void saveDocsTokensAmtToFile(File outFile, TreeMap<Integer, Integer> docs_TokensAmt)
	{
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(docs_TokensAmt);
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
		
	}

	public void saveTotalDocsToFile(File outFile, double totalDocs)
	{
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(totalDocs);
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
		
	}

	
	public void saveAvgDocsTokensToFile(File outFile, double avgDocsTokens)
	{
		try
		{
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(avgDocsTokens);
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
	}
	
	
	
	
	
	
}
