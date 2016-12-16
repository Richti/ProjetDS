package registries;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import services.Sorter;

public class GlobalRegistry implements Registry {
	
//	private Map<String,Remote> services = new HashMap<>();
	private Map<String, Map<String, Remote>> services = new HashMap<>();
	private LoadBalancingType loadBalancingType;
	
	public GlobalRegistry(LoadBalancingType loadBalancingType){
		this.loadBalancingType = loadBalancingType;
	}
	
	//Use to perform Round Robin implementation
	private Map<String, Integer> currentServiceIndex = new HashMap<>();

	private static final int REGISTRY_PORT = 1099;
	  
	public static synchronized void main(String[] args) throws Exception {
		
	    System.out.println("Global registry: running on host " + InetAddress.getLocalHost());
	    
	    // create the registry on the local machine, on the default port number
	    LocateGlobalRegistry.createGlobalRegistry(REGISTRY_PORT, LoadBalancingType.MINCPU);
//	    LocateGlobalRegistry.createGlobalRegistry(REGISTRY_PORT, loadBalancingType.ROUNDROBIN);
	    System.out.println("Global registry: listening on port " + REGISTRY_PORT);

	    // block forever
	    GlobalRegistry.class.wait();
	    System.out.println("Global registry: exiting (should not happen)");
	  }
	
	public Sorter getMinCPUForAService(String genericServiceName) throws RemoteException{
		int size = services.get(genericServiceName).size();
		Map<Double, Sorter> listServicesTime = new HashMap<>();
		
		for(int i = 0 ; i < size; i++){
			Sorter a = (Sorter) services.get(genericServiceName).values().toArray()[i];
			String name = (String) services.get(genericServiceName).keySet().toArray()[i];
			listServicesTime.put(a.getCPULoad(), a);
			System.out.println("Service name : " + name + " CPULoad : "+ a.getCPULoad());
		}
		
		Double min = Collections.min(listServicesTime.keySet());
		System.out.println(" -> Le minimum pour " + genericServiceName + " est " + min);
		return listServicesTime.get(min);
	}
	
	public Remote getServiceRoundRobin(String name){
		int i = currentServiceIndex.get(name);
		currentServiceIndex.replace(name, getNewServiceIndex(name));
		System.out.println((String) services.get(name).keySet().toArray()[i]);
		return (Remote) services.get(name).values().toArray()[i];
	}
	
	public int getNewServiceIndex(String name){
		int index = currentServiceIndex.get(name);
		int nbReplicatedServices = services.get(name).size();
		
		if(index >= nbReplicatedServices - 1){
			index = 0;
		} else {
			index++;
		}
		System.out.println("index of " + name + " is now : " + index);
		return index;
	}
	
	@Override
	public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
		String genericServiceName = name.split("_")[0];
		
		if(!services.containsKey(genericServiceName)){
			currentServiceIndex.put(genericServiceName, 0);
			services.put(genericServiceName, new LinkedHashMap<>());
		}
			
		Map<String, Remote> servicesStored = services.get(genericServiceName);
		if(servicesStored.containsKey(name)){
			System.out.println("bind : AlreadyBoundException in bind method");
			throw new AlreadyBoundException(name);
		}
		
		servicesStored.put(name, obj);
		services.put(genericServiceName, servicesStored);
		System.out.println("bind succed : " + name + " - " + genericServiceName);
	}

	@Override
	public String[] list() throws RemoteException, AccessException {
		String[] names = services.keySet().toArray(new String[services.keySet().size()]);
		List<String> result = new ArrayList<>();
		for(String name : names){
			Map<String, Remote> servicesStored = services.get(name);
			for(String serviceName : servicesStored.keySet()){
				result.add(serviceName);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	@Override
	public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
		if(services.containsKey(name)){
			switch(loadBalancingType){
			case MINCPU:
				return getServiceRoundRobin(name);
			case ROUNDROBIN:
				return getMinCPUForAService(name);
			default:
				return null;
			}		
			
		} else {
			throw new NotBoundException(name);
		}
	}

	@Override
	public void rebind(String name, Remote obj) throws RemoteException, AccessException {
		String genericServiceName = name.split("_")[0];
		
		if(!services.containsKey(genericServiceName)){
			currentServiceIndex.put(genericServiceName, 0);
			services.put(genericServiceName, new LinkedHashMap<>());
		}
		
		services.get(genericServiceName).remove(name);
		
		try {
			bind(name, obj);
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
		String genericServiceName = name.split("_")[0];
		
		if(services.containsKey(genericServiceName)){
			services.get(genericServiceName).remove(name);
			currentServiceIndex.replace(genericServiceName, getNewServiceIndex(genericServiceName));
		}
	}
	
	public Map<String, Map<String, Remote>> getServices() {
		return services;
	}

	public void setServices(Map<String, Map<String, Remote>> services) {
		this.services = services;
	}

	public Map<String, Integer> getCurrentServiceIndex() {
		return currentServiceIndex;
	}

	public void setCurrentServiceIndex(Map<String, Integer> currentServiceIndex) {
		this.currentServiceIndex = currentServiceIndex;
	}

	
}
