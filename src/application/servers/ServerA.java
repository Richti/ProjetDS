package application.servers;

import java.net.InetAddress;
import java.rmi.server.UnicastRemoteObject;

import application.services.BankAccount;
import application.services.IBankAccount;
import application.services.SimpleSorter;
import application.services.Sorter;
import framework.registries.LocateGlobalRegistry;

/**
 * Server program.
 *
 * Note: After the main method exits, the JVM will still run. This is because
 * the skeleton implements a non-daemon listening thread, which waits for
 * incoming requests forever.
 *
 */
public class ServerA {

  //
  // CONSTANTS
  //
  private static final String SERVICE_NAME1 = "SimpleSorterA_ServerA";
  private static final String SERVICE_NAME2 = "SimpleSorterB_ServerA";
  
  private static final String SERVICE_NAME3 = "BankAccountA_ServerA";
  private static final String SERVICE_NAME4 = "BankAccountB_ServerA";

  //
  // MAIN
  //
  public static void main(String[] args) throws Exception {

    // check the name of the local machine (two methods)
    System.out.println("ServerA: running on host " + InetAddress.getLocalHost());
    System.out.println("ServerA: hostname property " + System.getProperty("java.rmi.server.hostname"));

    // instanciate the remote object
    Sorter sorterA = new SimpleSorter();
    Sorter sorterB = new SimpleSorter();
    System.out.println("ServerA: instanciated SimpleSorter A and B.");

    // create a skeleton and a stub for that remote object
    Sorter stubA = (Sorter) UnicastRemoteObject.exportObject(sorterA, 0);
    Sorter stubB = (Sorter) UnicastRemoteObject.exportObject(sorterB, 0);
    System.out.println("ServerA: generated skeletons and stubs");

    // register the remote object's stub in the registry
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME1, stubA);
    LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME2, stubB);
    
    // instanciate the remote object
    IBankAccount bankA = new BankAccount();
    IBankAccount bankB = new BankAccount();
    System.out.println("ServerA: instanciated BankAccount A and B.");

    // create a skeleton and a stub for that remote object
    IBankAccount stubBankA = (IBankAccount) UnicastRemoteObject.exportObject(bankA, 0);
    IBankAccount stubBankB = (IBankAccount) UnicastRemoteObject.exportObject(bankB, 0);
    System.out.println("ServerA: generated skeleton and stub");

    // register the remote object's stub in the registry
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME3, stubBankA); 
	LocateGlobalRegistry.getLocateGlobalRegistry().bind(SERVICE_NAME4, stubBankB);    


    // main terminates here, but the JVM still runs because of the skeleton
    System.out.println("ServerA: ready");

  }

}
