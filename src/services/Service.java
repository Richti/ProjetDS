package services;

import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 *  Interface for all services to be created (needed in the Global registry to perform load balancing and replication)
 */
public interface Service extends Remote {

	public double getCPULoad() throws RemoteException;
	
	public String getServiceName() throws RemoteException;

	public void launchPeriodicUpdate() throws RemoteException;
}
