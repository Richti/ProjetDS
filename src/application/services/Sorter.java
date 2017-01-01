package application.services;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import framework.registries.Service;

public interface Sorter extends Service {

  public List<String> sort(List<String> list) throws RemoteException, NotBoundException;
  public List<String> reverseSort(List<String> list) throws RemoteException, NotBoundException;
  
}
