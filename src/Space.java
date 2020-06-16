import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Space extends Index{
	private int x,y;
	private Space L,R,UL,UR,DL,DR;
	private ArrayList<General> claims;
	private boolean isTrade;
	private ArrayList<Ship> fleet;
	private ArrayList<Resource> pile;
	private boolean examined;
	public Space(int x, int y, boolean block){
		this.x = x;
		this.y = y;
		this.isTrade = block;
		claims = new ArrayList<General>();
		fleet = new ArrayList<Ship>();
		pile = new ArrayList<Resource>();
		examined = false;
	}
	public Space(ArrayList<General> players, String[] desc){
		x = Integer.parseInt(desc[0]);
		y = Integer.parseInt(desc[1]);
		int tradeValue = Integer.parseInt(desc[2]);
		isTrade = (tradeValue == 1 || tradeValue == 2);
		claims = new ArrayList<General>();
		fleet = new ArrayList<Ship>();
		pile = new ArrayList<Resource>();
		examined = false;
	}
	/*
	 * <x-coord>!<y-coord>!<identifier>
	 * identifier is zero for a basic space, 1 for a trade block, 2 for a planet.
	 */
	public String topologicalDescription(){
		int tradeValue = 0;
		if(isTrade){
			tradeValue = 1;
		}
		return x + "!" + y + "!" + tradeValue;
	}
	public boolean interestingSpace(){
		if(isTrade){
			return true;
		}
		if(fleet.size() > 0){
			return true;
		}
		if(pile.size() > 0){
			return true;
		}
		return false;
	}
	public Ship getTouchedShip(double centerX, double centerY, double sideLength, double mouseX, double mouseY){
		int boxLength = (int) (Math.sqrt(fleet.size()) + 0.99);
		double length = boxMultiplier() * sideLength;
		double mL = (length*2)/boxLength;
		double xC = centerX-length;
		double yC = centerY-length;
		int s = fleet.size();
		if(s == 2){
			if(xC+(1)*mL-mL/2 <= mouseX && mouseX <= xC+(1)*mL+mL/2){
				if(yC+(0.5)*mL-mL/2 <= mouseY && mouseY <= yC+(0.5)*mL+mL/2){
					return fleet.get(0);
				}
			}
			if(xC+(1)*mL-mL/2 <= mouseX && mouseX <= xC+(1)*mL+mL/2){
				if(yC+(1.5)*mL-mL/2 <= mouseY && mouseY <= yC+(1.5)*mL+mL/2){
					return fleet.get(1);
				}
			}
		}else if(s == 3){
			if(xC+(0.5)*mL-mL/2 <= mouseX && mouseX <= xC+(0.5)*mL+mL/2){
				if(yC+(0.5)*mL-mL/2 <= mouseY && mouseY <= yC+(0.5)*mL+mL/2){
					return fleet.get(0);
				}
			}
			if(xC+(1.5)*mL-mL/2 <= mouseX && mouseX <= xC+(1.5)*mL+mL/2){
				if(yC+(0.5)*mL-mL/2 <= mouseY && mouseY <= yC+(0.5)*mL+mL/2){
					return fleet.get(1);
				}
			}
			if(xC+(1)*mL-mL/2 <= mouseX && mouseX <= xC+(1)*mL+mL/2){
				if(yC+(1.5)*mL-mL/2 <= mouseY && mouseY <= yC+(1.5)*mL+mL/2){
					return fleet.get(2);
				}
			}
		}else if(s == 5){
			if(xC+(0.5)*mL-mL/2 <= mouseX && mouseX <= xC+(0.5)*mL+mL/2){
				if(yC+(0.5)*mL-mL/2 <= mouseY && mouseY <= yC+(0.5)*mL+mL/2){
					return fleet.get(0);
				}
			}
			if(xC+(2.5)*mL-mL/2 <= mouseX && mouseX <= xC+(2.5)*mL+mL/2){
				if(yC+(0.5)*mL-mL/2 <= mouseY && mouseY <= yC+(0.5)*mL+mL/2){
					return fleet.get(1);
				}
			}
			if(xC+(1.5)*mL-mL/2 <= mouseX && mouseX <= xC+(1.5)*mL+mL/2){
				if(yC+(1.5)*mL-mL/2 <= mouseY && mouseY <= yC+(1.5)*mL+mL/2){
					return fleet.get(2);
				}
			}
			if(xC+(0.5)*mL-mL/2 <= mouseX && mouseX <= xC+(0.5)*mL+mL/2){
				if(yC+(2.5)*mL-mL/2 <= mouseY && mouseY <= yC+(2.5)*mL+mL/2){
					return fleet.get(3);
				}
			}
			if(xC+(2.5)*mL-mL/2 <= mouseX && mouseX <= xC+(2.5)*mL+mL/2){
				if(yC+(2.5)*mL-mL/2 <= mouseY && mouseY <= yC+(2.5)*mL+mL/2){
					return fleet.get(4);
				}
			}
		}else if(s == 6){
			if(xC+(0.8)*mL-mL/2 <= mouseX && mouseX <= xC+(0.8)*mL+mL/2){
				double yCoord = (1.0 * mouseY - yC + mL/2)/mL - 0.5;
				if(yCoord > 0){
					int index = (int) yCoord;
					if(index >= 0 && index < 3){
						return fleet.get(index);
					}
				}
			}
			if(xC+(2.2)*mL-mL/2 <= mouseX && mouseX <= xC+(2.2)*mL+mL/2){
				double yCoord = (1.0 * mouseY - yC + mL/2)/mL - 0.5;
				if(yCoord > 0){
					int index = (int) yCoord;
					if(index >= 0 && index < 3){
						return fleet.get(index + 3);
					}
				}
			}
		}else if(s == 7){
			if(xC+(0.5)*mL-mL/2 <= mouseX && mouseX <= xC+(0.5)*mL+mL/2){
				double yCoord = (1.0 * mouseY - yC + mL/2)/mL - 0.5;
				if(yCoord > 0){
					int index = (int) yCoord;
					if(index >= 0 && index < 3){
						return fleet.get(index);
					}
				}
			}
			if(xC+(1.5)*mL-mL/2 <= mouseX && mouseX <= xC+(1.5)*mL+mL/2){
				if(yC+(1.5)*mL-mL/2 <= mouseY && mouseY <= yC+(1.5)*mL+mL/2){
					return fleet.get(3);
				}
			}
			if(xC+(2.5)*mL-mL/2 <= mouseX && mouseX <= xC+(2.5)*mL+mL/2){
				double yCoord = (1.0 * mouseY - yC + mL/2)/mL - 0.5;
				if(yCoord > 0){
					int index = (int) yCoord;
					if(index >= 0 && index < 3){
						return fleet.get(index+4);
					}
				}
			}
		}else if(s == 8){
			if(yC+(0.5)*mL-mL/2 <= mouseY && mouseY <= yC+(0.5)*mL+mL/2){
				double xCoord = (1.0 * mouseX - xC + mL/2)/mL - 0.5;
				if(xCoord > 0){
					int index = (int) xCoord;
					if(index >= 0 && index < 3){
						return fleet.get(index);
					}
				}
			}
			if(yC+(2.5)*mL-mL/2 <= mouseY && mouseY <= yC+(2.5)*mL+mL/2){
				double xCoord = (1.0 * mouseX - xC + mL/2)/mL - 0.5;
				if(xCoord > 0){
					int index = (int) xCoord;
					if(index >= 0 && index < 3){
						return fleet.get(index+5);
					}
				}
			}
			if(xC+(0.9)*mL-mL/2 <= mouseX && mouseX <= xC+(0.9)*mL+mL/2){
				if(yC+(1.5)*mL-mL/2 <= mouseY && mouseY <= yC+(1.5)*mL+mL/2){
					return fleet.get(3);
				}
			}
			if(xC+(2.1)*mL-mL/2 <= mouseX && mouseX <= xC+(2.1)*mL+mL/2){
				if(yC+(1.5)*mL-mL/2 <= mouseY && mouseY <= yC+(1.5)*mL+mL/2){
					return fleet.get(4);
				}
			}
		}else{
			double xCoord = (1.0 * mouseX - xC + mL/2)/mL - 0.5;
			int index = (int) xCoord;
			double yCoord = (1.0 * mouseY - yC + mL/2)/mL - 0.5;
			int indey = (int) yCoord;
			if(xCoord > 0 && yCoord > 0){
				if(indey >= 0 && indey < boxLength){
					if(index >= 0 && index < boxLength){
						int realIndex = index + indey * boxLength;
						if(realIndex < fleet.size()){
							return fleet.get(realIndex);
						}
					}
				}
			}
		}
		return null;
	}
	public void drawLayerOne(double centerX, double centerY, double sideLength, Graphics g, General v){
		drawOutline(centerX, centerY, sideLength, g, v);
		drawBorder(centerX, centerY, sideLength, g, v);
	}
	public void drawLayerTwo(double centerX, double centerY, double sideLength, Graphics g, General v){
		if(isTrade()){
			drawTradeRoutes(centerX, centerY, sideLength, g, v);
		}
	}
	public void drawLayerThree(double centerX, double centerY, double sideLength, Graphics g, General v){
	}
	public void drawLayerFour(double centerX, double centerY, double sideLength, Graphics g, General v){
		drawShips(centerX, centerY, sideLength, g, v);
	}
	private void drawShips(double centerX, double centerY, double sideLength, Graphics g, General v){
		int boxLength = (int) (Math.sqrt(fleet.size()) + 0.99);
		double length = boxMultiplier() * sideLength;
		double mL = (length*2)/boxLength;
		double xC = centerX-length;
		double yC = centerY-length;
		ArrayList<Ship> fleet = getFleet();
		int s = fleet.size();
		if(s == 2){
			BufferedImage p0 = fleet.get(0).getImage(mL, v, true);
			g.drawImage(p0, (int) (xC+(1)*mL-p0.getWidth()/2), (int) (yC+(0.5)*mL-p0.getHeight()/2), null);
			BufferedImage p1 = fleet.get(1).getImage(mL, v, true);
			g.drawImage(p1, (int) (xC+(1)*mL-p1.getWidth()/2), (int) (yC+(1.5)*mL-p1.getHeight()/2), null);
		}else if(s == 3){
			BufferedImage p0 = fleet.get(0).getImage(mL, v, true);
			g.drawImage(p0, (int) (xC+(0.5)*mL-p0.getWidth()/2), (int) (yC+(0.5)*mL-p0.getHeight()/2), null);
			BufferedImage p1 = fleet.get(1).getImage(mL, v, true);
			g.drawImage(p1, (int) (xC+(1.5)*mL-p1.getWidth()/2), (int) (yC+(0.5)*mL-p1.getHeight()/2), null);
			BufferedImage p2 = fleet.get(2).getImage(mL, v, true);
			g.drawImage(p2, (int) (xC+(1)*mL-p2.getWidth()/2), (int) (yC+(1.5)*mL-p2.getHeight()/2), null);
		}else if(s == 5){
			BufferedImage p0 = fleet.get(0).getImage(mL, v, true);
			g.drawImage(p0, (int) (xC+(0.5)*mL-p0.getWidth()/2), (int) (yC+(0.5)*mL-p0.getHeight()/2), null);
			BufferedImage p1 = fleet.get(1).getImage(mL, v, true);
			g.drawImage(p1, (int) (xC+(2.5)*mL-p1.getWidth()/2), (int) (yC+(0.5)*mL-p1.getHeight()/2), null);
			BufferedImage p2 = fleet.get(2).getImage(mL, v, true);
			g.drawImage(p2, (int) (xC+(1.5)*mL-p2.getWidth()/2), (int) (yC+(1.5)*mL-p2.getHeight()/2), null);
			BufferedImage p3 = fleet.get(3).getImage(mL, v, true);
			g.drawImage(p3, (int) (xC+(0.5)*mL-p3.getWidth()/2), (int) (yC+(2.5)*mL-p3.getHeight()/2), null);
			BufferedImage p4 = fleet.get(4).getImage(mL, v, true);
			g.drawImage(p4, (int) (xC+(2.5)*mL-p4.getWidth()/2), (int) (yC+(2.5)*mL-p4.getHeight()/2), null);
		}else if(s == 6){
			for(int i = 0; i < 3; i++){
				BufferedImage p0 = fleet.get(i).getImage(mL, v, true);
				g.drawImage(p0, (int) (xC+(0.8)*mL-p0.getWidth()/2), (int) (yC+(i+0.5)*mL-p0.getHeight()/2), null);
			}
			for(int i = 0; i < 3; i++){
				BufferedImage p0 = fleet.get(i+3).getImage(mL, v, true);
				g.drawImage(p0, (int) (xC+(2.2)*mL-p0.getWidth()/2), (int) (yC+(i+0.5)*mL-p0.getHeight()/2), null);
			}
		}else if(s == 7){
			for(int i = 0; i < 3; i++){
				BufferedImage p0 = fleet.get(i).getImage(mL, v, true);
				g.drawImage(p0, (int) (xC+(0.5)*mL-p0.getWidth()/2), (int) (yC+(i+0.5)*mL-p0.getHeight()/2), null);
			}
			BufferedImage p1 = fleet.get(3).getImage(mL, v, true);
			g.drawImage(p1, (int) (xC+(1.5)*mL-p1.getWidth()/2), (int) (yC+(1.5)*mL-p1.getHeight()/2), null);
			for(int i = 0; i < 3; i++){
				BufferedImage p0 = fleet.get(i+4).getImage(mL, v, true);
				g.drawImage(p0, (int) (xC+(2.5)*mL-p0.getWidth()/2), (int) (yC+(i+0.5)*mL-p0.getHeight()/2), null);
			}
		}else if(s == 8){
			for(int i = 0; i < 3; i++){
				BufferedImage p0 = fleet.get(i).getImage(mL, v, true);
				g.drawImage(p0, (int) (xC+(i+0.5)*mL-p0.getWidth()/2), (int) (yC+(0.5)*mL-p0.getHeight()/2), null);
			}
			BufferedImage p1 = fleet.get(3).getImage(mL, v, true);
			g.drawImage(p1, (int) (xC+(0.9)*mL-p1.getWidth()/2), (int) (yC+(1.5)*mL-p1.getHeight()/2), null);
			BufferedImage p2 = fleet.get(4).getImage(mL, v, true);
			g.drawImage(p2, (int) (xC+(2.1)*mL-p2.getWidth()/2), (int) (yC+(1.5)*mL-p2.getHeight()/2), null);
			for(int i = 0; i < 3; i++){
				BufferedImage p0 = fleet.get(i+5).getImage(mL, v, true);
				g.drawImage(p0, (int) (xC+(i+0.5)*mL-p0.getWidth()/2), (int) (yC+(2.5)*mL-p0.getHeight()/2), null);
			}
		}else{
			for(int i = 0; i < fleet.size(); i++){
				int y = i/boxLength;
				int x = i%boxLength;
				BufferedImage paint = fleet.get(i).getImage(mL, v, true);
				g.drawImage(paint, (int) (xC+(x+0.5)*mL-paint.getWidth()/2), (int) (yC+(y+0.5)*mL-paint.getHeight()/2), null);
			}
		}
	}
	private double boxMultiplier(){
		return Math.sqrt(3)/2 - (Math.sqrt(3) - 1)/(2*Math.sqrt(3)/3 + 2);
	}
	private void drawOutline(double centerX, double centerY, double sideLength, Graphics g, General v){
		g.setColor(new Color(190,190,190));
		g.drawPolygon(getHexagon(centerX, centerY, sideLength));
	}
	private void drawTradeRoutes(double centerX, double centerY, double sideLength, Graphics g, General v){
		int thickness = (int) (sideLength * 0.1);
		g.setColor(new Color(0, 60, 20));
		g.fillOval((int) (centerX - thickness/2), (int) (centerY - thickness/2), thickness, thickness);
		if(getR().isTrade()){
			drawBar(centerX, centerY, centerX + sideLength * Math.sqrt(3)/2, centerY, thickness, g);
		}
		if(getL().isTrade()){
			drawBar(centerX, centerY, centerX - sideLength * Math.sqrt(3)/2, centerY, thickness, g);
		}
		if(getUL().isTrade()){
			drawBar(centerX, centerY, centerX - sideLength * Math.sqrt(3)/4, centerY + sideLength * 3/4, thickness, g);
		}
		if(getDR().isTrade()){
			drawBar(centerX, centerY, centerX + sideLength * Math.sqrt(3)/4, centerY - sideLength * 3/4, thickness, g);
		}
		if(getUR().isTrade()){
			drawBar(centerX, centerY, centerX + sideLength * Math.sqrt(3)/4, centerY + sideLength * 3/4, thickness, g);
		}
		if(getDL().isTrade()){
			drawBar(centerX, centerY, centerX - sideLength * Math.sqrt(3)/4, centerY - sideLength * 3/4, thickness, g);
		}
	}
	private void drawBorder(double centerX, double centerY, double sideLength, Graphics g, General v){
		Point[] ppp = calculateVertices(centerX, centerY, sideLength);
		int[] xs = new int[ppp.length];
		int[] ys = new int[ppp.length];
		int size = ppp.length;
		for (int i = 0; i < size; i++) {
			xs[i] = ppp[i].x;
			ys[i] = ppp[i].y;
		}
		int thickness = 3;
		//upRight
		Space ru = getUR();
		Color ruc = Space.borderColor(this, ru);
		if(ruc != null){
			g.setColor(ruc);
			drawRoundBar(ppp[0], ppp[1], thickness, g);
		}
		//upLeft
		Space lu = getUL();
		Color luc = Space.borderColor(this, lu);
		if(luc != null){
			g.setColor(luc);
			drawRoundBar(ppp[1], ppp[2], thickness, g);
		}
		//left
		Space l = getL();
		Color lc = Space.borderColor(this, l);
		if(lc != null){
			g.setColor(lc);
			drawRoundBar(ppp[2], ppp[3], thickness, g);
		}
		//downLeft
		Space ld = getDL();
		Color ldc = Space.borderColor(this, ld);
		if(ldc != null){
			g.setColor(ldc);
			drawRoundBar(ppp[3], ppp[4], thickness, g);
		}
		//downRight
		Space rd = getDR();
		Color rdc = Space.borderColor(this, rd);
		if(rdc != null){
			g.setColor(rdc);
			drawRoundBar(ppp[4], ppp[5], thickness, g);
		}
		//right
		Space r = getR();
		Color rc = Space.borderColor(this, r);
		if(rc != null){
			g.setColor(rc);
			drawRoundBar(ppp[5], ppp[0], thickness, g);
		}
	}
	public void drawSelectionOutline(double centerX, double centerY, double sideLength, Graphics g, General v, ArrayList<Space> within, boolean ally){
		if(ally){
			g.setColor(new Color(100,190,255));
		}else{
			g.setColor(new Color(210,150,190));
		}
		g.drawPolygon(getHexagon(centerX, centerY, sideLength));
	}
	public void drawSelectionBorder(double centerX, double centerY, double sideLength, Graphics g, General v, ArrayList<Space> within, boolean ally){
		Point[] ppp = calculateVertices(centerX, centerY, sideLength);
		int[] xs = new int[ppp.length];
		int[] ys = new int[ppp.length];
		int size = ppp.length;
		for (int i = 0; i < size; i++) {
			xs[i] = ppp[i].x;
			ys[i] = ppp[i].y;
		}
		int thickness = 3;
		//upRight
		Space ru = getUR();
		Color ruc = selectionColor(ru, within, v, ally);
		if(ruc != null){
			g.setColor(ruc);
			drawRoundBar(ppp[0], ppp[1], thickness, g);
		}
		//upLeft
		Space lu = getUL();
		Color luc = selectionColor(lu, within, v, ally);
		if(luc != null){
			g.setColor(luc);
			drawRoundBar(ppp[1], ppp[2], thickness, g);
		}
		//left
		Space l = getL();
		Color lc = selectionColor(l, within, v, ally);
		if(lc != null){
			g.setColor(lc);
			drawRoundBar(ppp[2], ppp[3], thickness, g);
		}
		//downLeft
		Space ld = getDL();
		Color ldc = selectionColor(ld, within, v, ally);
		if(ldc != null){
			g.setColor(ldc);
			drawRoundBar(ppp[3], ppp[4], thickness, g);
		}
		//downRight
		Space rd = getDR();
		Color rdc = selectionColor(rd, within, v, ally);
		if(rdc != null){
			g.setColor(rdc);
			drawRoundBar(ppp[4], ppp[5], thickness, g);
		}
		//right
		Space r = getR();
		Color rc = selectionColor(r, within, v, ally);
		if(rc != null){
			g.setColor(rc);
			drawRoundBar(ppp[5], ppp[0], thickness, g);
		}
	}
	public boolean foreignShips(General v){
		for(Ship s: fleet){
			if(s.getOwner() != v){
				return true;
			}
		}
		return false;
	}
	public Color selectionColor(Space adjacent, ArrayList<Space> list, General v, boolean ally){
		if(isExamined()){
			return new Color(0, 176, 240);
		}
		boolean adjacentEnemies = false;
		if(list.contains(adjacent)){
			for(Ship s: adjacent.getFleet()){
				if(s.getOwner() != v){
					adjacentEnemies = true;
					break;
				}
			}
		}
		for(Ship s: fleet){
			if(s.getOwner() == v){
				
			}else if(adjacentEnemies){
				return new Color(60,60,60);
			}else if(v.lookupRelation(s.getOwner()) > 0){
				return new Color(0, 126, 30);
			}else if(v.lookupRelation(s.getOwner()) == 0){
				return new Color(160, 112, 0);
			}else if(v.lookupRelation(s.getOwner()) < 0){
				return new Color(184, 0, 52);
			}
		}
		if(!list.contains(adjacent)){
			if(ally){
				return new Color(0, 90, 190);
			}else{
				return new Color(110, 0, 25);
			}
		}
		return null;
	}
	public void drawRoundBar(double sX, double sY, double eX, double eY, double thickness, Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke((float) thickness));
		g2.draw(new Line2D.Double(sX, sY, eX, eY));
	}
	public void drawRoundBar(Point s, Point e, double thickness, Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		Stroke str = g2.getStroke();
		g2.setStroke(new BasicStroke((float) thickness));
		g2.draw(new Line2D.Double(s.getX(), s.getY(), e.getX(), e.getY()));
		g2.setStroke(str);
	}
	public void drawBar(double sX, double sY, double eX, double eY, double thickness, Graphics g){
		double slope = (eY - sY)/(eX - sX);
		double otherSlope = -1/slope;
		double theta = Math.atan(otherSlope);
		double dX = thickness/2 * Math.cos(theta);
		double dY = thickness/2 * Math.sin(theta);
		int[] xs = new int[]{(int) (sX + dX + 0.5), (int) (eX + dX + 0.5), (int) (eX - dX + 0.5), (int) (sX - dX + 0.5)};
		int[] ys = new int[]{(int) (sY + dY + 0.5), (int) (eY + dY + 0.5), (int) (eY - dY + 0.5), (int) (sY - dY + 0.5)};
		int n = 4;
		g.fillPolygon(xs, ys, n);
	}
	public Polygon getHexagon(double centerX, double centerY, double sideLength){
		Point[] ppp = calculateVertices(centerX, centerY, sideLength);
		int[] xs = new int[ppp.length];
		int[] ys = new int[ppp.length];
		int size = ppp.length;
		for (int i = 0; i < size; i++) {
			xs[i] = ppp[i].x;
			ys[i] = ppp[i].y;
		}
		return new Polygon(xs, ys, size);
	}
	public Point[] calculateVertices(double centerX, double centerY, double sideLength) {
		Point[] points = new Point[6];
		for (int i = 0; i < 6; i++) {
			int theta = i * 60 + 30;
			double xOne = Math.cos(Math.toRadians(theta)) * sideLength;
			double yOne = Math.sin(Math.toRadians(theta)) * sideLength;
			Point p = new Point((int) (xOne + centerX), (int) (yOne + centerY));
			points[i] = p;
		}
		return points;
	}
	public Space[] getFullSide(Space s){
		if(s == L){
			return new Space[]{UL, DL};
		}else if(s == R){
			return new Space[]{UR, DR};
		}else if(s == UL){
			return new Space[]{L, UR};
		}else if(s == DR){
			return new Space[]{DL, R};
		}else if(s == DL){
			return new Space[]{DR, L};
		}else if(s == UR){
			return new Space[]{UL, UR};
		}else{
			return null;
		}
	}
	public Space[] getTriangle(Space s){
		if(s == L){
			return new Space[]{UR, DR};
		}else if(s == R){
			return new Space[]{UL, DL};
		}else if(s == UL){
			return new Space[]{DL, R};
		}else if(s == DR){
			return new Space[]{UR, L};
		}else if(s == DL){
			return new Space[]{UL, R};
		}else if(s == UR){
			return new Space[]{DR, L};
		}else{
			return null;
		}
	}
	public Space getOpposite(Space s){
		if(s == L){
			return R;
		}else if(s == R){
			return L;
		}else if(s == UL){
			return DR;
		}else if(s == DR){
			return UL;
		}else if(s == DL){
			return UR;
		}else if(s == UR){
			return DL;
		}else{
			return null;
		}
	}
	public ArrayList<Space> getAdjacent(){
		ArrayList<Space> adjacent = new ArrayList<Space>();
		if(L != null){
			adjacent.add(L);
		}
		if(UL != null){
			adjacent.add(UL);
		}
		if(UR != null){
			adjacent.add(UR);
		}
		if(R != null){
			adjacent.add(R);
		}
		if(DR != null){
			adjacent.add(DR);
		}
		if(DL != null){
			adjacent.add(DL);
		}
		return adjacent;
	}
	public ArrayList<Space> getTradeAdjacent(General g){
		ArrayList<Space> adjacent = new ArrayList<Space>();
		if(isTrade() && wouldTransferFor(g)){
			for(Space s: getAdjacent()){
				if(s.isTrade() && s.wouldTransferFor(g)){
					adjacent.add(s);
				}
			}
		}
		return adjacent;
	}
	public boolean wouldTransferFor(General g){
		if(getOwner() != null){
			if(getOwner().lookupRelation(g) <= 0){
				return false;
			}
		}else if(getIdentifier() == 1){
			return false;
		}
		for(Ship s: fleet){
			if(s.getOwner().lookupRelation(g) < 0){
				return false;
			}
		}
		return true;
	}
	public ArrayList<Ship> getFleet(){
		ArrayList<Ship> fleet = this.fleet;
		if(!EnemyShipsVisible()){
			ArrayList<Ship> myFleet = new ArrayList<Ship>();
			for(Ship s: fleet){
				if(s.getOwner().lookupRelation(GameManager.viewing) > 0){
					myFleet.add(s);
				}
			}
			fleet = myFleet;
		}
		
		return fleet;
	}
	public void refreshShips(General g){
		for(Ship s: fleet){
			if(s.getOwner() == g){
				s.refresh();
			}
		}
	}
	public void addShip(Ship s){
		fleet.add(s);
	}
	public void removeShip(Ship s){
		fleet.remove(s);
	}
	public boolean containsShip(Ship s){
		return fleet.contains(s);
	}
	public ArrayList<Resource> getPile(){
		return pile;
	}
	public void addResource(Resource r){
		pile.add(r);
	}
	public void emptyResources(General g){
		/*
		 * TODO: We've currently said that resources just hang around. As such, this method, which is called
		 * by the Map class in beginTurn, will no longer delete resources. That said, I may change how it works,
		 * and instead say that resources disappear after 3 turns, or 5 turns, or whatever - hence I'm leaving
		 * this method around, even if it won't actually *do* anything.
		 */
		/*
		for(int i = 0; i < pile.size(); i++){
			if(pile.get(i).getCreator() == g){
				pile.remove(i);
				i--;
			}
		}*/
	}
	public void emptyResources(){
		pile = new ArrayList<Resource>();
	}
	public void removeResource(Resource r){
		pile.remove(r);
	}
	public void removeResource(General r, int type){
		for(int i = 0; i < pile.size(); i++){
			if(pile.get(i).getCreator() == r && pile.get(i).getType() == type){
				pile.remove(i);
				return;
			}
		}
		System.out.println("THERE WAS A PROBLEM WE SAID WE'D REMOVE SOMETHING OF TYPE " + type);
		Toolbox.breakThings();
	}
	public void calculateClaims(){
		claims = new ArrayList<General>();
		if(getOwner() != null){
			claims.add(getOwner());
		}else{
			ArrayList<Space> nearby = Map.getAllWithin(2, this);
			for(Space s: nearby){
				if(s.getOwner() != null && !claims.contains(s.getOwner())){
					claims.add(s.getOwner());
				}
			}
		}
	}
	public ArrayList<General> getClaims(){
		return claims;
	}
	public void addClaim(General g){
		if(!claims.contains(g)){
			claims.add(g);
		}
	}
	public boolean containsClaim(General g){
		return claims.contains(g);
	}
	public void removeClaim(General g){
		claims.remove(g);
	}
	public Space getL() {
		return L;
	}
	public void setL(Space l) {
		L = l;
	}
	public Space getR() {
		return R;
	}
	public void setR(Space r) {
		R = r;
	}
	public Space getUL() {
		return UL;
	}
	public void setUL(Space uL) {
		UL = uL;
	}
	public Space getUR() {
		return UR;
	}
	public void setUR(Space uR) {
		UR = uR;
	}
	public Space getDL() {
		return DL;
	}
	public void setDL(Space dL) {
		DL = dL;
	}
	public Space getDR() {
		return DR;
	}
	public void setDR(Space dR) {
		DR = dR;
	}
	public boolean isExamined(){
		return examined;
	}
	public void setExamined(boolean e){
		examined = e;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public boolean isTrade() {
		return isTrade;
	}
	public General getOwner(){
		return null;
	}
	public void setOwner(General owner) {}
	public int getIdentifier(){
		return 0;
	}
	public String getName(){
		return x + ", " + y;
	}
	public boolean EnemyShipsVisible(){
		if(GameManager.obscureEnemies){
			return false;
		}
		return true;
	}
	public static Color borderColor(Space one, Space two){
		switch(spaceRelation(one,two)){
		case 1: return new Color(40, 40, 40);
		case 2: return new Color(60, 10, 10);
		default: return null;
		}
	}
	public static int spaceRelation(Space one, Space two){
		/*
		 * 0: no border
		 * 1: border with neutral space
		 * 2: border with an ally
		 * 3: border with an enemy
		 */
		boolean identical = true;
		for(General g: one.getClaims()){
			if(!two.containsClaim(g)){
				identical = false;
			}
		}
		for(General g: two.getClaims()){
			if(!one.containsClaim(g)){
				identical = false;
			}
		}
		if(identical){
			return 0;
		}else if(one.getClaims().size() == 0 || two.getClaims().size() == 0){
			return 1;
		}else{
			return 2;
		}
	}
}