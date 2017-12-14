package Crawler;
// UrlHarvestingCrawler.java

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import websphinx.Crawler;
import websphinx.Link;
import websphinx.Page;

public class UrlHarvestingCrawler extends CustomCrawler {

	private static final long serialVersionUID = 9015164947202781853L;
	private static final String URL_PATTERN = ".html";
	
	private static TreeMap<String, Integer> pages = new TreeMap<String, Integer>();
	private static int count = 0;
	private static int numberOfDocs = 0;
	
	private PrintWriter output;

	protected void init() throws Exception
	{
		output = new PrintWriter(new OutputStreamWriter(new FileOutputStream("urls-from-public-site.txt")), true);
	}

	protected void destroy() {
		output.flush();
		output.close();
	}

	@Override
	protected void doVisit(Page page)
	{
		URL url = page.getURL();
		//count++;
		
		if (this.count >= this.numberOfDocs)
		{
			System.out.println("Stop");
			this.stop();
			return;
		}
		
		String document = url.toString();
		
		if ((document.endsWith(".html")) && (!document.contains(".pdf")) && (!document.contains(".img")) && (!document.contains(".png")) && (!document.contains(".jpg")) && (!document.contains(".jpeg")) &&
				(!document.contains(".gif")) && (!document.contains(".txt")))
		{
			document = document.substring(0, document.indexOf(".html") + 5);
			
			if (!pages.containsKey(document))
			{
				pages.put(document, ++count);
				System.out.println(document);
				output.println(document);
			}
			
		}

	}

	@Override
	public boolean shouldVisit(Link link)
	{
		if (this.count >= this.numberOfDocs)
		{
			System.out.println("Stop");
			this.stop();
			return false;
		}
		
		URL linkUrl = link.getURL();
		
		String linkText = linkUrl.toString();
		if ((linkText.endsWith(".html")) && (!linkText.contains(".pdf")) && (!linkText.contains(".img")) && (!linkText.contains(".png")) && (!linkText.contains(".jpg")) && (!linkText.contains(".jpeg")) &&
				(!linkText.contains(".gif")) && (!linkText.contains(".txt")))
			return true;
		else
			return false;
		//return (linkUrl.toString().contains(URL_PATTERN));
		
		
	}

	/**
	 * This is how we are called.
	 * @param argv command line args.
	 */
	
	public TreeMap<Integer, String> processPage(String page, int depth, int numberOfDocs)
	{
		//reset variables
		
		this.numberOfDocs = numberOfDocs;
		
		//UrlHarvestingCrawler crawler = new UrlHarvestingCrawler();
		try
		{
			this.init();
			this.setRoot(new Link(new URL(page)));
			this.setDomain(Crawler.SERVER); // reset this since we are interested in siblings
			this.setMaxDepth(depth); // only crawl depth levels, default 4
			this.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			this.destroy();
		}
		
		TreeMap<Integer, String> toReturn = this.returnDocs();
		
		count = 0;
		numberOfDocs = 0;
		pages.clear();
		
		return toReturn;
		
	}
	
	public TreeMap<Integer, String> returnDocs()
	{
		
		Iterator documents_IDs = pages.entrySet().iterator();
		Entry documents_IDsEntry = null;
		
		TreeMap<Integer, String> temp = new TreeMap<Integer, String>();
		
		while (documents_IDs.hasNext())
		{
			documents_IDsEntry = (Entry) documents_IDs.next();
			
			String document = (String) documents_IDsEntry.getKey();
			int documentID = (Integer) documents_IDsEntry.getValue();
			
			
			if (!temp.containsKey(documentID))
			{
				temp.put(documentID, document);
			}
		}
		
		return temp;
		
	}
	
}