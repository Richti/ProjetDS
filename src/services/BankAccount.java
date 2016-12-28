package services;

import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.management.OperatingSystemMXBean;

import message.Message;
import message.MessageType;
import registries.IGlobalRegistry;
import registries.LocateGlobalRegistry;
import registries.ReplicationType;

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
		if(registry.getReplicationType() != ReplicationType.PASSIVE){
			int id = registry.getAndIncreaseMaxIdByService(genericServiceName);	
			Message msg = new Message(id, MessageType.DEPOSIT);
			msg.getArguments().add(money);
			notifyAll(msg);
		} else {
			getCorrectBankAccount().effectiveDeposit(money);
		}
	}
	
	@Override
	public void effectiveDeposit(int money) throws RemoteException {
		System.out.println("Deposit of " + money + " on " + serviceName + "(balance before = " + balance + ")");
		balance += money;
	}
 
	// Calcul et changement d'état
	@Override
	public void withdraw(int money) throws RemoteException, NotBoundException {	
		if(registry.getReplicationType() != ReplicationType.PASSIVE){
			int id = registry.getAndIncreaseMaxIdByService(genericServiceName);
			Message msg = new Message(id, MessageType.WITHDRAW);
			msg.getArguments().add(money);
			notifyAll(msg);
		} else {
			getCorrectBankAccount().effectiveWithdraw(money);
		}
	}
	
	@Override
	public void effectiveWithdraw(int money) {
		if(balance >= money){
			System.out.println("Withdraw of " + money + " on " + serviceName+ "(balance before = " + balance + ")");
			balance -= money;
		}else {
			System.out.println("Error when trying to withdraw " + money + " on " + serviceName + "(balance before = " + balance + ")");
		}
	}
	
	@Override
	public double getCPULoad() {
		OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return os.getProcessCpuLoad();
	}
	
	@Override
	public void handleMessage(Message msg) throws RemoteException {
		if((msg.getId() != idExpected) && (msg.getType() != MessageType.UPDATE)){
			messageQueue.add(msg);
			return;
		}
		
		try {
			switch(msg.getType()){
				case DEPOSIT:
					int moneyToDepose = (Integer) msg.getArguments().get(0);
					effectiveDeposit(moneyToDepose);
					break;
				case WITHDRAW:
					int moneyToWithdraw = (Integer) msg.getArguments().get(0);
					effectiveWithdraw(moneyToWithdraw);
					break;
				case UPDATE:
					balance = (Integer) msg.getArguments().get(0);
					System.out.println("Balance updated on " + serviceName + " : " + balance);
					idExpected--;
					break;
				default:
					break;
			}
		} catch(Exception e){
			e.printStackTrace();
		}	
		
		idExpected++;
		checkMessageAlreadyArrived();	
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
	
	private void notifyAllOthers(Message msg) throws RemoteException{
		Map<String, Remote> services = registry.getSpecificsServices(genericServiceName);
		services.remove(serviceName);
		for(Remote bankAccount : services.values()){
			((IBankAccount) bankAccount).handleMessage(msg);
		}
	}
	
	@Override
	public void launchPeriodicUpdate() throws RemoteException{
		if(registry.getReplicationType() == ReplicationType.PASSIVE){
			Thread t = new PeriodicUpdate(3);
			t.start();
		}
	}

	private IBankAccount getCorrectBankAccount() throws RemoteException, NotBoundException{
		// Si on fait de la réplication active, on doit effectuer un vote (on renvoie le service le plus à jour donc celui avec l'id attendu le plus haut):
		if(registry.getReplicationType() == ReplicationType.ACTIVE){
			Map<String, Remote> services = registry.getSpecificsServices(genericServiceName);
			Map<Integer, IBankAccount> idExpectedByService = new HashMap<>();
			for(Remote remote : services.values()) {
				IBankAccount bankAccount = (IBankAccount) remote;
				idExpectedByService.put(bankAccount.getIdExpected(), bankAccount);
			}			
			
			int max = Collections.max(idExpectedByService.keySet());
			return idExpectedByService.get(max);
		}
		// Sinon, on envoie la réponse du serveur primaire :
		else {
			return (IBankAccount) registry.getPrimaryReplica().get(genericServiceName);
		}
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

	@Override
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
	
	/*
	 * 	Classe utilisée dans la réplication passive pour mettre à jour les services secondaires toutes les nbSecond secondes
	 */
	class PeriodicUpdate extends Thread {
		
		private int nbSecond = 1;
		
		public PeriodicUpdate(int nbSecond) {
			if(nbSecond > 1){
				this.nbSecond = nbSecond;
			}
		}

		@Override
		public void run() {	
			while(true){
				int id = 0;
				try {
					Thread.sleep(nbSecond * 1000);
					id = registry.getAndIncreaseMaxIdByService(genericServiceName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Message msg = new Message(id, MessageType.UPDATE);
				msg.getArguments().add(balance);
				synchronized(this){
					try {
						notifyAllOthers(msg);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}	
	}
	
}
