package packets;
/**
 * This is the PlayerPacket class.
 * This object is used as a packet sent over a Object Stream containing
 * information about a player, such as playerID, x, y and score.
 * @author Daniel Detlefsen
 * @version 2018-03-05
 */
public class PlayerPacket extends Packet {

	private static final long serialVersionUID = -7166969022156404657L;
	private int x, y, score;
	
	/**
	 * Creates the PlayerPacket
	 * @param id The PlayerID
	 * @param x	The X coordinate of the player
	 * @param y	The Y coordinate of the player
	 * @param score	The score of the player
	 */
	public PlayerPacket(int id, int x, int y, int score) {
		super(id);
		this.x = x;
		this.y = y;
		this.score = score;
	}
	
	/**
	 * Getter for the X coordinate
	 * @return The X coordinate of the player
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * Getter for the Y coordinate
	 * @return The Y coordinate of the player
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * Setter for the X coordinate
	 * @param x Sets the X coordinate for the packet
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * Setter for the Y coordinate
	 * @param y Sets the Y coordinate for the packet
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * Getter for the score
	 * @return Returns the score of the player from the packet
	 */
	public int getScore() {
		return this.score;
	}
}
