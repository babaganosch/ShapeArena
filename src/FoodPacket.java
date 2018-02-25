import java.util.HashMap;

public class FoodPacket extends Packet{

	private static final long serialVersionUID = 5424711620222586250L;

	private HashMap<Integer, Food> foodList;
	
	public FoodPacket(int id, HashMap<Integer, Food> foodList) {
		super(id);
		this.foodList = foodList;
	}
	
	public HashMap<Integer, Food> getFoodList() {
		return foodList;
	}

}
