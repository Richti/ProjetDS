package registries;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import services.Service;
import services.Sorter;

public class GlobalRegistry implements IGlobalRegistry {
	
	private static final int REGISTRY_PORT = 1099;
	private LoadBalancingType loadBalancingType;
	private ReplicationType replicationType;
	private Map<String, Map<String, Remote>> services = new HashMap<>();
	
	// Used in passive and semi-active replication
	private Map<String, Remote> primaryReplica = new HashMap<>();
	
	// Used to implement fifo ordering between methods calls (key : genericServiceName and value : id du dernier message à recevoir)
	private Map<String, Integer> maxIdByService = new HashMap<>();
	
	//Use to perform RoundRobin load balancing
	private Map<String, Integer> currentServiceIndex = new HashMap<>();
	  
	public GlobalRegistry(LoadBalancingType loadBalancingType, ReplicationType replicationType){
		this.loadBalancingType = loadBalancingType;
		this.replicationType = replicationType;
	}
	
	public static synchronized void main(String[] args) throws Exception {
		
	    System.out.println("Global registry: running on host " + InetAddress.getLocalHost());
	    
	    // create the registry on the local machine, on the default port number
	    //LocateGlobalRegistry.createGlobalRegistry(REGISTRY_PORT, LoadBalancingType.MINCPU, ReplicationType.SEMI_ACTIVE);
	    LocateGlobalRegistry.createGlobalRegistry(REGISTRY_PORT, LoadBalancingType.ROUNDROBIN, ReplicationType.PASSIVE);
	    System.out.println("Global registry: listening on port " + REGISTRY_PORT);

	    // block forever
	    GlobalRegistry.class.wait();
	    System.out.println("Global registry: exiting (should not happen)");
	  }
	
	
	
	@Override
	public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
		String genericServiceName = name.split("_")[0];
		
		if(!services.containsKey(genericServiceName)){
			currentServiceIndex.put(genericServiceName, 0);
			maxIdByService.put(genericServiceName, 0);
			services.put(genericServiceName, new LinkedHashMap<>());
			primaryReplica.put(genericServiceName, obj);
			if(replicationType == ReplicationType.PASSIVE){
				Service service = (Service) obj;
				service.launchPeriodicUpdate();
			}
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
		return getRemote(name);
	}

	@Override
	public void rebind(String name, Remote obj) throws RemoteException, AccessException {
		String genericServiceName = name.split("_")[0];
		
		if(!services.containsKey(genericServiceName)){
			currentServiceIndex.put(genericServiceName, 0);
			services.put(genericServiceName, new LinkedHashMap<>());
			primaryReplica.put(genericServiceName, obj);
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
			Service service = (Service) primaryReplica.get(genericServiceName);
			// Si le service est une replique primaire alors on le retire de la liste
			if(service.getServiceName().equals(name)){
				primaryReplica.remove(service);
				Iterator<Entry<String, Remote>> i = services.get(genericServiceName).entrySet().iterator();
				// Et si il y a d'autres répliques on ajoute une nouvelle réplique primaire
				if(i.hasNext()){
					Remote remote = i.next().getValue();
					primaryReplica.put(genericServiceName, remote);
				}
			}
		}
	}
	
	@Override
	public Remote getRemote(String name) throws RemoteException, NotBoundException{
		if(services.containsKey(name)){
			switch(loadBalancingType){
				case MINCPU:
					return getMinCPUForAService(name);
				case ROUNDROBIN:
					return getServiceRoundRobin(name);
				default:
					return null;
			}		
		} else {
			throw new NotBoundException(name);
		}
	}
	
	@Override
	public Map<String, Remote> getSpecificsServices(String genericName){
		if(!services.containsKey(genericName)){
			return null;
		} else{
			return services.get(genericName);
		}
	}
	
	private Remote getMinCPUForAService(String genericServiceName) throws RemoteException{
		int size = services.get(genericServiceName).size();
		Map<Double, Service> listServicesTime = new HashMap<>();
		
		for(int i = 0 ; i < size; i++){
			Service a = (Service) services.get(genericServiceName).values().toArray()[i];
			String name = (String) services.get(genericServiceName).keySet().toArray()[i];
			listServicesTime.put(a.getCPULoad(), a);
			System.out.println("Service name : " + name + " CPULoad : "+ a.getCPULoad());
		}
		
		Double min = Collections.min(listServicesTime.keySet());
		System.out.println(" -> Le minimum pour " + genericServiceName + " est " + min);
		return listServicesTime.get(min);
	}
	
	private Remote getServiceRoundRobin(String name){
		int i = currentServiceIndex.get(name);
		currentServiceIndex.replace(name, getNewServiceIndex(name));
		System.out.println((String) services.get(name).keySet().toArray()[i]);
		return (Remote) services.get(name).values().toArray()[i];
	}
	
	private int getNewServiceIndex(String name){
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

	@Override
	public Map<String, Remote> getPrimaryReplica() throws RemoteException {
		return primaryReplica;
	}

	public Map<String, Integer> getMaxIdByService() {
		return maxIdByService;
	}
	
	@Override
	public int getAndIncreaseMaxIdByService(String genericServiceName) throws RemoteException {
		int result = maxIdByService.get(genericServiceName);
		maxIdByService.replace(genericServiceName, result + 1);
		return result;
	}

	public void setMaxIdByService(Map<String, Integer> maxIdByService) {
		this.maxIdByService = maxIdByService;
	}

	@Override
	public ReplicationType getReplicationType() {
		return replicationType;
	}

	public void setReplicationType(ReplicationType replicationType) {
		this.replicationType = replicationType;
	}	
}
