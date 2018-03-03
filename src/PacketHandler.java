import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

class PacketHandler extends Observable implements Runnable {

	// Thread
	private Thread activity = new Thread(this);
	
	private HashMap<Integer, Food> foodList;
	private ArrayList<Packet> packetList = new ArrayList<Packet>();
	private ClientHandler[] clientHandlers;
	private Server server;
	private int notifyTimer = 70;

	public PacketHandler(HashMap<Integer, Food> inFoodList, ClientHandler[] users, Server server, Observer serverFrame) throws IOException {
		this.foodList = inFoodList;
		this.clientHandlers = users;
		this.server = server;
		addObserver(serverFrame);
		activity.start();
	}
	
	public synchronized void updateFood(Food food) {
		this.foodList.put(food.getId(), food);
		//System.out.println("Adding food id: " + food.getId());
	}

	public synchronized void addPacket(Packet packet) {
		packetList.add(packet);
	}

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
