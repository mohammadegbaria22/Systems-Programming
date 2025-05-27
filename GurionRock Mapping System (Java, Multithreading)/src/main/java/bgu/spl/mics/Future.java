package bgu.spl.mics;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	//AtomicReference<T> ??
	private T rslt;
	private boolean isResolve = false;

	/**
	 * This should be the only public constructor in this class.
	 */
	public Future(){}

	/**
	 * retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method! It waits for the computation in case it has
	 * not been completed.
	 * <p>
	 * @return return the result of type T if it is available, if not wait until it is available.
	 *
	 */

	public T get() {
		while(!isResolve) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException ignored) {
				}
			}
		}
		return rslt;
	}




	/**
	 * Resolves the result of this Future object.
	 */
	//lidar should do the result (if camera sends DetectObject for example)
	public void  resolve(T result) {
		rslt = result;
		isResolve = true;
		synchronized (this){
		notifyAll();}
	}

	/**
	 * @return true if this object has been resolved, false otherwise
	 */
	public boolean isDone() {
		return isResolve;
	}

	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */

	public synchronized T get(long timeout, TimeUnit unit) {
		if (!isResolve){
			synchronized (this) {
				try {
					wait(unit.toMillis(timeout));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return this.rslt;




	}
//		if (isResolve){return mSG;}
//		long TimeWait = unit.toMillis(timeout);
//		wait(TimeWait);
//		if (isResolve){return mSG;}
//		return null;
//}
//		if (isResolve) { // If already resolved, return the result immediately
//			return rslt;
//		}
//
//		long timeoutMillis = unit.toMillis(timeout); // Convert timeout to milliseconds
//		long startTime = System.currentTimeMillis(); // Record start time
//
//		while (!isResolve) {
//			long elapsedTime = System.currentTimeMillis() - startTime; // Time already waited
//			long remainingTime = timeoutMillis - elapsedTime; // Time left to wait
//
//			if (remainingTime <= 0) {
//				return null; // Timeout expired, return null
//			}
//
//			try {
//				wait(remainingTime); // Wait for the remaining time
//			}
//			catch (InterruptedException ignored){}
//		}
//
//		return rslt; // Return the result if resolved
//	}
}