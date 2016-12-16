package services;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;

import com.sun.management.OperatingSystemMXBean;

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
public class SimpleSorterA implements Sorter {

  @Override
  public List<String> sort(List<String> list) {

    System.out.println(this + ": receveid " + list);

    Collections.sort(list);

    System.out.println(this + ": returning " + list);
    return list;
  }

  @Override
  public List<String> reverseSort(List<String> list) {

    System.out.println(this + ": receveid " + list);

    Collections.sort(list);
    Collections.reverse(list);

    System.out.println(this + ": returning " + list);
    return list;
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

}
