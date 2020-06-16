import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Ship extends Index implements Consumer{
	private int type;
	private int m,a,d;
	private General owner;
	private Space location;
	private int amountMoved;
	private boolean attackedYet;
	private boolean selected;
	private boolean heldMissile;
	private boolean activeMissile;
	private boolean buffed;
	private int buffStatus;
	private ArrayList<ButtonAction> actions;
	private double theta;
	/*
	 * 0: cannot buff
	 * 1: a buffer who can buff!
	 * 2: a buffer who cannot buff
	 */
	public Ship(int type, General owner, Space location){
		this.type = type;
		this.owner = owner;
		this.location = location;
		setStats(type);
		amountMoved = 0;
		attackedYet = false;
		selected = false;
		heldMissile = false;
		activeMissile = false;
		buffed = false;
		theta = 0;
	}
	public Ship(String[] desc, ArrayList<General> grl, ArrayList<Space> loc){
		int type = Integer.parseInt(desc[0]);
		int ownerIndex = Integer.parseInt(desc[1]);
		int locationIndex = Integer.parseInt(desc[2]);
		this.type = type;
		this.owner = grl.get(ownerIndex);
		this.location = loc.get(locationIndex);
		setStats(type);
		amountMoved = Integer.parseInt(desc[3]);
		attackedYet = Boolean.parseBoolean(desc[4]);
		heldMissile = Boolean.parseBoolean(desc[5]);
		activeMissile = Boolean.parseBoolean(desc[6]);
		buffed = Boolean.parseBoolean(desc[7]);
		buffStatus = Integer.parseInt(desc[8]);
		theta = Double.parseDouble(desc[9]);
	}
	public String getDescription(){
		String desc = "";
		desc += type + "!";
		desc += owner.getIndex() + "!";
		desc += location.getIndex() + "!";
		desc += amountMoved + "!";
		desc += attackedYet + "!";
		desc += heldMissile + "!";
		desc += activeMissile + "!";
		desc += buffed + "!";
		desc += buffStatus + "!";
		desc += theta;
		return desc;
	}
	public static Ship createNew(int type, General owner, Space location){
		Ship s = new Ship(type, owner, location);
		s.setAmountMoved(s.getM());
		s.setAttackedYet(true);
		if(s.buffStatus == 1){
			s.buffStatus = 2;
		}
		return s;
	}
	public void setStats(int type){
		if(type == 0){
			furyStats();
		}else if(type == 1){
			hunterStats();
		}else if(type == 2){
			eagleStats();
		}else if(type == 3){
			generalStats();
		}else{
			System.out.println("Warning: we attempted to set the stats of a ship with an invalid type " + type);
			Toolbox.breakThings();
		}
	}
	public void refresh(){
		buffed = false;
		attackedYet = false;
		amountMoved = 0;
		activeMissile = false;
		if(buffStatus == 2){
			buffStatus = 1;
		}
	}
	private void furyStats(){
		m = 4;
		a = 2;
		d = 1;
		buffStatus = 0;
	}
	private void hunterStats(){
		m = 3;
		a = 3;
		d = 3;
		buffStatus = 0;
	}
	private void eagleStats(){
		m = 2;
		a = 4;
		d = 5;
		buffStatus = 0;
	}
	private void generalStats(){
		m = 5;
		a = 0;
		d = 1;
		buffStatus = 1;
	}
	public BufferedImage getTooltip(double height){
		int h = (int) height;
		int w = (int) (height * 2);
		int lieH = 0;
		if(!isMissileHeld() && !isMissileActive()){
			lieH = (int) (height/3);
		}
		BufferedImage write = new BufferedImage(w, h - lieH, BufferedImage.TYPE_INT_ARGB);
		Graphics g = write.getGraphics();
		g.setColor(new Color(200,200,200));
		g.fillRoundRect(0, 0, w, h-lieH, h/4, h/4);
		g.setColor(Color.black);
		String name = getOwner().getNameFor(getType());
		drawText(name, w-height*2/3, height/3, (w + height*2/3)/2, height/6, g);
		g.drawImage(getOwner().getInsignia((int)(height * 2/3)), 0, 0, null);
		g.setColor(new Color(104,104,0));
		drawText(""+getM(), height/3, height/3, w/2, height/2, g);
		g.setColor(new Color(200,0,0));
		drawText(""+getA(), height/3, height/3, height/3 + w/2, height/2, g);
		g.setColor(new Color(0,100,0));
		drawText(""+getD(), height/3, height/3, 2*height/3 + w/2, height/2, g);
		if(isMissileHeld()){
			BufferedImage mis0 = SolarWarfare4_2.il.findImage("Missile0").getFit((int)(w/2*0.9), (int) (height/3*0.9));
			g.drawImage(mis0, w/4 - mis0.getWidth()/2, 5*h/6-mis0.getHeight()/2, null);
		}
		if(isMissileActive()){
			BufferedImage mis1 = SolarWarfare4_2.il.findImage("Missile1").getFit((int)(w/2*0.9), (int) (height/3*0.9));
			g.drawImage(mis1, 3*w/4 - mis1.getWidth()/2, 5*h/6 - mis1.getHeight()/2, null);
		}
		if(isBuffed()){
			BufferedImage realWrite = new BufferedImage((int) (write.getWidth() * 1.06), (int) (write.getHeight() + write.getWidth() * 0.06), BufferedImage.TYPE_INT_ARGB);
			Graphics gg = realWrite.getGraphics();
			gg.setColor(new Color(172, 167, 2));
			gg.fillRoundRect(0, 0, realWrite.getWidth(), realWrite.getHeight(), h/4, h/4);
			gg.drawImage(write, (realWrite.getWidth() - write.getWidth())/2, (realWrite.getHeight() - write.getHeight())/2, null);
			write = realWrite;
		}
		return write;
	}
	private void drawText(String draw, double width, double height, double cX, double cY, Graphics g) {
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.setFont(g.getFont().deriveFont(TextPackage.idealFont(draw, (int) (width), (int) (height), 1, g)));
		width = g.getFontMetrics().stringWidth(draw);
		height = g.getFontMetrics().getHeight();
		int xCoord = (int) (cX - width / 2);
		int yCoord = (int) (cY - height / 2);
		g.drawString(draw, xCoord, (int) (yCoord + 3 * height / 4));
	}
	public BufferedImage requestRotatedImage(double sideLength, General viewing, double theta){
		return getImage(sideLength, viewing, theta);
	}
	public BufferedImage getImage(double sideLength, General viewing, boolean allowRotation){
		if(allowRotation){
			return getImage(sideLength, viewing, theta);
		}
		String name = "Ship" + type;
		if(isSelected()){
			sideLength *= 31.0/25.0;
			name+= "S";
		}else if(viewing != null){
			if(viewing.lookupRelation(getOwner()) > 0 && getOwner() != viewing){
				sideLength *= 28.0/25.0;
				name+= "A";
			}else if(viewing.lookupRelation(getOwner()) < 0){
				sideLength *= 28.0/25.0;
				name+= "E";
			}else if(viewing.lookupRelation(getOwner()) == 0){
				sideLength *= 28.0/25.0;
				name+= "N";
			}else if(isBuffed()){
				sideLength *= 31.0/25.0;
				name += "B";
			}
		}else if(isBuffed()){
			sideLength *= 31.0/25.0;
			name += "B";
		}
		sideLength *= 0.9;
		return SolarWarfare4_2.il.findImage(name).getFit((int) sideLength, (int) sideLength);
	}
	private BufferedImage getImage(double sideLength, General viewing, double theta){
		String name = "Ship" + type;
		if(isSelected()){
			sideLength *= 31.0/25.0;
			name+= "S";
		}else if(viewing.lookupRelation(getOwner()) > 0 && getOwner() != viewing){
			sideLength *= 28.0/25.0;
			name+= "A";
		}else if(viewing.lookupRelation(getOwner()) < 0){
			sideLength *= 28.0/25.0;
			name+= "E";
		}else if(viewing.lookupRelation(getOwner()) == 0){
			sideLength *= 28.0/25.0;
			name+= "N";
		}else if(isBuffed()){
			sideLength *= 31.0/25.0;
			name += "B";
		}
		sideLength *= 0.9;
		BufferedImage rot = new BufferedImage((int) (sideLength * Math.sqrt(2)), (int) (sideLength * Math.sqrt(2)), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) rot.getGraphics();
		g.translate(rot.getWidth()/2, rot.getHeight()/2);
		g.rotate(theta);
		BufferedImage pic =  SolarWarfare4_2.il.findImage(name).getFit((int) sideLength, (int) sideLength);
		g.drawImage(pic, -pic.getWidth()/2, -pic.getHeight()/2, null);
		return rot;
	}
	public ArrayList<ButtonAction> getNewActs(General viewing, ArrayList<Resource> avail){
		Ship buffer = null;
		for(Ship s: getLocation().getFleet()){
			if(s.canBuff() && s.getOwner() == viewing){
				buffer = s;
			}
		}
		ArrayList<ButtonAction> alba = new ArrayList<ButtonAction>();
		if(getA() > 0){
			ShipBuff sb = new ShipBuff(buffer, this, viewing);
			if(sb.legalChange()){
				alba.add(sb);
			}
			SummonMissile sm = new SummonMissile(this, viewing, avail);
			if(sm.legalChange()){
				alba.add(sm);
			}
			EquipMissile em = new EquipMissile(this, viewing);
			if(em.legalChange()){
				alba.add(em);
			}
			
		}
		ConquerPlanet cp = new ConquerPlanet(viewing, this);
		if(cp.legalChange()){
			alba.add(cp);
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
	public int maxMove(){
		return getM() - amountMoved;
	}
	public void changeAmountMoved(int delta){
		amountMoved += delta;
	}
	public int getAmountMoved() {
		return amountMoved;
	}
	public void setAmountMoved(int amountMoved) {
		this.amountMoved = amountMoved;
	}
	public int getBuffStatus(){
		return buffStatus;
	}
	public void setBuffStatus(int bs){
		buffStatus = bs;
	}
	public void donateBuff(){
		if(buffStatus == 1){
			buffStatus = 2;
		}
	}
	public void undonateBuff(){
		if(buffStatus == 2){
			buffStatus = 1;
		}
	}
	public boolean canBuff(){
		return buffStatus == 1;
	}
	public boolean isBuffed(){
		return buffed;
	}
	public void setBuffed(boolean buffed){
		this.buffed = buffed;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean isAttackedYet() {
		return attackedYet;
	}
	public void setAttackedYet(boolean attackedYet) {
		this.attackedYet = attackedYet;
	}
	public Space getLocation(){
		return location;
	}
	public void setLocation(Space s){
		location = s;
	}
	public int getType() {
		return type;
	}
	public boolean isMissileHeld(){
		return heldMissile;
	}
	public boolean isMissileActive(){
		return activeMissile;
	}
	public void setHeldMissile(boolean missile){
		heldMissile = missile;
	}
	public void setActiveMissile(boolean missile){
		activeMissile = missile;
	}
	public int getM() {
		return m;
	}
	public int getA() {
		return a;
	}
	public int getD() {
		return d;
	}
	public double getTheta(){
		return theta;
	}
	public void setTheta(double theta){
		this.theta = theta;
	}
	public General getOwner() {
		return owner;
	}
	public class ConquerPlanet extends ButtonAction{
		private General viewer;
		private Ship bomber;
		private boolean legal;
		private int forwardOpinion;
		private int backwardOpinion;
		private General previousOwner;
		public ConquerPlanet(General v, Ship source) {
			super();
			viewer = v;
			bomber = source;
			legal = true;
			if(viewer == null){
				legal = false;
				return;
			}
			if(!viewer.isActive()){
				legal = false;
				return;
			}
			if(bomber == null){
				legal = false;
				return;
			}
			if(source.getOwner() != v){
				legal = false;
				return;
			}
			previousOwner = source.getLocation().getOwner();
			if(previousOwner == source.getOwner()){
				legal = false;
				return;
			}
			if(source.getLocation().getIdentifier() != 1){
				legal = false;
				return;
			}
			Planet p = (Planet) source.getLocation();
			if(p.getStructures().size() > 0 && p.getOwner() != null){
				legal = false;
				return;
			}
			if(previousOwner != null){
				forwardOpinion = bomber.getOwner().lookupRelation(previousOwner);
				backwardOpinion = previousOwner.lookupRelation(bomber.getOwner());
			}
			
		}
		public void makeChange() {
			bomber.getLocation().setOwner(bomber.getOwner());
			if(previousOwner != null){
				bomber.getOwner().setRelation(previousOwner, -1);
				previousOwner.setRelation(bomber.getOwner(), -1);
			}
			for(Space s: Map.getAllWithin(2, bomber.getLocation())){
				s.calculateClaims();
			}
		}
		public void undoChange() {
			bomber.getLocation().setOwner(previousOwner);
			if(previousOwner != null){
				bomber.getOwner().setRelation(previousOwner, forwardOpinion);
				previousOwner.setRelation(bomber.getOwner(), backwardOpinion);
			}
			for(Space s: Map.getAllWithin(2, bomber.getLocation())){
				s.calculateClaims();
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Conquer Planet";
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			BufferedImage missile = SolarWarfare4_2.il.findImage("Conquer").getFit(aw, ah);
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
			return null;
		}
	
	}
	public class EquipMissile extends ButtonAction{
		private Ship place;
		private boolean legal;
		public EquipMissile(Ship b1, General v) {
			super();
			place = b1;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			if(place == null){
				legal = false;
				return;
			}
			if(place.isMissileActive()){
				legal = false;
				return;
			}
			if(!place.isMissileHeld()){
				legal = false;
				return;
			}
			if(place.getOwner() != v){
				legal = false;
				return;
			}
			if(place.getA() < 1){
				legal = false;
				return;
			}
		}
		public void makeChange() {
			place.setHeldMissile(false);
			place.setActiveMissile(true);
		}
		public void undoChange() {
			place.setHeldMissile(true);
			place.setActiveMissile(false);
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Prepare Missile for Detonation";
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			BufferedImage missile = SolarWarfare4_2.il.findImage("Bomb").getFit(aw, ah);
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
			return null;
		}
	}
	public class SummonMissile extends ButtonAction{
		private Ship place;
		private boolean legal;
		private ArrayList<Resource> willSpend;
		public SummonMissile(Ship b1, General v, ArrayList<Resource> avail) {
			super();
			place = b1;
			legal = true;
			if(v == null){
				legal = false;
				return;
			}
			if(!v.isActive()){
				legal = false;
				return;
			}
			if(place == null){
				legal = false;
				return;
			}
			if(place.isMissileHeld()){
				legal = false;
				return;
			}
			if(place.getOwner() != v){
				legal = false;
				return;
			}
			if(place.getA() < 1){
				legal = false;
				return;
			}
			int[] amounts = Resource.toIndexedArray(avail);
			for(int i = 0; i < Costs.equipCost.length; i++){
				if(Costs.equipCost[i] > amounts[i]){
					legal = false;
					return;
				}
			}
			willSpend = Map.getClosestAvailableResources(location, v, Costs.equipCost);
		}
		public void makeChange() {
			place.setHeldMissile(true);
			for(Resource r: willSpend){
				r.getLocation().removeResource(r);
			}
		}
		public void undoChange() {
			place.setHeldMissile(false);
			for(Resource r: willSpend){
				r.getLocation().addResource(r);
			}
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Load Missile";
		}
		@Override
		public ArrayList<String> getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public BufferedImage getImage(int aw, int ah, int mx, int my) {
			BufferedImage missile = SolarWarfare4_2.il.findImage("Missile").getFit(aw/2, ah/2);
			return missile;
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
	public class ShipBuff extends ButtonAction{
		private Ship buffer;
		private Ship buffed;
		private boolean legal;
		public ShipBuff(Ship b1, Ship b2, General viewing) {
			super();
			buffer = b1;
			buffed = b2;
			legal = true;
			if(buffer == null){
				legal = false;
				return;
			}
			if(viewing == null){
				legal = false;
				return;
			}
			if(!viewing.isActive()){
				legal = false;
				return;
			}
			if(!buffer.canBuff()){
				legal = false;
				return;
			}
			if(buffed.isBuffed()){
				legal = false;
				return;
			}
			if(buffer.getOwner() != viewing){
				legal = false;
				return;
			}
			if(buffer.getOwner() != buffed.getOwner()){
				legal = false;
				return;
			}
			if(buffed.getA() < 1){
				legal = false;
				return;
			}
		}
		public void makeChange() {
			buffer.donateBuff();
			buffed.setBuffed(true);
		}
		public void undoChange() {
			buffed.setBuffed(false);
			buffer.undonateBuff();
		}
		public boolean legalChange() {
			return legal;
		}
		public String getTooltipText(){
			return "Empower Ship";
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