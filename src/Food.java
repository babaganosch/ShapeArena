import java.io.Serializable;

/**
 * This is the Food class.
 * Each object of this class represents a Food Object in the world.
 * @author Hasse Aro
 * @version 2018-03-xx
 */
public class Food implements Serializable {
	
	private static final long serialVersionUID = -8986608731006981204L;
	private int id;
	private int x;
	private int y;
	
	/**
	 * Creates a Food object.
	 * @param id The index of the Food object.
	 * @param x The X coordinate of the Food object.
	 * @param y The Y coordinate of the Food object.
	 */
	public Food(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Getter for the index of the Food object.
	 * @return Returns the index of the Food object.
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Getter for the X coordinate of the Food object.
	 * @return Returns the X coordinate of the Food object.
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * Getter for the Y coordinate of the Food object
	 * @return Returns the Y coordinate of the Food object.
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * Setter of the X coordinate of the Food object.
	 * @param x Sets the X coordinate of the Food object.
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * Setter of the Y coordinate of the Food object.
	 * @param y Sets the Y coordinate of the Food object.
	 */
	public void setY(int y) {
		this.y = y;
	}
}
