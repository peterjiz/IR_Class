package UI;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ParserIndexer.ParserIndexer;
import Retriever_Clusterer.Retriever_Clusterer;
import Crawler.FileManager;

public class Query
{
	//public static TreeMap<Integer, String> dIDLink = null;
	
	public static void main(String[] args)
	{
		//System.out.println("Calling the ParserIndexer on that fi");
		
		//System.out.println("Loading the UI will take a while ... Sit back ... It should only take 3-4 mins");
		
		
		FileManager fm = new FileManager();
		
		//Loading DocID-Links
		System.out.println("Loading DocID-Links from file");
		File docs1000 = new File("1000 docs saved links.blob");
		Retriever_Clusterer.dIDLink = fm.loadFromFiledocumentID_Link(docs1000);
		
		
		System.out.println("Loading Index from File");
		docs1000 = new File("1000 docs INDEX.blob");
		Retriever_Clusterer.documents = fm.loadIndexFromFile(docs1000);
		
		System.out.println("Loading Variables");
		docs1000 = new File("1000 docs Vocabulary.blob");
		ParserIndexer.vocabulary = fm.loadVocabularyFromFile(docs1000);
		docs1000 = new File("1000 docs DocsTokensAmt.blob");
		ParserIndexer.setDocs_TokensAmt(fm.loadDocsTokensAmtFromFile(docs1000));
		docs1000 = new File("1000 docs TotalDocs.blob");
		ParserIndexer.setTotalDocs(fm.loadTotalDocsFromFile(docs1000));
		docs1000 = new File("1000 docs AvgDocsTokens.blob");
		ParserIndexer.setAvgDocsTokens(fm.loadAvgDocsTokens(docs1000));
		
		
		
		//System.out.println(Retriever_Clusterer.dIDLink);
		//ParserIndexer.printMap(Retriever_Clusterer.documents);

		
		// Calls the Main Frame and sets the dimensions of the window
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
			JFrame frame = new MainFrame("Search Engine");
			frame.setSize(1000,800);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			
			}
		});
	}
}