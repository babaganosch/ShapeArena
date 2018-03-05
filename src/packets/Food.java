package packets;
import java.io.Serializable;

/**
 * This is the Food class.
 * Each object of this class represents a Food Object in the world.
 * @author Daniel Detlefsen
 * @version 2018-03-05
 */
public class Food extends Packet implements Serializable {
	
	private static final long serialVersionUID = -8986608731006981204L;
	private int x;
	private int y;
	
	/**
	 * Creates a Food object.
	 * @param id The index of the Food object.
	 * @param x The X coordinate of the Food object.
	 * @param y The Y coordinate of the Food object.
	 */
	public Food(int id, int x, int y) {
		super(id);
		this.x = x;
		this.y = y;
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
