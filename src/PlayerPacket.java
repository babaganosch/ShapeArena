
public class PlayerPacket extends Packet {

	private int score;
	
	public PlayerPacket(int id, int x, int y, int score) {
		super(id, x, y);
		this.score = score;
	}
}
