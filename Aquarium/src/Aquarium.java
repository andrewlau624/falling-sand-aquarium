import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

public class Aquarium extends JPanel implements Runnable, MouseListener, MouseWheelListener, MouseMotionListener {
	final double scale = 1.6;
	final int width = (int) (1600/ scale);
	final int height = (int) (900 / scale);
	final int cellSize = 6;
	final int borderSize = cellSize / 2;
	final int airSpace = cellSize * 2;
	final int cols = width / cellSize;
	final int rows = height / cellSize;
	int brushSize = 10;
	int mX, mY;
	double deltaTime;
	
	int fishDir = -1;
	int fishStr = 10;
	double fishSpd = 10;
	
	Thread thread = new Thread(this);
	Cell cells[][] = new Cell[cols][rows];
	
	public Aquarium() {
		this.setPreferredSize(new Dimension(width, height));
		this.setOpaque(false);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		start();
	}
	
	public void start() {
		thread.start();
		
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < cols; c++) {
				cells[c][r] = new Cell(c, r, "void");
			}
		}
		
		cells[cols / 2][rows / 2].name = "fish";
		cells[cols / 2 + 1][rows / 2 + 4].name = "fish";
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		double FPS = 60;
		double drawInterval = 1000000000/FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		
		while(thread != null) {
			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			deltaTime += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;
			if(delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		for(Cell[] cells: cells) {
			for(Cell cell: cells) {
				switch(cell.name) {
					case "void":
						g.setColor(new Color(0, 0, 0, 0));
						break;
					case "sand":
						if(true)
							g.setColor(new Color(150, 150, 70));
						break;
					case "fish":
						g.setColor(Color.RED);
						break;
				}
				g.fillRect(cell.col * cellSize, cell.row * cellSize, cellSize, cellSize);
			}
		}
		
		g.setColor(new Color(64, 150, 225, 120));
		g.fillRect(0, airSpace, width, height);
		g.setColor(new Color(64, 150, 225, 255));
		g.fillRect(0, airSpace, width, 2);
		
		
		g.setColor(new Color(255, 255, 255, 80));

		for(int i = mY - brushSize; i <= mY + brushSize; i++) {
			for(int j = mX; (j-  mX) * (j - mX) + (i - mY) * (i - mY) <= brushSize * brushSize; j++) {
				if(checkEmpty(j, i, "void"))
					g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
			}
			for(int j = mX - 1; (j - mX) * (j - mX) + (i - mY) * (i - mY) <= brushSize * brushSize; j--) {
				if(checkEmpty(j, i, "void"))
					g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
			}
		}
		
		g.setColor(Color.BLACK);
		g.fillRect(0, height - borderSize, width, borderSize);
		g.fillRect(0, 0 , width, cellSize);
		g.fillRect(0, 0, borderSize, height);
		g.fillRect(width - borderSize, 0, borderSize, height);
	}

	public void update() {
		for(int r = rows - 1; r >= 0; r--) {
			for(int c = cols - 1; c >= 0; c--) {
				if(cells[c][r].name != "void")
					moveCell(c, r);
			}
		}
	}
	
	public void moveCell(int col, int row) {
		switch(cells[col][row].name) {
			case "sand":
				if(checkEmpty(col, row + 1, "void")) {
					cells[col][row].name = "void";
					cells[col][row + 1].name = "sand";
					cells[col][row].isFalling = false;
					cells[col][row + 1].isFalling = true;
				} else if(Math.random() > 0.5) {
					if(checkEmpty(col + 1, row + 1, "void")) {
						cells[col][row].name = "void";
						cells[col + 1][row + 1].name = "sand";
					}
				} else if(checkEmpty(col - 1, row + 1, "void")) {
					cells[col][row].name = "void";
					cells[col - 1][row + 1].name = "sand";
				}else if(Math.random() > 0.5) {
					if(checkEmpty(col + 2, row, "void") && cells[col][row].isFalling) {
						cells[col][row].name = "void";
						cells[col + 2][row].name = "sand";
						cells[col + 2][row].isFalling = false;
					}
				} else if(checkEmpty(col - 2, row, "void") && cells[col][row].isFalling) {
					cells[col][row].name = "void";
					cells[col - 2][row].name = "sand";
					cells[col - 2][row].isFalling = false;
				}
				break;
			case "fish":
				if(deltaTime >= fishSpd) {
					moveFish(col, row);
					deltaTime -= fishSpd;
				}
				break;
		}
	}
	
	public boolean checkEmpty(int col, int row, String name) {
		if(inBounds(col, row) && cells[col][row].name == name) {
			return true;
		}
		return false;
	}
	
	public boolean inBounds(int col, int row) {
		return row < rows && row >= 0 && col < cols && col >= 0;
	}
	
	public void pushCell(int col, int row) {
		if(checkEmpty(col + fishDir, row, "void"))
			cells[col + fishDir][row].name = "sand";
		else if(checkEmpty(col + fishDir, row, "sand"))
			pushCell(col + fishDir, row);
	}
	
	public void moveFish(int col, int row) {

		if(!inBounds(col + fishDir, row)) return;
		
		for(int i = 0; i <= fishStr; i++) {
			if(checkEmpty(col + i * fishDir, row, "void")) {
				break;
			} else if(checkEmpty(col + i * fishDir, row, "sand") && i == fishStr)
					return;
		}
		
		if(checkEmpty(col + fishDir, row, "sand")) {
			pushCell(col + fishDir, row);
		}
		
		cells[col][row].name = "void";
		cells[col + fishDir][row].name = "fish";
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX() / cellSize;
		int y = e.getY() / cellSize;
		
		if(e.getButton() == MouseEvent.BUTTON1)
		for(int i = y - brushSize; i <= y + brushSize; i++) {
			for(int j = x; (j-  x) * (j - x) + (i - y) * (i - y) <= brushSize * brushSize; j++) {
				if(i % 2 == (int) (Math.random() * 2) ||  (j == x && i == y - brushSize))
					if(checkEmpty(j, i, "void"))
						cells[j][i].name = "sand";
			}
			for(int j = x - 1; (j-  x) * (j - x) + (i - y) * (i - y) <= brushSize * brushSize; j--) {
				if(j % 2 == (int) (Math.random() * 2) && i % 2 == (int) (Math.random() * 2))
					if(checkEmpty(j, i, "void"))
						cells[j][i].name = "sand";
			}
		}
		
		if(e.getButton() == MouseEvent.BUTTON2)
			fishDir = fishDir == 1 ? -1 : 1;
	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		if(e.getWheelRotation() < 0)
			brushSize++;
		else
			brushSize--;
		
		if(brushSize < 0) brushSize = 0;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		mX = e.getX() / cellSize;
		mY = e.getY() / cellSize;
	
	}
}
