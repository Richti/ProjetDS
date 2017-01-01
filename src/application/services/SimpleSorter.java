package application.services;

import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import com.sun.management.OperatingSystemMXBean;

import framework.message.Message;
import framework.message.MessageType;
import framework.registries.IGlobalRegistry;
import framework.registries.LocateGlobalRegistry;

/**
 * A simple implementation of the <@link Sorter> using methods of class
 * <code>Collections</code>. For test purposes, the <code>toString()</code>
 * method displays the name of the current thread.
 *
 * Note: methods <code>sort</code> and <code>reverseSort</code> do not throw
 * <code>RemoteException</code>. This shows that this exception is not thrown by
 * the server code, but rather by the RMI runtime when a communication failure
 * is detected in the object's stub, on the client side.
 *
 */
public class SimpleSorter implements Sorter {

	private String genericServiceName = "";
	private String serviceName = "";
	private IGlobalRegistry registry;
	
	public SimpleSorter() throws RemoteException, NotBoundException{
		this.registry = (IGlobalRegistry) LocateGlobalRegistry.getLocateGlobalRegistry();
	}
	
  @Override
  public List<String> sort(List<String> list) throws RemoteException, NotBoundException {
	  Message msg = new Message(MessageType.SORT);
	  return getReponse(msg, list);
  }
  
  private List<String> effectiveSort(List<String> list) {
    System.out.println(this + ": receveid " + list);

    Collections.sort(list);

    System.out.println(this + ": returning " + list);
    return list;
  }
    
  @Override
  public List<String> reverseSort(List<String> list) throws RemoteException, NotBoundException {
	  Message msg = new Message(MessageType.REVERSE_SORT);
	  return getReponse(msg, list);
  }
  
  @SuppressWarnings("unchecked")
  private List<String> getReponse(Message msg, List<String> list) throws RemoteException, NotBoundException{
	  msg.getArguments().add(list);
	  Message response = getBestSorter().handleMessage(msg);
	  return (List<String>) response.getArguments().get(0);
  }

  private List<String> effectiveReverseSort(List<String> list) {
	  
    System.out.println(this + ": receveid " + list);
    
    Collections.sort(list);
    Collections.reverse(list);

    System.out.println(this + ": returning " + list);
    return list;
  }
  
    @SuppressWarnings("unchecked")
    @Override
	public Message handleMessage(Message msg) throws RemoteException {
    	Message response = new Message(MessageType.RESPONSE);
		switch(msg.getType()){
			case SORT:
				List<String> listToSort = (List<String>) msg.getArguments().get(0);
				response.getArguments().add(effectiveSort(listToSort));
				break;
			case REVERSE_SORT:
				List<String> listToReverseSort = (List<String>) msg.getArguments().get(0);
				response.getArguments().add(effectiveReverseSort(listToReverseSort));
				break;
			default:
				break;
		}	
		return response;
	}

  @Override
  public String toString() {
    return "SimpleSorter " + Thread.currentThread();
  }

	@Override
	public double getCPULoad() {
		OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return os.getProcessCpuLoad();
	}
	
	private Sorter getBestSorter() throws RemoteException, NotBoundException{
		return (Sorter) registry.getRemote(genericServiceName);
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public void setServiceName(String serviceName){
		this.genericServiceName = serviceName.split("_")[0];
		this.serviceName = serviceName;
	}

	@Override
	public void launchPeriodicUpdate() throws RemoteException {
		// Do nothing : stateless object doesn't need passive replication
	}
}
