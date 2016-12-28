package services;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public interface Sorter extends Service {

  public List<String> sort(List<String> list) throws RemoteException, NotBoundException;
  public List<String> reverseSort(List<String> list) throws RemoteException, NotBoundException;
  
  public List<String> sortFromBestService(List<String> list) throws RemoteException;
  public List<String> reverseSortFromBestService(List<String> list) throws RemoteException;

}
