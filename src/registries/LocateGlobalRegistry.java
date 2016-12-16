package registries;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class LocateGlobalRegistry {
	
	public static final String GLOBAL_REGISTRY = "GlobalRegistry";
	public static final String LOCALHOST = "localhost";
	
	public static Registry getLocateGlobalRegistry(String host) throws RemoteException, NotBoundException {
		Registry r = LocateRegistry.getRegistry(host);
		return (Registry) r.lookup(GLOBAL_REGISTRY);
	}
	
	public static Registry getLocateGlobalRegistry() throws RemoteException, NotBoundException {
		return getLocateGlobalRegistry(LOCALHOST);
	}
	
	public static Registry createGlobalRegistry(int port, LoadBalancingType loadServices) throws RemoteException, AlreadyBoundException, NotBoundException {
		Registry r = LocateRegistry.createRegistry(port);
		Registry stub = (Registry) UnicastRemoteObject.exportObject(new GlobalRegistry(loadServices), 0);
		r.bind(GLOBAL_REGISTRY, stub);
		return stub;
	}
}
