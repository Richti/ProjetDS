package services;

import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.management.OperatingSystemMXBean;

import message.Message;
import message.MessageType;
import registries.IGlobalRegistry;
import registries.LocateGlobalRegistry;

public class BankAccount implements IBankAccount {
	
	private String genericServiceName;
	private String serviceName;
	private IGlobalRegistry registry;
	private int balance = 0;
	private int idExpected = 0;
	private List<Message> messageQueue = new ArrayList<>();
	
	public BankAccount(String serviceName) throws RemoteException, NotBoundException{
		String genericServiceName = serviceName.split("_")[0];
		this.genericServiceName = genericServiceName;
		this.serviceName = serviceName;
		setRegistry((IGlobalRegistry) LocateGlobalRegistry.getLocateGlobalRegistry());
	}
	
	public static void main(String[] args) throws Exception {
	    IBankAccount bank = new BankAccount("BankAccountA_ServerA");
		try{
			
			Message msg1 = new Message(0, MessageType.DEPOSIT);
			msg1.getArguments().add(10);
			Message msg2 = new Message(1, MessageType.WITHDRAW);
			msg2.getArguments().add(15);
			Message msg3 = new Message(2, MessageType.DEPOSIT);
			msg3.getArguments().add(10);
			Message msg4 = new Message(3, MessageType.WITHDRAW);
			msg4.getArguments().add(10);
			
			
			bank.handleMessage(msg1);
			bank.handleMessage(msg2);
			bank.handleMessage(msg3);
			bank.handleMessage(msg4);
			System.out.println(bank.getBalance());
		} catch (Exception e){
			e.printStackTrace();
		}

	  }
	
	// Fonction de type consultation :
	@Override
	public String getBalance() throws RemoteException, NotBoundException {
		return getCorrectBankAccount().effectiveGetBalance();
	}
	
	@Override
	public String effectiveGetBalance() throws RemoteException {
		System.out.println("Account balance on " + serviceName + " is " + balance);
		return  "Your account balance is " + balance + " from service : " + serviceName;
	}

	// Calcul et changement d'état
	@Override
	public void deposit(int money) throws RemoteException, NotBoundException {
		int id = registry.getAndIncreaseMaxIdByService(genericServiceName);
		
		Message msg = new Message(id, MessageType.DEPOSIT);
		msg.getArguments().add(money);	
		notifyAll(msg);
	}
	
	@Override
	public void effectiveDeposit(int money) throws RemoteException {
		System.out.println("Deposit of " + money + " on " + serviceName + "(balance before = " + balance + ")");
		balance += money;
	}
 
	// Calcul et changement d'état
	@Override
	public void withdraw(int money) throws RemoteException {
		int id = registry.getAndIncreaseMaxIdByService(genericServiceName);
		Message msg = new Message(id, MessageType.WITHDRAW);
		msg.getArguments().add(money);
		notifyAll(msg);
	}
	
	@Override
	public void effectiveWithdraw(int money) {
		if(balance >= money){
			System.out.println("Withdraw of " + money + " on " + serviceName+ "(balance before = " + balance + ")");
			balance -= money;
		}else {
			System.out.println("Error when trying to withdraw" + money + " on " + serviceName + "(balance before = " + balance + ")");
		}
	}
	
	@Override
	public double getCPULoad() {
		OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return os.getProcessCpuLoad();
	}
	
	@Override
	public void handleMessage(Message msg) throws RemoteException {
		if(msg.getId() != idExpected){
			messageQueue.add(msg);
		} else {
			switch(msg.getType()){
				case DEPOSIT:
					int moneyToDepose = (Integer) msg.getArguments().get(0);
					effectiveDeposit(moneyToDepose);
					break;
				case WITHDRAW:
					int moneyToWithdraw = (Integer) msg.getArguments().get(0);
					effectiveWithdraw(moneyToWithdraw);
					break;
				default:
					break;
			}
			idExpected++;
			checkMessageAlreadyArrived();
		}
	}
	
	private void checkMessageAlreadyArrived() throws RemoteException {
    	for(int i = 0; i < messageQueue.size(); i++){
    		Message msg = messageQueue.get(i);
    		if(idExpected == msg.getId()){
    			messageQueue.remove(msg);
    			handleMessage(msg);  
    			return;
    	    }
    	}
	}
	
	private void notifyAll(Message msg) throws RemoteException{
		Map<String, Remote> services = registry.getSpecificsServices(genericServiceName);
		for(Remote bankAccount : services.values()){
			((IBankAccount) bankAccount).handleMessage(msg);
		}
	}

	private IBankAccount getCorrectBankAccount() throws RemoteException, NotBoundException{
		return (IBankAccount) registry.getPrimaryReplica().get(genericServiceName);
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public IGlobalRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(IGlobalRegistry registry) {
		this.registry = registry;
	}
	
	public String getGenericServiceName() {
		return genericServiceName;
	}

	public void setGenericServiceName(String genericServiceName) {
		this.genericServiceName = genericServiceName;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getIdExpected() {
		return idExpected;
	}

	public void setIdExpected(int idExpected) {
		this.idExpected = idExpected;
	}

	public List<Message> getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(List<Message> messageQueue) {
		this.messageQueue = messageQueue;
	}
	
}
