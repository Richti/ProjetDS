package application.clients;

import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import application.services.IBankAccount;
import application.services.Sorter;
import framework.registries.LocateGlobalRegistry;

public class ClientC {
	
	  //
	  // CONSTANTS
	  //
	  private static String SERVICE_NAME1 = "BankAccountA";
	  private static String SERVICE_NAME2 = "BankAccountB";
	  private static String SERVICE_NAME3 = "SimpleSorterB";


	  //
	  // MAIN
	  //
	  public static void main(String[] args) throws Exception {

		// locate the registry that runs on the remote object's server
	    Registry registry = LocateGlobalRegistry.getLocateGlobalRegistry();
	    System.out.println("client: retrieved registry");
	    
	    // retrieve the stub of the remote object by its name
	    Sorter sorter = (Sorter) registry.lookup(SERVICE_NAME3);
	    System.out.println("client: retrieved Sorter stub");

	    // call the remote object to perform sorts and reverse sorts
	    List<String> list = Arrays.asList("3", "5", "1", "2", "4");
	    System.out.println("client: sending " + list);

	    list = sorter.sort(list);
	    System.out.println("client: received " + list);

	    list = Arrays.asList("mars", "saturne", "neptune", "jupiter");
	    System.out.println("client: sending " + list);

	    list = sorter.reverseSort(list);
	    System.out.println("client: received " + list);
	    
	    
	    // Testing BankAccount service
	    IBankAccount bank1 = (IBankAccount) registry.lookup(SERVICE_NAME1);
	    System.out.println("client: retrieved IBankAccount stub");
	    
		bank1.deposit(5);
		System.out.println(bank1.getBalance() +  " on Account A");
		bank1.withdraw(15);                   
		System.out.println(bank1.getBalance() +  " on Account A");
		bank1.deposit(12);                    
		System.out.println(bank1.getBalance() +  " on Account A");
		bank1.withdraw(10);                  
		System.out.println(bank1.getBalance() +  " on Account A");
		
	    IBankAccount bank2 = (IBankAccount) registry.lookup(SERVICE_NAME2);
	    System.out.println("client: retrieved IBankAccount stub");
	    
	    bank2.deposit(5);
		System.out.println(bank2.getBalance() +  " on Account B");
		bank2.withdraw(15);
		System.out.println(bank2.getBalance() +  " on Account B");
		bank2.deposit(12);
		System.out.println(bank2.getBalance() +  " on Account B");
		bank2.withdraw(10);
		System.out.println(bank2.getBalance() +  " on Account B");


	    // main terminates here
	    System.out.println("client: exiting");

	  }
}
