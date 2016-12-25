package services;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import message.Message;

public interface IBankAccount extends Service {
	
	public String getBalance()throws RemoteException, NotBoundException;
	
	public String effectiveGetBalance() throws RemoteException;
	
	public String getServiceName() throws RemoteException;
	
	public void deposit(int money)throws RemoteException, NotBoundException;

	public void effectiveDeposit(int money) throws RemoteException;
	
	public void withdraw(int money)throws RemoteException, NotBoundException;
	
	public void effectiveWithdraw(int money) throws RemoteException;

	public void handleMessage(Message msg) throws RemoteException;

	public int getIdExpected()throws RemoteException;

	
}
