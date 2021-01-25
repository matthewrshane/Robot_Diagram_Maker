package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main implements Runnable {
	
	private JFrame frame;
	
	private static final int FRAME_WIDTH = 1200;
	private static final int FRAME_HEIGHT = 875;
	private static final int CANVAS_WIDTH = 1200;
	private static final int CANVAS_HEIGHT = 675;
	private static final int FPS = 60;
	
	private static final int GOAL_X = 508, GOAL_Y = 826;
	private static final int LINE_X = 1130, WALL_Y = 445;
	
	private static final double PX_PER_IN = 4.025;
	private static final Color OVERLAY_COLOR = new Color(1.0f, 0.0f, 0.0f, 0.5f);
	private static final double LOW_SLOPE = (902d - 827d)/(1517d - 490d);
	private static final double HIGH_SLOPE = (993d - 827d)/(1490d - 490d);
	
	private static final int DIST_SLIDER_SCALE = 1;
	private static final int C_SLIDER_SCALE = 100;
	private static final int D_SLIDER_SCALE = 100;
	
	private boolean running = false;
	private long tickCount = 0;
	
	private JLabel distLabel = new JLabel("Max distance (in): ");
	private JSlider distSlider = new JSlider(0 * DIST_SLIDER_SCALE, 300 * DIST_SLIDER_SCALE, 180 * DIST_SLIDER_SCALE);
	private JTextField distField = new JTextField(distSlider.getValue() / (double) DIST_SLIDER_SCALE + "", 6);
	
	private JLabel cLabel = new JLabel("Available inner hole size (in): ");
	private JSlider cSlider = new JSlider(0 * C_SLIDER_SCALE, 50 * C_SLIDER_SCALE, 4 * C_SLIDER_SCALE);
	private JTextField cField = new JTextField(cSlider.getValue() / (double) C_SLIDER_SCALE + "", 6);
	
	private JLabel dLabel = new JLabel("Available outer hole size (in): ");
	private JSlider dSlider = new JSlider(0 * D_SLIDER_SCALE, 50 * D_SLIDER_SCALE, (int) (32.25 * D_SLIDER_SCALE));
	private JTextField dField = new JTextField(dSlider.getValue() / (double) D_SLIDER_SCALE + "", 6);
	
	private JButton saveButton = new JButton("Save");
	
	private BufferedImage diagram = null;
	private BufferedImage diagramCached = null;
	private Graphics2D g;
	private boolean save = false;
	
	public static void main(String[] args) {
		new Main().start();
	}
	
	public Main() {
		frame = new JFrame("Robot Diagram Maker");
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		
		c.gridwidth = 48;
		c.gridheight = 27;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(new Content(), c);
		
		JPanel swingPanel = new JPanel();
		swingPanel.setLayout(new GridLayout(4, 3, 0, 10));
		
		swingPanel.add(distLabel);
		swingPanel.add(distSlider);
		swingPanel.add(distField);
		
		swingPanel.add(cLabel);
		swingPanel.add(cSlider);
		swingPanel.add(cField);
		
		swingPanel.add(dLabel);
		swingPanel.add(dSlider);
		swingPanel.add(dField);
		
		swingPanel.add(saveButton);
		
		c.gridwidth = 48;
		c.gridheight = 8;
		c.gridx = 0;
		c.gridy = 28;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(swingPanel, c);
		
		frame.add(mainPanel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private void init() {
		try {
			diagram = ImageIO.read(new File("C:\\Users\\Matthew\\Desktop\\2020 frc field diagram.png"));
			diagramCached = ImageIO.read(new File("C:\\Users\\Matthew\\Desktop\\2020 frc field diagram.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		distSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				distField.setText("" + distSlider.getValue() / (double) DIST_SLIDER_SCALE);
			}
		});
		
		distField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				distSlider.setValue((int) (Double.parseDouble(distField.getText()) * DIST_SLIDER_SCALE));
			}
		});
		
		cSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				cField.setText("" + cSlider.getValue() / (double) C_SLIDER_SCALE);
			}
		});
		
		cField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cSlider.setValue((int) (Double.parseDouble(cField.getText()) * C_SLIDER_SCALE));
			}
		});
		
		dSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				dField.setText("" + dSlider.getValue() / (double) D_SLIDER_SCALE);
			}
		});
		
		dField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dSlider.setValue((int) (Double.parseDouble(dField.getText()) * D_SLIDER_SCALE));
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				save = true;
			}
		});
	}
	
	public synchronized void start() {
		running = true;
		new Thread(this).start();
	}

	public synchronized void stop() {
		running = false;
	}

	public void run() {
		long lastTime = System.nanoTime();
		double nsPerTick = 1000000000D / FPS;

		int ticks = 0;
		int frames = 0;

		long lastTimer = System.currentTimeMillis();
		double delta = 0;
		long now;
		boolean render;

		while (running) {
			now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			render = false;

			while (delta >= 1) {
				ticks++;
				tick();
				delta--;
				render = true;
			}
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (render) {
				frames++;
				frame.repaint();
			}

			if (System.currentTimeMillis() - lastTimer >= 1000) {
				lastTimer += 1000;
				frames = 0;
				ticks = 0;
			}
		}
	}

	public void tick() {
		tickCount++;
	}
	
	class Content extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		public Dimension getPreferredSize() {
	        return new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
	    }

		public void paintComponent(Graphics graphics) {
			Graphics2D contentG = (Graphics2D) graphics;
			contentG.setColor(Color.WHITE);
			contentG.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
			
			diagram.setData(diagramCached.getData());
			g = (Graphics2D) diagram.getGraphics();
			
			double dist = (distSlider.getValue() / DIST_SLIDER_SCALE + 29.25) * PX_PER_IN;
			double c = cSlider.getValue() / C_SLIDER_SCALE;
			double d = dSlider.getValue() / D_SLIDER_SCALE;
			
			double x = Math.max(-(117*c)/(4*(c-d)), -(117*c)/(4*(c+d)));
			double a = Math.toDegrees(Math.atan(x/(c/2)));
			double theta = 180 - 2*a;
			double angle = theta/2;
			double slope = Math.tan(Math.toRadians(angle));
			x *= PX_PER_IN;
			
			g.setColor(OVERLAY_COLOR);
			for(int cX = LINE_X; cX < GOAL_X + dist; cX++) {
				double pos = (slope * cX) + (GOAL_Y - (slope * (GOAL_X - x)));
				double neg = (-slope * cX) + (GOAL_Y - (-slope * (GOAL_X - x)));
				
				int o = (int) Math.sqrt(Math.pow(dist, 2) - Math.pow(cX - GOAL_X, 2));
				int sY = (int) Math.max(GOAL_Y - o, Math.max(neg, WALL_Y));
				int eY = (int) Math.min(GOAL_Y + o, pos);
				
				int j = (int) (GOAL_X - x);
				int lowCut = (int) (LOW_SLOPE * (cX - j)) + GOAL_Y;
				int highCut = (int) (HIGH_SLOPE * (cX - j)) + GOAL_Y;
				
				if(cX > 1470) {
					g.fillRect(cX, sY, 1, lowCut - sY);
					g.fillRect(cX, highCut, 1, eY - highCut);
				} else {
					g.fillRect(cX, sY, 1, eY - sY);
				}
			}
			
			contentG.drawImage(diagram, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, null);
			
			if(save) {
				save = false;
				try {
					JFileChooser chooser = new JFileChooser();
					if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
						ImageIO.write(diagram, "jpeg", chooser.getSelectedFile());
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
