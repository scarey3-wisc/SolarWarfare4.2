import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class SolarWarfare4_2{
	public static ImageLibrary il;
	public static JFrame jf;
	public static JFrame gameFrame;
	public static boolean activeGame;
	public static void main(String[] args){
		jf = new JFrame("Solar Warfare Login Screen");
		int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		jf.setSize(width/2, height/2);
		jf.setLocation(width/4, height/4);
		jf.setVisible(true);
		JPanel jp = new JPanel();
		
		
		jp.setBackground(Color.black);
		jf.add(jp);
		
		JTextField namer = new JTextField("Please enter your username", 50);
		jp.add(namer);
		JTextField password = new JTextField("Please enter your password", 50);
		jp.add(password);
		JTextField gameNamer = new JTextField("Please enter the game name", 50);
		jp.add(gameNamer);
		jp.updateUI();
		Toolbox.sleep(100);
		
		JPanel southField = new JPanel();
		southField.setLayout(new BorderLayout());
		southField.setBackground(Color.white);
		southField.setPreferredSize(new Dimension((int) (width * 0.45), (int) (height * 0.35)));
		jp.add(southField);
		
		JButton startGame = new JButton("Start Game");
		southField.add(startGame, BorderLayout.EAST);
		startGame.addActionListener(new OpenGame(namer, gameNamer, password));
		
		JTextArea jta = new JTextArea();
		JScrollPane jsp = new JScrollPane(jta);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		southField.add(jsp, BorderLayout.CENTER);
		PrintStream ps = new PrintStream(new ConsoleOutput(jta));
		System.setOut(ps);
		System.setErr(ps);
		
		JTextField cheater = new JTextField("Enter Admin Codewords", 50);
		southField.add(cheater, BorderLayout.SOUTH);
		
		JButton cheat = new JButton("Command");
		southField.add(cheat, BorderLayout.WEST);
		cheat.addActionListener(new Cheater(cheater));
		
		jp.updateUI();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
	}
	public static void openNaiveGame(String gamename, String playername){
		il = new ImageLibrary(10);
		ArrayList<General> list = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		Map theMap = Map.loadTopologicalDescription(list, "SW4_2Data/" + gamename + "/WorldDesc.txt", Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.044);
		Relation.loadRelationMatrix(list, "SW4_2Data/" + gamename + "/RelationMatrix.txt");
		
		gameFrame = new JFrame();
		gameFrame.setUndecorated(true);
		//gameFrame.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		gameFrame.setLocation(0, 0);
		JPanel jp = new JPanel();
		gameFrame.add(jp);
		jp.setBackground(Color.white);
		gameFrame.setVisible(true);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jp.updateUI();
		Toolbox.sleep(500);
		
		GameManager gs = new GameManager(jp, theMap, list);
		gs.RunNaiveDropbox(list, playername, gamename);
		gs.startDrawLoop();
	}
	public static void checkForDuplicates(String gamename){
		ArrayList<General> list = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		Map theMap = Map.loadTopologicalDescription(list, "SW4_2Data/" + gamename + "/WorldDesc.txt", Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.033);
		for(Planet p: theMap.getAllPlanets()){
			for(Planet pp: theMap.getAllPlanets()){
				if(p.getName().equals(pp.getName()) && p != pp){
					System.out.println(p.getName());
				}
			}
		}
		System.out.println("Done");
	}
	public static void scrambleGenerals(String gamename){
		ArrayList<String> read = Toolbox.load("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		Collections.shuffle(read);
		Toolbox.save(read, "SW4_2Data/" + gamename + "/GeneralInfo.txt", false);
	}
	public static void analyzeMap(String gamename){
		ArrayList<General> playerlist = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		ArrayList<ArrayList<String>> status = Toolbox.loadSplit("SW4_2Data/" + gamename + "/FullDescription.txt");
		Map master = Map.createMap(status, playerlist, 70.0);
		master.calculateClaims();
		
		int mostTerritory = 0;
		General gmt = null;
		int mostPlanets = 0;
		General gmp = null;
		int mostResources = 0;
		General gmr = null;
		int mostHydrogenIncome = 0;
		General gmhi = null;
		int mostIronIncome = 0;
		General gmii = null;
		int mostGoldIncome = 0;
		General gmgi = null;
		int mostSiliconIncome = 0;
		General gmsi = null;
		int mostIncome = 0;
		General gmi = null;
		int mostShips = 0;
		General gmss = null;
		int mostRaiders = 0;
		General gmsr = null;
		int mostCruisers = 0;
		General gmsc = null;
		int mostJuggernauts = 0;
		General gmsj = null;
		int mostSpaceStations = 0;
		General gmst = null;
		int mostFactories = 0;
		General gmf = null;
		int mostTEV = 0;
		General gmTEV = null;
		for(General g: playerlist){
			System.out.println("Report For " + g.getFormalName());
			int territory = 0;
			int planets = 0;
			int resources = 0;
			int hydrogen = 0;
			int iron = 0;
			int gold = 0;
			int silicon = 0;
			int income = 0;
			int ships = 0;
			int raiders = 0;
			int cruisers = 0;
			int juggernauts = 0;
			int stations = 0;
			int factories = 0;
			int TEV = 0;
			for(int i = 0; i < master.cellWidth(); i++){
				for(int j = 0; j < master.cellHeight(); j++){
					Space analyze = master.getAt(i, j);
					if(analyze.containsClaim(g)){
						territory++;
					}
					if(analyze.getIdentifier() == 1){
						Planet anap = (Planet) analyze;
						if(anap.getOwner() == g){
							planets++;
							for(Structure s: anap.getStructures()){
								if(s.getType().equals("Hydrogen Refinery")){
									hydrogen++;
									income++;
								}
								if(s.getType().equals("Iron Refinery")){
									iron++;
									income++;
								}
								if(s.getType().equals("Gold Refinery")){
									gold++;
									income++;
								}
								if(s.getType().equals("Silicon Refinery")){
									silicon++;
									income++;
								}
								if(s.getType().equals("Space Station")){
									stations++;
								}
								if(s.getType().equals("Factory")){
									factories++;
								}
							}
						}
					}
					for(Resource r: analyze.getPile()){
						if(r.getOwner() == g){
							resources++;
						}
					}
					for(Ship s: analyze.getFleet()){
						if(s.getOwner() == g){
							ships++;
							if(s.getType() == 0){
								raiders++;
							}
							if(s.getType() == 1){
								cruisers++;
							}
							if(s.getType() == 2){
								juggernauts++;
							}
						}
					}
				}
			}
			System.out.println(territory + " spaces claimed.");
			if(territory > mostTerritory){
				mostTerritory = territory;
				gmt = g;
			}
			System.out.println(planets + " planets owned.");
			if(planets > mostPlanets){
				mostPlanets = planets;
				gmp = g;
			}
			System.out.println(resources + " resources stockpiled.");
			if(resources > mostResources){
				mostResources = resources;
				gmr = g;
			}
			System.out.println(hydrogen + " hydrogen produced.");
			if(hydrogen > mostHydrogenIncome){
				mostHydrogenIncome = hydrogen;
				gmhi = g;
			}
			System.out.println(iron + " iron produced.");
			if(iron > mostIronIncome){
				mostIronIncome = iron;
				gmii = g;
			}
			System.out.println(gold + " gold produced.");
			if(gold > mostGoldIncome){
				mostGoldIncome = gold;
				gmgi = g;
			}
			System.out.println(silicon + " silicon produced.");
			if(silicon > mostSiliconIncome){
				mostSiliconIncome = silicon;
				gmsi = g;
			}
			System.out.println(income + " total income.");
			if(income > mostIncome){
				mostIncome = income;
				gmi = g;
			}
			System.out.println(raiders + " raiders owned.");
			if(raiders > mostRaiders){
				mostRaiders = raiders;
				gmsr = g;
			}
			System.out.println(cruisers + " cruisers owned.");
			if(cruisers > mostCruisers){
				mostCruisers = cruisers;
				gmsc = g;
			}
			System.out.println(juggernauts + " juggernauts owned.");
			if(juggernauts > mostJuggernauts){
				mostJuggernauts = juggernauts;
				gmsj = g;
			}
			System.out.println(ships + " ships owned.");
			if(ships > mostShips){
				mostShips = ships;
				gmss = g;
			}
			System.out.println(stations + " space stations owned.");
			if(stations > mostSpaceStations){
				mostSpaceStations = stations;
				gmst = g;
			}
			System.out.println(factories + " factories owned.");
			if(factories > mostFactories){
				mostFactories = factories;
				gmf = g;
			}
			System.out.println();
			System.out.println();
		}
		System.out.println(gmp.getFormalName() + " has the most planets: " + mostPlanets + "!");
		System.out.println(gmt.getFormalName() + " has the largest territory: " + mostTerritory + "!");
		System.out.println(gmr.getFormalName() + " has stockpiled the most resources: " + mostResources + "!");
		System.out.println(gmhi.getFormalName() + " produces the most hydrogen: " + mostHydrogenIncome + "!");
		System.out.println(gmii.getFormalName() + " produces the most iron: " + mostIronIncome + "!");
		System.out.println(gmgi.getFormalName() + " produces the most gold: " + mostGoldIncome + "!");
		System.out.println(gmsi.getFormalName() + " produces the most silicon: " + mostSiliconIncome + "!");
		System.out.println(gmi.getFormalName() + " has the largest income: " + mostIncome + "!");
		System.out.println(gmss.getFormalName() + " has the largest fleet: " + mostShips + "!");
		System.out.println(gmsr.getFormalName() + " has the most raiders: " + mostRaiders + "!");
		System.out.println(gmsc.getFormalName() + " has the most cruisers: " + mostCruisers + "!");
		System.out.println(gmsj.getFormalName() + " has the most juggernauts: " + mostJuggernauts + "!");
		System.out.println(gmst.getFormalName() + " has the most space stations: " + mostSpaceStations + "!");
		System.out.println(gmf.getFormalName() + " has the most factories: " + mostFactories + "!");
	}
	public static void paintMap(String gamename){
		il = new ImageLibrary(10);
		ArrayList<General> playerlist = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		ArrayList<ArrayList<String>> status = Toolbox.loadSplit("SW4_2Data/" + gamename + "/FullDescription.txt");
		Map master = Map.createMap(status, playerlist, 70.0);
		master.calculateClaims();
		master.setDeltaX(70);
		master.setDeltaY(70);
		BufferedImage bi = new BufferedImage((int) (master.pixelWidth() + 140), (int) (master.pixelHeight()+140), BufferedImage.TYPE_INT_ARGB);
		master.paintMap(bi.getWidth(), bi.getHeight(), playerlist.get(0), bi.getGraphics(), false);
		String filename = "SW4_2Data/Images/" + System.currentTimeMillis() + ".png";
		try{
			ImageIO.write(bi, "png", new File(filename));
		}catch(IOException e){
			System.out.println(filename);
			e.printStackTrace();
		}
	}
	public static void fuseMap(String gamename){
		ArrayList<General> playerlist = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		ArrayList<ArrayList<String>> status = Toolbox.loadSplit("SW4_2Data/" + gamename + "/FullDescription.txt");
		Map master = Map.createMap(status, playerlist, 1.0);
		for(General g: playerlist){
			ArrayList<ArrayList<String>> me = Toolbox.loadSplit("SW4_2Data/" + gamename + "/"+ g.getInformalName() + "Description.txt");
			if(me != null){
				Map indi = Map.createMap(me, playerlist, 1.0);
				for(Planet p: indi.getAllPlanets()){
					if(p.getOwner() == g){
						master.replaceAt(p.getX(), p.getY(), p);
					}
				}
			}
		}
		ArrayList<String> data = new ArrayList<String>();
		data.add("1");
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for(int i = 0; i < playerlist.size(); i++){
			numbers.add(i);
		}
		Collections.shuffle(numbers);
		String tO = "";
		for(int i = 0; i < numbers.size(); i++){
			tO += numbers.get(i);
			if(i != numbers.size() - 1){
				tO += "!";
			}
		}
		data.add(tO);
		data.add("-1");
		data.add("*");
		data.addAll(master.fullDescription(playerlist));
		Toolbox.save(data, "SW4_2Data/" + gamename + "/FusedDescription.txt", false);
	}
	public static boolean openSlowGame(String gamename, String playername, String password){
		ArrayList<General> playerlist = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		boolean accepted = false;
		for(General g: playerlist){
			if(g.getInformalName().equals(playername) && g.checkPassword(password)){
				accepted = true;
			}
		}
		if(!accepted){
			return false;
		}
		il = new ImageLibrary(10);
		gameFrame = new JFrame();
		gameFrame.setUndecorated(true);
		gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		gameFrame.setLocation(0, 0);
		JPanel jp = new JPanel();
		gameFrame.add(jp);
		jp.setBackground(Color.white);
		gameFrame.setVisible(true);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jp.updateUI();
		Toolbox.sleep(500);
		GameManager gs = new GameManager(jp, gamename, playername);
		gs.startDrawLoop();
		return true;
	}
	public static void openMonoGame(String gamename){
		il = new ImageLibrary(10);
		ArrayList<General> list = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		Map theMap = Map.loadTopologicalDescription(list, "SW4_2Data/" + gamename + "/WorldDesc.txt", Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.044);
		Relation.loadRelationMatrix(list, "SW4_2Data/" + gamename + "/RelationMatrix.txt");
		gameFrame = new JFrame();
		gameFrame.setUndecorated(true);
		gameFrame.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		gameFrame.setLocation(0, 0);
		JPanel jp = new JPanel();
		gameFrame.add(jp);
		jp.setBackground(Color.white);
		gameFrame.setVisible(true);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jp.updateUI();
		Toolbox.sleep(500);
		
		GameManager gs = new GameManager(jp, theMap, list);
		gs.Run1V1(list);
		gs.startDrawLoop();
	}
	public static void openWorldCreater(int x, int y, String gamename){
		il = new ImageLibrary(10);
		ArrayList<General> list = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		Map theMap;
		try{
			theMap = Map.loadTopologicalDescription(list, "SW4_2Data/" + gamename + "/WorldDesc.txt", Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.033);
		}catch(Exception e){
			theMap = new Map(x, y, Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.033);
		}
		
		gameFrame = new JFrame();
		gameFrame.setUndecorated(true);
		gameFrame.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		gameFrame.setLocation(0, 0);
		JPanel jp = new JPanel();
		gameFrame.add(jp);
		jp.setBackground(Color.white);
		gameFrame.setVisible(true);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jp.updateUI();
		Toolbox.sleep(500);
		
		GameManager gs = new GameManager(jp, theMap, list);
		gs.CreateWorld(list, "SW4_2Data/StaticFiles/HalbethisNames.txt", "SW4_2Data/" + gamename + "/WorldDesc.txt");
		gs.startDrawLoop();
	}
	public static void startGame(String gamename) {
		File description = new File("SW4_2Data/" + gamename + "/FullDescription.txt");
		File game = new File("SW4_2Data/" + gamename + "/FusedDescription.txt");
		File backup = new File("SW4_2Data/" + gamename + "/DefaultMap.txt");
		if(game.exists()) {
			description.renameTo(backup);
			game.renameTo(description);
		}else {
			return;
		}
		
		System.out.println("Begin: Complete");
	}
	public static void resetGame(String gamename) {
		File description = new File("SW4_2Data/" + gamename + "/FullDescription.txt");
		File backup = new File("SW4_2Data/" + gamename + "/DefaultMap.txt");
		if(backup.exists()) {
			description.delete();
			backup.renameTo(description);
		}else {
			return;
		}
		ArrayList<General> list = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		for(General g: list) {
			File hisFile = new File("SW4_2Data/" + gamename + "/" + g.getInformalName() + "Description.txt");
			hisFile.delete();
		}
		System.out.println("Reset: Complete");
	}
	public static class Cheater implements ActionListener, Runnable{
		private JTextField info;
		public Cheater(JTextField i){
			info = i;
		}
		@Override
		public void run() {
			String[] read = info.getText().split(", ");
			if(read[0].equals("WorldBuilder")){
				openWorldCreater(Integer.parseInt(read[1]), Integer.parseInt(read[2]), read[3]);
			}
			if(read[0].equals("Mono")){
				openMonoGame(read[1]);
			}
			if(read[0].equals("Fuse")){
				fuseMap(read[1]);
				System.out.println("Fuse: Complete");
			}
			if(read[0].equals("Scramble")){
				scrambleGenerals(read[1]);
				System.out.println("Scramble: Complete");
			}
			if(read[0].equals("Sweep")){
				checkForDuplicates(read[1]);
				System.out.println("Sweep: Complete");
			}
			if(read[0].equals("Encrypt")){
				Toolbox.encodeFile(read[1]);
				System.out.println("Encrypt: Complete");
			}
			if(read[0].equals("Decrypt")){
				Toolbox.decodeFile(read[1]);
				System.out.println("Decypt: Complete");
			}
			if(read[0].equals("Display")){
				paintMap(read[1]);
				System.out.println("Display: Complete");
			}
			if(read[0].equals("Analyze")){
				analyzeMap(read[1]);
			}
			if(read[0].equals("Initiate")) {
				startGame(read[1]);
			}
			if(read[0].equals("Reset")) {
				resetGame(read[1]);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Thread start = new Thread(this);
			start.start();
		}
		
	}
	public static class OpenGame implements ActionListener, Runnable{
		private JTextField pname;
		private JTextField pword;
		private JTextField gname;
		private boolean good;
		public OpenGame(JTextField p, JTextField g, JTextField w){
			pname = p;
			gname = g;
			pword = w;
			good = true;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(good){
				Thread start = new Thread(this);
				start.start();
			}
		}
		@Override
		public void run() {
			good = false;
			boolean result = openSlowGame(gname.getText(), pname.getText(), pword.getText());
			if(result){
				jf.setVisible(false);
			}else{
				System.out.println("Invalid Username/password");
			}
			
			good = true;
		}
		
	}
	public static void kill(){
		gameFrame.setVisible(false);
		il.save(false);
		jf.setVisible(true);
	}
	public static class ConsoleOutput extends OutputStream{
		private JTextArea jta;
		public ConsoleOutput(JTextArea jta){
			this.jta = jta;
		}
		@Override
		public void write(int arg0) throws IOException {
			jta.append(String.valueOf((char)arg0));
			jta.setCaretPosition(jta.getDocument().getLength());
		}
		
	}
}