import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImageCycler{
	static JSlider rr;
	static JSlider rg;
	static JSlider rb;
	static JSlider gr;
	static JSlider gg;
	static JSlider gb;
	static JSlider br;
	static JSlider bg;
	static JSlider bb;
	static BufferedImage source;
	static BufferedImage current;
	public static void main(String[] args){
		source = LoadImage("SW4_2Data/Images/Liam.png");
		BufferedImage next = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		Graphics g = next.getGraphics();
		g.drawImage(source.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH), 0, 0, null);
		source = next;
		JFrame frame = new JFrame();
		frame.setLocation(200, 000);
		frame.setVisible(true);
		frame.setSize(700, 900);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		JPanel display = new JPanel();
		display.setBackground(Color.black);
		display.setPreferredSize(new Dimension(500, 500));
		panel.add(display);
		frame.add(panel);
		rr = new JSlider(0,10);
		rg = new JSlider(0,10);
		rb = new JSlider(0,10);
		gr = new JSlider(0,10);
		gg = new JSlider(0,10);
		gb = new JSlider(0,10);
		br = new JSlider(0,10);
		bg = new JSlider(0,10);
		bb = new JSlider(0,10);
		JPanel red = new JPanel();
		red.setBackground(Color.gray);
		JPanel green = new JPanel();
		green.setBackground(Color.gray);
		JPanel blue = new JPanel();
		blue.setBackground(Color.gray);
		panel.add(red);
		panel.add(green);
		panel.add(blue);
		red.add(rr);
		rr.addChangeListener(new RewriteImage());
		red.add(rg);
		rg.addChangeListener(new RewriteImage());
		red.add(rb);
		rb.addChangeListener(new RewriteImage());
		green.add(gr);
		gr.addChangeListener(new RewriteImage());
		green.add(gg);
		gg.addChangeListener(new RewriteImage());
		green.add(gb);
		gb.addChangeListener(new RewriteImage());
		blue.add(br);
		br.addChangeListener(new RewriteImage());
		blue.add(bg);
		bg.addChangeListener(new RewriteImage());
		blue.add(bb);
		bb.addChangeListener(new RewriteImage());
		JButton save = new JButton("Save Image In CycleResults Folder");
		save.addActionListener(new SaveResult());
		panel.add(save);
		panel.updateUI();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderloop(display);
	}
	public static void renderloop(JPanel jp){
		while(true){
			if(current!=null){
				Graphics g = jp.getGraphics();
				g.drawImage(current, 0, 0, null);
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static class SaveResult implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(current != null){
				SaveImage(current, "CycleResults/Result0.png");
			}
		}
		
	}
	public static class RewriteImage implements ChangeListener{
		
		@Override
		public void stateChanged(ChangeEvent arg0) {
			double[] r = new double[]{0.1*rr.getValue(),0.1*rg.getValue(),0.1*rb.getValue()};
			double[] g = new double[]{0.1*gr.getValue(),0.1*gg.getValue(),0.1*gb.getValue()};
			double[] b = new double[]{0.1*br.getValue(),0.1*bg.getValue(),0.1*bb.getValue()};
			double[][] data = new double[][]{r, g, b};
			current = CycleImage(source, data);
		}
		
	}
	public static BufferedImage CycleImage(BufferedImage bi, double[][] data){
		BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int i = 0; i < bi.getWidth(); i++){
			for(int j = 0; j < bi.getHeight(); j++){
				Color c = new Color(bi.getRGB(i, j));
				int r = (int) (data[0][0] * c.getRed() + data[0][1] * c.getGreen() + data[0][2] * c.getBlue());
				int g = (int) (data[1][0] * c.getRed() + data[1][1] * c.getGreen() + data[1][2] * c.getBlue());
				int b = (int) (data[2][0] * c.getRed() + data[2][1] * c.getGreen() + data[2][2] * c.getBlue());
				if(r > 255){
					r = 255;
				}
				if(g > 255){
					g = 255;
				}
				if(b > 255){
					b = 255;
				}
				Color newC = new Color(r, g, b, c.getAlpha());
				copy.setRGB(i, j, newC.getRGB());
			}
		}
		return copy;
	}
	public static BufferedImage LoadImage(String filename){
		BufferedImage bill = null;
		try {
			bill = ImageIO.read(new File(filename));
		} catch (IOException e) {
			System.out.println(filename);
			e.printStackTrace();
		}
		return bill;
	}
	public static void SaveImage(BufferedImage bi, String filename){
		try{
			ImageIO.write(bi, "png", new File(filename));
		}catch(IOException e){
			System.out.println(filename);
			e.printStackTrace();
		}
	}
}