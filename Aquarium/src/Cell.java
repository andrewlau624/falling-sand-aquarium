
public class Cell {
	int row;
	int col;
	String name;
	boolean isFalling = false;
	
	public Cell(int col, int row, String name) {
		this.row = row;
		this.col = col;
		this.name = name;
	}
}
