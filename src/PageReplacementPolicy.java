import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;


public class PageReplacementPolicy {
	
	
	int replacementPolicy; //stores the choice of Replacement Policy
	int memorySize; //stores the memory size =total number of pages that can exist in memory at the same time
	String accessPattern; //name of file that has the access pattern of pages
	int pageFaultCounter=0; //Keeps a count of total page faults
	int totalAccess = 0; //keeps a count of total accesses in a file
	Scanner fScanner = null;
	ArrayList<Integer> pagesInMemory=new ArrayList<Integer>();//frames table containing page no
	List<List<Integer>> pageList; //Contains a list of pages in sequence (i.e. by time)
	List<Integer> removedList; //List of pages removed; for FIFO
	File f=null;
	BufferedWriter output = null;
	Stack<Integer> lruStack ;
	List<Integer> referencedList;
	
	
	PageReplacementPolicy(String args[]){
		try{
			replacementPolicy = Integer.parseInt(args[0]);
			memorySize = Integer.parseInt(args[1]);
			accessPattern = args[2];
			/*replacementPolicy = 2;
			memorySize = 3;
			accessPattern = "E:\\Eclipse workspace\\PageReplacementPolicy\\src\\in2.txt";*/
			pageList = new ArrayList<List<Integer>>();
			removedList = new ArrayList<Integer>();
			referencedList= new ArrayList<Integer>();
			lruStack=new Stack<Integer>();
			
			
			//Depending on choice of policy, start the algorithm appropriately
			RouteToSelectedPolicy();
				
		}
		catch (Exception ex){
			System.out.println("In-correct number of command line arguments.Program will terminate..");
			System.exit(1);
		}
	}
	
	
	private void RouteToSelectedPolicy(){
		if(replacementPolicy == 0){
			processFIFO();
		}
		else if(replacementPolicy == 1){
			processSecondChance();
		}
		else if(replacementPolicy == 2){
			processLRU();
		}
		else{
			System.out.println("Incorrect choice of Replacement Policy! Progam will exit!");
			System.exit(1);
		}
	}
	private void GetScanner(){
		try{
			FileReader fReader = new FileReader(accessPattern);
			fScanner = new Scanner(new BufferedReader(fReader));
		}
		catch (FileNotFoundException ex){
			System.out.println("File " + accessPattern + " not Found. Program will exit.");
			System.out.println("Execute again and provide proper file name");
			System.exit(0);
			
			
		}
		
	}
	private void BuildPageList(){
		GetScanner();
		
		//Find the total number of accesses
		while(fScanner.hasNextLine()){
			String line = fScanner.nextLine();
			List<Integer> list = new ArrayList<Integer>();
			StringTokenizer tokenizer = new StringTokenizer(line," ");
			
			while(tokenizer.hasMoreTokens()){
				int temp = Integer.parseInt(tokenizer.nextToken());
				list.add(temp);
			}
			
			if(list!=null && list.size() > 0){
				pageList.add(list);
			}
		
			totalAccess++; //Counter to update total accesses ; total lines in access file
		}
	}
	private void ExtractFileNameWithoutType(){
		String extract = accessPattern.substring(0,accessPattern.length()-3);
		if(replacementPolicy==0){
			f = new File(extract+"fifo");
		}
		else if(replacementPolicy == 1){
			f= new File(extract+"secondchance");
		}
		else if(replacementPolicy == 2){
			f = new File(extract + "lru");
		}
		try{
			output = new BufferedWriter(new FileWriter(f));
			
		}
		catch(IOException ex){
			System.out.println("File could not be written");
			System.exit(1);
		}
	}
	private void processFIFO(){
		BuildPageList();
		ExtractFileNameWithoutType();
		
		int pagesInMemoryCounter=0;
		boolean indiator=false;
		for (int j = 0; j < pageList.size(); j++) {
			List<Integer> lst = new ArrayList<Integer>();
			lst = pageList.get(j);
			
			if(lst != null && lst.size() > 0) {
				if(pagesInMemory.size() <= memorySize ){
					if(!pagesInMemory.contains(lst.get(0))){
						if(pagesInMemory.size() != memorySize){
							pagesInMemory.add(pagesInMemoryCounter, lst.get(0));
						}
						else{
							for (int k = 0; k <= j; k++) {
								for (int k2 = memorySize-1; k2 >=0 ; k2--) {
									if((pagesInMemory.get(k2)==pageList.get(k).get(0)) && (!removedList.contains(pagesInMemory.get(k2)))){
											
											pagesInMemory.remove(k2);
											removedList.add(pageList.get(k).get(0));
											pagesInMemory.add(k2, lst.get(0));
											indiator=true;
										
									}
									else if(j==pageList.size()-1 && (pagesInMemory.get(k2)==pageList.get(k).get(0))){
										int temp = removedList.get(0);
										if(pagesInMemory.contains(temp)){
											int tmpIndex = pagesInMemory.indexOf(temp);
											pagesInMemory.remove(pagesInMemory.indexOf(temp));
											pagesInMemory.add(tmpIndex,lst.get(0));
											indiator=true;
										}
									}
								}
								if(indiator==true){
									indiator=false;
									break;
								}
							
							}				
						}
						pageFaultCounter++;
						pagesInMemoryCounter++;
					}
				}
				
				processOutput(pagesInMemory);
			}
			
		}
		
		CalculatePercentageFaults();
	}
	private void CalculatePercentageFaults(){
		//convert pageFaultCounter and TotalAccess variables to double in order to calculate the percentage
		double pFault = pageFaultCounter;
		double ptotalAccess = totalAccess;
		
		double percentage = 0; 
		percentage=	pFault/ptotalAccess;
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		//System.out.println();
		try{
			output.newLine();
			output.write("Percentage of page faults: "+ Double.valueOf(twoDForm.format(percentage)));
			output.close();
		}
		catch (IOException ex){
			
		}
		//System.out.println("Percentage of page faults: "+ Double.valueOf(twoDForm.format(percentage)));
	
	}
	
	private void processOutput(ArrayList<Integer> pagesInMemory){
		if(pagesInMemory!=null && pagesInMemory.size() >0){
			for (int i = 0; i < pagesInMemory.size(); i++) {
				try{
					output.write(pagesInMemory.get(i)+" ");
					//System.out.print(pagesInMemory.get(i)+" ");
				}
				catch(IOException ex){
					System.out.println("File could not be written");
					System.exit(1);
				}
			}
			//System.out.println();
			try{
				output.newLine();
			}
			catch(IOException ex){
				
			}
		}
	}
	
	private void processLRU(){
		BuildPageList();
		ExtractFileNameWithoutType();
		int pagesInMemoryCounter=0;
		
		for (int j = 0; j < pageList.size(); j++) {
			List<Integer> lst = new ArrayList<Integer>();
			lst = pageList.get(j);
			if(lst != null && lst.size() > 0) {
				if(pagesInMemory.size() <= memorySize ){
					if(!pagesInMemory.contains(lst.get(0))){
						if(pagesInMemory.size() != memorySize){
							pagesInMemory.add(pagesInMemoryCounter, lst.get(0));
							if(lruStack.size() <= memorySize)
								lruStack.push(lst.get(0));						
						}
						else{
							int itemToBeRemoved= lruStack.get(0);
							lruStack.remove(0);
							lruStack.push(lst.get(0));
							if(pagesInMemory.contains(itemToBeRemoved)){
								int indexOfElementInList=pagesInMemory.indexOf(itemToBeRemoved);
								pagesInMemory.remove(indexOfElementInList);
								pagesInMemory.add(indexOfElementInList, lst.get(0));
							}
							
						}
						pageFaultCounter++;
						pagesInMemoryCounter++;
					}
					else{      //If pagesInMemory contains the element we bring it to the top of the stack and remove from the bottom
						int indexOfElementFound = lruStack.indexOf(lst.get(0));
						lruStack.remove(indexOfElementFound);
						lruStack.push(lst.get(0));
						
					}
				}
			}
			processOutput(pagesInMemory);
		}
		CalculatePercentageFaults();
	}
	
	private void processSecondChance(){
		BuildPageList();
		ExtractFileNameWithoutType();
		
		int pagesInMemoryCounter=0;
		
		boolean indiator=false;
		for (int j = 0; j < pageList.size(); j++) {
			List<Integer> lst = new ArrayList<Integer>();
			lst = pageList.get(j);
			int cntr=0;
			if(lst != null && lst.size() > 0) {
				if(pagesInMemory.size() <= memorySize ){
					if(!pagesInMemory.contains(lst.get(0))){
						
						if(pagesInMemory.size() != memorySize){
							pagesInMemory.add(pagesInMemoryCounter, lst.get(0));
						}
						else{
							for (int k2 = 0; k2 < memorySize ; k2++) {		
								for (int k = 0; k <= j; k++) {
								
									if((pagesInMemory.get(k2)==pageList.get(k).get(0)) && (!referencedList.contains(pagesInMemory.get(k2)))){
											int tr = pagesInMemory.get(k2+1);
											int intK2=0;
											int intK = 0;
											for (int i = 0; i <= j; i++) {
												if(pageList.get(i).get(0) == tr){
													intK2 = i;
												}
												if(pageList.get(i).get(0) == pagesInMemory.get(k2)){
													intK=i;
													
												}
											}
										if(!referencedList.contains(tr)){
											if(intK <= intK2){
												pagesInMemory.remove(k2);
												//removedList.add(pageList.get(k).get(0));
												pagesInMemory.add(k2, lst.get(0));
												indiator=true;
											}
											else{
												int ind = 0;
												if(pagesInMemory.contains(tr)){
													ind = pagesInMemory.indexOf(tr);
												}
												pagesInMemory.remove(ind);
												//removedList.add(pageList.get(k).get(0));
												pagesInMemory.add(ind, lst.get(0));
												indiator=true;
											}
											break;
										}
										
									}
									else if(j==pageList.size()-1 ){
											pagesInMemory.remove(0);
											pagesInMemory.add(0,lst.get(0));
											indiator=true;
											break;
										
									}
								}
								if(indiator==true){
									indiator=false;
									break;
								}
								
							}				
						}
						pageFaultCounter++;
						pagesInMemoryCounter++;
					}
					else{
						referencedList.add(pagesInMemory.get(0));
					}
				}
			processOutput(pagesInMemory);
			}
			
		}
		
		CalculatePercentageFaults();
	}
	
	public static void main(String[] args){
		new PageReplacementPolicy(args);
		
	}

}
