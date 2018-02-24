import java.io.IOException;
import java.util.HashMap;

class FoodHandler extends Thread {

	private HashMap<Integer, Food> foodList;
	private User[] users;

	public FoodHandler(HashMap<Integer, Food> inFoodList, User[] users) throws IOException {
		foodList = inFoodList;
		this.users = users;
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
				Thread.sleep(Server.tickRate);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
