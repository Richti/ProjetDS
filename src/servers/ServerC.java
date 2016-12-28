package servers;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import registries.LocateGlobalRegistry;
import services.BankAccount;
import services.IBankAccount;
import services.SimpleSorter;
import services.Sorter;

/**
 * Server program.
 *
 * Note: After the main method exits, the JVM will still run. This is because
 * the skeleton implements a non-daemon listening thread, which waits for
 * incoming requests forever.
 *
 */
public class ServerC {

  //
  // CONSTANTS
  //
  private static final String SERVICE_NAME1 = "SimpleSorterA_ServerC";
  private static final String SERVICE_NAME2 = "SimpleSorterB_ServerC";
  
  private static final String SERVICE_NAME3 = "BankAccountA_ServerC";
  private static final String SERVICE_NAME4 = "BankAccountB_ServerC";

  //
  // MAIN
  //
  public static void main(String[] args) throws Exception {

    // check the name of the local machine (two methods)
    System.out.println("ServerC: running on host " + InetAddress.getLocalHost());
    System.out.println("ServerC: hostname property " + System.getProperty("java.rmi.server.hostname"));

    // instanciate the remote object
    Sorter sorterA = new SimpleSorter(SERVICE_NAME1);
    Sorter sorterB = new SimpleSorter(SERVICE_NAME2);
    System.out.println("ServerC: instanciated SimpleSorter");

    // create a skeleton and a stub for that remote object
    Sorter stubA = (Sorter) UnicastRemoteObject.exportObject(sorterA, 0);
    Sorter stubB = (Sorter) UnicastRemoteObject.exportObject(sorterB, 0);
    System.out.println("ServerC: generated skeleton and stub");

    // register the remote object's stub in the registry
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME1, stubA);
    LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME2, stubB);
    
    listService();
    testGlobalRegistry(stubA);
    
    
 // instanciate the remote object
    IBankAccount bankA = new BankAccount(SERVICE_NAME3);
    IBankAccount bankB = new BankAccount(SERVICE_NAME4);
    System.out.println("ServerA: instanciated BankAccount A and B.");

    // create a skeleton and a stub for that remote object
    IBankAccount stubBankA = (IBankAccount) UnicastRemoteObject.exportObject(bankA, 0);
    IBankAccount stubBankB = (IBankAccount) UnicastRemoteObject.exportObject(bankB, 0);
    System.out.println("ServerA: generated skeleton and stub");

    // register the remote object's stub in the registry
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME3, stubBankA); 
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME4, stubBankB); 
    // main terminates here, but the JVM still runs because of the skeleton
    System.out.println("ServerC: ready");

  }
  
  private static void testGlobalRegistry(Sorter stub) throws AccessException, RemoteException, AlreadyBoundException, NotBoundException{
	// unbind Test
	LocateGlobalRegistry.getLocateGlobalRegistry().unbind(SERVICE_NAME1);
	System.out.println("ServerC: unbind done");
	
	listService();
	
	// bind it
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME1, stub);
	System.out.println("ServerC: bind done");

  }
  
  public static void listService() throws AccessException, RemoteException, NotBoundException{
		String[] services = LocateGlobalRegistry.getLocateGlobalRegistry().list();
		System.out.println("List : ");
		for(String service : services){
			System.out.println(" - " + service);
		}	
		System.out.println("End");	  
  }

}
