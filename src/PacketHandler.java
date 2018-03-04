import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * This is the Packet Handler class.
 * This class acts as a output machine for the server, receiving packets from ClientHandlers and
 * forwarding that packet to all the Clients.
 * It also keeps a HashMap containing all the Food objects updated and sends that to every Client also.
 * The PacketHandler is observed by the ServerFrame.
 * @author Hasse Aro
 * @version 2018-03-xx
 */
class PacketHandler extends Observable implements Runnable {

	// Thread
	private Thread activity = new Thread(this);
	
	private HashMap<Integer, Food> foodList;
	private ArrayList<Packet> packetList = new ArrayList<Packet>();
	private ClientHandler[] clientHandlers;
	private Server server;
	private int notifyTimer = 70;

	/**
	 * Constructs a packethandler that adds references and starts the packethandler.
	 * 
	 * @param inFoodList is a reference to the foodlist.
	 * @param users is a reference to the array of connected clientHandlers.
	 * @param server is a reference to the server.
	 * @param serverFrame is a reference to the server's frame.
	 */
	public PacketHandler(HashMap<Integer, Food> inFoodList, ClientHandler[] users, Server server, Observer serverFrame){
		this.foodList = inFoodList;
		this.clientHandlers = users;
		this.server = server;
		addObserver(serverFrame);
		activity.start();
	}
	
	/**
	 * Updates the HashMap foodList with a new Food object
	 * sent from a Client.
	 * @param food The Food object to put in the HashMap.
	 */
	public synchronized void updateFood(Food food) {
		this.foodList.put(food.getId(), food);
	}

	/**
	 * Adds a packet to the packet queue list packetList.
	 * @param packet The packet to add to the queue.
	 */
	public synchronized void addPacket(Packet packet) {
		packetList.add(packet);
	}

	/**
	 * Constantly sends packets from packetlist and a deep copied list of the foodobjects to all users, clears the packetlist if it gets too full and notifies the observer 
	 * with packetlist size.
	*/
	public void run() {

		while (true) {
			try {
				
				HashMap<Integer, Food> newFoodList = new HashMap<Integer, Food>();
				for (int i = 0; i < foodList.size(); i++) {
					Food food = new Food(foodList.get(i).getId(), foodList.get(i).getX(), foodList.get(i).getY()); // Deep copy
					newFoodList.put(i, food);
				}
				FoodPacket foodPacket = new FoodPacket(newFoodList);

				// Send out a foodPacket with ID 1 (Receiver: Clients)
				for (ClientHandler clientHandler : clientHandlers) {
					if (clientHandler != null) {
						if (clientHandler.isReady()) {
							clientHandler.getObjectOutputStream().writeObject(foodPacket);
							clientHandler.getObjectOutputStream().flush();
						}
					}
				}

				// Forward packets
				if (packetList.size() > 0) {
					for (ClientHandler clientHandler : clientHandlers) {
						if (clientHandler != null) {
							if (clientHandler.isReady()) {
								clientHandler.getObjectOutputStream().writeObject(packetList.get(0));
								clientHandler.getObjectOutputStream().flush();
							}
						}
					}
					packetList.remove(0);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// Notify the server about the server bloat
			if (notifyTimer <= 0) {
				setChanged();
				notifyObservers((Integer) packetList.size());
				notifyTimer = 70;
			} else {
				notifyTimer--;
			}
			
			if (packetList.size() > 100) {
				packetList = new ArrayList<Packet>();
			}

			try {
				// Sleep for a while
				Thread.sleep(server.getTickRate());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
}
