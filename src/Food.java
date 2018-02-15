
public class Food {
	
	private int id;
	private int x;
	private int y;
	
	public Food(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public int getId() {
		return this.id;
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
}
