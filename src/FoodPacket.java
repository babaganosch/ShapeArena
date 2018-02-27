import java.util.HashMap;

public class FoodPacket extends Packet{

	private static final long serialVersionUID = 5424711620222586250L;

	private HashMap<Integer, Food> foodList;
	private Food food;
	
	public FoodPacket(HashMap<Integer, Food> foodList) {
		// Id 1 means the packet contains a HashMap
		super(1);
		this.foodList = foodList;
	}
	
	public FoodPacket(Food food) {
		// Id 0 means the packet contains a Food
		super(0);
		this.food = food;
	}
	
	public Food getFood() {
		return food;
	}
	
	public HashMap<Integer, Food> getFoodList() {
		return foodList;
	}

}