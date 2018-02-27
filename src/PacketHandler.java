import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class PacketHandler extends Thread {

	private HashMap<Integer, Food> foodList;
	private ArrayList<Packet> packetList = new ArrayList<Packet>();
	private ClientHandler[] clientHandlers;
	private Server server;

	public PacketHandler(HashMap<Integer, Food> inFoodList, ClientHandler[] users, Server server) throws IOException {
		this.foodList = inFoodList;
		this.clientHandlers = users;
		this.server = server;
		start();
	}
	
	public synchronized void updateFood(Food food) {
		foodList.put(food.getId(), food);
		System.out.println("Adding food id: " + food.getId());
	}

	public synchronized void addPacket(Packet packet) {
		packetList.add(packet);
	}

	public void run() {

		while (true) {
			try {

				// Send out a foodPacket with ID 1 (Receiver: Clients)
				for (ClientHandler clientHandler : clientHandlers) {
					if (clientHandler != null) {
						if (clientHandler.isReady()) {
							clientHandler.getObjectOutputStream().writeObject(new FoodPacket(1, foodList));
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

			try {
				// Sleep for a while
				Thread.sleep(server.getTickRate());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
