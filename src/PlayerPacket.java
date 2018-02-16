
public class PlayerPacket extends Packet {

	private static final long serialVersionUID = -7166969022156404657L;
	private int x, y, score;
	
	public PlayerPacket(int id, int x, int y, int score) {
		super(id);
		this.x = x;
		this.y = y;
		this.score = score;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getScore() {
		return this.score;
	}
}
