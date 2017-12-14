
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import websphinx.Crawler;
//import gd.AllPairsAlgorithm;
import websphinx.Link;
import Crawler.FileManager;
import Crawler.UrlHarvestingCrawler;
import ParserIndexer.ParserIndexer;
import Retriever_Clusterer.Retriever_Clusterer;

public class Main
{
	
	public static void main(String[] args)
	{
		/*
		UrlHarvestingCrawler c = new UrlHarvestingCrawler();

		File docs1000 = new File("1000 docs saved links.blob");
		FileManager fm = new FileManager();
		fm.saveToFiledocumentID_Link(docs1000, c.processPage("http://www.concordia.ca", 4, 1000));
		
		docs1000 = new File("1000 docs INDEX.blob");
		TreeMap<String, TreeMap<Integer, TreeMap<String, Integer>>> documents = ParserIndexer.parseDocuments(docs1000);
		Retriever_Clusterer r = new Retriever_Clusterer();
		
		FileManager fm = new FileManager();
		
		System.out.println("Saving Variables");
		docs1000 = new File("1000 docs Vocabulary.blob");
		fm.saveVocabularyToFile(docs1000, ParserIndexer.vocabulary);
		
		docs1000 = new File("1000 docs DocsTokensAmt.blob");
		fm.saveDocsTokensAmtToFile(docs1000, ParserIndexer.getDocs_TokensAmt());
		
		docs1000 = new File("1000 docs TotalDocs.blob");
		fm.saveTotalDocsToFile(docs1000, ParserIndexer.getTotalDocs());

		docs1000 = new File("1000 docs AvgDocsTokens.blob");
		fm.saveAvgDocsTokensToFile(docs1000, ParserIndexer.getAvgDocsTokens());
		*/
		
		
	}
	
}
