package application.services;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import framework.registries.Service;

public interface IBankAccount extends Service {
	
	public String getBalance()throws RemoteException, NotBoundException;
	public void deposit(int money)throws RemoteException, NotBoundException;
	public void withdraw(int money)throws RemoteException, NotBoundException;
	
	public int getIdExpected()throws RemoteException;
	
}
