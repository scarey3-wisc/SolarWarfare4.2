import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JPanel;

public class GameManager{
	private JPanel jp;
	private Map world;
	private RenderLoop renderer;
	public static General viewing;
	private DetailWindow info;
	private LinkedList<Change> changeStack;
	private BrowseClick mainClick;
	private int mouseX, mouseY;
	public static boolean obscureEnemies;
	private ArrayList<General> playerlist;
	public GameManager(JPanel p, Map w, ArrayList<General> plg){
		jp = p;
		world = w;
		mouseX = 0;
		mouseY = 0;
		mainClick = new BrowseClick();
		changeStack = new LinkedList<Change>();
		info = new DetailWindow(mainClick, this);
		obscureEnemies = false;
		playerlist = plg;
	}
	public GameManager(JPanel p, String gamename, String playername){
		jp = p;
		mouseX = 0;
		mouseY = 0;
		changeStack = new LinkedList<Change>();
		obscureEnemies = false;
		playerlist = General.loadGenerals("SW4_2Data/" + gamename + "/GeneralInfo.txt");
		mainClick = new BrowseClick();
		info = new DetailWindow(mainClick, this);
		SlowFlow sf = new SlowFlow(playerlist, playername, gamename);
		linkListener(mainClick);
		linkListener(sf);
		SlowFlow.FilePinger fp = sf.new FilePinger();
		Thread runner = new Thread(fp);
		runner.start();
	}
	public void Run1V1(ArrayList<General> plist){
		MonoFlow mf = new MonoFlow(plist);
		linkListener(mainClick);
		linkListener(mf);
	}
	public void RunNaiveDropbox(ArrayList<General> plist, String playername, String gamename){
		NaiveFlow nf = new NaiveFlow(plist, playername, gamename);
		linkListener(mainClick);
		linkListener(nf);
		Toolbox.sleep(50);
		NaiveFlow.FilePinger fp = nf.new FilePinger();
		Thread runer = new Thread(fp);
		runer.start();
	}
	public void CreateWorld(ArrayList<General> plist, String namesfileppath, String savefilepath){
		ArrayList<String> names = Toolbox.load(namesfileppath);
		LinkedList<String> realNames = new LinkedList<String>();
		realNames.addAll(names);
		for(Planet p: world.getAllPlanets()){
			realNames.remove(p.getName());
		}
		CreationFlow cf = new CreationFlow(plist, realNames, savefilepath);
		linkListener(cf);
	}
	public void startDrawLoop(){
		Toolbox.sleep(50);
		renderer = new RenderLoop();
		Thread runer = new Thread(renderer);
		runer.start();
	}
	public void setEmpireCreationMode(){
		obscureEnemies = true;
		Structure.CreationAllowed = true;
		//Structure.ImmediateResourceGeneration = true;
	}
	public void setPlayMode(){
		obscureEnemies = false;
		Structure.CreationAllowed = false;
		//Structure.ImmediateResourceGeneration = false;
	}
	public void linkListener(ScreenListener sl){
		jp.addMouseListener(sl);
		jp.addMouseMotionListener(sl);
		jp.addKeyListener(sl);
	}
	public void unlinkListener(ScreenListener sl){
		jp.removeMouseListener(sl);
		jp.removeMouseMotionListener(sl);
		jp.removeKeyListener(sl);
	}
	public void notifyOfSelectionClick(Ship s, Space l){
		mainClick.shipSelectionLogic(s, l);
	}
	public void change(Change c){
		c.makeChange();
		changeStack.addFirst(c);
	}
	public void unchange(){
		if(changeStack.size() > 0){
			Change c = changeStack.removeFirst();
			c.undoChange();
		}
	}
	public boolean obscureEnemies(){
		return obscureEnemies;
	}
	public ArrayList<General> getPlayerlist(){
		return playerlist;
	}
	public Map getWorld(){
		return world;
	}
	public class SlowFlow implements ScreenListener{
		private boolean linked;
		private LinkedList<General> turnOrder;
		ArrayList<General> pList;
		private int number;
		private String gamename;
		private General active;
		private boolean paused;
		private int height, width;
		private double otherLength;
		ArrayList<String> pauseOptions;
		public SlowFlow(ArrayList<General> pList, String playername, String gamename){
			this.gamename = gamename;
			linked = false;
			this.pList = pList;
			for(int i = 0; i < pList.size(); i++){
				General g = pList.get(i);
				g.setIndex(i);
				if(g.getInformalName().equals(playername)){
					viewing = g;
				}
			}
			number = -1;
			pingStatusFile("FullDescription");
			if(number == 0){
				pingStatusFile(viewing.getInformalName() + "Description");
			}
			if(!viewing.isActive()){
				pingDiplomaticChangeFile(viewing.getInformalName() + "Diplomacy", true);
			}
			otherLength = world.getSideLength()/2;
			pauseOptions = new ArrayList<String>();
			if(number == 0 || number == 1){
				pauseOptions.add("Return to Game");
				pauseOptions.add("Reset Empire");
				pauseOptions.add("Save Empire");
				pauseOptions.add("Reset Empire and Exit");
				pauseOptions.add("Save Empire and Exit");
				pauseOptions.add("Exit");
			}else{
				pauseOptions.add("Return to Game");
				pauseOptions.add("Save Moves");
				pauseOptions.add("End Turn");
				pauseOptions.add("Save Moves and Exit");
				pauseOptions.add("End Turn and Exit");
				pauseOptions.add("Exit");
			}
			
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			Toolbox.delay();
			Toolbox.lock();
			if(arg0.getKeyChar() == 27){
				if(paused){
					paused = false;
					linkListener(mainClick);
				}else{
					paused = true;
					unlinkListener(mainClick);
				}
			}
			if(arg0.getKeyChar() == 6){
				double temp = world.getSideLength();
				world.setSideLength(otherLength);
				otherLength = temp;
			}
			Toolbox.release();
		}
		private void saveChangeStack(String filename){
			ArrayList<String> data = new ArrayList<String>();
			if(active == viewing || viewing.isActive()){
				System.out.println("The Save Change Stack method should *only* occur for inactive players.");
			}
			for(int i = changeStack.size() - 1; i>=0; i--){
				Change c = changeStack.get(i);
				ArrayList<String> s = c.getDescription();
				if(s == null){
					System.out.println("We should ONLY have changes where I've written the description");
				}else if(s.size() != 1){
					System.out.println("The only descriptions in this stack should be 1 long");
				}else{
					String sample = s.get(0).split("!")[0];
					if(!(sample.equals("Trading")||sample.equals("Exchange")||sample.equals("Relation"))){
						System.out.println("We should only have descriptions from those 3 changes");
					}else{
						data.add(s.get(0));
					}
				}
			}
			Toolbox.save(data, "SW4_2Data/" + gamename + "/" + filename + ".txt", true);
		}
		private void pingDiplomaticChangeFile(String filename, boolean personal){
			String filepath = "SW4_2Data/" + gamename + "/" + filename + ".txt";
			ArrayList<String> info = Toolbox.load(filepath);
			for(String s: info){
				String[] desc = s.split("!");
				Change c = null;
				if(desc[0].equals("Trading")){
					c = new DetailWindow.TradeChange(pList, getWorld(), desc);
				}else if(desc[0].equals("Exchange")){
					c = new DetailWindow.ExchangeChange(getWorld(), pList, desc);
				}else if(desc[0].equals("Relation")){
					c = new DetailWindow.ChangeRelation(pList, desc);
				}else{
					System.out.println("This *really* shouldn't happen");
				}
				if(c != null){
					if(personal){
						change(c);
					}else{
						c.makeChange();
					}
				}
			}
			if(!personal){
				try{
					File f = new File(filepath);
					f.delete();
				}catch(Exception e){
					
				}
			}
		}
		private void saveDescriptionFile(String filename){
			ArrayList<String> data = new ArrayList<String>();
			data.add(Integer.toString(number));
			String tO = "";
			for(int i = 0; i < turnOrder.size(); i++){
				tO += turnOrder.get(i).getIndex();
				if(i != turnOrder.size() - 1){
					tO += "!";
				}
			}
			data.add(tO);
			if(active == null){
				data.add("-1");
			}else{
				data.add("" + active.getIndex());
			}
			data.add("*");
			data.addAll(world.fullDescription(pList));
			Toolbox.save(data, "SW4_2Data/" + gamename + "/" + filename + ".txt", true);
		}
		private void pingStatusFile(String filename){
			ArrayList<ArrayList<String>> status = Toolbox.loadSplit("SW4_2Data/" + gamename + "/"+filename+".txt");
			if(status == null){
				return;
			}
			int newNumber = Integer.parseInt(status.get(0).get(0));
			if(number >= newNumber){
				return;
			}else{
				Toolbox.delay();
				Toolbox.lock();
				number = newNumber;
				turnOrder = new LinkedList<General>();
				for(String s: status.get(0).get(1).split("!")){
					int index = Integer.parseInt(s);
					turnOrder.add(pList.get(index));
				}
				int activeIndex = Integer.parseInt(status.get(0).get(2));
				if(activeIndex >= 0 && activeIndex < pList.size()){
					active = pList.get(activeIndex);
					active.activate();
				}
				world = Map.createMap(status, playerlist, Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.044);
				if(number == 0){
					Structure.ResetShipCreation();
					setEmpireCreationMode();
					mainClick.deselectShips();
					mainClick.deselectLoc();
					changeStack = new LinkedList<Change>();
					viewing.activate();
					info.notifyOfChange(viewing);
				}else{
					setPlayMode();
					if(turnOrder.get(0) == viewing && !viewing.isActive()){
						world.beginTurn(viewing);
						mainClick.deselectShips();
						mainClick.deselectLoc();
						changeStack = new LinkedList<Change>();
						active = viewing;
						viewing.activate();
						for(General g: playerlist){
							pingDiplomaticChangeFile(g.getInformalName() + "Diplomacy", false);
						}
						info.notifyOfChange(viewing);
						number++;
						saveDescriptionFile("FullDescription");
					}
				}
				world.calculateClaims();
				Toolbox.release();
			}
		}
		public class FilePinger implements Runnable{
			@Override
			public void run() {
				while(true){
					if(!viewing.isActive() || number == -1){
						pingStatusFile("FullDescription");
					}
					Toolbox.sleep(5000);
				}
			}
			
		}
		private void respondToClick(int index){
			if(index == 0){
				//return to game
				paused = false;
				linkListener(mainClick);
			}else if(index == 1){
				//save moves or reset empire
				mainClick.deselectShips();
				mainClick.deselectLoc();
				info.notifyOfChange(viewing);
				if(viewing.isActive()){
					changeStack = new LinkedList<Change>();
					if(number == 0 || number == 1){
						try{
							File f = new File("SW4_2Data/" + gamename + "/" +viewing.getInformalName()+"Description.txt");
							f.delete();
						}catch(Exception e){
							System.out.println("Not surprising");
						}
						number = -1;
					}else{
						saveDescriptionFile("FullDescription");
					}
				}else{
					if(number == 0 || number == 1){
						try{
							File f = new File("SW4_2Data/" + gamename + "/" +viewing.getInformalName()+"Description.txt");
							f.delete();
						}catch(Exception e){
							System.out.println("Not surprising");
						}
						number = -1;
					}else{
						saveChangeStack(viewing.getInformalName() + "Diplomacy");
					}
				}
				paused = false;
				linkListener(mainClick);
			}else if(index == 2){
				//end turn
				mainClick.deselectShips();
				mainClick.deselectLoc();
				info.notifyOfChange(viewing);
				if(viewing.isActive()){
					changeStack = new LinkedList<Change>();
					viewing.deactivate();
					active = null;
					number++;
					if(number == 1){
						saveDescriptionFile(viewing.getInformalName() + "Description");
					}else if(number%2 == 1){
						turnOrder.addLast(turnOrder.removeFirst());
						saveDescriptionFile("FullDescription");
						saveDescriptionFile("Backups/FullDescription" + number);
					}else{
						number--;
						System.out.println("We're just going to ignore this bit.");
					}
					
				}
				paused = false;
				linkListener(mainClick);
			}else if(index == 3){
				mainClick.deselectShips();
				mainClick.deselectLoc();
				info.notifyOfChange(viewing);
				if(viewing.isActive()){
					changeStack = new LinkedList<Change>();
					if(number == 0 || number == 1){
						try{
							File f = new File("SW4_2Data/" + gamename + "/" +viewing.getInformalName()+"Description.txt");
							f.delete();
						}catch(Exception e){
							
						}
						number = -1;
					}else{
						saveDescriptionFile("FullDescription");
					}
				}else{
					if(number == 0 || number == 1){
						try{
							File f = new File("SW4_2Data/" + gamename + "/" +viewing.getInformalName()+"Description.txt");
							f.delete();
						}catch(Exception e){
							
						}
						number = -1;
					}else{
						saveChangeStack(viewing.getInformalName() + "Diplomacy");
					}
				}
				SolarWarfare4_2.kill();
			}else if(index == 4){
				//end turn and exit
				mainClick.deselectShips();
				mainClick.deselectLoc();
				info.notifyOfChange(viewing);
				if(viewing.isActive()){
					changeStack = new LinkedList<Change>();
					viewing.deactivate();
					active = null;
					number++;
					if(number == 1){
						saveDescriptionFile(viewing.getInformalName() + "Description");
					}else if(number%2 == 1){
						turnOrder.addLast(turnOrder.removeFirst());
						saveDescriptionFile("FullDescription");
						saveDescriptionFile("Backups/FullDescription" + number);
					}else{
						number--;
						System.out.println("We're just ignoring this bit...");
					}
					
				}
				SolarWarfare4_2.kill();
			}else if(index == 5){
				SolarWarfare4_2.kill();
			}else{
				
			}
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(paused){
				int h = (int) (height * 0.7);
				int w = (int) (h * 0.6);
				int sh = h/pauseOptions.size();
				int rw = (int) (w*0.9);
				int rh = (int) (h * 0.05);
				int xmid = width/2;
				int ystart = height/2 - h/2;
				int mx = arg0.getX();
				int my = arg0.getY();
				if(mx > xmid - rw && mx < xmid + rw){
					int yIndex = (my-ystart)/sh;
					int yPlacement = (my-ystart)%sh;
					if(yPlacement > sh/2-rh/2&& yPlacement < sh/2+rh/2){
						if(yIndex >= 0 && yIndex < pauseOptions.size()){
							respondToClick(yIndex);
						}
					}
				}
			}
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
		@Override
		public void mouseDragged(MouseEvent arg0) {
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			jp.requestFocus();
		}
		@Override
		public void mouseMoved(MouseEvent arg0) {
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			jp.requestFocus();
		}
		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent arg0) {}
		@Override
		public void deactivate() {}
		@Override
		public void paintLayerOne(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerTwo(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerThree(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerFour(int width, int height, General viewing, Graphics g) {
			if(paused){
				this.width = width;
				this.height = height;
				int h = (int) (height * 0.7);
				int w = (int) (h * 0.6);
				BufferedImage background = SolarWarfare4_2.il.findImage("Escape").getFit(width, h).getSubimage(0, 0, w, h);
				DetailWindow.greyOutline(width/2-w/2, height/2-h/2, w, h, w/-10, g);
				g.drawImage(background, width/2-w/2, height/2-h/2, null);
				
				int sh = h/pauseOptions.size();
				int xmid = width/2;
				int ymid = height/2 - h/2 + sh/2;
				for(String s: pauseOptions){
					int rw = (int) (w * 0.9);
					int rh = (int) (h * 0.05);
					int mult = 1;
					if(mouseX>xmid-rw/2&&mouseX<xmid+rw/2&&mouseY>ymid-rh/2&&mouseY<ymid+rh/2){
						mult = -1;
					}
					DetailWindow.greyBox(xmid-rw/2, ymid-rh/2, rw, rh, rh/(4*mult), g);
					g.setColor(new Color(255, 255, 255));
					g.setFont(g.getFont().deriveFont(Font.BOLD));
					TextPackage tp = new TextPackage(s, rw, rh, 1, g);
					g.setFont(g.getFont().deriveFont(tp.getFontValue()));
					int stringH = g.getFontMetrics().getHeight();
					for(int i = 0; i < tp.getResult().length; i++){
						String write = tp.getResult()[i];
						int swidth = g.getFontMetrics().stringWidth(write);
						g.drawString(s, xmid-swidth/2, ymid + stringH/4 - stringH/2 * (tp.getResult().length/2 - i));
					}
					ymid+=sh;
				}
			}
		}
		@Override
		public boolean isLinked() {
			return linked;
		}
		@Override
		public void setLinked(boolean linked) {
			this.linked = linked;
		}
	}
	public class CreationFlow extends DeltaClick{
		private ArrayList<General> pList;
		private LinkedList<String> pNames;
		private String filepath;
		public CreationFlow(ArrayList<General> pList, LinkedList<String> planetNames, String filepath){
			this.pList = pList;
			this.filepath = filepath;
			this.pNames = planetNames;
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			Toolbox.delay();
			Toolbox.lock();
			if(arg0.getButton() == 1){
				Space s = world.getHexagonConversion(mouseX, mouseY);
				if(s.getIdentifier() == 1){
					Planet former = (Planet) s;
					pNames.add(former.getName());
				}
				if(s.isTrade()){
					Space newS = new Space(s.getX(), s.getY(), false);
					world.replaceAt(s.getX(), s.getY(), newS);
				}else{
					Space newS = new Space(s.getX(), s.getY(), true);	
					world.replaceAt(s.getX(), s.getY(), newS);
				}
			}else if(arg0.getButton() == 3){
				//planet
				Space s = world.getHexagonConversion(mouseX, mouseY);
				int x = s.getX();
				int y = s.getY();
				LinkedList<Integer> production = new LinkedList<Integer>();
				production.add(0);
				production.add(1);
				production.add(2);
				production.add(3);
				int[] prod = new int[production.size()];
				for(int i = 0; i < prod.length; i++){
					prod[i] = production.remove((int) (Math.random() * production.size()));
				}
				String name = "";
				General owner = null;
				if(s.getIdentifier() == 1){
					Planet former = (Planet) s;
					name = former.getName();
					owner = former.getOwner();
				}else{
					name = pNames.remove((int)(Math.random()*pNames.size()));
				}
				Planet p = new Planet(x, y, name, prod);
				p.setOwner(owner);
				world.replaceAt(x, y, p);
			}
			Toolbox.release();
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			Toolbox.delay();
			Toolbox.lock();
			if(arg0.getKeyChar() == 19){
				//save
				world.saveTopologicalDescription(pList, filepath);
			}else if(arg0.getKeyChar() == 27){
				SolarWarfare4_2.kill();
			}else{
				//set owner
				int index = arg0.getKeyChar() - 97;
				Space s = world.getHexagonConversion(mouseX, mouseY);
				if(s.getIdentifier() == 1){
					Planet set = (Planet) s;
					if(index < pList.size() && index >= 0){
						set.setOwner(pList.get(index));
					}else{
						set.setOwner(null);
					}
				}
			}
			Toolbox.release();
		}
		/*@Override
		public void mouseDragged(MouseEvent arg0) {
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			jp.requestFocus();
		}*/
		@Override
		public void mouseMoved(MouseEvent arg0) {
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			jp.requestFocus();
		}
	}
	public class NaiveFlow implements ScreenListener{
		private boolean linked;
		private LinkedList<General> turnOrder;
		ArrayList<General> pList;
		private boolean creating;
		private int number;
		private String gamename;
		public NaiveFlow(ArrayList<General> pList, String playername, String gamename){
			this.gamename = gamename;
			linked = false;
			this.pList = pList;
			for(int i = 0; i < pList.size(); i++){
				General g = pList.get(i);
				g.setIndex(i);
				if(g.getInformalName().equals(playername)){
					viewing = g;
				}
			}
			number = -1;
			pingStatusFile();
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			Toolbox.delay();
			Toolbox.lock();
			if(arg0.getKeyChar() == ' '){
				if(viewing.isActive()){
					boolean remember = obscureEnemies;
					obscureEnemies = false;
					mainClick.deselectShips();
					mainClick.deselectLoc();
					changeStack = new LinkedList<Change>();
					viewing.deactivate();
					info.notifyOfChange(viewing);
					number++;
					if(creating && number == pList.size()){
						creating = false;
						remember = false;
					}
					turnOrder.addLast(turnOrder.removeFirst());
					world.saveMap(pList, gamename);
					saveStatusFile();
					obscureEnemies = remember;
				}
			}else if(arg0.getKeyChar() == 27){
				SolarWarfare4_2.kill();
			}
			Toolbox.release();
		}
		private void saveStatusFile(){
			ArrayList<String> data = new ArrayList<String>();
			data.add(Integer.toString(number));
			String tO = "";
			for(int i = 0; i < turnOrder.size(); i++){
				tO += turnOrder.get(i).getIndex();
				if(i != turnOrder.size() - 1){
					tO += "!";
				}
			}
			data.add(tO);
			data.add(Boolean.toString(creating));
			Toolbox.save(data, "SW4_2Data/" + gamename + "/GameStatus.txt", false);
		}
		private void pingStatusFile(){
			ArrayList<String> status = Toolbox.load("SW4_2Data/" + gamename + "/GameStatus.txt");
			int newNumber = Integer.parseInt(status.get(0));
			if(number == newNumber){
				return;
			}else{
				Toolbox.sleep(5000);
				Toolbox.delay();
				Toolbox.lock();
				number = newNumber;
				turnOrder = new LinkedList<General>();
				for(String s: status.get(1).split("!")){
					int index = Integer.parseInt(s);
					turnOrder.add(pList.get(index));
				}
				creating = Boolean.parseBoolean(status.get(2));
				world = Map.loadMap(pList, gamename, Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.044);
				if(turnOrder.get(0) == viewing){
					if(creating){
						Structure.ResetShipCreation();
						setEmpireCreationMode();
					}else{
						setPlayMode();
						world.beginTurn(viewing);
					}
					mainClick.deselectShips();
					mainClick.deselectLoc();
					changeStack = new LinkedList<Change>();
					viewing.activate();
					info.notifyOfChange(viewing);
				}
				Toolbox.release();
			}
		}
		public class FilePinger implements Runnable{
			@Override
			public void run() {
				while(true){
					if(!viewing.isActive()){
						pingStatusFile();
					}
					Toolbox.sleep(5000);
				}
			}
			
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
		@Override
		public void mouseDragged(MouseEvent arg0) {jp.requestFocus();}
		@Override
		public void mouseMoved(MouseEvent arg0) {jp.requestFocus();}
		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent arg0) {}
		@Override
		public void deactivate() {}
		@Override
		public void paintLayerOne(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerTwo(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerThree(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerFour(int width, int height, General viewing, Graphics g) {	}
		@Override
		public boolean isLinked() {
			return linked;
		}
		@Override
		public void setLinked(boolean linked) {
			this.linked = linked;
		}
	}
	public class MonoFlow implements ScreenListener{
		private boolean linked;
		private ArrayList<General> pList;
		private int index;
		private boolean creating;
		public MonoFlow(ArrayList<General> pList){
			linked = false;
			this.pList = pList;
			index = 0;
			creating = true;
			setEmpireCreationMode();
			viewing = pList.get(index);
			viewing.activate();
			Structure.ResetShipCreation();
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			Toolbox.delay();
			Toolbox.lock();
			if(arg0.getKeyChar() == ' '){
				mainClick.deselectShips();
				mainClick.deselectLoc();
				changeStack = new LinkedList<Change>();
				viewing.deactivate();
				if(creating){
					index++;
					if(index < pList.size()){
						viewing = pList.get(index);
						Structure.ResetShipCreation();
					}else{
						index = 0;
						creating = false;
						setPlayMode();
						viewing = pList.get(index);
						world.beginTurn(viewing);
					}
				}else{
					index = (index + 1)%pList.size();
					viewing = pList.get(index);
					world.beginTurn(viewing);
				}
				viewing.activate();
				info.notifyOfChange(viewing);
			}
			Toolbox.release();
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
		@Override
		public void mouseDragged(MouseEvent arg0) {
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			jp.requestFocus();
		}
		@Override
		public void mouseMoved(MouseEvent arg0) {
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			jp.requestFocus();
		}
		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent arg0) {}
		@Override
		public void deactivate() {}
		@Override
		public void paintLayerOne(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerTwo(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerThree(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerFour(int width, int height, General viewing, Graphics g) {	}
		@Override
		public boolean isLinked() {
			return linked;
		}
		@Override
		public void setLinked(boolean linked) {
			this.linked = linked;
		}
	}
	public interface ScreenListener extends MouseListener, MouseMotionListener, KeyListener{
		public void deactivate();
		public void paintLayerOne(int width, int height, General viewing, Graphics g);
		public void paintLayerTwo(int width, int height, General viewing, Graphics g);
		public void paintLayerThree(int width, int height, General viewing, Graphics g);
		public void paintLayerFour(int width, int height, General viewing, Graphics g);
		public boolean isLinked();
		public void setLinked(boolean linked);
	}
	public class BrowseClick extends DeltaClick{
		private LinkedList<Ship> selected;
		private Space specLocation;
		public BrowseClick() {
			super();
			selected = new LinkedList<Ship>();
			specLocation = null;
		}
		public boolean shipSelectionLogic(Ship s, Space l){
			boolean shipChange = false;
			if(s != null){
				if(s.getOwner() == viewing){
					shipChange = true;
					if(s.isSelected()){
						s.setSelected(false);
						selected.remove(s);
					}else{
						s.setSelected(true);
						selected.add(s);
					}
				}
			}
			if(shipChange){
				if(specLocation != null){
					specLocation.setExamined(false);
					specLocation = null;
				}else if(selected.size() == 0){
					specLocation = l;
					specLocation.setExamined(true);
				}
			}
			return shipChange;
		}
		public void deselectShips(){
			for(Ship ss: selected){
				ss.setSelected(false);
			}
			selected = new LinkedList<Ship>();
			/*
			 * current thought is that if the player wants to look at a space, we should let 'em, even during the undo.
			deselectLoc();
			*/
		}
		public void deselectLoc(){
			if(specLocation != null){
				specLocation.setExamined(false);
			}
			specLocation = null;
		}
		public LinkedList<Ship> selected(){
			return selected;
		}
		public Space location(){
			Space loc = null;
			if(selected == null){
				return specLocation;
			}
			for(Ship s: selected){
				if(loc == null){
					loc = s.getLocation();
				}else if(s.getLocation() != loc){
					System.out.println("THEY SHOULD BE THE SAME");
					Toolbox.breakThings();
				}
			}
			if(loc == null){
				return specLocation;
			}
			return loc;
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			if(arg0.getKeyChar() == 26){
				Toolbox.delay();
				Toolbox.lock();
				ArrayList<Ship> homeless = new ArrayList<Ship>();
				if(specLocation == null){
					for(Ship s: selected){
						if(specLocation == null){
							specLocation = s.getLocation();
							if(specLocation == null){
								homeless.add(s);
							}else{
								specLocation.setExamined(true);
							}
						}else if(s.getLocation() != specLocation){
							System.out.println("THEY SHOULD BE THE SAME");
							Toolbox.breakThings();
						}
					}
				}
				unchange();
				for(Ship s: homeless){
					if(specLocation == null){
						specLocation = s.getLocation();
						specLocation.setExamined(true);
					}
				}
				deselectShips();
				info.notifyOfChange(viewing);
				Toolbox.release();
			}
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			super.mouseClicked(arg0);
			Toolbox.delay();
			Toolbox.lock();
			if(info.within(jp.getWidth(), jp.getHeight(), mouseX, mouseY)){
				info.click(jp.getHeight(), viewing, arg0.getX(), arg0.getY(), arg0.getButton());
			}else{
				if(arg0.getButton() == 3){
					//This is for selecting ships
					Space l = world.getHexagonConversion(arg0.getX(), arg0.getY());
					Ship s = world.getTouchedShip(arg0.getX(), arg0.getY());
					if(l != location()){
						deselectShips();
						deselectLoc();
					}
					boolean shipChange = shipSelectionLogic(s, l);
					if(!shipChange){
						if(specLocation == null){
							if(selected.size() == 0){
								specLocation = l;
								specLocation.setExamined(true);
							}
						}else{
							specLocation.setExamined(false);
							specLocation = null;
						}
					}
				}else if(arg0.getButton() == 1 && selected.size() > 0){
					ArrayList<Ship> realSelected = new ArrayList<Ship>();
					realSelected.addAll(selected);
					Space l = world.getHexagonConversion(arg0.getX(), arg0.getY());
					if(l.foreignShips(viewing)){
						AttackChange ac = new AttackChange(realSelected, location(), l);
						if(ac.legalChange()){
							change(ac);
							Space home = null;
							for(Ship s: realSelected){
								if(home == null){
									home = s.getLocation();
								}else if(home != l){
									if(s.getLocation() == l){
										home = l;
									}
								}
							}
							for(Ship s: realSelected){
								if(s.getLocation() == null || s.getLocation() != home){
									s.setSelected(false);
									selected.remove(s);
								}
							}
						}else{
							deselectShips();
						}
					}else{
						MoveChange mc = new MoveChange(realSelected, location(), l);
						if(mc.legalChange()){
							change(mc);
						}else{
							deselectShips();
						}
					}
				}
				info.notifyOfChange(viewing);
			}
			Toolbox.release();
		}
		public void deactivate(){
			deselectShips();
			deselectLoc();
		}
		public void paintLayerOne(int w, int h, General v, Graphics g){
			Ship hover = world.getTouchedShip(mouseX, mouseY);
			if(selected != null && location() != null && selected.size() != 0){
				int min = -1;
				for(Ship s: selected){
					int max =s.maxMove();
					if(max < min || min == -1){
						min = max;
					}
				}
				if(min != -1){
					ArrayList<Space> withinList = Map.getAllWithinForMovement(min, location(), v);
					for(Space s: withinList){
						double[] centers = world.screenCoordinates(s);
						s.drawSelectionOutline(centers[0], centers[1], world.getSideLength(), g, v, withinList, true);
					}
				}
			}
			if(hover != null){
				int min = hover.getM();
				ArrayList<Space> withinList = Map.getAllWithinForMovement(min, hover.getLocation(), hover.getOwner());
				for(Space s: withinList){
					double[] centers = world.screenCoordinates(s);
					s.drawSelectionOutline(centers[0], centers[1], world.getSideLength(), g, hover.getOwner(), withinList, hover.getOwner() == v);
				}
			}
		}
		public void paintLayerThree(int w, int h, General v, Graphics g){
			Ship hover = world.getTouchedShip(mouseX, mouseY);
			if(selected != null && location() != null && selected.size() != 0){
				int min = -1;
				for(Ship s: selected){
					int max = s.getM() - s.getAmountMoved();
					if(max < min || min == -1){
						min = max;
					}
				}
				if(min != -1){
					ArrayList<Space> withinList = Map.getAllWithinForMovement(min, location(), v);
					for(Space s: withinList){
						double[] centers = world.screenCoordinates(s);
						s.drawSelectionBorder(centers[0], centers[1], world.getSideLength(), g, v, withinList, true);
					}
				}
			}
			if(hover != null){
				int min = hover.getM();
				ArrayList<Space> withinList = Map.getAllWithinForMovement(min, hover.getLocation(), hover.getOwner());
				for(Space s: withinList){
					double[] centers = world.screenCoordinates(s);
					s.drawSelectionBorder(centers[0], centers[1], world.getSideLength(), g, hover.getOwner(), withinList, hover.getOwner() == v);
				}
			}
			if(specLocation != null){
				double[] centers = world.screenCoordinates(specLocation);
				specLocation.drawSelectionBorder(centers[0], centers[1], world.getSideLength(), g, v, new ArrayList<Space>(), true);
			}
		}
	}
	public class DeltaClick implements ScreenListener{
		private double savedDX, savedDY;
		private double savedX, savedY;
		private int buttonClicked;
		private boolean linked;
		public DeltaClick(){
			linked = false;
			buttonClicked = -1;
			if(world != null){
				savedDX = world.getDeltaX();
				savedDY = world.getDeltaY();
			}else{
				savedDX = 0;
				savedDY = 0;
			}
			savedX = 0;
			savedY = 0;
			mouseX = 0;
			mouseY = 0;
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {
			buttonClicked = arg0.getButton();
			if (arg0.getButton() != 1) {
				return;
			}
			// set up the button for dragging; return if its wrong.
			savedDX = world.getDeltaX();
			savedDY = world.getDeltaY();
			savedX = arg0.getX();
			savedY = arg0.getY();
			// we save the original setup of the variables for dragging. We want
			// to compare the movement
			// of the mouse to its original position, so we need to save that
			// AND the delta x.
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {
			buttonClicked = -1;
			savedDX = world.getDeltaX();
			savedDY = world.getDeltaY();
		}
		@Override
		public void mouseDragged(MouseEvent arg0) {
			// delta should be the same as our old delta + however far the mouse
			// has been dragged.
			// this accomplishes that, and then loops the delta around if we
			// went over the board
			// boundaries
			if (buttonClicked != 1) {
				return;
			}
			if(info.within(jp.getWidth(), jp.getHeight(), (int)savedX, (int)savedY)){
				return;
			}
			double deltaX = savedDX + (arg0.getX() - savedX);
			double deltaY = savedDY + (arg0.getY() - savedY);
			world.setDeltaX(Toolbox.negativeMod(deltaX, world.pixelWidth()));
			world.setDeltaY(Toolbox.negativeMod(deltaY, world.pixelHeight()));
			
		}
		@Override
		public void mouseMoved(MouseEvent arg0) {}
		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent arg0) {}
		@Override
		public void keyTyped(KeyEvent arg0) {
			if(arg0.getKeyChar() == 26){
				unchange();
			}
		}
		@Override
		public void deactivate(){}
		@Override
		public void paintLayerOne(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerTwo(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerThree(int width, int height, General viewing, Graphics g) {}
		@Override
		public void paintLayerFour(int width, int height, General viewing, Graphics g) {}
		@Override
		public boolean isLinked() {
			return linked;
		}
		@Override
		public void setLinked(boolean linked) {
			this.linked = linked;
		}
	}
	private class RenderLoop implements Runnable{
		private boolean running;
		private boolean kill;
		public RenderLoop(){
			kill = false;
			running = true;
		}
		/*
		public void pause(){
			running = false;
		}
		public void play(){
			running = true;
		}
		public boolean running(){
			return running;
		}
		public void end(){
			kill = true;
		}*/
		public void step(){
			Graphics main = jp.getGraphics();
			BufferedImage buffer = new BufferedImage(jp.getWidth(), jp.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = buffer.getGraphics();
			world.paintBaseLayer(jp.getWidth(), jp.getHeight(), viewing, g);
			world.paintLayerOne(jp.getWidth(), jp.getHeight(), viewing, g);
			for(MouseListener ml: jp.getMouseListeners()){
				ScreenListener sl = (ScreenListener) ml;
				sl.paintLayerOne(jp.getWidth(), jp.getHeight(), viewing, g);
			}
			world.paintLayerTwo(jp.getWidth(), jp.getHeight(), viewing, g);
			for(MouseListener ml: jp.getMouseListeners()){
				ScreenListener sl = (ScreenListener) ml;
				sl.paintLayerTwo(jp.getWidth(), jp.getHeight(), viewing, g);
			}
			world.paintLayerThree(jp.getWidth(), jp.getHeight(), viewing, g);
			for(MouseListener ml: jp.getMouseListeners()){
				ScreenListener sl = (ScreenListener) ml;
				sl.paintLayerThree(jp.getWidth(), jp.getHeight(), viewing, g);
			}
			world.paintLayerFour(jp.getWidth(), jp.getHeight(), viewing, g);
			int mX = mouseX;
			int mY = mouseY;
			info.paint(jp.getWidth(), jp.getHeight(), viewing, g, mX, mY);
			if(info.within(jp.getWidth(), jp.getHeight(), mX, mY)){
				BufferedImage tt = info.generateTooltip(jp.getHeight()/15, viewing, mX, mY);
				if(tt != null){
					g.drawImage(tt, mX - tt.getWidth(), mY - tt.getHeight(), null);
				}
			}else{
				Ship s = world.getTouchedShip(mX, mY);
				Planet h = world.getTouchedPlanet(mX, mY);
				if(s !=null && (s.getLocation().EnemyShipsVisible()||s.getOwner().lookupRelation(viewing) > 0)){
					BufferedImage tt = s.getTooltip(jp.getHeight()/15);
					g.drawImage(tt, mX, mY - tt.getHeight(), null);
				}else if(h != null){
					BufferedImage tt = h.getTooltip(jp.getHeight()/15);
					g.drawImage(tt, mX, mY - tt.getHeight(), null);
				}
			}
			for(MouseListener ml: jp.getMouseListeners()){
				ScreenListener sl = (ScreenListener) ml;
				sl.paintLayerFour(jp.getWidth(), jp.getHeight(), viewing, g);
			}
			
			main.drawImage(buffer, 0, 0, null);
		}
		@Override
		public void run() {
			while(!kill){
				if(running){
					Toolbox.delay();
					Toolbox.lock();
					step();
					Toolbox.release();
					Toolbox.sleep(20);
				}else{
					Toolbox.sleep(100);
				}
			}
		}
	}
}