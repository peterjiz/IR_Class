import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


public class Project
{
	private static void clearConsole()
	{
	    try
	    {
	        String os = System.getProperty("os.name");

	        if (os.contains("Windows"))
	        {
	            Runtime.getRuntime().exec("cls");
	        }
	        else
	        {
	            Runtime.getRuntime().exec("clear");
	        }
	    }
	    catch (Exception exception)
	    {
	        //  Handle exception.
	    }
	}
	
	public static void main(String[] args)
	{
		
		Spimi.SPIMIalgo(Runtime.getRuntime().freeMemory());
		
		//System.out.println("Enter your query type: AND or OR");
		Scanner scan = new Scanner(System.in);
		//String type = scan.next();
		System.out.println("Enter your query: (without the operators) ");

		//scan = new Scanner(System.in);
		String query = scan.nextLine();
		
		//Gets the list of documents
		Map<Integer, Double> list = null; //new HashMap<Integer, Double>();
		//list = Spimi.processQuery(query, type);
		
		list = Spimi.processQueryTF(query);
		Spimi.retrieveDocumentsWithIDs(list, query);
		
		scan = new Scanner(System.in);
		scan.next();
				
		list = Spimi.processQueryBM25(query, 0, 0.75);
		Spimi.retrieveDocumentsWithIDs(list, query);
		
		scan = new Scanner(System.in);
		scan.next();
				
		list = Spimi.processQueryBM25(query, 1.2, 0.75);
		Spimi.retrieveDocumentsWithIDs(list, query);
		
		scan = new Scanner(System.in);
		scan.next();
				
		list = Spimi.processQueryBM25(query, 50, 0.75);
		Spimi.retrieveDocumentsWithIDs(list, query);
		
		//scan = new Scanner(System.in);
		//scan.next();
		
		//Passes the list of documents and retrieves the matching documents
		
	}
	
}
