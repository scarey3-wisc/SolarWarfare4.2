import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Structure extends Index implements Consumer{
	private Space location;
	private String type;
	private int usedStatus;
	private ArrayList<ButtonAction> actions;
	public static boolean CreationAllowed;
	public static boolean ImmediateResourceGeneration;
	/*
	 * 0: unused, unbuffed
	 * 1: used, unbuffed
	 * 2: unused, buffed
	 * 3: used, buffed
	 * 4: used, buffed, used again
	 */
	public Structure(String type, Space l){
		this.type = type;
		usedStatus = 0;
		location = l;
		actions = new ArrayList<ButtonAction>();
	}
	public Structure(String[] desc, ArrayList<Space> loc){
		type = desc[0];
		usedStatus = Integer.parseInt(desc[1]);
		location = loc.get(Integer.parseInt(desc[2]));
	}
	public String getDescription(){
		String desc = "";
		desc += type + "!";
		desc += usedStatus + "!";
		desc += location.getIndex();
		return desc;
	}
	public static Structure createNew(String type, Space l){
		Structure product = new Structure(type, l);
		product.setUsedStatus(1);
		return product;
	}
	public BufferedImage getMapImage(double width, double height){
		width *= 0.95;
		height *= 0.95;
		if(type.contains("Refinery")){
			return SolarWarfare4_2.il.findImage("Refinery").getFit((int)(width), (int) (height));
		}else{
			return SolarWarfare4_2.il.findImage(type).getFit((int)(width), (int) (height));
		}
	}
	public BufferedImage getDetailImage(double width, double height){
		width *= 0.95;
		height *= 0.95;
		if(type.contains("Refinery")){
			BufferedImage source = SolarWarfare4_2.il.findImage("Refinery").getFit((int) width, (int) height);
			BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = copy.getGraphics();
			g.drawImage(source, 0, 0, null);
			BufferedImage r = SolarWarfare4_2.il.findImage("Resource" + Resource.toType(type.split(" ")[0])).getFit((int) width, (int)(height/2));
			g.drawImage(r, copy.getWidth()/2 - r.getWidth()/2, copy.getHeight()/2 - r.getHeight()/2, null);
			return copy;
		}else{
			return SolarWarfare4_2.il.findImage(type).getFit((int)(width), (int) (height));
		}
	}
	public BufferedImage getConstructionImage(double width, double height){
		width *= 1.0;
		height *= 1.0;
		if(type.contains("Refinery")){
			BufferedImage source = SolarWarfare4_2.il.findImage("Refinery").getFit((int) width, (int) height);
			BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = copy.getGraphics();
			g.drawImage(source, 0, 0, null);
			BufferedImage r = SolarWarfare4_2.il.findImage("Resource" + Resource.toType(type.split(" ")[0])).getFit((int) width, (int)(height/1.3));
			g.drawImage(r, copy.getWidth()/2 - r.getWidth()/2, copy.getHeight()/2 - r.getHeight()/2, null);
			return copy;
		}else{
			return SolarWarfare4_2.il.findImage(type).getFit((int)(width), (int) (height));
		}
	}
	public Space getLocation(){
		return location;
	}
	public void beginTurn(General g){
		usedStatus = 0;
		if(type.contains("Refinery")){
			usedStatus = 1;
			getLocation().addResource(new Resource(Resource.toType(type.split(" ")[0]), getLocation(), g));
		}
	}
	public boolean isUsedYet() {
		return usedStatus != 0 && usedStatus != 2;
	}
	public int getUsedStatus(){
		return usedStatus;
	}
	public void setUsedStatus(int status) {
		this.usedStatus = status;
	}
	public String getType() {
		return type;
	}
	public ArrayList<ButtonAction> getNewActs(General viewing, ArrayList<Resource> avail){
		Ship buffer = null;
		Ship bomber = null;
		for(Ship s: getLocation().getFleet()){
			if(s.canBuff() && s.getOwner() == viewing){
				buffer = s;
			}
			if(s.isMissileActive() && s.getOwner() == viewing){
				bomber = s;
			}
		}
		ArrayList<ButtonAction> alba = new ArrayList<ButtonAction>();
		if(type.contains("Refinery")){
			DestroyBuilding db = new DestroyBuilding(this, viewing, bomber);
			if(db.legalChange()){
				alba.add(db);
			}
			BuildingBuff bb = new BuildingBuff(buffer, this);
			if(bb.legalChange()){
				alba.add(bb);
			}
		}else if(type.equals("Factory")){
			DestroyBuilding db = new DestroyBuilding(this, viewing, bomber);
			if(db.legalChange()){
				alba.add(db);
			}
			BuildingBuff bb = new BuildingBuff(buffer, this);
			if(bb.legalChange()){
				alba.add(bb);
			}
			ProduceMissile pm = new ProduceMissile(this, viewing, avail);
			if(pm.legalChange()){
				alba.add(pm);
			}
		}else if(type.equals("Space Station")){
			DestroyBuilding db = new DestroyBuilding(this, viewing, bomber);
			if(db.legalChange()){
				alba.add(db);
			}
			BuildingBuff bb = new BuildingBuff(buffer, this);
			if(bb.legalChange()){
				alba.add(bb);
			}
			
			if(!CreationAllowed){
				alba.add(new VerticalBarrier());
				ProduceShip pm0 = new ProduceShip(this, viewing, avail, 0);
				if(pm0.legalChange()){
					alba.add(pm0);
				}
				ProduceShip pm1 = new ProduceShip(this, viewing, avail, 1);
				if(pm1.legalChange()){
					alba.add(pm1);
				}
				ProduceShip pm2 = new ProduceShip(this, viewing, avail, 2);
				if(pm2.legalChange()){
					alba.add(pm2);
				}
			}
		}else if(type.equals("Building Site")){
			if(CreationAllowed){
				CreateBuilding cb0 = new CreateBuilding(viewing, "Hydrogen Refinery", (Planet) location);
				if(cb0.legalChange()){
					alba.add(cb0);
				}
				CreateBuilding cb1 = new CreateBuilding(viewing, "Iron Refinery", (Planet) location);
				if(cb1.legalChange()){
					alba.add(cb1);
				}
				CreateBuilding cb2 = new CreateBuilding(viewing, "Gold Refinery", (Planet) location);
				if(cb2.legalChange()){
					alba.add(cb2);
				}
				CreateBuilding cb3 = new CreateBuilding(viewing, "Silicon Refinery", (Planet) location);
				if(cb3.legalChange()){
					alba.add(cb3);
				}
				CreateBuilding cb4 = new CreateBuilding(viewing, "Factory", (Planet) location);
				if(cb4.legalChange()){
					alba.add(cb4);
				}
				CreateBuilding cb5 = new CreateBuilding(viewing, "Space Station", (Planet) location);
				if(cb5.legalChange()){
					alba.add(cb5);
				}
			}else{
				ConstructBuilding cb0 = new ConstructBuilding(viewing, avail, "Hydrogen Refinery", (Planet) location);
				if(cb0.legalChange()){
					alba.add(cb0);
				}
				ConstructBuilding cb1 = new ConstructBuilding(viewing, avail, "Iron Refinery", (Planet) location);
				if(cb1.legalChange()){
					alba.add(cb1);
				}
				ConstructBuilding cb2 = new ConstructBuilding(viewing, avail, "Gold Refinery", (Planet) location);
				if(cb2.legalChange()){
					alba.add(cb2);
				}
				ConstructBuilding cb3 = new ConstructBuilding(viewing, avail, "Silicon Refinery", (Planet) location);
				if(cb3.legalChange()){
					alba.add(cb3);
				}
				ConstructBuilding cb4 = new ConstructBuilding(viewing, avail, "Factory", (Planet) location);
				if(cb4.legalChange()){
					alba.add(cb4);
				}
				ConstructBuilding cb5 = new ConstructBuilding(viewing, avail, "Space Station", (Planet) location);
				if(cb5.legalChange()){
					alba.add(cb5);
				}
			}
		}
		if(CreationAllowed && !type.equals("Building Site")){
			alba.add(new VerticalBarrier());
			CreateShip pm0 = new CreateShip(location, viewing, 0);
			if(pm0.legalChange()){
				alba.add(pm0);
			}
			CreateShip pm1 = new CreateShip(location, viewing, 1);
			if(pm1.legalChange()){
				alba.add(pm1);
			}
			CreateShip pm2 = new CreateShip(location, viewing, 2);
			if(pm2.legalChange()){
				alba.add(pm2);
			}
			CreateShip pm3 = new CreateShip(location, viewing, 3);
			if(pm3.legalChange()){
				alba.add(pm3);
			}
		}
		return alba;
	}
	public ArrayList<ButtonAction> getCurrentActs(){
		return actions;
	}
	public ArrayList<ButtonAction> generateActs(General viewing, ArrayList<Resource> avail){
		actions = getNewActs(viewing, avail);
		return actions;
	}
	public class VerticalBarrier extends ButtonAction{

		@Override
		public void makeChange() {}

		@Override
		public void undoChange() {}

		@Override
		public boolean legalChange() {
			return true;
		}

		@Override
		public ArrayList<String> getDescription() {
			return null;
		}

		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			BufferedImage b = new BufferedImage(1, ah, BufferedImage.TYPE_INT_ARGB);
			return b;
		}

		@Override
		public String getTooltipText() {
			return null;
		}

		@Override
		public int getWidth(int aw, int ah) {
			return 1;
		}

		@Override
		public int getHeight(int aw, int ah) {
			return ah;
		}

		@Override
		public int[] cost() {
			return null;
		}
	}
	public class CreateBuilding extends ButtonAction{
		private boolean legal;
		private String type;
		private Structure create;
		private Planet location;
		private int[] cost;
		public CreateBuilding(General v, String type, Planet loc) {
			super();
			this.type = type;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			location = loc;
			if(location == null){
				legal = false;
				return;
			}
			if(location.getStructures().size() > 2){
				legal = false;
				return;
			}
			if(location.getOwner() != v){
				legal = false;
				return;
			}
			if(type.contains("Refinery")){
				String prodType = type.split(" ")[0];
				int index = Resource.toType(prodType);
				int amount = location.getProduction()[index];
				for(Structure s: location.getStructures()){
					if(s.getType().equals(type)){
						amount--;
					}
				}
				if(amount <= 0){
					legal = false;
					return;
				}
			}
			cost = new int[]{0,0,0,0};
			switch(type){
			case "Factory": cost = Costs.factoryCost; break;
			case "Space Station": cost = Costs.spaceStationCost; break;
			case "Hydrogen Refinery": cost = Costs.refineryCost; break;
			case "Iron Refinery": cost = Costs.refineryCost; break;
			case "Gold Refinery": cost = Costs.refineryCost; break;
			case "Silicon Refinery": cost = Costs.refineryCost; break;
			default: System.out.println("Bad type given to ConstructBuilding");Toolbox.breakThings();
			}
			create = Structure.createNew(type, location);
		}
		public void makeChange() {
			location.addStructure(create);
			if(create.getType().contains("Refinery") && ImmediateResourceGeneration){
				getLocation().addResource(new Resource(Resource.toType(type.split(" ")[0]), getLocation(), location.getOwner()));
			}
		}
		public void undoChange() {
			location.removeStructure(create);
			if(create.getType().contains("Refinery") && ImmediateResourceGeneration){
				getLocation().removeResource(location.getOwner(), Resource.toType(type.split(" ")[0]));
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Create " + type;
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			return create.getConstructionImage(aw, ah/2);
		}
		@Override
		public int getWidth(int aw, int ah) {
			return 3*ah/4;
		}
		@Override
		public int getHeight(int aw, int ah) {
			return ah/2;
		}
		@Override
		public int[] cost() {
			return cost;
		}
	}
	public class ConstructBuilding extends ButtonAction{
		private boolean legal;
		private ArrayList<Resource> willSpend;
		private String type;
		private Structure create;
		private Planet location;
		private int[] cost;
		public ConstructBuilding(General v, ArrayList<Resource> avail, String type, Planet loc) {
			super();
			this.type = type;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			location = loc;
			if(location == null){
				legal = false;
				return;
			}
			if(location.getStructures().size() > 2){
				legal = false;
				return;
			}
			if(location.getOwner() != v){
				legal = false;
				return;
			}
			if(type.contains("Refinery")){
				String prodType = type.split(" ")[0];
				int index = Resource.toType(prodType);
				int amount = location.getProduction()[index];
				for(Structure s: location.getStructures()){
					if(s.getType().equals(type)){
						amount--;
					}
				}
				if(amount <= 0){
					legal = false;
					return;
				}
			}
			int[] amounts = Resource.toIndexedArray(avail);
			cost = new int[]{0,0,0,0};
			switch(type){
			case "Factory": cost = Costs.factoryCost; break;
			case "Space Station": cost = Costs.spaceStationCost; break;
			case "Hydrogen Refinery": cost = Costs.refineryCost; break;
			case "Iron Refinery": cost = Costs.refineryCost; break;
			case "Gold Refinery": cost = Costs.refineryCost; break;
			case "Silicon Refinery": cost = Costs.refineryCost; break;
			default: System.out.println("Bad type given to ConstructBuilding");Toolbox.breakThings();
			}
			for(int i = 0; i < cost.length; i++){
				if(cost[i] > amounts[i]){
					legal = false;
					return;
				}
			}
			create = Structure.createNew(type, location);
			willSpend = Map.getClosestAvailableResources(location, v, cost);
		}
		public void makeChange() {
			location.addStructure(create);
			for(Resource r: willSpend){
				r.getLocation().removeResource(r);
			}
			if(create.getType().contains("Refinery") && ImmediateResourceGeneration){
				getLocation().addResource(new Resource(Resource.toType(type.split(" ")[0]), getLocation(), location.getOwner()));
			}
		}
		public void undoChange() {
			location.removeStructure(create);
			for(Resource r: willSpend){
				r.getLocation().addResource(r);
			}
			if(create.getType().contains("Refinery") && ImmediateResourceGeneration){
				getLocation().removeResource(location.getOwner(), Resource.toType(type.split(" ")[0]));
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Construct " + type;
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			return create.getConstructionImage(aw, ah/2);
		}
		@Override
		public int getWidth(int aw, int ah) {
			return 3*ah/4;
		}
		@Override
		public int getHeight(int aw, int ah) {
			return ah/2;
		}
		@Override
		public int[] cost() {
			return cost;
		}
	}
	public static void ResetShipCreation(){
		CreateShip.available = new int[]{2, 2, 2, 1};
	}
	public static class CreateShip extends ButtonAction{
		private Space location;
		private boolean legal;
		private int type;
		private Ship create;
		private static int[] available;
		private int[] cost;
		public CreateShip(Space location, General v, int type) {
			super();
			this.type = type;
			this.location = location;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			if(location.getOwner() != v){
				legal = false;
				return;
			}
			if(available[type] < 1){
				legal = false;
				return;
			}
			switch(type){
			case 0: cost = Costs.ship0Cost; break;
			case 1: cost = Costs.ship1Cost; break;
			case 2: cost = Costs.ship2Cost; break;
			case 3: cost = Costs.ship3Cost; break;
			default: System.out.println("Bad type given to ProduceShip");Toolbox.breakThings();
			}
			create = Ship.createNew(type, v, location);
		}
		public void makeChange() {
			location.addShip(create);
			available[type] --;
		}
		public void undoChange(){
			location.removeShip(create);
			available[type] ++;
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Create " + create.getOwner().getNameFor(type);
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			if(type == 2){
				BufferedImage missile = SolarWarfare4_2.il.findImage("Ship" + type + "Prod").getFit(aw, ah);
				return missile;
			}else if(type == 3){
				BufferedImage missile = SolarWarfare4_2.il.findImage("Ship" + type + "Prod").getFit(aw, ah);
				return missile;
			}else{
				BufferedImage missile = SolarWarfare4_2.il.findImage("Ship" + type + "Prod").getFit(aw, ah/2);
				return missile;
			}
		}
		@Override
		public int getWidth(int aw, int ah) {
			if(type == 3){
				if(aw < ah){
					return aw/3;
				}else{
					return ah/3;
				}
			}else{
				if(aw < ah){
					return aw;
				}else{
					return ah;
				}
			}
			
		}
		@Override
		public int getHeight(int aw, int ah) {
			if(type == 2 || type == 3){
				if(aw < ah){
					return aw;
				}else{
					return ah;
				}
			}else{
				if(aw < ah){
					return aw/2;
				}else{
					return ah/2;
				}
			}
		}
		@Override
		public int[] cost() {
			return cost;
		}
	}
	public class ProduceShip extends ButtonAction{
		private Structure station;
		private boolean legal;
		private ArrayList<Resource> willSpend;
		private int type;
		private Ship create;
		private int[] cost;
		public ProduceShip(Structure b2, General v, ArrayList<Resource> avail, int type) {
			super();
			this.type = type;
			station = b2;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			if(station == null){
				legal = false;
				return;
			}
			if(!station.getType().equals("Space Station")){
				legal = false;
				return;
			}
			if(!(station.usedStatus == 0 || station.usedStatus == 2 || station.usedStatus == 3)){
				legal = false;
				return;
			}
			for(Ship s: location.getFleet()){
				if(s.getOwner() != v){
					legal = false;
					return;
				}
			}
			if(location.getOwner() != v){
				legal = false;
				return;
			}
			int[] amounts = Resource.toIndexedArray(avail);
			cost = new int[]{0,0,0,0};
			switch(type){
			case 0: cost = Costs.ship0Cost; break;
			case 1: cost = Costs.ship1Cost; break;
			case 2: cost = Costs.ship2Cost; break;
			default: System.out.println("Bad type given to ProduceShip");Toolbox.breakThings();
			}
			for(int i = 0; i < cost.length; i++){
				if(cost[i] > amounts[i]){
					legal = false;
					return;
				}
			}
			create = Ship.createNew(type, v, station.getLocation());
			willSpend = Map.getClosestAvailableResources(location, v, cost);
		}
		public void makeChange() {
			if(station.usedStatus == 0){
				station.usedStatus = 1;
			}else if(station.usedStatus == 2){
				station.usedStatus = 3;
			}else if(station.usedStatus == 3){
				station.usedStatus = 4;
			}
			getLocation().addShip(create);
			for(Resource r: willSpend){
				r.getLocation().removeResource(r);
			}
		}
		public void undoChange() {
			if(station.usedStatus == 1){
				station.usedStatus = 0;
			}else if(station.usedStatus == 3){
				station.usedStatus = 2;
			}else if(station.usedStatus == 4){
				station.usedStatus = 3;
			}
			getLocation().removeShip(create);
			for(Resource r: willSpend){
				r.getLocation().addResource(r);
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Construct " + station.getLocation().getOwner().getNameFor(type);
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			if(type == 2){
				BufferedImage missile = SolarWarfare4_2.il.findImage("Ship" + type + "Prod").getFit(aw, ah);
				return missile;
			}else{
				BufferedImage missile = SolarWarfare4_2.il.findImage("Ship" + type + "Prod").getFit(aw, ah/2);
				return missile;
			}
		}
		@Override
		public int getWidth(int aw, int ah) {
			if(aw < ah){
				return aw;
			}else{
				return ah;
			}
		}
		@Override
		public int getHeight(int aw, int ah) {
			if(type == 2){
				if(aw < ah){
					return aw;
				}else{
					return ah;
				}
			}else{
				if(aw < ah){
					return aw/2;
				}else{
					return ah/2;
				}
			}
		}
		@Override
		public int[] cost() {
			return cost;
		}
	}
	public class ProduceMissile extends ButtonAction{
		private Structure factory;
		private boolean legal;
		private ArrayList<Resource> willSpend;
		private int[] cost;
		public ProduceMissile(Structure b2, General v, ArrayList<Resource> avail) {
			super();
			factory = b2;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			if(factory == null){
				legal = false;
				return;
			}
			if(!factory.getType().equals("Factory")){
				legal = false;
				return;
			}
			if(!(factory.usedStatus == 0 || factory.usedStatus == 2 || factory.usedStatus == 3)){
				legal = false;
				return;
			}
			if(location.getOwner() != v){
				legal = false;
				return;
			}
			int[] amounts = Resource.toIndexedArray(avail);
			cost = Costs.missileCost;
			for(int i = 0; i < Costs.missileCost.length; i++){
				if(Costs.missileCost[i] > amounts[i]){
					legal = false;
					return;
				}
			}
			willSpend = Map.getClosestAvailableResources(location, v, Costs.missileCost);
		}
		public void makeChange() {
			if(factory.usedStatus == 0){
				factory.usedStatus = 1;
			}else if(factory.usedStatus == 2){
				factory.usedStatus = 3;
			}else if(factory.usedStatus == 3){
				factory.usedStatus = 4;
			}
			getLocation().addResource(new Resource(4, getLocation(), getLocation().getOwner()));
			for(Resource r: willSpend){
				r.getLocation().removeResource(r);
			}
		}
		public void undoChange() {
			if(factory.usedStatus == 1){
				factory.usedStatus = 0;
			}else if(factory.usedStatus == 3){
				factory.usedStatus = 2;
			}else if(factory.usedStatus == 4){
				factory.usedStatus = 3;
			}
			getLocation().removeResource(getLocation().getOwner(), 4);
			for(Resource r: willSpend){
				r.getLocation().addResource(r);
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Produce Missile";
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			BufferedImage missile = SolarWarfare4_2.il.findImage("Missile").getFit(aw, ah);
			return missile;
		}
		@Override
		public int getWidth(int aw, int ah) {
			if(aw < ah){
				return aw;
			}else{
				return ah;
			}
		}
		@Override
		public int getHeight(int aw, int ah) {
			if(aw < ah){
				return aw;
			}else{
				return ah;
			}
		}
		@Override
		public int[] cost() {
			return cost;
		}
	}
	public class DestroyBuilding extends ButtonAction{
		private Structure target;
		private General viewer;
		private Ship bomber;
		private boolean legal;
		private int index;
		private int forwardOpinion;
		private int backwardOpinion;
		private General previousOwner;
		public DestroyBuilding(Structure b2, General v, Ship source) {
			super();
			target = b2;
			viewer = v;
			bomber = source;
			legal = true;
			if(viewer == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			if(target == null){
				legal = false;
				return;
			}
			if(target.getLocation().getOwner() == v){
				bomber = null;
			}else if(bomber == null){
				legal = false;
				return;
			}else if(!bomber.isMissileActive()){
				legal = false;
				return;
			}
			if(bomber != null){
				if(bomber.isAttackedYet()){
					legal = false;
					return;
				}
				previousOwner = target.getLocation().getOwner();
				if(previousOwner != null){
					forwardOpinion = bomber.getOwner().lookupRelation(previousOwner);
					backwardOpinion = previousOwner.lookupRelation(bomber.getOwner());
				}
			}
			Planet p = (Planet) target.getLocation();
			index = p.getStructures().indexOf(target);
		}
		public void makeChange() {
			Planet p = (Planet) target.getLocation();
			p.getStructures().remove(index);
			if(bomber != null){
				bomber.setActiveMissile(false);
				bomber.setAttackedYet(true);
				if(previousOwner != null){
					target.getLocation().setOwner(null);
					bomber.getOwner().setRelation(previousOwner, -1);
					previousOwner.setRelation(bomber.getOwner(), -1);
					for(Space s: Map.getAllWithin(2, bomber.getLocation())){
						s.calculateClaims();
					}
				}
			}
			if(target.getType().contains("Refinery") && ImmediateResourceGeneration){
				getLocation().removeResource(location.getOwner(), Resource.toType(type.split(" ")[0]));
			}
		}
		public void undoChange() {
			Planet p = (Planet) target.getLocation();
			p.getStructures().add(index, target);
			if(bomber != null){
				bomber.setActiveMissile(true);
				bomber.setAttackedYet(false);
				if(previousOwner != null){
					target.getLocation().setOwner(previousOwner);
					bomber.getOwner().setRelation(previousOwner, forwardOpinion);
					previousOwner.setRelation(bomber.getOwner(), backwardOpinion);
					for(Space s: Map.getAllWithin(2, bomber.getLocation())){
						s.calculateClaims();
					}
				}
			}
			if(target.getType().contains("Refinery") && ImmediateResourceGeneration){
				getLocation().addResource(new Resource(Resource.toType(type.split(" ")[0]), getLocation(), location.getOwner()));
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			if(bomber == null){
				return "Demolish Structure";
			}else{
				return "Bomb Structure";
			}
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			if(bomber == null){
				BufferedImage missile = SolarWarfare4_2.il.findImage("Bomb").getFit(aw/2, ah/2);
				return missile;
			}else{
				BufferedImage missile = SolarWarfare4_2.il.findImage("Bomb").getFit(aw, ah);
				return missile;
			}
			
		}
		@Override
		public int getWidth(int aw, int ah) {
			if(bomber == null){
				if(aw < ah){
					return aw/2;
				}else{
					return ah/2;
				}
			}else{
				if(aw < ah){
					return aw;
				}else{
					return ah;
				}
			}
		}
		@Override
		public int getHeight(int aw, int ah) {
			if(bomber == null){
				if(aw < ah){
					return aw/2;
				}else{
					return ah/2;
				}
			}else{
				if(aw < ah){
					return aw;
				}else{
					return ah;
				}
			}
		}
		@Override
		public int[] cost() {
			return null;
		}
	}
	public class BuildingBuff extends ButtonAction{
		private Ship buffer;
		private Structure buffed;
		private boolean legal;
		public BuildingBuff(Ship b1, Structure b2) {
			super();
			buffer = b1;
			buffed = b2;
			legal = true;
			if(buffer == null){
				legal = false;
				return;
			}
			if(!buffer.getOwner().isActive()){
				legal = false;
				return;
			}
			if(!buffer.canBuff()){
				legal = false;
				return;
			}
			if(buffed.usedStatus > 1){
				legal = false;
				return;
			}
			if(buffed.getType().equals("Building Site")){
				legal = false;
				return;
			}
			if(location.getOwner() != buffer.getOwner()){
				legal = false;
				return;
			}
		}
		public void makeChange() {
			buffer.donateBuff();
			buffed.usedStatus += 2;
			if(buffed.getType().contains("Refinery")){
				buffed.usedStatus += 1;
				getLocation().addResource(new Resource(Resource.toType(type.split(" ")[0]), getLocation(), buffer.getOwner()));
			}
		}
		public void undoChange() {
			if(buffed.getType().contains("Refinery")){
				getLocation().removeResource(buffer.getOwner(), Resource.toType(type.split(" ")[0]));
				buffed.usedStatus -= 1;
			}
			buffed.usedStatus -= 2;
			buffer.undonateBuff();
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Empower Structure";
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			BufferedImage buff = SolarWarfare4_2.il.findImage("Buff").getFit(aw/2, ah/2);
			return buff;
		}
		@Override
		public int getWidth(int aw, int ah) {
			if(aw < ah){
				return aw/2;
			}else{
				return ah/2;
			}
		}
		@Override
		public int getHeight(int aw, int ah) {
			if(aw < ah){
				return aw/2;
			}else{
				return ah/2;
			}
		}
		@Override
		public int[] cost() {
			return null;
		}
	}
}