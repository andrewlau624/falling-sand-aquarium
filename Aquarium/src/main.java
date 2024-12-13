
import java.awt.Color;

import javax.swing.JFrame;

public class main extends JFrame {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame system = new JFrame();
		
		system.setResizable(false);
		system.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		system.setTitle("Aquarium");
		
		system.setUndecorated(true);
		system.setBackground(new Color(0, 0, 0, 0));
		
		Aquarium aquarium = new Aquarium();
		system.add(aquarium);
		
		system.pack();
		
		system.setLocationRelativeTo(null);
		system.setVisible(true);

	}

}
