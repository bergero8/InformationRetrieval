import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class InvertedIndexTable {

	ArrayList<String> termsArray = new ArrayList<String>();
	ArrayList<TermTriplet> treeMap = new ArrayList<TermTriplet>(); // makeshift treemap
	ArrayList<String> stopArray = new ArrayList<String>();
	
	/**
	 * 
	 * @param fileNameString
	 * @throws IOException 
	 */
	protected void buildPostings(String fileNameString) throws IOException{
	
		File inputFile = new File(fileNameString);
		Scanner key = null;
		File stopWords = new File("./src/stoplist1.txt");
		Scanner stopKey = null;
		
		try {
			stopKey = new Scanner(stopWords);
			key = new Scanner(inputFile); //open spec file for reading
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(stopKey.hasNext()){
			stopArray.add(stopKey.nextLine());
		}
		stopKey.close();
		
		while(key.hasNext()){
			termsArray.add(key.nextLine()); //add all file names from spec file to array
		}
		key.close();
			
		HashMap<String, Integer> map = new HashMap<>(); //using this to count occurrences. I will then add to array so as to have duplicate keys
		boolean isStopWord = false;
		
		for(int j = 0; j < termsArray.size(); ++j){ //loop to process each of files in array
			inputFile = new File("./src/" + termsArray.get(j));
			int docId = j + 1; // create unique id based up index location in array
			Scanner key2 = null;
			
			try {
				key2 = new Scanner(inputFile); //open file from array element
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while(key2.hasNext()){ //add all terms from file to map, then add entries from map to arraylist so we can have duplicate terms
				   String word = key2.next().toLowerCase();
				   word = word.replaceAll("[.,:;()?!\" \t\n\r\']+", ""); // delimiter
				 
				   for(int i = 0; i < stopArray.size(); ++i){
					   if(word.equals(stopArray.get(i))){
						   isStopWord = true;
						}
					}
				   
				   if(!isStopWord){
					   if (map.containsKey(word)) {
				            map.put(word, map.get(word) + 1); // revise occurrences
				        } else {
				            map.put(word, 1);
				        }
				   }
				   
				   isStopWord = false;
			}	
			
			 for(@SuppressWarnings("rawtypes") Map.Entry entry: map.entrySet()){
				treeMap.add(new TermTriplet((String)entry.getKey(), docId, (int)entry.getValue())); // add "key value" pair to makeshift treemap
			 }
			
			 map = new HashMap<>(); // need to re-set hashmap to get accurate frequency count from distinct text files
			
			 /*
			  *  Sort array alphabetically
			  */
			 Collections.sort(treeMap, new Comparator<TermTriplet>(){

			   public int compare(TermTriplet o1, TermTriplet o2)
			   {
			      return o1.name.compareTo(o2.name);
			   }
			 });
			 
			 /*
			  * Add sorted treeMap list to output file
			  */
			 try {
				@SuppressWarnings("resource")
				PrintWriter outputFile = new PrintWriter("./src/specFile.postings");
				//logFile = new FileWriter("./src/demo.logfile", true);
				//logFile.printf( "%-15s %5s %10s%n%n", "Term", "docID", "Frequency");			
				outputFile.printf( "%-15s %5s %10s%n%n", "Term", "docID", "Frequency");
				for(TermTriplet x: treeMap){
					if(!Character.isDigit(x.name.charAt(0))){ // don't add terms starting with numbers
						outputFile.printf( "%-15s %3s %10s%n", x.name, x.id, x.freq); 
					}
				 }
				outputFile.close();
			 } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }	 
		}			
	}
	
	/**
	 * 
	 * @param postingFile
	 */
	protected void buildDictionary(String postingFile){
		File inputFile = new File(postingFile);
		Scanner key3 = null;
		ArrayList<TermTriplet> dictionaryMap = new ArrayList<>();// name, doc count, freq count
		
		try{
			key3 = new Scanner(inputFile);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
		
		int totalFreqCount = 0;
		int totalDocCount = 0;
		String last = null;
		String current = null;
		
		while(key3.hasNext()){
			String word = key3.nextLine();
			
			String textStr[] = word.split("\\s+");
			current = textStr[0];
			
			if(textStr.length == 3 && Character.isDigit(textStr[1].charAt(0))){ // This is necessary due to my formatting of column headings in the passed in argument (File)
				
				if(current.equals(last)){
					totalFreqCount += Integer.parseInt(textStr[2]);
					++totalDocCount;
					dictionaryMap.remove(dictionaryMap.size()-1); // useful for adding/removing from dictionaryMap array kind of a hack to create a Set (non-repeating)
					dictionaryMap.add(new TermTriplet(current, totalDocCount, totalFreqCount));
					
				}
				else{
					if(totalFreqCount == 0) // this is necessary to get accurate frequency count
						totalFreqCount += Integer.parseInt(textStr[2]);
					else
						totalFreqCount = Integer.parseInt(textStr[2]);
					
					totalDocCount = 1;
					last = current;
					dictionaryMap.add(new TermTriplet(current, totalDocCount, totalFreqCount));
				}
				
			}
		}
		/*
		 * Create actual output file from dictionaryMap array
		 */
		 try {
				@SuppressWarnings("resource")
				PrintWriter dictionaryOutputFile = new PrintWriter("./src/specFile.dict");
				dictionaryOutputFile.printf( "%-15s %5s %10s%n%n", "Term", "docOccurences", "totalFrequency");
				for(TermTriplet x: dictionaryMap){
					dictionaryOutputFile.printf( "%-15s %3s %10s%n", x.name, x.id, x.freq); 
					}
				dictionaryOutputFile.close();
			 } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }		 
	}
	
	/**
	 * 
	 * @param term
	 * @param specFile
	 */
	protected void query1(String term, String specFile){
		File queryFile = new File(specFile);
		Scanner key4 = null;
		ArrayList<TermTriplet> queryResults = new ArrayList<>();
		try {
			key4 = new Scanner(queryFile);
			while(key4.hasNext()){
				String word = key4.nextLine();
				String wordStr[] = word.split("\\s+");
				
				if(wordStr[0].equals(term)){
					queryResults.add(new TermTriplet(wordStr[0], Integer.parseInt(wordStr[1]), Integer.parseInt(wordStr[2])));
				}			
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Collections.sort(queryResults, new Comparator<TermTriplet>(){

		   public int compare(TermTriplet o1, TermTriplet o2)
		   {
		      return Integer.compare((o2.freq), (o1.freq)); // descending order
		   }
		});
		 
		try {
			PrintWriter queryOutputFile = new PrintWriter("./src/" + term + ".specFile");
			queryOutputFile.println("Term searched for: " + term);
			queryOutputFile.printf( "%3s %10s%n", "DocId", "Frequency(Descending Order");
			for(TermTriplet x: queryResults){
				queryOutputFile.printf( "%3s %10s%n", x.id, x.freq); 
			}	
			queryOutputFile.close();
					
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(queryResults.size() == 0)
			System.out.println("Your term was not found!");
	}
	
	protected void demo(){
		@SuppressWarnings("resource")
		Scanner key5 = new Scanner(System.in);
		System.out.println("Enter spc file: ");
		String specFile= key5.next();
		try {
			buildPostings(specFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buildDictionary("./src/specFile.postings");
		System.out.println("Enter term to search for or type quit to end: ");
		String userCmd = key5.next();
		while(!userCmd.toLowerCase().equals("quit")){
			query1(userCmd, "./src/specFile.postings");
			
			System.out.println("Enter term to search for or type quit to end: ");
			userCmd = key5.next();
		}
		System.out.println("Thank you, goodbye! ");
	}
			
	protected void print(){
		/*
		 * TEST
		 */
		System.out.printf( "%-15s %5s %10s%n%n", "Term", "docID", "Frequency");
		for(int i=0; i<treeMap.size(); i++){
		  
		     System.out.printf( "%-15s %3s %10s%n", treeMap.get(i).name, treeMap.get(i).id, treeMap.get(i).freq);
		   
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InvertedIndexTable y = new InvertedIndexTable();
		y.demo();
		
	}
	
	class TermTriplet {
		String name = null;
		int id = 0;
		int freq = 0;
		
		TermTriplet(String x, int y, int z){
			name = x;
			id = y;
			freq = z;
		}
	}
      

}
