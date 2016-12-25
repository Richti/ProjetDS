package registries;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Map;

public interface IGlobalRegistry extends Registry {
	
	public Remote getRemote(String name) throws RemoteException, NotBoundException;
	
	public Map<String, Remote> getSpecificsServices(String genericName) throws RemoteException;

	public Map<String, Remote> getPrimaryReplica() throws RemoteException;

	public int getAndIncreaseMaxIdByService(String genericServiceName) throws RemoteException;

	public ReplicationType getReplicationType() throws RemoteException;
}
