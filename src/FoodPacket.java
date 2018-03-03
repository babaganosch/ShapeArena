import java.util.HashMap;

/**
 * This is the FoodPacket class.
 * This object is used as a packet sent over a Object Stream containing
 * information about either a Food object or a HashMap containing Food objects.
 * @author Hasse Aro
 * @version 2018-03-xx
 */
public class FoodPacket extends Packet{

	private static final long serialVersionUID = 5424711620222586250L;

	private HashMap<Integer, Food> foodList;
	private Food food;
	
	/**
	 * Creates a FoodPacket containing a HashMap.
	 * The ID of this packet is 1.
	 * @param foodList The HashMap to contain in the packet.
	 */
	public FoodPacket(HashMap<Integer, Food> foodList) {
		// Id 1 means the packet contains a HashMap
		super(1);
		this.foodList = foodList;
	}
	
	/**
	 * Creates a FoodPacket containing a Food object.
	 * The ID of this packet is 0.
	 * @param food The Food object to contain in the packet.
	 */
	public FoodPacket(Food food) {
		// Id 0 means the packet contains a Food
		super(0);
		this.food = food;
	}
	
	/**
	 * Getter for the Food object contained in the packet.
	 * @return Returns the Food object from the packet.
	 */
	public Food getFood() {
		return food;
	}
	
	/**
	 * Getter for the HashMap contained in the packet.
	 * @return Returns the HashMap from the packet.
	 */
	public HashMap<Integer, Food> getFoodList() {
		return foodList;
	}

}