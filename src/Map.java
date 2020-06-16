import java.util.LinkedList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
public class Map{
	public static final String backgroundName = "Nebulae2";
	private Space[][] contents;
	private double sideLength;
	private double deltaX;
	private double deltaY;
	public Map(int width, int height, double sideLength){
		contents = new Space[width][height];
		this.sideLength = sideLength;
		this.deltaX = 0;
		this.deltaY = 0;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				contents[i][j] = new Space(i, j, false);
			}
		}
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[i].length; j++){
				Space nova = getAt(i, j);
				nova.setL(getAt(i - 1, j));
				nova.setR(getAt(i + 1, j));
				if(Toolbox.even(j)){
					nova.setUL(getAt(i - 1, j + 1));
					nova.setDL(getAt(i - 1, j - 1));
					nova.setUR(getAt(i, j + 1));
					nova.setDR(getAt(i, j - 1));
				}else{
					nova.setUL(getAt(i, j + 1));
					nova.setDL(getAt(i, j - 1));
					nova.setUR(getAt(i + 1, j + 1));
					nova.setDR(getAt(i + 1, j - 1));
				}
			}
		}
	}
	public ArrayList<String> fullDescription(ArrayList<General> playerlist){
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(Relation.fullDescription(playerlist));
		result.add("*");
		result.add(""+contents.length);
		result.add(""+contents[0].length);
		int index = 0;
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[0].length; j++){
				Space s = contents[i][j];
				if(s.interestingSpace()){
					s.setIndex(index);
					index++;
					result.add(s.topologicalDescription());
				}
			}
		}
		result.add("*");
		index = 0;
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[0].length; j++){
				for(Ship s: contents[i][j].getFleet()){
					s.setIndex(index);
					index++;
					result.add(s.getDescription());
				}
			}
		}
		result.add("*");
		index = 0;
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[0].length; j++){
				for(Resource r: contents[i][j].getPile()){
					r.setIndex(index);
					index++;
					result.add(r.getDescription());
				}
			}
		}
		index = 0;
		result.add("*");
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[0].length; j++){
				Space s = contents[i][j];
				if(s.getIdentifier() == 1){
					Planet p = (Planet) s;
					for(Structure ss: p.getStructures()){
						ss.setIndex(index);
						index++;
						result.add(ss.getDescription());
					}
				}
			}
		}
		result.add("*");
		return result;
	}
	public static Map createMap(ArrayList<ArrayList<String>> record, ArrayList<General> playerlist, double sl){
		Relation.createRelationMatrix(playerlist, record.get(1));
		
		ArrayList<Space> loc = new ArrayList<Space>();
		ArrayList<String> desc = record.get(2);
		int width = Integer.parseInt(desc.get(0));
		int height = Integer.parseInt(desc.get(1));
		Map m = new Map(width, height, sl);
		for(int i = 2; i < desc.size(); i++){
			String[] info = desc.get(i).split("!");
			if(Integer.parseInt(info[2]) == 0){
				//its an empty space, so we literally don't care.
				Space s = new Space(playerlist, info);
				m.replaceAt(s.getX(), s.getY(), s);
				loc.add(s);
			}else if(Integer.parseInt(info[2]) == 1){
				//its a trade block
				Space s = new Space(playerlist, info);
				m.replaceAt(s.getX(), s.getY(), s);
				loc.add(s);
			}else if(Integer.parseInt(info[2]) == 2){
				//its a planet!
				Planet p = new Planet(playerlist, info);
				m.replaceAt(p.getX(), p.getY(), p);
				loc.add(p);
			}
		}
		
		ArrayList<String> info = record.get(3);
		ArrayList<Ship> ships = new ArrayList<Ship>();
		for(String s: info){
			Ship nova = new Ship(s.split("!"), playerlist, loc);
			nova.getLocation().addShip(nova);
			ships.add(nova);
		}
		
		info = record.get(4);
		ArrayList<Resource> resources = new ArrayList<Resource>();
		for(String s: info){
			Resource nova = new Resource(s.split("!"), playerlist, loc);
			nova.getLocation().addResource((nova));
			resources.add(nova);
		}
		
		info = record.get(5);
		ArrayList<Structure> structures = new ArrayList<Structure>();
		for(String s: info){
			Structure nova = new Structure(s.split("!"), loc);
			Planet p = (Planet) nova.getLocation();
			p.addStructure(nova);
			structures.add(nova);
		}
		
		return m;
	}
	public static Map loadMap(ArrayList<General> playerlist, String gameName, double sidelength){
		String fpstart = "SW4_2Data/" + gameName + "/";
		//playerlist starts as empty, and gets filled - a rather sneak bit of pass by reference.
		/*for(General g: General.loadGenerals(fpstart + "GeneralInfo.txt")){
			playerlist.add(g);
		}*/
		Relation.loadRelationMatrix(playerlist, fpstart + "RelationMatrix.txt");
		
		ArrayList<Space> loc = new ArrayList<Space>();
		ArrayList<String> desc = Toolbox.load(fpstart + "WorldDesc.txt");
		int width = Integer.parseInt(desc.get(0));
		int height = Integer.parseInt(desc.get(1));
		Map m = new Map(width, height, sidelength);
		for(int i = 2; i < desc.size(); i++){
			String[] info = desc.get(i).split("!");
			if(Integer.parseInt(info[2]) == 0){
				//its an empty space, so we literally don't care.
				Space s = new Space(playerlist, info);
				m.replaceAt(s.getX(), s.getY(), s);
				loc.add(s);
			}else if(Integer.parseInt(info[2]) == 1){
				//its a trade block
				Space s = new Space(playerlist, info);
				m.replaceAt(s.getX(), s.getY(), s);
				loc.add(s);
			}else if(Integer.parseInt(info[2]) == 2){
				//its a planet!
				Planet p = new Planet(playerlist, info);
				m.replaceAt(p.getX(), p.getY(), p);
				loc.add(p);
			}
		}
		
		loadShipDescriptions(playerlist, loc, fpstart + "ShipDesc.txt");
		loadResourceDescriptions(playerlist, loc, fpstart + "ResourceDesc.txt");
		loadStructureDescriptions(playerlist, loc, fpstart + "StructureDesc.txt");
		
		return m;
		
		
	}
	public void saveMap(ArrayList<General> players, String gameName){
		String fpstart = "SW4_2Data/" + gameName + "/";
		for(int i = 0; i < players.size(); i++){
			players.get(i).setIndex(i);
		}
		General.saveGenerals(players, fpstart + "GeneralInfo.txt");
		Relation.saveRelationMatrix(players, fpstart + "RelationMatrix.txt");
		
		ArrayList<String> desc = new ArrayList<String>();
		desc.add(""+contents.length);
		desc.add(""+contents[0].length);
		int index = 0;
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[0].length; j++){
				Space s = contents[i][j];
				if(s.interestingSpace()){
					s.setIndex(index);
					index++;
					desc.add(s.topologicalDescription());
				}
			}
		}
		Toolbox.save(desc, fpstart + "WorldDesc.txt", false);
		
		saveShipDescriptions(fpstart + "ShipDesc.txt");
		saveResourceDescriptions(fpstart + "ResourceDesc.txt");
		saveStructureDescriptions(fpstart + "StructureDesc.txt");
	}
	public static ArrayList<Structure> loadStructureDescriptions(ArrayList<General> players, ArrayList<Space> locations, String filepath){
		ArrayList<String> info = Toolbox.load(filepath);
		ArrayList<Structure> structures = new ArrayList<Structure>();
		for(String s: info){
			Structure nova = new Structure(s.split("!"), locations);
			Planet p = (Planet) nova.getLocation();
			p.addStructure(nova);
			structures.add(nova);
		}
		return structures;
	}
	public void saveStructureDescriptions(String filepath){
		ArrayList<String> desc = new ArrayList<String>();
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents.length; j++){
				Space s = contents[i][j];
				if(s.getIdentifier() == 1){
					Planet p = (Planet) s;
					for(Structure ss: p.getStructures()){
						ss.setIndex(desc.size());
						desc.add(ss.getDescription());
					}
				}
			}
		}
		Toolbox.save(desc, filepath, false);
	}
	public static ArrayList<Resource> loadResourceDescriptions(ArrayList<General> players, ArrayList<Space> locations, String filepath){
		ArrayList<String> info = Toolbox.load(filepath);
		ArrayList<Resource> resources = new ArrayList<Resource>();
		for(String s: info){
			Resource nova = new Resource(s.split("!"), players, locations);
			nova.getLocation().addResource((nova));
			resources.add(nova);
		}
		return resources;
	}
	public void saveResourceDescriptions(String filepath){
		ArrayList<String> desc = new ArrayList<String>();
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents.length; j++){
				for(Resource r: contents[i][j].getPile()){
					r.setIndex(desc.size());
					desc.add(r.getDescription());
				}
			}
		}
		Toolbox.save(desc, filepath, false);
	}
	public static ArrayList<Ship> loadShipDescriptions(ArrayList<General> players, ArrayList<Space> locations, String filepath){
		ArrayList<String> info = Toolbox.load(filepath);
		ArrayList<Ship> ships = new ArrayList<Ship>();
		for(String s: info){
			Ship nova = new Ship(s.split("!"), players, locations);
			nova.getLocation().addShip(nova);
			ships.add(nova);
		}
		return ships;
	}
	public void saveShipDescriptions(String filepath){
		ArrayList<String> desc = new ArrayList<String>();
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents.length; j++){
				for(Ship s: contents[i][j].getFleet()){
					s.setIndex(desc.size());
					desc.add(s.getDescription());
				}
			}
		}
		Toolbox.save(desc, filepath, false);
	}
	
	public static Map loadTopologicalDescription(ArrayList<General> players, String filepath, double sidelength){
		ArrayList<String> desc = Toolbox.load(filepath);
		int width = Integer.parseInt(desc.get(0));
		int height = Integer.parseInt(desc.get(1));
		Map m = new Map(width, height, sidelength);
		for(int i = 2; i < desc.size(); i++){
			String[] info = desc.get(i).split("!");
			if(Integer.parseInt(info[2]) == 0){
				//its an empty space, so we literally don't care.
				Space s = new Space(players, info);
				m.replaceAt(s.getX(), s.getY(), s);
			}else if(Integer.parseInt(info[2]) == 1){
				//its a trade block
				Space s = new Space(players, info);
				m.replaceAt(s.getX(), s.getY(), s);
			}else if(Integer.parseInt(info[2]) == 2){
				//its a planet!
				Planet p = new Planet(players, info);
				m.replaceAt(p.getX(), p.getY(), p);
			}
		}
		return m;
	}
	public void saveTopologicalDescription(ArrayList<General> players, String filepath){
		for(int i = 0; i < players.size(); i++){
			players.get(i).setIndex(i);
		}
		ArrayList<String> desc = new ArrayList<String>();
		desc.add(""+contents.length);
		desc.add(""+contents[0].length);
		int index = 0;
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[0].length; j++){
				Space s = contents[i][j];
				if(s.interestingSpace()){
					s.setIndex(index);
					index++;
					desc.add(s.topologicalDescription());
				}
			}
		}
		Toolbox.save(desc, filepath, false);
	}
	public ArrayList<Planet> getAllPlanets(){
		ArrayList<Planet> p = new ArrayList<Planet>();
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[i].length; j++){
				Space s = contents[i][j];
				if(s.getIdentifier() == 1){
					p.add((Planet) s);
				}
			}
		}
		return p;
	}
	public void replaceAt(int x, int y, Space nova){
		Space previous = contents[x][y];
		nova.setL(previous.getL());
		if(previous.getL() != null){
			previous.getL().setR(nova);
		}
		
		nova.setUL(previous.getUL());
		if(previous.getUL() != null){
			previous.getUL().setDR(nova);
		}
		
		nova.setUR(previous.getUR());
		if(previous.getUR() != null){
			previous.getUR().setDL(nova);
		}
		
		nova.setR(previous.getR());
		if(previous.getR() != null){
			previous.getR().setL(nova);
		}
		
		nova.setDR(previous.getDR());
		if(previous.getDR() != null){
			previous.getDR().setUL(nova);
		}
		
		nova.setDL(previous.getDL());
		if(previous.getDL() != null){
			previous.getDL().setUR(nova);
		}
		
		contents[x][y] = nova;
	}
	public void beginTurn(General g){
		for(Space[] ss: contents){
			for(Space s: ss){
				s.emptyResources(g);
				s.refreshShips(g);
				if(s.getIdentifier() == 1){
					Planet p = (Planet) s;
					p.refreshStructures(g);
				}
			}
		}
	}
	public void calculateClaims(){
		for(int i = 0; i < contents.length; i++){
			for(int j = 0; j < contents[i].length; j++){
				contents[i][j].calculateClaims();
			}
		}
	}
	public Ship getTouchedShip(int mouseX, int mouseY){
		Space h = getHexagonConversion(sideLength, mouseX, mouseY);
		double[] centers = screenCoordinates(h);
		return h.getTouchedShip(centers[0], centers[1], sideLength, mouseX, mouseY);
	}
	public Planet getTouchedPlanet(int mouseX, int mouseY){
		Space h = getHexagonConversion(sideLength, mouseX, mouseY);
		double[] centers = screenCoordinates(h);
		if(h.getIdentifier() != 1){
			return null;
		}
		Planet p = (Planet) h;
		if(p.touchingVisualPlanet(centers[0], centers[1], sideLength, mouseX, mouseY)){
			return p;
		}else{
			return null;
		}
	}
	public void paintBaseLayer(int screenWidth, int screenHeight, General v, Graphics g){
		g.setColor(Color.white);
		g.fillRect(0, 0, screenWidth, screenHeight);
		BufferedImage bi = SolarWarfare4_2.il.findImage(backgroundName).getFit(-1, screenHeight);
		g.drawImage(bi, 0, 0, null);
	}
	public void paintLayerOne(int screenWidth, int screenHeight, General v, Graphics g){
		Space start = getHexagonConversion(sideLength, 0, 0).getDL();
		double startCX = Math.pow(3, 0.5) * sideLength * start.getX() + deltaX;
		startCX = Toolbox.negativeMod(startCX, (int) (pixelWidth()));
		double startCY = 1.5 * sideLength * start.getY() + deltaY;
		startCY = Toolbox.negativeMod(startCY, (int) (pixelHeight()));
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerOne(cX, cY, sideLength, g, v);
			}
		}
	}
	public void paintLayerTwo(int screenWidth, int screenHeight, General v, Graphics g){
		Space start = getHexagonConversion(sideLength, 0, 0).getDL();
		double startCX = Math.pow(3, 0.5) * sideLength * start.getX() + deltaX;
		startCX = Toolbox.negativeMod(startCX, (int) (pixelWidth()));
		double startCY = 1.5 * sideLength * start.getY() + deltaY;
		startCY = Toolbox.negativeMod(startCY, (int) (pixelHeight()));
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerTwo(cX, cY, sideLength, g, v);
			}
		}
	}
	public void paintLayerThree(int screenWidth, int screenHeight, General v, Graphics g){
		Space start = getHexagonConversion(sideLength, 0, 0).getDL();
		double startCX = Math.pow(3, 0.5) * sideLength * start.getX() + deltaX;
		startCX = Toolbox.negativeMod(startCX, (int) (pixelWidth()));
		double startCY = 1.5 * sideLength * start.getY() + deltaY;
		startCY = Toolbox.negativeMod(startCY, (int) (pixelHeight()));
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerThree(cX, cY, sideLength, g, v);
			}
		}
	}
	public void paintLayerFour(int screenWidth, int screenHeight, General v, Graphics g){
		Space start = getHexagonConversion(sideLength, 0, 0).getDL();
		double startCX = Math.pow(3, 0.5) * sideLength * start.getX() + deltaX;
		startCX = Toolbox.negativeMod(startCX, (int) (pixelWidth()));
		double startCY = 1.5 * sideLength * start.getY() + deltaY;
		startCY = Toolbox.negativeMod(startCY, (int) (pixelHeight()));
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerFour(cX, cY, sideLength, g, v);
			}
		}
	}
	public void paintMap(int screenWidth, int screenHeight, General v, Graphics g, boolean useBackground){
		Space start = getHexagonConversion(sideLength, 0, 0).getDL();
		g.setColor(new Color(100, 100, 100));
		g.fillRect(0, 0, screenWidth, screenHeight);
		if(useBackground){
			BufferedImage bi = SolarWarfare4_2.il.findImage(backgroundName).getFit(-1, screenHeight);
			g.drawImage(bi, 0, 0, null);
		}
		double startCX = Math.pow(3, 0.5) * sideLength * start.getX() + deltaX;
		startCX = Toolbox.negativeMod(startCX, (int) (pixelWidth()));
		double startCY = 1.5 * sideLength * start.getY() + deltaY;
		startCY = Toolbox.negativeMod(startCY, (int) (pixelHeight()));
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerOne(cX, cY, sideLength, g, v);
			}
		}
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerTwo(cX, cY, sideLength, g, v);
			}
		}
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerThree(cX, cY, sideLength, g, v);
			}
		}
		for (double centerX = startCX, i = start.getX(); centerX < screenWidth
				+ sideLength * Math.pow(3, 0.5); centerX += Math
				.pow(3, 0.5) * sideLength, i = Toolbox.positiveMod(i + 1,
				cellWidth())) {
			for (double centerY = startCY, j = start.getY(); centerY < screenHeight
					+ 1.5 * sideLength; centerY += 1.5 * sideLength, j = Toolbox
					.positiveMod(j + 1, cellHeight())) {
				Space s = getAt((int) i, (int) j);
				double cX = centerX;
				double cY = centerY;
				if (!Toolbox.even((int) j)) {
					cX += (sideLength * Math.pow(3, 0.5) / 2);
				}
				s.drawLayerFour(cX, cY, sideLength, g, v);
			}
		}
	}
	public double[] screenCoordinates(Space s) {
		double centerX = s.getX() * Math.sqrt(3) * sideLength + deltaX;
		if (!Toolbox.even(s.getY())) {
			centerX += Math.sqrt(3) / 2 * sideLength;
		}
		centerX = Toolbox.versatileMod(centerX, sideLength * Math.sqrt(3)
				/ -2, cellWidth() * sideLength * Math.pow(3, 0.5));
		//we calculate X be the knowledge that each hexagon is root 3 * sidelength wide, and that odd Ys get
		//pushed forward by root 3/2 sideLength. Then, we use our new versatile mod method, which insures that
		//it only gets modded to the positives if its off screen.
		double centerY = s.getY() * 1.5 * sideLength + deltaY;
		centerY = Toolbox.versatileMod(centerY, sideLength * -1, 1.5
				* sideLength * cellHeight());
		//we calculate Y using the same versatile mod method, but knowing that each hexagons non-overlapping height
		//is 1.5 side length (because 0.5 side length overlaps with top and bottom).
		return new double[]{centerX, centerY};
	}
	public Space getHexagonConversion(int testX, int testY){
		// OKAY! So, we have a semi brilliant plan here, and we are going to see
		// how well it works.
		testX -= deltaX;
		testY -= deltaY;
		// step one: essentially undo the delta: now the map starts painted at
		// the zero, and the mouse goes off the screen the way
		// the math looks at it.
		double vX = testX;
		double vV = testX / 2 + Math.pow(3, 0.5) * testY / 2;
		double vU = testX / 2 - Math.pow(3, 0.5) * testY / 2;
		double smallRadius = (sideLength) * Math.pow(3, 0.5) / 2.0;
		vX /= smallRadius;
		vU /= smallRadius;
		vV /= smallRadius;
		// Step two: we express our point in terms of not one, not two, but
		// THREE different vectors. Also, we shrink our scale
		// so that "1" = the distance from the center of a hexagon to the center
		// of a side. This will help, believe me.
		double sigX = Math.signum(vX);
		double sigU = Math.signum(vU);
		double sigV = Math.signum(vV);
		double absX = Math.abs(vX);
		double absU = Math.abs(vU);
		double absV = Math.abs(vV);
		int absolXU = (int) (absX + 1);
		int absolXD = (int) (absX);
		int absolUU = (int) (absU + 1);
		int absolUD = (int) (absU);
		int absolVU = (int) (absV + 1);
		int absolVD = (int) (absV);
		int centX = 0;
		int centU = 0;
		int centV = 0;
		if (vX >= 0 && vU >= 0 && vV >= 0) {
			// then we've got the right cone.
			if (absolXU == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXD == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXU == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXD == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXU == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXD == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXU == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolXD == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}

		} else if (vX >= 0 && vU >= 0 && vV <= 0) {
			// then we've got the upper right cone.
			if (absolUU == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolUU == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		} else if (vX <= 0 && vU >= 0 && vV <= 0) {
			// then we've got the upper left cone.
			if (absolVU == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolVU == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		} else if (vX <= 0 && vU <= 0 && vV <= 0) {
			// then we've got the left cone.
			if (absolXU == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXD == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXU == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXD == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXU == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXD == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXU == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolXD == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}

		} else if (vX <= 0 && vU <= 0 && vV >= 0) {
			// then we've got the lower left cone.
			if (absolUU == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolUU == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		} else if (vX >= 0 && vU <= 0 && vV >= 0) {
			// then we've got the lower right cone.
			if (absolVU == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolVU == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		}
		centX = (int) (centX * sigX);
		centU = (int) (centU * sigU);
		centV = (int) (centV * sigV);
		// after an annoyingly long process, we have the coordinates of the
		// center of our hexagon. Basically, in FAR too many
		// if statements, here is what we are doing. First, in the big if
		// statement, we figure out which of the 6 cardinal
		// directions we are in - this is important, because there is a
		// coordinate associated with each cardinal direction.
		// in that direction's cone, the coordinate associated with it ALWAYS
		// measures min distance from the center.
		// Next, we try rounding our decimal mouse coordinates up and down.
		// Since there are 3 coordinates, this leads to
		// 2^3 or 8 combinations, each of which we give its own iff statement.
		// For each of these, we do a check to see if
		// it is the center of a hexagon. First, to be a valid point the
		// cardinal coordinate has to equal the sums of the other
		// coordinates. Secondly, if its a center the difference of the
		// non-cardinal coordinates, % 3, will equal 0.
		// finally, because we've been working with absolute values this whole
		// time, we return the positives and negatives.
		int xCoord = 0;
		if (centX < 0) {
			xCoord = (centX - 1) / 2;
		} else {
			xCoord = centX / 2;
		}
		int yCoord = (centV - centU) / 3;
		return getAt(xCoord,yCoord);
	}
	public Space getHexagonConversion(double sideLength, int testX, int testY) {
		// OKAY! So, we have a semi brilliant plan here, and we are going to see
		// how well it works.
		testX -= deltaX;
		testY -= deltaY;
		// step one: essentially undo the delta: now the map starts painted at
		// the zero, and the mouse goes off the screen the way
		// the math looks at it.
		double vX = testX;
		double vV = testX / 2 + Math.pow(3, 0.5) * testY / 2;
		double vU = testX / 2 - Math.pow(3, 0.5) * testY / 2;
		double smallRadius = (sideLength) * Math.pow(3, 0.5) / 2.0;
		vX /= smallRadius;
		vU /= smallRadius;
		vV /= smallRadius;
		// Step two: we express our point in terms of not one, not two, but
		// THREE different vectors. Also, we shrink our scale
		// so that "1" = the distance from the center of a hexagon to the center
		// of a side. This will help, believe me.
		double sigX = Math.signum(vX);
		double sigU = Math.signum(vU);
		double sigV = Math.signum(vV);
		double absX = Math.abs(vX);
		double absU = Math.abs(vU);
		double absV = Math.abs(vV);
		int absolXU = (int) (absX + 1);
		int absolXD = (int) (absX);
		int absolUU = (int) (absU + 1);
		int absolUD = (int) (absU);
		int absolVU = (int) (absV + 1);
		int absolVD = (int) (absV);
		int centX = 0;
		int centU = 0;
		int centV = 0;
		if (vX >= 0 && vU >= 0 && vV >= 0) {
			// then we've got the right cone.
			if (absolXU == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXD == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXU == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXD == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXU == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXD == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXU == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolXD == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}

		} else if (vX >= 0 && vU >= 0 && vV <= 0) {
			// then we've got the upper right cone.
			if (absolUU == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolUU == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		} else if (vX <= 0 && vU >= 0 && vV <= 0) {
			// then we've got the upper left cone.
			if (absolVU == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolVU == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		} else if (vX <= 0 && vU <= 0 && vV <= 0) {
			// then we've got the left cone.
			if (absolXU == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXD == absolUU + absolVU) {
				if (Math.abs(absolUU - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolXU == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXD == absolUD + absolVU) {
				if (Math.abs(absolUD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolXU == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXD == absolUU + absolVD) {
				if (Math.abs(absolUU - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolXU == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolXD == absolUD + absolVD) {
				if (Math.abs(absolUD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}

		} else if (vX <= 0 && vU <= 0 && vV >= 0) {
			// then we've got the lower left cone.
			if (absolUU == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXU + absolVU) {
				if (Math.abs(absolXU - absolVU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolUD == absolXD + absolVU) {
				if (Math.abs(absolXD - absolVU) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolUU == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXU + absolVD) {
				if (Math.abs(absolXU - absolVD) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolUU == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolUD == absolXD + absolVD) {
				if (Math.abs(absolXD - absolVD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		} else if (vX >= 0 && vU <= 0 && vV >= 0) {
			// then we've got the lower right cone.
			if (absolVU == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXU) {
				if (Math.abs(absolUU - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXU) {
				if (Math.abs(absolUD - absolXU) % 3 == 0) {
					centX = absolXU;
					centU = absolUD;
					centV = absolVD;
				}
			}
			if (absolVU == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVU;
				}
			}
			if (absolVD == absolUU + absolXD) {
				if (Math.abs(absolUU - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUU;
					centV = absolVD;
				}
			}
			if (absolVU == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVU;
				}
			}
			if (absolVD == absolUD + absolXD) {
				if (Math.abs(absolUD - absolXD) % 3 == 0) {
					centX = absolXD;
					centU = absolUD;
					centV = absolVD;
				}
			}
		}
		centX = (int) (centX * sigX);
		centU = (int) (centU * sigU);
		centV = (int) (centV * sigV);
		// after an annoyingly long process, we have the coordinates of the
		// center of our hexagon. Basically, in FAR too many
		// if statements, here is what we are doing. First, in the big if
		// statement, we figure out which of the 6 cardinal
		// directions we are in - this is important, because there is a
		// coordinate associated with each cardinal direction.
		// in that direction's cone, the coordinate associated with it ALWAYS
		// measures min distance from the center.
		// Next, we try rounding our decimal mouse coordinates up and down.
		// Since there are 3 coordinates, this leads to
		// 2^3 or 8 combinations, each of which we give its own iff statement.
		// For each of these, we do a check to see if
		// it is the center of a hexagon. First, to be a valid point the
		// cardinal coordinate has to equal the sums of the other
		// coordinates. Secondly, if its a center the difference of the
		// non-cardinal coordinates, % 3, will equal 0.
		// finally, because we've been working with absolute values this whole
		// time, we return the positives and negatives.
		int xCoord = 0;
		if (centX < 0) {
			xCoord = (centX - 1) / 2;
		} else {
			xCoord = centX / 2;
		}
		int yCoord = (centV - centU) / 3;
		return getAt(xCoord,yCoord);
	}
	public int cellWidth(){
		return contents.length;
	}
	public int cellHeight(){
		return contents[0].length;
	}
	public double pixelHeight() {
		return cellHeight() * sideLength * 1.5;
	}
	public double pixelWidth() {
		return cellWidth() * sideLength * Math.pow(3, 0.5);
	}
	public int[] convert(int i, int j){
		int modI = (int) Toolbox.positiveMod(i, cellWidth());
		int modJ = (int) Toolbox.positiveMod(j, cellHeight());
		return new int[]{modI, modJ};
	}
	public Space getAt(int i, int j){
		int[] ints = convert(i, j);
		return contents[ints[0]][ints[1]];
	}
	public void setSideLength(double sl){
		sideLength = sl;
	}
	public double getSideLength(){
		return sideLength;
	}
	public double getDeltaX() {
		return deltaX;
	}
	public void setDeltaX(double deltaX) {
		this.deltaX = deltaX;
	}
	public double getDeltaY() {
		return deltaY;
	}
	public void setDeltaY(double deltaY) {
		this.deltaY = deltaY;
	}
	public static ArrayList<Space> getPath(Space from, Space to, General v){
		ArrayList<Space> finalList = new ArrayList<Space>();
		ArrayList<Integer> recorder = new ArrayList<Integer>();
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		temporaryList.add(from);
		recorder.add(-1);
		if(from == to){
			ArrayList<Space> path = new ArrayList<Space>();
			path.add(from);
			return path;
		}
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			finalList.add(temp);
			boolean allowed = true;
			for(Ship s: temp.getFleet()){
				if(s.getOwner() != v){
					allowed = false;
					break;
				}
			}
			if(allowed){
				ArrayList<Space> adjacent = temp.getAdjacent();
				for(Space s: adjacent){
					if(!finalList.contains(s) && !temporaryList.contains(s)){
						temporaryList.addLast(s);
						recorder.add(finalList.size() - 1);
						if(s == to){
							LinkedList<Space> reversePath = new LinkedList<Space>();
							int index = finalList.size() - 1;
							Space node = s;
							while(index != -1){
								reversePath.addLast(node);
								node = finalList.get(index);
								index = recorder.get(index);
							}
							reversePath.addLast(node);
							ArrayList<Space> finalPath = new ArrayList<Space>();
							while(!reversePath.isEmpty()){
								finalPath.add(reversePath.removeLast());
							}
							return finalPath;
						}
					}
				}
			}
		}
		System.out.println("We should never get all the way through here");
		Toolbox.breakThings();
		return finalList;
	}
	public static ArrayList<Space> getPath(Space from, Space to, General v, int terminationDistance){
		ArrayList<Space> finalList = new ArrayList<Space>();
		ArrayList<Integer> recorder = new ArrayList<Integer>();
		LinkedList<Integer> recorderb = new LinkedList<Integer>();
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		temporaryList.add(from);
		recorderb.add(0);
		recorder.add(-1);
		if(from == to){
			ArrayList<Space> path = new ArrayList<Space>();
			path.add(from);
			return path;
		}
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			int dist = recorderb.removeFirst();
			if(dist > terminationDistance){
				return null;
			}
			finalList.add(temp);
			boolean allowed = true;
			for(Ship s: temp.getFleet()){
				if(s.getOwner() != v){
					allowed = false;
					break;
				}
			}
			if(allowed){
				ArrayList<Space> adjacent = temp.getAdjacent();
				for(Space s: adjacent){
					if(!finalList.contains(s) && !temporaryList.contains(s)){
						temporaryList.addLast(s);
						recorderb.addLast(dist + 1);
						recorder.add(finalList.size() - 1);
						if(s == to){
							LinkedList<Space> reversePath = new LinkedList<Space>();
							int index = finalList.size() - 1;
							Space node = s;
							while(index != -1){
								reversePath.addLast(node);
								node = finalList.get(index);
								index = recorder.get(index);
							}
							reversePath.addLast(node);
							ArrayList<Space> finalPath = new ArrayList<Space>();
							while(!reversePath.isEmpty()){
								finalPath.add(reversePath.removeLast());
							}
							return finalPath;
						}
					}
				}
			}
		}
		System.out.println("We should never get all the way through here");
		Toolbox.breakThings();
		return finalList;
	}
	public static ArrayList<Space> getAllWithinForMovement(int distance, Space root, General v){
		ArrayList<Space> finalList = new ArrayList<Space>();
		LinkedList<Integer> recorder = new LinkedList<Integer>();
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		if(distance >= 0){
			temporaryList.add(root);
			recorder.add(0);
		}
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			int dist = recorder.removeFirst();
			boolean allowed = true;
			for(Ship s: temp.getFleet()){
				if(s.getOwner() != v){
					allowed = false;
					break;
				}
			}
			if(allowed){
				ArrayList<Space> adjacent = temp.getAdjacent();
				for(Space s: adjacent){
					if(!finalList.contains(s) && !temporaryList.contains(s) && dist < distance){
						temporaryList.addLast(s);
						recorder.addLast(dist + 1);
					}
				}
			}
			finalList.add(temp);
		}
		return finalList;
	}
	public static ArrayList<Space> getAllWithin(int distance, Space root){
		ArrayList<Space> finalList = new ArrayList<Space>();
		LinkedList<Integer> recorder = new LinkedList<Integer>();
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		if(distance >= 0){
			temporaryList.add(root);
			recorder.add(0);
		}
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			int dist = recorder.removeFirst();
			ArrayList<Space> adjacent = temp.getAdjacent();
			for(Space s: adjacent){
				if(!finalList.contains(s) && !temporaryList.contains(s) && dist < distance){
					temporaryList.addLast(s);
					recorder.addLast(dist + 1);
				}
			}
			finalList.add(temp);
		}
		return finalList;
	}
	public static boolean isTradeConnected(Space root, Space target, General owner){
		if(root == target){
			return true;
		}
		ArrayList<Space> finalList = new ArrayList<Space>();
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		temporaryList.add(root);
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			ArrayList<Space> adjacent = temp.getTradeAdjacent(owner);
			for(Space s: adjacent){
				if(!finalList.contains(s) && !temporaryList.contains(s)){
					temporaryList.addLast(s);
					if(s == target){
						return true;
					}
				}
			}
			finalList.add(temp);
		}
		return false;
	}
	public static ArrayList<Resource> getClosestAvailableResources(Space root, General owner, int[] requested){
		ArrayList<Space> finalList = new ArrayList<Space>();
		ArrayList<Resource> found = new ArrayList<Resource>();
		int[] findingLog = new int[requested.length];
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		temporaryList.add(root);
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			ArrayList<Space> adjacent = temp.getTradeAdjacent(owner);
			for(Space s: adjacent){
				if(!finalList.contains(s) && !temporaryList.contains(s)){
					temporaryList.addLast(s);
				}
			}
			finalList.add(temp);
			for(Resource r: temp.getPile()){
				if(r.getOwner() == owner && r.getType() < requested.length){
					if(findingLog[r.getType()] < requested[r.getType()]){
						found.add(r);
						findingLog[r.getType()]++;
					}
				}
			}
			boolean allFound = true;
			for(int i = 0; i < findingLog.length; i++){
				if(findingLog[i] != requested[i]){
					allFound = false;
				}
			}
			if(allFound){
				return found;
			}
		}
		System.out.println("We weren't supposed to ever get here.... we were supposed to find it inside the loop");
		Toolbox.breakThings();
		return found;
	}
	public static ArrayList<Resource> getAllAvailableResources(Space root, General owner){
		ArrayList<Resource> found = new ArrayList<Resource>();
		for(Space s: getAllTradeConnections(root, owner)){
			for(Resource r: s.getPile()){
				if(r.getOwner() == owner){
					found.add(r);
				}
			}
		}
		return found;
	}
	public static ArrayList<Space> getAllTradeConnections(Space root, General owner){
		ArrayList<Space> finalList = new ArrayList<Space>();
		LinkedList<Space> temporaryList = new LinkedList<Space>();
		temporaryList.add(root);
		while(temporaryList.size() > 0){
			Space temp = temporaryList.removeFirst();
			ArrayList<Space> adjacent = temp.getTradeAdjacent(owner);
			for(Space s: adjacent){
				if(!finalList.contains(s) && !temporaryList.contains(s)){
					temporaryList.addLast(s);
				}
			}
			finalList.add(temp);
		}
		return finalList;
	}
}