package servers;

import java.net.InetAddress;
import java.rmi.AccessException;
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
public class ServerB {

  //
  // CONSTANTS
  //
  private static final String SERVICE_NAME1 = "SimpleSorterA_ServerB";
  private static final String SERVICE_NAME2 = "BankAccountA_ServerB";


  //
  // MAIN
  //
  public static void main(String[] args) throws Exception {

    // check the name of the local machine (two methods)
    System.out.println("ServerB : running on host " + InetAddress.getLocalHost());
    System.out.println("ServerB : hostname property " + System.getProperty("java.rmi.server.hostname"));

    // instanciate the remote object
    Sorter sorterA = new SimpleSorter(SERVICE_NAME1);
    System.out.println("ServerB: instanciated SimpleSorter");

    // create a skeleton and a stub for that remote object
    Sorter stubA = (Sorter) UnicastRemoteObject.exportObject(sorterA, 0);
    System.out.println("ServerB: generated skeleton and stub");

    // register the remote object's stub in the registry
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME1, stubA);
    
    IBankAccount bankA = new BankAccount(SERVICE_NAME2);
    System.out.println("ServerB: instanciated SimpleSorter");

    // create a skeleton and a stub for that remote object
    IBankAccount bankStub = (IBankAccount) UnicastRemoteObject.exportObject(bankA, 0);
    System.out.println("ServerB: generated skeleton and stub");

    // register the remote object's stub in the registry
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME2, bankStub); 

    listService();
    
    // main terminates here, but the JVM still runs because of the skeleton
    System.out.println("ServerB: ready");

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
