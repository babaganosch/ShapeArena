
public class PlayerPacket extends Packet {

	private static final long serialVersionUID = -7166969022156404657L;
	private int score;
	
	public PlayerPacket(int id, int x, int y, int score) {
		super(id, x, y);
		this.score = score;
	}
	
	public int getScore() {
		return this.score;
	}
}
