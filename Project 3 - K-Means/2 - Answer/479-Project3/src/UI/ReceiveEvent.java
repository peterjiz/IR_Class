package UI;

import java.io.File;
import java.util.EventObject;
import java.util.Map;
import java.util.TreeMap;

import ParserIndexer.ParserIndexer;
import Retriever_Clusterer.Retriever_Clusterer;

public class ReceiveEvent extends EventObject {
	
	private static final long serialVersionUID = -717625138112228665L;
	
	private String text;
	
	/**
	 * Receives text from source
	 * @param source
	 * @param text
	 */
	public ReceiveEvent(Object source, String text, String KSetting, String iterationsSetting, String okapiK, String okapiB, String titleWeight, String headerWeight, String paragraphWeight, String top)
	{
		super(source);
		
		
		Map<Integer, Double> list = null;
		String output = "";
		
		//System.out.println("HERE");
		
		if ( (!text.equals("")) && (!KSetting.equals("")) && (!iterationsSetting.equals("")) && (!okapiK.equals("")) && (!okapiB.equals("")) && (!titleWeight.equals("")) && (!headerWeight.equals("")) && (!paragraphWeight.equals("")) && (!top.equals("")) )
		{	//Get the list of top scoring documents;
			
			int K = Integer.parseInt(KSetting);
			int iterations = Integer.parseInt(iterationsSetting);
			double k = Double.parseDouble(okapiK);
			double b = Double.parseDouble(okapiB);
			
			int t = Integer.parseInt(titleWeight);
			int h = Integer.parseInt(headerWeight);
			int p = Integer.parseInt(paragraphWeight);
			
			int topWanted = Integer.parseInt(top);
			
			//System.out.println("HERE");
			System.out.println();
			//list = r.processQueryBM25F(text, 3, 10, documents, 5, 5, 3, 1, 1.2, 0.75);
			Retriever_Clusterer r = new Retriever_Clusterer();
			list = r.processQueryBM25F(text, K, iterations, Retriever_Clusterer.documents, topWanted, t, h, p, k, b);
			
			//System.out.println("HERE");
			
			//Performs calculations and returns output
			output += "Searching for the best match for: \"" + text + " \"\n";
			
			//System.out.println(list);
			
			if (list != null)
				output += Retriever_Clusterer.retrieveDocumentsWithIDs(list);
			
			
			// Call method that returns a string Eg public String outputResults(String text)
			// text = outputResults(String text)
		}
		System.gc();
		this.text= output;
	}
	
	public String getText()
	{
		return text;
	}
	
}
