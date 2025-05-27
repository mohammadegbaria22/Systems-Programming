package bgu.spl.mics;
import bgu.spl.mics.application.Messages.Broadcasts.TickBroadcast;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	//SingletonHolder?? because maybe 2 threads will be in line if(instance==null){...}!
	//we need to use concurrentLinkedQueue

	private static MessageBusImpl instance;
	//we should have collection of Queues for Microservices
	//each MessageType should have queue of which microservices are subscribed
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> micros = new ConcurrentHashMap<>();
	private ConcurrentHashMap< Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventMicro = new ConcurrentHashMap<>();
	private ConcurrentHashMap< Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadMicro = new ConcurrentHashMap<>();
	private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Class<? extends Event>>> microEvent = new ConcurrentHashMap<>();
	private ConcurrentHashMap<MicroService , ConcurrentLinkedQueue<Class<? extends Broadcast>>> microBroad = new ConcurrentHashMap<>();//must be Blocking queue
	private ConcurrentHashMap<Event , Future> event_Future = new ConcurrentHashMap<>();

	/**
	 * @PRE: None
	 * @POST: instance != null
	 */
	//singleton DesignPattern - need to be changed
	public static MessageBusImpl getInstance(){
		if(instance==null){
			instance=new MessageBusImpl();
		}
		return instance;
	}

	/*
	 * @PRE:m.isRegistered() == true
	 * @POST:type.queue!=null && type.queue.size == @PRE(type.queue.size)+1 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventMicro.putIfAbsent(type, new ConcurrentLinkedQueue<>()); //initialize a queue for Event type if not initialized before
		microEvent.putIfAbsent(m, new ConcurrentLinkedQueue<Class<? extends Event>>()); //initialize a queue for MicroService m if not initialized before

		synchronized (m) {
			ConcurrentLinkedQueue<Class<? extends Event>> eventQ = microEvent.get(m);
			if (eventQ != null) {
				eventQ.add(type);
			}
		}

		synchronized (type) {
			ConcurrentLinkedQueue<MicroService> microQ = eventMicro.get(type);
			if (microQ != null) {
				microQ.add(m);
			}
		}
	}

	/**
	 * @PRE: m.isRegistered() == true
	 * @POST: type.queue != null && type.queue.size == @PRE(type.queue.size) + 1
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadMicro.putIfAbsent(type, new ConcurrentLinkedQueue<>()); //initialize a queue for Broad type if not initialized before
		microBroad.putIfAbsent(m, new ConcurrentLinkedQueue<Class<? extends Broadcast>>()); //initialize a queue for MicroService m if not initialized before

		synchronized (m) {
			ConcurrentLinkedQueue<Class<? extends Broadcast>>  queueOfBroad = microBroad.get(m);
			if (queueOfBroad != null) {
				queueOfBroad.add(type);
			}
		}

		synchronized (type){
			ConcurrentLinkedQueue<MicroService> Q = broadMicro.get(type);
			if (Q != null){
				Q.add(m);
				//System.out.println("here im subscribed!");
			}
		}
	}

	/**
	 * @PRE: event_Future.contains(e) == true
	 * @POST: future.isResolved() == true
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = event_Future.get(e);
		future.resolve(result);
	}

	/**
	 * @PRE: broadMicro.containsKey(b.getClass()) == true
	 * @POST: All registered microservices for b.getClass() have received the broadcast.
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (b.getClass()) {
			//System.out.println("im before the if statement - MsgBus line 90");
			if (broadMicro.containsKey(b.getClass())) {
				//System.out.println("im in the if statement - MsgBus line 92");

				ConcurrentLinkedQueue<MicroService> micro = broadMicro.get(b.getClass());
				//System.out.println("the sizeOF TickBroadCAst Q is: " + micro.size());
				for (MicroService microService : micro) {
					//System.out.println("im " + microService.getName() + " subscribed to "+b.getClass().getName());
					LinkedBlockingQueue<Message> Q = micros.get(microService);

					boolean sign = Q != null;
					//System.out.println("is Q != null: " + sign);

					if (Q != null) {
						Q.add(b);
					}
				}
			}
		}
	}

	/**
	 * @PRE: eventMicro.containsKey(e.getClass()) == true
	 * @POST: event_Future.containsKey(e) == true && queue of assigned microservice contains e
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> future = new Future<>();

		if (eventMicro.get(e.getClass()) == null || !eventMicro.containsKey(e.getClass())){return null;}

		event_Future.put(e,future);
		ConcurrentLinkedQueue<MicroService> currQofEvent = eventMicro.get(e.getClass());

		if (currQofEvent == null){return null;} //if no queue then no one has registered to it yet, or already unregistered


		//round-robin
		MicroService currMicro = currQofEvent.poll();//should never return null because of the if above
		if(currMicro==null){return null;} //there's no microService to proccess the event
		currQofEvent.add(currMicro);
		//

		synchronized (currMicro){
			LinkedBlockingQueue<Message> Q = micros.get(currMicro);
			if (Q == null) {
				return null;
			}
			Q.add(e);}

		return future;}

	/**
	 * @PRE: !micros.containsKey(m)
	 * @POST: micros.containsKey(m) == true && queue for m is initialized
	 */
	@Override
	public void register(MicroService m) {
		micros.putIfAbsent(m, new LinkedBlockingQueue<>());

	}

	/**
	 * @PRE: micros.containsKey(m) == true
	 * @POST: !micros.containsKey(m)
	 */
	@Override
	public void unregister(MicroService m) {
		if (micros.containsKey(m)) {
			LinkedBlockingQueue<Message> q;

			if (microEvent.containsKey(m)) {
				ConcurrentLinkedQueue<Class<? extends Event>> q3 = microEvent.get(m);
				for (Class<? extends Event> type : q3) {
					synchronized (type) {
						eventMicro.get(type).remove(m);
					}
				}
				microEvent.remove(m);
			}
			synchronized (m) {
				if (microBroad.containsKey(m)) {
					ConcurrentLinkedQueue<Class<? extends Broadcast>> q2 = microBroad.get(m);
					for (Class<? extends Broadcast> type : q2) {
						synchronized (type) {
							broadMicro.get(type).remove(m);
						}
					}
					microBroad.remove(m);
				}
				q = micros.remove(m);
				if (q == null) {
					return;
				}
			}
			while (!q.isEmpty()) {
				Message message = q.poll();
				if (message != null) {
					Future<?> future = event_Future.get(message);
					if (future != null) {
						future.resolve(null);
					}
				}
			}
		}
	}

	/**
	 * @PRE: micros.containsKey(m) == true
	 * @POST: Message queue of m is processed and returned.
	 */
	@Override
	public Message awaitMessage(MicroService m)  {
//		if(!isRegistered(m)) throw new IllegalStateException();
//		LinkedBlockingQueue<Message> currQ = micros.get(m);
//		while(currQ.isEmpty()){
//			try {
//				wait();
//			}
//			//where notify should be?
//			catch(InterruptedException ignored){}
//		}
//		return currQ.remove();



		LinkedBlockingQueue<Message> q = micros.get(m);
		if (q == null) {
			throw new IllegalArgumentException("MicroService is not registered");
		}
		Message msg = null;
		synchronized (q) {
			try {
				msg = q.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	/////////////////////////////////////////////////////////////////-METHODS FOR UNIT TESTS-//////////////////////////////////////////
	private boolean isRegistered(MicroService m){
		return microEvent.get(m)!=null;
	}


	public ConcurrentLinkedQueue<MicroService> getQueueOfEvent(Class<? extends Event> type ){
		return eventMicro.get(type);
	}


	public ConcurrentLinkedQueue<Class<? extends Broadcast>> getQueueMicroBroad(MicroService service) {
		return microBroad.get(service);
	}

	public ConcurrentLinkedQueue<MicroService> getQueueOfBroad(Class<? extends Broadcast> type){
		return broadMicro.get(type);
	}

	public LinkedBlockingQueue<Message> getQueueMicroAll(MicroService service) {
		return micros.get(service);
	}

}




