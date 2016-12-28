package clients;

import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import registries.LocateGlobalRegistry;
import services.IBankAccount;
import services.Sorter;


public class ClientA {

  //
  // CONSTANTS
  //
  private static String SERVICE_NAME1 = "SimpleSorterA";
  private static String SERVICE_NAME2 = "BankAccountA";


  //
  // MAIN
  //
  public static void main(String[] args) throws Exception {

    // locate the registry that runs on the remote object's server
    Registry registry = LocateGlobalRegistry.getLocateGlobalRegistry();
    System.out.println("client: retrieved registry");

    // retrieve the stub of the remote object by its name
    Sorter sorter = (Sorter) registry.lookup(SERVICE_NAME1);
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
    
    
    // Testing the BankAccount service (statefull service)
    IBankAccount bank = (IBankAccount) registry.lookup(SERVICE_NAME2);
    System.out.println("client: retrieved IBankAccount stub");
    
	bank.deposit(10);
	System.out.println(bank.getBalance());
	bank.withdraw(15);
	System.out.println(bank.getBalance());
	bank.deposit(10);
	System.out.println(bank.getBalance());
	bank.withdraw(10);
	System.out.println(bank.getBalance());


    // main terminates here
    System.out.println("client: exiting");

  }

}
