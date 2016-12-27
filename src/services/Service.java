package services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Service extends Remote {

	public double getCPULoad() throws RemoteException;
	
	public String getServiceName() throws RemoteException;

	public void launchPeriodicUpdate() throws RemoteException;
}
