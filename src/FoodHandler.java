import java.io.IOException;
import java.util.HashMap;

class FoodHandler extends Thread {

	private HashMap<Integer, Food> foodList;
	private User[] users;
	private Server server;

	public FoodHandler(HashMap<Integer, Food> inFoodList, User[] users, Server server) throws IOException {
		foodList = inFoodList;
		this.users = users;
		this.server = server;
		start();
	}

	public void setFoodList(HashMap<Integer, Food> inFoodList) {
		foodList = inFoodList;
	}

	public void run() {

		while (true) {
			try {

				// Send out a foodPacket with ID 1 (Receiver: Clients)
				for (User user : users) {
					if (user != null) {
						user.getObjectOutputStream().writeObject(new FoodPacket(1, foodList));
						user.getObjectOutputStream().flush();
					}
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
