import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Planet extends Space{
	private static final double psl = 0.75;
	//planet size ratio, determines how much of the space the planet visually occupies
	private General owner;
	private String name;
	private int[] production;
	private ArrayList<Structure> buildings;
	private double rov;
	private double rod;
	private long ct;
	public Planet(int x, int y, String name, int[] production) {
		super(x, y, true);
		this.name = name;
		this.production = production;
		buildings = new ArrayList<Structure>();
		rov = 0.00009 + Math.random() * 0.00003;
		rod = Math.random() * Math.PI*2;
		ct = System.currentTimeMillis();
	}
	public Planet(ArrayList<General> players, String[] desc){
		super(players, desc);
		int ownerIndex = Integer.parseInt(desc[3]);
		if(ownerIndex < players.size() && ownerIndex >= 0){
			owner = players.get(ownerIndex);
		}
		name = desc[4];
		production = new int[desc.length - 5];
		if(production.length != 4){
			System.out.println(name);
		}
		for(int i = 5; i < desc.length; i++){
			production[i-5] = Integer.parseInt(desc[i]);
		}
		buildings = new ArrayList<Structure>();
		rov = 0.00009 + Math.random() * 0.00003;
		rod = Math.random() * Math.PI*2;
		ct = System.currentTimeMillis();
	}
	/*
	 * <x-coord>!<y-coord>!<identifier>!<owner-index>!<name>!<production[i]>*
	 * identifier is 2 for a planet (1 for a trade block, 0 for a basic space)
	 * owner-index is -1 for null, otherwise assumes pre-calculated owner indices.
	 * production[0]![production[1]!production[2]!etc
	 */
	public String topologicalDescription(){
		String form = getX() + "!" + getY() + "!" + 2 + "!";
		if(owner == null){
			form += -1  + "!";
		}else{
			form += owner.getIndex() + "!";
		}
		form += name;
		for(int i = 0; i < production.length; i++){
			form += "!" + production[i];
		}
		return form;
	}
	public Ship getTouchedShip(double centerX, double centerY, double sideLength, double mouseX, double mouseY){
		double smallLength = Math.pow(3, 0.5) * sideLength / 2;
		//first, we calculate where the space is, and begin setting up the circle.
		Ship chosen = null;
		for (int i = 0; i < getFleet().size(); i++) {
			//going through all the ships, 
			Ship ship = getFleet().get(i);
			double theta = i * Math.PI * 2 / getFleet().size() + rod + rov * (System.currentTimeMillis() - ct);
			double dim = sideLength/2;
			int cX = (int) (centerX + Math.cos(theta) * smallLength * 0.8 + 0.5);
			int cY = (int) (centerY + Math.sin(theta) * smallLength * 0.8 + 0.5);
			if (cX - dim / 2 <= mouseX && mouseX <= cX + dim / 2) {
				if (cY - dim / 2 <= mouseY && mouseY <= cY + dim / 2) {
					//we check to see if the mouse is inside each ships image box. This would be FAR
					//harder if we change the angle of the images; one reason not to do it.
					chosen = ship;
				}
			}
		}
		return chosen;
	}
	public void drawLayerTwo(double centerX, double centerY, double sideLength, Graphics g, General v){
		super.drawLayerTwo(centerX, centerY, sideLength, g, v);
		drawPlanetImage(centerX, centerY, sideLength, g, v);
	}
	public void drawLayerFour(double centerX, double centerY, double sideLength, Graphics g, General v){
		if(getOwner() != null){
			drawOwner(centerX, centerY, sideLength, g, v);
		}
		drawTitle(centerX, centerY, sideLength, g, v);
		drawStructures(centerX, centerY, sideLength, g, v);
		drawShips(centerX, centerY, sideLength, g, v);
	}
	private void drawOwner(double centerX, double centerY, double sideLength, Graphics g, General v){
		double dim = 2 * Math.pow(3, 0.5) * sideLength / (2 * (2 + Math.pow(3, 0.5)));
		BufferedImage bi = owner.getInsignia((int)dim);
		int widthI = bi.getWidth();
		g.drawImage(bi, (int) (centerX - widthI / 2), (int) (centerY - sideLength / 2 - widthI), null);
	}
	private void drawTitle(double centerX, double centerY, double sideLength, Graphics g, General v){
		int maxWidth = (int) (sideLength * Math.pow(3, 0.5) + 0.5 - sideLength / 3);
		int maxHeight = (int) (sideLength/3);
		String name = getName();
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		TextPackage idealText = new TextPackage(name, maxWidth, maxHeight, 1, g);
		g.setFont(g.getFont().deriveFont(idealText.getFontValue()));
		int sHeight = g.getFontMetrics().getHeight();
		g.setColor(Color.white);
		
		for(int i = 0; i < idealText.getResult().length; i++){
			String now = idealText.getResult()[i];
			int width = g.getFontMetrics().stringWidth(now);
			g.drawString(now, (int) (centerX - width/2), (int) (centerY - sideLength/2 + 3 * sHeight/4 + i * sHeight - (idealText.getResult().length - 1) * sHeight/2));
		}
	}
	private void drawStructures(double centerX, double centerY, double sideLength, Graphics g, General viewing){
		double radius = psl * sideLength * Math.pow(3, 0.5) / 2;
		double width = radius * 0.9;
		double height = radius * 0.6;
		double vO = radius * 0.05;
		ArrayList<Structure> buildings = getStructures();
		if(buildings.size() == 1){
			BufferedImage s0 = buildings.get(0).getMapImage(width, height);
			g.drawImage(s0, (int) (centerX - s0.getWidth()/2), (int) (centerY - s0.getHeight()/2-vO), null);
		}else if(buildings.size() == 2){
			BufferedImage s0 = buildings.get(0).getMapImage(width, height);
			g.drawImage(s0, (int) (centerX - s0.getWidth()/2), (int) (centerY - s0.getHeight()/2-vO), null);
			BufferedImage s1 = buildings.get(1).getMapImage(width, height);
			g.drawImage(s1, (int) (centerX - s1.getWidth()/2), (int) (centerY + height/2 + (height - s1.getHeight())/2-vO), null);
		}else if(buildings.size() == 3){
			BufferedImage s0 = buildings.get(0).getMapImage(width, height);
			g.drawImage(s0, (int) (centerX - width + (width-s0.getWidth())/2), (int) (centerY - s0.getHeight()/2-vO), null);
			BufferedImage s1 = buildings.get(1).getMapImage(width, height);
			g.drawImage(s1, (int) (centerX + (width-s0.getWidth())/2), (int) (centerY - s1.getHeight()/2-vO), null);
			BufferedImage s2 = buildings.get(2).getMapImage(width, height);
			g.drawImage(s2, (int) (centerX - s2.getWidth()/2), (int) (centerY + height/2 + (height - s1.getHeight())/2-vO), null);
		}else if(buildings.size() == 0){
			
		}else{
			System.out.println("Non-zero number of buildings more than three? Sounds like a problem to me.");
			Toolbox.breakThings();
		}
	}
	private void drawShips(double centerX, double centerY, double sideLength, Graphics g, General viewing){
		/*
		 * so, here is the deal with this method: it sets up a circle with a
		 * particular radius, and then increments a semi-random theta along that
		 * circle, drawing ships at regular thetas. Now, I've commented out a
		 * section of code that will rotate the ship images, because I'm not
		 * sure if we want them. We can decide later.
		 */
		double smallLength = Math.pow(3, 0.5) * sideLength / 2;
		for (int i = 0; i < getFleet().size(); i++) {
			Ship ship = getFleet().get(i);
			double theta = i * Math.PI * 2 / getFleet().size() + rod + rov * (System.currentTimeMillis() - ct);
			BufferedImage bi = ship.requestRotatedImage(sideLength/2, viewing, theta - Math.PI/2);
			int cX = (int) (centerX + Math.cos(theta) * smallLength * 0.8 + 0.5);
			int cY = (int) (centerY + Math.sin(theta) * smallLength * 0.8 + 0.5);
			g.drawImage(bi, cX - bi.getWidth() / 2, cY - bi.getHeight() / 2, null);
		}
	}
	private void drawPlanetImage(double centerX, double centerY, double sideLength, Graphics g, General v){
		double distance = psl * sideLength * Math.pow(3, 0.5) / 2;
		String name = getImageName();
		BufferedImage bi = SolarWarfare4_2.il.findImage(name).getFit((int)(distance*2), (int)(distance*2));
		g.drawImage(bi, (int) (centerX - distance), (int) (centerY - distance),null);
	}
	public String getImageName(){
		String name = "DefaultPlanet";
		int threes = 0;
		if(production[0] == 3){
			name = "HydrogenPlanet";
			threes++;
		}
		if(production[1] == 3){
			name = "IronPlanet";
			threes++;
		}
		if(production[2] == 3){
			name = "GoldPlanet";
			threes++;
		}
		if(production[3] == 3){
			name = "SiliconPlanet";
			threes++;
		}
		if(threes > 1){
			name = "DefaultPlanet";
		}
		return name;
	}
	public boolean touchingVisualPlanet(double centerX, double centerY, double sideLength, int mouseX, int mouseY){
		double smallLength = Math.pow(3, 0.5) * sideLength / 2;
		double distance = Toolbox.distance(centerX, centerY, mouseX, mouseY);
		double planetDistance = psl * smallLength;
		return distance <= planetDistance;
	}
	public BufferedImage getTooltip(double ah){
		double h = ah;
		double b = h/3;
		double s = h * 2/3;
		double w = s * production.length;
		ArrayList<Structure> buildings = getStructures();
		h+=buildings.size() * h/3;
		BufferedImage tooltip = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = tooltip.getGraphics();
		g.setColor(new Color(200,200,200));
		g.fillRoundRect(0, 0, (int) w, (int) h, (int) (h/4), (int) (h/4));
		g.setColor(Color.black);
		for(int i = 0; i < production.length; i++){
			BufferedImage resourceBack = SolarWarfare4_2.il.findImage("Resource"+i).getFit((int)s, (int)s);
			g.drawImage(resourceBack, (int) (i*s), (int)(b), null);
			String amount = Integer.toString(getProduction()[i]);
			g.setFont(g.getFont().deriveFont(TextPackage.idealFont(amount, (int) s, (int) s, 1, g)));
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			int width = g.getFontMetrics().stringWidth(amount);
			int height = g.getFontMetrics().getHeight();
			g.drawString(amount, (int) ((0.5+i)*s - width/2), (int)(b+s-height/4));
		}
		//Step one: draw the background and resource images!
		g.setFont(g.getFont().deriveFont(TextPackage.idealFont(name, (int) w, (int) (b), 1, g)));
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		int width = g.getFontMetrics().stringWidth(name);
		int height = g.getFontMetrics().getHeight();
		g.drawString(getName(), tooltip.getWidth()/2 - width/2, height * 3 /4);
		//step two: draw the planet's name!
		for(int i = 0; i < buildings.size(); i++){
			Structure t = buildings.get(i);
			g.setFont(g.getFont().deriveFont(TextPackage.idealFont(t.getType(), (int) w, (int) (b), 1, g)));
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			width = g.getFontMetrics().stringWidth(t.getType());
			height = g.getFontMetrics().getHeight();
			g.drawString(t.getType(), (int) (w/20), (int)  (b + s + ((i+0.5) * b) + height/4));
		}
		return tooltip;
	}
	public boolean EnemyStructuresVisible(){
		return !GameManager.obscureEnemies;
	}
	public void refreshStructures(General g){
		if(getOwner() == g){
			for(Structure s: buildings){
				s.beginTurn(g);
			}
		}
	}
	public ArrayList<Structure> getStructures(){
		if(!super.EnemyShipsVisible()){
			if(getOwner() != null && getOwner().lookupRelation(GameManager.viewing) <= 0){
				return new ArrayList<Structure>();
			}
		}
		return buildings;
	}
	public void addStructure(Structure s){
		buildings.add(s);
	}
	public void removeStructure(Structure s){
		buildings.remove(s);
	}
	public boolean containsStructure(Structure s){
		return buildings.contains(s);
	}
	public General getOwner() {
		return owner;
	}
	public void setOwner(General owner) {
		this.owner = owner;
	}
	public int getIdentifier(){
		return 1;
	}
	public String getName() {
		return name;
	}
	public int[] getProduction() {
		return production;
	}
}