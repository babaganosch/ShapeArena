/**
 * This is the PlayerInitializationPacket class.
 * This object is used as a packet sent over a Object Stream containing
 * a playerID about a player for initialization purpose.
 * @author Hasse Aro
 * @version 2018-03-xx
 */
public class PlayerInitializationPacket extends Packet {

	private static final long serialVersionUID = 4688858102876818865L;

	/**
	 * Creates a PlayerInitializationPacket
	 * @param id The PlayerID
	 */
	public PlayerInitializationPacket(int id) {
		super(id);
	}

}
