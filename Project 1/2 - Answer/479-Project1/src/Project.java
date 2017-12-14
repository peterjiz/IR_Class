import java.util.ArrayList;
import java.util.Scanner;


public class Project
{
	public static void main(String[] args)
	{
		Spimi.SPIMIalgo(Runtime.getRuntime().freeMemory());
		
		System.out.println("Enter your query type: AND or OR");
		Scanner scan = new Scanner(System.in);
		String type = scan.next();
		System.out.println("Enter your query: (without the operators) ");

		scan = new Scanner(System.in);
		String query = scan.nextLine();
		
		//Gets the list of documents
		ArrayList<Integer> list = Spimi.processQuery(query, type);

		//Passes the list of documents and retrieves the matching documents
		Spimi.retrieveDocumentsWithIDs(list);
		
	}
	
}
