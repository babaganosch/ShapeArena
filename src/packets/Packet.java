package packets;
import java.io.Serializable;

/**
 * This is the Packet class.
 * This is a abstract class being the foundation for any type of packet.
 * @author Daniel Detlefsen
 * @version 2018-03-05
 */
public abstract class Packet implements Serializable {
	
	private static final long serialVersionUID = -8229001052268205876L;
	private int id;
	
	/**
	 * Creates a Packet object
	 * @param id The id is mostly used as a playerID or identifier for the packet.
	 */
	public Packet(int id) {
		this.id = id;
	}
	
	/**
	 * Getter for the id.
	 * @return Returns the id from the packet.
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Setter for the id.
	 * @param id Sets the id.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
}
