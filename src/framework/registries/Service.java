package framework.registries;

import java.rmi.Remote;
import java.rmi.RemoteException;

import framework.message.Message;
/*
 *  Interface for all services to be created (needed in the Global registry to perform load balancing and replication)
 */
public interface Service extends Remote {

	public double getCPULoad() throws RemoteException;
	
	public String getServiceName() throws RemoteException;
	
	public void setServiceName(String serviceName) throws RemoteException;

	public void launchPeriodicUpdate() throws RemoteException;
	
	public Message handleMessage(Message msg) throws RemoteException;
	
}
