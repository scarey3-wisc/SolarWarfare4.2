import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DetailWindow{
	private GameManager.BrowseClick source;
	private ArrayList<Resource> allAvailable;
	private ArrayList<InfoBox> painting;
	private GameManager manager;
	private Structure site;
	private int mode;
	private int submode;
	private Space currentloc;
	private static boolean showProduction;
	//mode is at zero as a default, meaning that we're just examining specific spaces. A mode of 1,
	//however, will mean that we're managing global stuff.
	public DetailWindow(GameManager.BrowseClick s, GameManager g){
		source = s;
		manager = g;
		allAvailable = new ArrayList<Resource>();
		painting = new ArrayList<InfoBox>();
		mode = 0;
		submode = 0;
		currentloc = null;
	}
	public boolean within(double w, double h, int x, int y){
		return x > w - w/6.0 - h/80.0;
	}
	public void click(double h, General v, int x, int y, int clicktype){
		for(InfoBox ib: painting){
			if(ib.within(x, y)){
				ib.click(x, y, h, v, clicktype);
			}
		}
	}
	public BufferedImage generateTooltip(double h, General v, int x, int y){
		for(InfoBox ib: painting){
			if(ib.within(x, y)){
				return ib.getTooltip(x, y, h);
			}
		}
		return null;
	}
	public void paint(double w, double h, General v, Graphics g, int mouseX, int mouseY){
		BufferedImage background = null;
		if((source.location() == null || source.location().getIdentifier() == 0) && mode == 0){
			background = SolarWarfare4_2.il.findImage("Nebulae").getFit(-1, (int) (h));
		}else{
			background = SolarWarfare4_2.il.findImage("City").getFit(-1, (int) (h));
		}
		g.drawImage(background, (int) (w - w/6.0), 0, null);
		int tW = (int) (w/6 - h/80);
		int tH = (int) (h - h/40);
		int sX = (int) (w - w/6.0 + h/80);
		int sY = (int) (h/80);
		for(InfoBox ib: painting){
			ib.paint(tW, tH, sX, sY, source.location(), g, mouseX, mouseY);
			sX = ib.getXStart();
			sY = ib.getYStart() + ib.getHeight();
			tH -= ib.getHeight();
		}
		greyOutline((int) (w - w/6.0), 0, (int) (w/6.0 + h/40.0), (int)(h), (int) (h/40.0), g);
	}
	public void notifyOfChange(General v){
		if(source.location() != currentloc){
			mode = 0;
		}
		currentloc = source.location();
		if(mode == 0){
			allAvailable = new ArrayList<Resource>();
			if(source.location() != null && (source.location().getOwner() == v || source.location().getOwner() == null)){
				allAvailable = Map.getAllAvailableResources(source.location(), v);
			}
			painting = new ArrayList<InfoBox>();
			painting.add(new SwitchButton());
			if(source.location() != null){
				boolean planet = source.location().getIdentifier() == 1;
				if(planet){
					painting.add(new PODetails());
				}
				painting.add(new SpaceTitle());
				if(planet){
					Planet p = (Planet) source.location();
					for(Structure b: p.getStructures()){
						b.generateActs(v, allAvailable);
					}
					site = new Structure("Building Site", p);
					site.generateActs(v, allAvailable);
					painting.add(new Buildings());
				}
				for(Ship s: source.location().getFleet()){
					s.generateActs(v, allAvailable);
				}
				painting.add(new Ships());
				painting.add(new AvailableResources());
			}else if(showProduction){
				painting.add(new AvailableResources());
			}
		}else if(mode == 1){
			allAvailable = new ArrayList<Resource>();
			if(source.location() != null && (source.location().getOwner() == v || source.location().getOwner() == null)){
				allAvailable = Map.getAllAvailableResources(source.location(), v);
			}
			painting = new ArrayList<InfoBox>();
			painting.add(new SwitchButton());
			painting.add(new EmpireTitle());
			painting.add(new SubmodeTitle());
			if(submode == 0){
				painting.add(new Relation());
			}else if(submode == 1){
				painting.add(new Trading());
			}else if(submode == 2){
				painting.add(new Exchange());
			}
		}
		
	}
	
	public int getYStart(double w, double h){
		return (int) (h/80.0);
	}
	public int getXStart(double w, double h){
		return (int) (w-w/6.0 + h/80.0);
	}
	public int getBoxHeight(double w, double h){
		return (int) (h - h/40.0);
	}
	public int getBoxWidth(double w, double h){
		return (int) (w/6.0 - h/80.0);
	}
	public static void greyOutline(int xStart, int yStart, int width, int height, int t, Graphics g){
		//outerUp
		g.setColor(new Color(140, 140, 140));
		int[] xs = new int[]{xStart + t/2, xStart + width - t/2, xStart + width + t/2, xStart - t/2};
		int[] ys = new int[]{yStart + t/2, yStart + t/2, yStart - t/2, yStart - t/2};
		int n = 4;	
		g.fillPolygon(xs, ys, n);
		//innerUp
		g.setColor(new Color(65, 65, 65));
		xs = new int[]{xStart + t/2, xStart + width - t/2, xStart + width, xStart};
		ys = new int[]{yStart + t/2, yStart + t/2, yStart, yStart};
		g.fillPolygon(xs, ys, n);
		//outerRight
		g.setColor(new Color(77, 77, 77));
		xs = new int[]{xStart + width - t/2, xStart + width + t/2, xStart + width + t/2, xStart + width - t/2};
		ys = new int[]{yStart + t/2, yStart - t/2, yStart + height + t/2, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//innerRight
		g.setColor(new Color(111, 111, 111));
		xs = new int[]{xStart + width - t/2, xStart + width, xStart + width, xStart + width - t/2};
		ys = new int[]{yStart + t/2, yStart, yStart + height, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//outerBot
		g.setColor(new Color(65, 65, 65));
		xs = new int[]{xStart + width - t/2, xStart + width + t/2, xStart - t/2, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height + t/2, yStart + height + t/2, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//innerBot
		g.setColor(new Color(140, 140, 140));
		xs = new int[]{xStart + width - t/2, xStart + width, xStart, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height, yStart + height, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//outerLeft
		g.setColor(new Color(111, 111, 111));
		xs = new int[]{xStart + t/2, xStart - t/2, xStart - t/2, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height + t/2, yStart - t/2, yStart + t/2};
		g.fillPolygon(xs, ys, n);
		//innerLeft
		g.setColor(new Color(77, 77, 77));
		xs = new int[]{xStart + t/2, xStart, xStart, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height, yStart, yStart + t/2};
		g.fillPolygon(xs, ys, n);
	}
	public static void greyBox(int xStart, int yStart, int width, int height, int t, Graphics g){
		//outerUp
		g.setColor(new Color(140, 140, 140));
		int[] xs = new int[]{xStart + t/2, xStart + width - t/2, xStart + width + t/2, xStart - t/2};
		int[] ys = new int[]{yStart + t/2, yStart + t/2, yStart - t/2, yStart - t/2};
		int n = 4;	
		g.fillPolygon(xs, ys, n);
		//innerUp
		g.setColor(new Color(65, 65, 65));
		xs = new int[]{xStart + t/2, xStart + width - t/2, xStart + width, xStart};
		ys = new int[]{yStart + t/2, yStart + t/2, yStart, yStart};
		g.fillPolygon(xs, ys, n);
		//outerRight
		g.setColor(new Color(77, 77, 77));
		xs = new int[]{xStart + width - t/2, xStart + width + t/2, xStart + width + t/2, xStart + width - t/2};
		ys = new int[]{yStart + t/2, yStart - t/2, yStart + height + t/2, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//innerRight
		g.setColor(new Color(111, 111, 111));
		xs = new int[]{xStart + width - t/2, xStart + width, xStart + width, xStart + width - t/2};
		ys = new int[]{yStart + t/2, yStart, yStart + height, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//outerBot
		g.setColor(new Color(65, 65, 65));
		xs = new int[]{xStart + width - t/2, xStart + width + t/2, xStart - t/2, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height + t/2, yStart + height + t/2, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//innerBot
		g.setColor(new Color(140, 140, 140));
		xs = new int[]{xStart + width - t/2, xStart + width, xStart, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height, yStart + height, yStart + height - t/2};
		g.fillPolygon(xs, ys, n);
		//outerLeft
		g.setColor(new Color(111, 111, 111));
		xs = new int[]{xStart + t/2, xStart - t/2, xStart - t/2, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height + t/2, yStart - t/2, yStart + t/2};
		g.fillPolygon(xs, ys, n);
		//innerLeft
		g.setColor(new Color(77, 77, 77));
		xs = new int[]{xStart + t/2, xStart, xStart, xStart + t/2};
		ys = new int[]{yStart + height - t/2, yStart + height, yStart, yStart + t/2};
		g.fillPolygon(xs, ys, n);
		xs = new int[]{xStart, xStart + width, xStart + width, xStart};
		ys = new int[]{yStart, yStart, yStart + height, yStart + height};
		g.fillPolygon(xs, ys, n);
	}
	private static int[] bubbleText(String draw, double width, double height, double sX, double sY, Graphics g) {
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.setFont(g.getFont().deriveFont(TextPackage.idealFont(draw, (int) (width - height/3), (int) (height), 1, g)));
		height = g.getFontMetrics().getHeight();
		width = g.getFontMetrics().stringWidth(draw) + height/3;
		int xCoord = (int) (sX);
		int yCoord = (int) (sY);
		g.setColor(new Color(200, 200, 200));
		g.fillRoundRect(xCoord, yCoord, (int) width, (int) height, (int) (height/5), (int) (height/5));
		g.setColor(Color.black);
		g.drawString(draw, (int) (xCoord+height/6), (int) (yCoord + height/2 + height/4));
		return new int[]{(int)width, (int)height};
	}
	private abstract class InfoBox{
		public abstract int getWidth();
		public abstract int getHeight();
		public abstract int getXStart();
		public abstract int getYStart();
		public abstract boolean active();
		public abstract BufferedImage getTooltip(int x, int y, double h);
		public abstract void click(int x, int y, double h, General g, int clicktype);
		public boolean within(int x, int y){
			return (x>=getXStart()&&x<=getXStart()+getWidth())&&(y>=getYStart()&&y<=getYStart()+getHeight());
		}
		public abstract void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY);
	}
	private class Exchange extends ScrollableBox{

		private ArrayList<Planet> selectedPlanet;
		public Exchange(){
			contents = new ArrayList<Cell>();
			selectedPlanet = new ArrayList<Planet>();
			ArrayList<Planet> all = new ArrayList<Planet>();
			for(Planet p: manager.getWorld().getAllPlanets()){
				if(p.getOwner() == GameManager.viewing){
					all.add(p);
				}
			}
			ArrayList<General> list = new ArrayList<General>();
			list.addAll(manager.getPlayerlist());
			list.remove(GameManager.viewing);
			for(int i = 0; i < list.size() || i < all.size(); i++){
				General g = null;
				Planet p = null;
				if(i < list.size()){
					g = list.get(i);
				}
				if(i < all.size()){
					p = all.get(i);
				}
				contents.add(new ExchangeCell(g, p));
			}
		}
		private class ExchangeCell extends Cell{
			private boolean selected;
			private General dis;
			private Planet trad;
			private int height;
			public ExchangeCell(General d, Planet t){
				dis = d;
				trad = t;
				selected = false;
			}
			@Override
			public BufferedImage getTooltip(int x, int y, double h) {
				double index = 1.0 * x/height;
				if(index < 4.0){
					if(trad != null){
						if(selected){
							BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText("Unselect " + trad.getName(), imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}else{
							BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText("Select " + trad.getName(), imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}
					}
				}else if(index < 5.0){
					
				}else if(index < 6.0){
					if(dis != null){
						BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
						Graphics g = imago.getGraphics();
						int[] size = bubbleText("Give Selected Planets to " + dis.getFormalName(), imago.getWidth(), imago.getHeight(), 0, 0, g);
						return imago.getSubimage(0, 0, size[0], size[1]);
					}
				}
				return null;
			}

			@Override
			public void click(int x, int y, double h, General g, int clicktype) {
				double index = 1.0 * x/height;
				if(index < 4.0){
					if(selected && trad != null){
						selectedPlanet.remove(trad);
					}
					if(!selected && trad != null){
						selectedPlanet.add(trad);
					}
					selected = !selected;
				}else if(index < 5.0){
					
				}else if(index < 6.0){
					if(dis != null){
						ExchangeChange tr = new ExchangeChange(g, dis, selectedPlanet);
						manager.change(tr);
						notifyOfChange(g);
					}
				}
			}

			@Override
			public void paint(int w, int h, int x, int y, Space source, Graphics g, int mx, int my) {
				height = h;
				if(trad != null){
					int rh = (int) (h * 0.9);
					g.setColor(new Color(220, 220, 220));
					if(selected){
						g.setColor(new Color(255, 255, 255));
					}
					if(selected){
						g.fillRoundRect(x, y + h/2 - rh/2, (int) (h * 4.25), rh, h/5, h/5);
					}else{
						g.fillRoundRect(x, y + h/2 - rh/2, h * 4, rh, h/5, h/5);
					}
					BufferedImage image = SolarWarfare4_2.il.findImage(trad.getImageName()).getFit(rh, rh);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
					g.setColor(new Color(0, 0, 0));
					g.setFont(g.getFont().deriveFont(Font.BOLD));
					TextPackage tp = new TextPackage(trad.getName(), 5*h/2, rh/2, 1, g);
					g.setFont(g.getFont().deriveFont(tp.getFontValue()));
					int height = g.getFontMetrics().getHeight();
					for(int i = 0; i < tp.getResult().length; i++){
						String s = tp.getResult()[i];
						int width = g.getFontMetrics().stringWidth(s);
						g.drawString(s, x+h+3*h/2-width/2, y+h/2 + height/4 - height/2 * (tp.getResult().length/2 - i));
					}
				}
				
				if(dis != null){
					g.setColor(new Color(220, 220, 220));
					if(mx>x+5*h&&mx<x+6*h&&my>y&&my<y+h){
						g.setColor(new Color(255, 255, 255));
					}
					g.fillRect(x + h * 5, y, h, h);
					int rh = (int) (h * 0.9);
					BufferedImage image = dis.getInsignia(rh);
					g.drawImage(image, x + h * 5 + h/2 - image.getWidth()/2, y + h/2 - image.getHeight()/2, null);
				}
			}
		}
	}
	public static class ExchangeChange implements Change{
		private General source;
		private General target;
		private ArrayList<Planet> slct;
		public ExchangeChange(General s, General t, ArrayList<Planet> slt){
			source = s;
			target = t;
			slct = slt;
		}
		@Override
		public void makeChange() {
			for(Planet p: slct){
				p.setOwner(target);
			}
		}
		@Override
		public void undoChange() {
			for(Planet p: slct){
				p.setOwner(source);
			}
		}
		@Override
		public boolean legalChange() {
			return true;
		}
		public ExchangeChange(Map world, ArrayList<General> playerlist, String[] desc){
			source = playerlist.get(Integer.parseInt(desc[1]));
			target = playerlist.get(Integer.parseInt(desc[2]));
			slct = new ArrayList<Planet>();
			for(int i = 3; i < desc.length; i++){
				String[] coordName = desc[i].split(", ");
				int x = Integer.parseInt(coordName[0]);
				int y = Integer.parseInt(coordName[1]);
				Space s = world.getAt(x, y);
				if(s.getIdentifier() == 1){
					Planet p = (Planet) s;
					slct.add(p);
				}else{
					System.out.println("This is a big change - those coordinates should be a planet...");
				}
			}
		}
		@Override
		public ArrayList<String> getDescription() {
			ArrayList<String> alas = new ArrayList<String>();
			String form = "Exchange!" + source.getIndex() + "!" + target.getIndex();
			for(Planet p: slct){
				form += "!" + p.getX() + ", " + p.getY();
			}
			alas.add(form);
			return alas;
		}
	}
	private class Trading extends ScrollableBox{
		private ArrayList<Resource> selectedResources;
		public Trading(){
			ArrayList<Resource> all = new ArrayList<Resource>();
			ArrayList<Resource> alr0 = new ArrayList<Resource>();
			ArrayList<Resource> alr1 = new ArrayList<Resource>();
			ArrayList<Resource> alr2 = new ArrayList<Resource>();
			ArrayList<Resource> alr3 = new ArrayList<Resource>();
			ArrayList<Resource> alr4 = new ArrayList<Resource>();
			for(Planet p: manager.getWorld().getAllPlanets()){
				for(Resource r: p.getPile()){
					if(r.getOwner() == GameManager.viewing){
						if(r.getType() == 0){
							alr0.add(r);
						}else if(r.getType() == 1){
							alr1.add(r);
						}else if(r.getType() == 2){
							alr2.add(r);
						}else if(r.getType() == 3){
							alr3.add(r);
						}else if(r.getType() == 4){
							alr4.add(r);
						}
					}
				}
			}
			all.addAll(alr4);
			all.addAll(alr0);
			all.addAll(alr1);
			all.addAll(alr2);
			all.addAll(alr3);
			contents = new ArrayList<Cell>();
			selectedResources = new ArrayList<Resource>();
			ArrayList<General> list = new ArrayList<General>();
			list.addAll(manager.getPlayerlist());
			list.remove(GameManager.viewing);
			for(int i = 0; i < list.size() || i < all.size(); i++){
				General g = null;
				Resource r = null;
				if(i < list.size()){
					g = list.get(i);
				}
				if(i < all.size()){
					r = all.get(i);
				}
				contents.add(new TradingCell(g, r));
			}
		}
		private class TradingCell extends Cell{
			private boolean selected;
			private General dis;
			private Resource trad;
			private int height;
			public TradingCell(General d, Resource t){
				dis = d;
				trad = t;
				selected = false;
			}
			@Override
			public BufferedImage getTooltip(int x, int y, double h) {
				double index = 1.0 * x/height;
				if(index < 4.0){
					if(trad != null){
						if(selected){
							BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText("Unselect Resource", imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}else{
							BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText("Select Resource", imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}
					}
				}else if(index < 5.0){
					
				}else if(index < 6.0){
					if(dis != null){
						BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
						Graphics g = imago.getGraphics();
						int[] size = bubbleText("Send Selected Resources to " + dis.getFormalName(), imago.getWidth(), imago.getHeight(), 0, 0, g);
						return imago.getSubimage(0, 0, size[0], size[1]);
					}
				}
				return null;
			}

			@Override
			public void click(int x, int y, double h, General g, int clicktype) {
				double index = 1.0 * x/height;
				if(index < 4.0){
					if(selected && trad != null){
						selectedResources.remove(trad);
					}
					if(!selected && trad != null){
						selectedResources.add(trad);
					}
					selected = !selected;
				}else if(index < 5.0){
					
				}else if(index < 6.0){
					if(dis != null){
						TradeChange tr = new TradeChange(g, dis, selectedResources);
						manager.change(tr);
						notifyOfChange(g);
					}
				}
			}

			@Override
			public void paint(int w, int h, int x, int y, Space source, Graphics g, int mx, int my) {
				height = h;
				if(trad != null){
					int rh = (int) (h * 0.9);
					g.setColor(new Color(220, 220, 220));
					if(selected){
						g.setColor(new Color(255, 255, 255));
					}
					if(selected){
						g.fillRoundRect(x, y + h/2 - rh/2, (int) (h * 4.25), rh, h/5, h/5);
					}else{
						g.fillRoundRect(x, y + h/2 - rh/2, h * 4, rh, h/5, h/5);
					}
					BufferedImage image = SolarWarfare4_2.il.findImage("Resource" + trad.getType()).getFit(rh, rh);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
					g.setColor(new Color(0, 0, 0));
					g.setFont(g.getFont().deriveFont(Font.BOLD));
					TextPackage tp = new TextPackage(trad.getLocation().getName(), 5*h/2, rh/2, 1, g);
					g.setFont(g.getFont().deriveFont(tp.getFontValue()));
					int height = g.getFontMetrics().getHeight();
					for(int i = 0; i < tp.getResult().length; i++){
						String s = tp.getResult()[i];
						int width = g.getFontMetrics().stringWidth(s);
						g.drawString(s, x+h+3*h/2-width/2, y+h/2 + height/4 - height/2 * (tp.getResult().length/2 - i));
					}
				}
				
				if(dis != null){
					g.setColor(new Color(220, 220, 220));
					if(mx>x+5*h&&mx<x+6*h&&my>y&&my<y+h){
						g.setColor(new Color(255, 255, 255));
					}
					g.fillRect(x + h * 5, y, h, h);
					int rh = (int) (h * 0.9);
					BufferedImage image = dis.getInsignia(rh);
					g.drawImage(image, x + h * 5 + h/2 - image.getWidth()/2, y + h/2 - image.getHeight()/2, null);
				}
			}
		}
	}
	public static class TradeChange implements Change{
		private General source;
		private General target;
		private ArrayList<Resource> slct;
		public TradeChange(General s, General t, ArrayList<Resource> slt){
			source = s;
			target = t;
			slct = slt;
		}
		@Override
		public void makeChange() {
			for(Resource r: slct){
				r.setOwner(target);
			}
		}
		@Override
		public void undoChange() {
			for(Resource r: slct){
				r.setOwner(source);
			}
		}
		@Override
		public boolean legalChange() {
			return true;
		}
		public TradeChange(ArrayList<General> playerlist, Map world, String[] desc){
			source = playerlist.get(Integer.parseInt(desc[1]));
			target = playerlist.get(Integer.parseInt(desc[2]));
			slct = new ArrayList<Resource>();
			for(int i = 3; i < desc.length; i+=5){
				int x = Integer.parseInt(desc[i]);
				int y = Integer.parseInt(desc[i+1]);
				General creator = playerlist.get(Integer.parseInt(desc[i+2]));
				General owner = playerlist.get(Integer.parseInt(desc[i+3]));
				int type = Integer.parseInt(desc[i+4]);
				Space s = world.getAt(x, y);
				boolean sent = false;
				for(Resource r: s.getPile()){
					if(r.getCreator() == creator && r.getOwner() == owner && r.getType() == type){
						slct.add(r);
						sent = true;
						break;
					}
				}
				if(!sent){
					System.out.println("We didn't find the resource whose data starts at index " + i + " of " + desc);
				}
			}
		}
		@Override
		public ArrayList<String> getDescription() {
			ArrayList<String> alas = new ArrayList<String>();
			String form = "Trading!" + source.getIndex() + "!" + target.getIndex();
			for(Resource r: slct){
				form += "!" + r.getLocation().getX() + "!" + r.getLocation().getY();
				form += "!" + r.getCreator().getIndex() + "!"  + source.getIndex() + "!" + r.getType();
			}
			alas.add(form);
			return alas;
		}
	}
	private class Relation extends ScrollableBox{
		public Relation(){
			contents = new ArrayList<Cell>();
			for(General g: manager.getPlayerlist()){
				if(g != GameManager.viewing){
					contents.add(new RelationCell(g));
				}
			}
		}
		private class RelationCell extends Cell{
			private General target;
			private int height;
			public RelationCell(General t){
				target = t;
			}

			@Override
			public BufferedImage getTooltip(int x, int y, double h) {
				double index = 1.0 * x/height;
				if(index < 0){
					
				}else if(index < 1){
					BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
					Graphics g = imago.getGraphics();
					int[] size = bubbleText(target.getFormalName(), imago.getWidth(), imago.getHeight(), 0, 0, g);
					return imago.getSubimage(0, 0, size[0], size[1]);
				}else if(index < 1.25){
					
				}else if(index < 4.25){
					String text = "";
					int check = GameManager.viewing.lookupRelation(target);
					if(check < 0){
						text = GameManager.viewing.getFormalName() + " is at war with " + target.getFormalName();
					}else if(check == 0){
						text = GameManager.viewing.getFormalName() + " is neutral toward " + target.getFormalName();
					}else{
						text = GameManager.viewing.getFormalName() + " is allies with " + target.getFormalName();
					}
					BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
					Graphics g = imago.getGraphics();
					int[] size = bubbleText(text, imago.getWidth(), imago.getHeight(), 0, 0, g);
					return imago.getSubimage(0, 0, size[0], size[1]);
				}else if(index < 5){
					
				}else if(index <= 6){
					String text = "";
					int check = target.lookupRelation(GameManager.viewing);
					if(check < 0){
						text = target.getFormalName() + " is at war with " + GameManager.viewing.getFormalName();
					}else if(check == 0){
						text = target.getFormalName() + " is neutral toward " + GameManager.viewing.getFormalName();
					}else{
						text = target.getFormalName() + " is allies with " + GameManager.viewing.getFormalName();
					}
					BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
					Graphics g = imago.getGraphics();
					int[] size = bubbleText(text, imago.getWidth(), imago.getHeight(), 0, 0, g);
					return imago.getSubimage(0, 0, size[0], size[1]);
				}
				return null;
			}
			@Override
			public void click(int x, int y, double h, General g, int clicktype) {

				double index = 1.0 * x/height;
				if(index >= 1.25 && index < 4.25){
					int id = (int) (index - 1.25);
					int rel = g.lookupRelation(target);
					if(id == 0 && rel >= 0){
						ChangeRelation cr = new ChangeRelation(g, target, -1);
						manager.change(cr);
					}else if(id == 1 && rel != 0){
						ChangeRelation cr = new ChangeRelation(g, target, 0);
						manager.change(cr);
					}else if(id == 2 && rel <= 0){
						ChangeRelation cr = new ChangeRelation(g, target, 1);
						manager.change(cr);
					}
				}			
			}
			@Override
			public void paint(int w, int h, int x, int y, Space source, Graphics g, int mx, int my) {
				height = h;
				int rh = (int) (0.9 * h);
				BufferedImage insignia = target.getInsignia(rh);
				g.drawImage(insignia, x + (int) (rh * 0.1), y + h/2 - rh/2, null);
				x+=h;
				x+=h/4;
				int sh = (int) (0.7 * h);
				if(GameManager.viewing.lookupRelation(target) < 0){
					BufferedImage image = SolarWarfare4_2.il.findImage("Enemies").getFit(h, h);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}else{
					BufferedImage image = SolarWarfare4_2.il.findImage("GEnemies").getFit(sh, sh);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}
				x+=h;
				if(GameManager.viewing.lookupRelation(target) == 0){
					BufferedImage image = SolarWarfare4_2.il.findImage("Neutrals").getFit(h, h);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}else{
					BufferedImage image = SolarWarfare4_2.il.findImage("GNeutrals").getFit(sh, sh);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}
				x+=h;
				if(GameManager.viewing.lookupRelation(target) > 0){
					BufferedImage image = SolarWarfare4_2.il.findImage("Allies").getFit(h, h);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}else{
					BufferedImage image = SolarWarfare4_2.il.findImage("GAllies").getFit(sh, sh);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}
				x+=h;
				x+=3*h/4;
				int relationship = target.lookupRelation(GameManager.viewing);
				int ih = (int) (0.9 * h);
				if(relationship < 0){
					BufferedImage image = SolarWarfare4_2.il.findImage("Enemies").getFit(ih, ih);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}else if(relationship == 0){
					BufferedImage image = SolarWarfare4_2.il.findImage("Neutrals").getFit(ih, ih);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}else{
					BufferedImage image = SolarWarfare4_2.il.findImage("Allies").getFit(ih, ih);
					g.drawImage(image, x+(h-image.getWidth())/2, y + h/2 - image.getHeight()/2, null);
				}
			}
		}
		
	}
	public static class ChangeRelation implements Change{
		private General source;
		private General target;
		private int nR;
		private int oR;
		public ChangeRelation(General s, General t, int n){
			source = s;
			target = t;
			nR = n;
			oR = s.lookupRelation(t);
		}
		public ChangeRelation(ArrayList<General> playerlist, String[] desc){
			source = playerlist.get(Integer.parseInt(desc[1]));
			target = playerlist.get(Integer.parseInt(desc[2]));
			oR = Integer.parseInt(desc[3]);
			nR = Integer.parseInt(desc[4]);
		}
		@Override
		public void makeChange() {
			source.setRelation(target, nR);
		}

		@Override
		public void undoChange() {
			source.setRelation(target, oR);
		}

		@Override
		public boolean legalChange() {
			return true;
		}

		@Override
		public ArrayList<String> getDescription() {
			ArrayList<String> alas = new ArrayList<String>();
			String form = "Relation!" + source.getIndex() + "!" + target.getIndex() + "!" + oR + "!" + nR;
			alas.add(form);
			return alas;
		}
		
	}
	private abstract class Cell{
		public abstract BufferedImage getTooltip(int x, int y, double h);
		public abstract void click(int x, int y, double h, General g, int clicktype);
		public abstract void paint(int w, int h, int x, int y, Space source, Graphics g, int mx, int my);
	}
	private abstract class ScrollableBox extends InfoBox{
		protected ArrayList<Cell> contents;
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private boolean active;
		private int delta;
		private int cellHeight;
		private int dhh;
		private boolean scrolling;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			y -= yStart;
			if(y >= 0){
				if(scrolling){
					int index = (int) (y/(0.5 * cellHeight));
					if(index == 0){
						BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
						Graphics g = imago.getGraphics();
						int[] size = bubbleText("Scroll Up", imago.getWidth(), imago.getHeight(), 0, 0, g);
						return imago.getSubimage(0, 0, size[0], size[1]);
					}else if(index < dhh - 1){
						int realIndex = (index - 1)/2 + delta;
						if(realIndex >= 0 && realIndex < contents.size()){
							return contents.get(realIndex).getTooltip(x-xStart, y - (cellHeight * 2) * index, h);
						}else{
							return null;
						}
					}else if(index == dhh - 1){
						BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
						Graphics g = imago.getGraphics();
						int[] size = bubbleText("Scroll Down", imago.getWidth(), imago.getHeight(), 0, 0, g);
						return imago.getSubimage(0, 0, size[0], size[1]);
					}else{
						return null;
					}
				}else{
					int index = (int) (y/cellHeight);
					if(index < contents.size()){
						return contents.get(index).getTooltip(x - xStart, y-cellHeight * index, h);
					}else{
						return null;
					}
				}
				
			}else{
				return null;
			}
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			active = true;
			this.width = WIDTH;
			this.cellHeight = width/6;
			this.xStart = STARTX;
			this.yStart = STARTY;
			this.height = 0;
			this.dhh = 0;
			int ys = yStart;
			if(contents.size() * cellHeight > HEIGHT){
				scrolling = true;
			}
			if(scrolling){
				BufferedImage up = SolarWarfare4_2.il.findImage("Upscroll").getFit(width, cellHeight/2);
				g.drawImage(up, xStart + width/2 - up.getWidth()/2, ys, null);
				height += up.getHeight();
				ys+= up.getHeight();
				dhh++;
			}
			for(int i = delta; i < contents.size(); i++){
				Cell c = contents.get(i);
				c.paint(width, cellHeight, xStart, ys, source, g, MX, MY);
				height+=cellHeight;
				ys+=cellHeight;
				dhh+=2;
				if(height + 3 * cellHeight/2 > HEIGHT){
					break;
				}
			}
			if(scrolling){
				BufferedImage down = SolarWarfare4_2.il.findImage("Downscroll").getFit(width, cellHeight/2);
				g.drawImage(down, xStart + width/2 - down.getWidth()/2, ys, null);
				dhh++;
				height+=down.getHeight();
			}
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {
			y -= yStart;
			if(y >= 0){
				if(scrolling){
					int index = (int) (y/(0.5 * cellHeight));
					if(index == 0){
						if(delta > 0){
							delta--;
						}
					}else if(index < dhh - 1){
						int realIndex = (index - 1)/2 + delta;
						if(realIndex >= 0 && realIndex < contents.size()){
							contents.get(realIndex).click(x - xStart, y - (cellHeight * 2) * index, h, g, clicktype);
						}
					}else if(index == dhh - 1){
						int maxHeight = contents.size() * 2 + 2;
						if(maxHeight > dhh){
							int maxDelta = (maxHeight - dhh)/2;
							if(delta < maxDelta){
								delta++;
							}
						}
					}else{
						
					}
				}else{
					int index = (int) (y/cellHeight);
					if(index < contents.size()){
						contents.get(index).click(x-xStart, y-cellHeight*index, h, g, clicktype);
					}
				}
			}
		}
	
	}
	private class SubmodeTitle extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
			Graphics g = imago.getGraphics();
			int[] size = bubbleText("Switch to " + textChoice((submode+1)%3), imago.getWidth(), imago.getHeight(), 0, 0, g);
			return imago.getSubimage(0, 0, size[0], size[1]);
		}
		private String textChoice(int i){
			if(i == 0){
				return "Relations";
			}else if(i == 1){
				return "Resource Trading";
			}else if(i == 2){
				return "Planet Trading";
			}
			return "";
		}
		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			active = true;
			this.width = WIDTH;
			this.height = width/9;
			this.xStart = STARTX;
			this.yStart = STARTY;
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			String text = textChoice(submode);
			
			g.setFont(g.getFont().deriveFont(TextPackage.idealFont(text, width, height, 0.5, g)));
			g.setColor(new Color(205, 255, 205));
			int stringWidth = g.getFontMetrics().stringWidth(text);
			int stringHeight = g.getFontMetrics().getHeight();
			g.drawString(text, xStart + width/2 - stringWidth/2, yStart + height/2 + stringHeight/2 - 3 * stringHeight/4 + height/2);
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {
			submode++;
			submode%=3;
			notifyOfChange(g);
		}
	}
	private class EmpireTitle extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			return null;
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			active = true;
			this.width = WIDTH;
			this.height = width/6;
			this.xStart = STARTX;
			this.yStart = STARTY;
			BufferedImage insignia = GameManager.viewing.getInsignia(height);			
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			String text = "";
			text = GameManager.viewing.getFormalName();
			g.setFont(g.getFont().deriveFont(TextPackage.idealFont(text, width - insignia.getWidth()*3, height, 0.5, g)));
			g.setColor(new Color(255, 255, 255));
			int stringWidth = g.getFontMetrics().stringWidth(text);
			int stringHeight = g.getFontMetrics().getHeight();
			g.drawString(text, xStart + width/2 - stringWidth/2, yStart + height/2 + stringHeight/4);
			g.drawImage(insignia, xStart+width/2-stringWidth/2-3*insignia.getWidth()/2, yStart, null);
			g.drawImage(insignia, xStart+width/2+stringWidth/2+1*insignia.getWidth()/2, yStart, null);
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {}
	}
	private class Ships extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private Space source;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			int bh = width/4;
			for(int i = 0; i < source.getFleet().size(); i++){
				if(x>=xStart+bh/4&&x<=xStart+5*bh/4){
					if(y>=yStart+i*bh&&y<=yStart+(i+1)*bh){
						return source.getFleet().get(i).getTooltip(h);
					}
				}
				int th = (int) (bh*0.9);
				int ah = th;
				int tw = width - bh * 3/4;
				int xs = xStart + bh*3/4+bh/2;
				int initialys = (int) (yStart + bh * (i + 0.5) - (bh * 0.8)/2);
				int ys = initialys;
				ArrayList<ButtonAction> actions = source.getFleet().get(i).getCurrentActs();
				for(int bIndex = 0; bIndex < actions.size(); bIndex++){
					ButtonAction ba = actions.get(bIndex);
					if(x >= xs && x <= xs + ba.getWidth(tw, th)){
						if(y >= ys && y <= ys + ba.getHeight(tw, th)){
							String tooltip = ba.getTooltipText();
							BufferedImage imago = new BufferedImage((int) (5 * h), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText(tooltip, imago.getWidth(), imago.getHeight(), 0, 0, g);
							if(size[0] < 1 || size[0] > imago.getWidth()){
								System.out.println("Image Width: " + imago.getWidth());
								System.out.println("Proposed Width: " + size[0]);
								System.out.println("Image Height: " + imago.getHeight());
								System.out.println("Proposed Height: " + size[1]);
								System.out.println("Displayed Text");
								System.out.println(tooltip);
								size[0] = imago.getWidth();
							}
							if(size[1] < 1 || size[1] > imago.getHeight()){
								System.out.println("Image Width: " + imago.getWidth());
								System.out.println("Proposed Width: " + size[0]);
								System.out.println("Image Height: " + imago.getHeight());
								System.out.println("Proposed Height: " + size[1]);
								System.out.println("Displayed Text");
								System.out.println(tooltip);
								size[1] = imago.getWidth();
							}
							return imago.getSubimage(0, 0, size[0], size[1]);
							//TODO WE GOT A BUG FOLKS
						}
					}
					if(bIndex < actions.size() - 1){
						ButtonAction next = actions.get(bIndex + 1);
						ah -= ba.getHeight(tw, th);
						if(next.getHeight(tw, th) > ah){
							ah = th;
							ys = initialys;
							xs+=ba.getWidth(tw, th);
						}else{
							ys += ba.getHeight(tw, th);
						}
					}
				}
			
			
			}
			return null;
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			
			this.width = WIDTH;
			int bh = width/4;
			this.height = source.getFleet().size() * bh;
			this.xStart = STARTX;
			this.yStart = STARTY;
			this.source = source;
			active = true;
			for(int i = 0; i < source.getFleet().size(); i++){
				BufferedImage bi = source.getFleet().get(i).getImage(bh, null, false);
				g.drawImage(bi,xStart+bh*3/4-bi.getWidth()/2, (int) (yStart + bh * (i + 0.5))- bi.getHeight()/2, null);
				int th = (int) (bh*0.9);
				int ah = th;
				int tw = width - bh * 3/4;
				int xs = xStart + bh*3/4+bh/2;
				int initialys = (int) (yStart + bh * (i + 0.5) - (bh * 0.8)/2);
				int ys = initialys;
				ArrayList<ButtonAction> actions = source.getFleet().get(i).getCurrentActs();
				for(int bIndex = 0; bIndex < actions.size(); bIndex++){
					ButtonAction ba = actions.get(bIndex);
					BufferedImage ai = ba.getImage(tw, th, MX, MY);
					g.drawImage(ai,xs+ba.getWidth(tw,th)/2-ai.getWidth()/2, ys+ba.getHeight(tw, th)/2-ai.getHeight()/2, null);
					if(bIndex < actions.size() - 1){
						ButtonAction next = actions.get(bIndex + 1);
						ah -= ba.getHeight(tw, th);
						if(next.getHeight(tw, th) > ah){
							ah = th;
							ys = initialys;
							xs+=ba.getWidth(tw, th);
						}else{
							ys += ba.getHeight(tw, th);
						}
					}
				}
			}
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {
			int bh = width/4;
			for(int i = 0; i < source.getFleet().size(); i++){
				if(clicktype == 3){
					if(x>=xStart+bh/4&&x<=xStart+5*bh/4){
						if(y>=yStart+i*bh&&y<=yStart+(i+1)*bh){
							manager.notifyOfSelectionClick(source.getFleet().get(i), source);
							notifyOfChange(g);
						}
					}
				}
				if(clicktype == 1){
					int th = (int) (bh*0.9);
					int ah = th;
					int tw = width - bh * 3/4;
					int xs = xStart + bh*3/4+bh/2;
					int initialys = (int) (yStart + bh * (i + 0.5) - (bh * 0.8)/2);
					int ys = initialys;
					ArrayList<ButtonAction> actions = source.getFleet().get(i).getCurrentActs();
					for(int bIndex = 0; bIndex < actions.size(); bIndex++){
						ButtonAction ba = actions.get(bIndex);
						if(x >= xs && x <= xs + ba.getWidth(tw, th)){
							if(y >= ys && y <= ys + ba.getHeight(tw, th)){
								manager.change(ba);
								notifyOfChange(g);
								break;
							}
						}
						if(bIndex < actions.size() - 1){
							ButtonAction next = actions.get(bIndex + 1);
							ah -= ba.getHeight(tw, th);
							if(next.getHeight(tw, th) > ah){
								ah = th;
								ys = initialys;
								xs+=ba.getWidth(tw, th);
							}else{
								ys += ba.getHeight(tw, th);
							}
						}
					}
				}
				
			}
		}
	}
	private class Buildings extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private Planet planet;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			int bh = (int) (width * 0.25);
			if(planet != null){
				for(int i = 0; i < 3; i++){
					BufferedImage bi = null;
					Structure s = null;
					if(i < planet.getStructures().size()){
						s = planet.getStructures().get(i);
						bi = s.getDetailImage(width, bh);
					}else{
						s = site;
						bi = s.getDetailImage(width, bh);
					}
					
					int th = (int) (bi.getHeight()*0.9);
					int ah = th;
					int tw = width - bi.getWidth() - bh/10;
					int xs = xStart + bh/10 + bi.getWidth();
					int initialys = (int) (yStart + bh * (i + 0.5) - (bi.getHeight() * 0.8)/2);
					int ys = initialys;
					if(x >= xStart + bh/10 && x <= xStart + bh/10 + bi.getWidth()){
						if(y >= ys && y <= ys + bi.getHeight()){
							String text = s.getType();
							BufferedImage imago = new BufferedImage((int) (h * 4), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText(text, imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}
					}
					ArrayList<ButtonAction> actions = s.getCurrentActs();
					for(int bIndex = 0; bIndex < actions.size(); bIndex++){
						ButtonAction ba = actions.get(bIndex);
						if(x >= xs && x <= xs + ba.getWidth(tw, th)){
							if(y >= ys && y <= ys + ba.getHeight(tw, th)){
								String text = ba.getTooltipText();
								if(text != null){
									if(ba.cost() == null){
										BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
										Graphics g = imago.getGraphics();
										int[] size = bubbleText(text, imago.getWidth(), imago.getHeight(), 0, 0, g);
										return imago.getSubimage(0, 0, size[0], size[1]);
									}else{
										int[] cost = ba.cost();
										double d = 2 * h /3;
										BufferedImage imago = new BufferedImage((int) (d * 4), (int) (d * 2), BufferedImage.TYPE_INT_ARGB);
										Graphics g = imago.getGraphics();
										g.setColor(new Color(200, 200, 200));
										g.fillRoundRect(0, 0, imago.getWidth(), imago.getHeight(), (int) (h/4), (int) (h/4));
										g.setColor(new Color(0, 0, 0));
										for(int j = 0; j < cost.length; j++){
											BufferedImage resourceBack = SolarWarfare4_2.il.findImage("Resource"+j).getFit((int)d, (int)d);
											g.drawImage(resourceBack, (int) (j*d), (int)(imago.getHeight() - d), null);
											String amount = Integer.toString(cost[j]);
											g.setFont(g.getFont().deriveFont(TextPackage.idealFont(amount, (int) d, (int) d, 1, g)));
											g.setFont(g.getFont().deriveFont(Font.BOLD));
											int width = g.getFontMetrics().stringWidth(amount);
											int height = g.getFontMetrics().getHeight();
											g.drawString(amount, (int) ((0.5+j)*d - width/2), (int)(imago.getHeight()-height/4));
										}
										g.setColor(new Color(0, 0, 0));
										g.setFont(g.getFont().deriveFont(Font.BOLD));
										TextPackage tp = new TextPackage(text,imago.getWidth(), (int) (imago.getHeight() - d), 1, g, 2);
										g.setFont(g.getFont().deriveFont(tp.getFontValue()));
										for(int j = 0; j < tp.getResult().length; j++){
											String write = tp.getResult()[j];
											g.drawString(write, imago.getWidth()/2 - g.getFontMetrics().stringWidth(write)/2, (int)((j+0.75) * g.getFontMetrics().getHeight()));
										}
										return imago;
									}
									
								}
								
							}
						}
						if(bIndex < actions.size() - 1){
							ButtonAction next = actions.get(bIndex + 1);
							ah -= ba.getHeight(tw, th);
							if(next.getHeight(tw, th) > ah){
								ah = th;
								ys = initialys;
								xs+=ba.getWidth(tw, th);
							}else{
								ys += ba.getHeight(tw, th);
							}
						}
					}
				
					
				}
			}
			return null;
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			
			this.width = WIDTH;
			int bh = (int) (width * 0.25);
			this.height = 3 * bh;
			this.xStart = STARTX;
			this.yStart = STARTY;
			if(source.getIdentifier() == 1){
				active = true;
				Planet p = (Planet) source;
				planet = p;
				for(int i = 0; i < 3; i++){
					BufferedImage bi = null;
					Structure s = null;
					if(i < p.getStructures().size()){
						s = p.getStructures().get(i);
						bi = s.getDetailImage(width, bh);
					}else{
						s = site;
						bi = s.getDetailImage(width, bh);
					}
					g.drawImage(bi, xStart + bh/10, (int) (yStart + bh * (i + 0.5))- bi.getHeight()/2, null);
					int th = (int) (bi.getHeight()*0.9);
					int ah = th;
					int tw = width - bi.getWidth() - bh/10;
					int xs = xStart + bh/10 + bi.getWidth();
					int initialys = (int) (yStart + bh * (i + 0.5) - (bi.getHeight() * 0.8)/2);
					int ys = initialys;
					ArrayList<ButtonAction> actions = s.getCurrentActs();
					for(int bIndex = 0; bIndex < actions.size(); bIndex++){
						ButtonAction ba = actions.get(bIndex);
						BufferedImage ai = ba.getImage(tw, th, MX, MY);
						g.drawImage(ai,xs+ba.getWidth(tw,th)/2-ai.getWidth()/2, ys+ba.getHeight(tw, th)/2-ai.getHeight()/2, null);
						if(bIndex < actions.size() - 1){
							ButtonAction next = actions.get(bIndex + 1);
							ah -= ba.getHeight(tw, th);
							if(next.getHeight(tw, th) > ah){
								ah = th;
								ys = initialys;
								xs+=ba.getWidth(tw, th);
							}else{
								ys += ba.getHeight(tw, th);
							}
						}
					}
				}
			}
			
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {
			int bh = (int) (width * 0.25);
			if(planet != null){
				for(int i = 0; i < 3; i++){
					BufferedImage bi = null;
					Structure s = null;
					if(i < planet.getStructures().size()){
						s = planet.getStructures().get(i);
						bi = s.getDetailImage(width, bh);
					}else{
						s = site;
						bi = s.getDetailImage(width, bh);
					}
					if(clicktype == 1){
						int th = (int) (bi.getHeight()*0.9);
						int ah = th;
						int tw = width - bi.getWidth() - bh/10;
						int xs = xStart + bh/10 + bi.getWidth();
						int initialys = (int) (yStart + bh * (i + 0.5) - (bi.getHeight() * 0.8)/2);
						int ys = initialys;
						ArrayList<ButtonAction> actions = s.getCurrentActs();
						for(int bIndex = 0; bIndex < actions.size(); bIndex++){
							ButtonAction ba = actions.get(bIndex);
							if(x >= xs && x <= xs + ba.getWidth(tw, th)){
								if(y >= ys && y <= ys + ba.getHeight(tw, th)){
									manager.change(ba);
									notifyOfChange(g);
									break;
								}
							}
							if(bIndex < actions.size() - 1){
								ButtonAction next = actions.get(bIndex + 1);
								ah -= ba.getHeight(tw, th);
								if(next.getHeight(tw, th) > ah){
									ah = th;
									ys = initialys;
									xs+=ba.getWidth(tw, th);
								}else{
									ys += ba.getHeight(tw, th);
								}
							}
						}
					}
					
				}
			}
		}
	}
	private class AvailableResources extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private boolean active;
		private int[] production;
		public AvailableResources(){
			production = new int[Resource.numResources];
			for(Planet p: manager.getWorld().getAllPlanets()){
				if(p.getOwner() == GameManager.viewing){
					for(Structure s: p.getStructures()){
						if(s.getType().contains("Refinery")){
							production[Resource.toType(s.getType().split(" ")[0])]++;
						}
					}
				}
			}
		}
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		public boolean within(int x, int y){
			return super.within(x, y);
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			if(y >= yStart && y <= yStart + height){
				double xLoc = (x - xStart)/height;
				if(xLoc >= 0){
					if(showProduction){
						int index = (int) (xLoc);
						if(index >= 0 && index < production.length){
							String tooltip = production[index] + "x Produced " + Resource.toStringType(index);
							BufferedImage imago = new BufferedImage((int) (h * 4), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText(tooltip, imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}
					}else{
						int[] avail = Resource.toIndexedArray(allAvailable);
						int index = (int) (xLoc);
						if(index >= 0 && index < avail.length){
							String tooltip = avail[index] + "x Available " + Resource.toStringType(index);
							BufferedImage imago = new BufferedImage((int) (h * 4), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
							Graphics g = imago.getGraphics();
							int[] size = bubbleText(tooltip, imago.getWidth(), imago.getHeight(), 0, 0, g);
							return imago.getSubimage(0, 0, size[0], size[1]);
						}
					}
					
				}
			}
			return null;
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			active = true;
			this.width = WIDTH;
			int[] avail = Resource.toIndexedArray(allAvailable);
			if(showProduction){
				avail = production;
			}
			this.height = width/avail.length;
			this.xStart = STARTX;
			this.yStart = STARTY + HEIGHT - height;
			g.setColor(Color.black);
			for(int i = 0; i < avail.length; i++){
				BufferedImage bi = SolarWarfare4_2.il.findImage("Resource" + i).getFit(height, height);
				g.drawImage(bi, xStart + i * height, yStart, null);
				String amount = Integer.toString(avail[i]);
				g.setFont(g.getFont().deriveFont(TextPackage.idealFont(amount, height, height, 1, g)));
				g.setFont(g.getFont().deriveFont(Font.BOLD));
				int swidth = g.getFontMetrics().stringWidth(amount);
				int sheight = g.getFontMetrics().getHeight();
				g.drawString(amount, (int) (xStart + (0.5+i)*height - swidth/2), (int)(yStart+height-sheight/4));
			}		
		}
		@Override
		public void click(int x, int y, double h, General g, int clicktype) {
			showProduction = !showProduction;
		}
	}
	private class SwitchButton extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
			Graphics g = imago.getGraphics();
			int[] size = bubbleText("Switch to " + viewType((mode+1)%2), imago.getWidth(), imago.getHeight(), 0, 0, g);
			return imago.getSubimage(0, 0, size[0], size[1]);
		}
		private String viewType(int mode){
			if(mode == 0){
				return "Space Details";
			}else if(mode == 1){
				return "Diplomacy Panel";
			}else{
				return "???";
			}
		}
		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			active = true;
			this.width = WIDTH;
			this.height = width/12;
			this.xStart = STARTX;
			this.yStart = STARTY;
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			String text = viewType(mode);
			g.setFont(g.getFont().deriveFont(TextPackage.idealFont(text, width, height, 0.5, g)));
			int stringWidth = g.getFontMetrics().stringWidth(text);
			int stringHeight = g.getFontMetrics().getHeight();
			g.setColor(new Color(200, 200, 200));
			//g.fillRoundRect(xStart + width/2 - stringWidth/2, yStart, stringWidth, stringHeight, height/5, height/5);
			g.setColor(new Color(200, 200, 200));
			g.drawString(text, xStart + width/2 - stringWidth/2, yStart + height/2 + stringHeight/2 - 3 * stringHeight/4 + height/2);
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {
			mode = (mode+1)%2;
			notifyOfChange(g);
		}
	}
	private class SpaceTitle extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			return null;
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			active = true;
			this.width = WIDTH;
			this.height = width/6;
			this.xStart = STARTX;
			this.yStart = STARTY;
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			String text = "";
			text = source.getName();
			g.setFont(g.getFont().deriveFont(TextPackage.idealFont(text, width, height, 0.5, g)));
			g.setColor(new Color(255, 255, 255));
			int stringWidth = g.getFontMetrics().stringWidth(text);
			int stringHeight = g.getFontMetrics().getHeight();
			g.drawString(text, xStart + width/2 - stringWidth/2, yStart + height/2 + stringHeight/2 - 3 * stringHeight/4 + height/2);
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {}
	}
	private class PODetails extends InfoBox{
		private int width;
		private int height;
		private int xStart;
		private int yStart;
		private Space source;
		private boolean active;
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getXStart() {
			return xStart;
		}

		@Override
		public int getYStart() {
			return yStart;
		}
		@Override
		public boolean active(){
			return active;
		}
		@Override
		public BufferedImage getTooltip(int x, int y, double h) {
			int mh = width/6;
			String text = null;
			int rx = x - xStart;
			int ry = y - yStart;
			if(source.getIdentifier() == 1){
				Planet p = (Planet) source;
				int[] prod = p.getProduction();
				if(rx < width/2 && rx >= 0 && ry >= 0 && ry <= mh){
					int index = rx/mh;
					if(index < prod.length/2){
						text = "Max " + Resource.toStringType(index) + " Production: " + prod[index];
					}
				}
				if(rx > width/2 && rx <= width && ry >= 0 && ry <= mh){
					int index = prod.length - (width - rx)/mh - 1;
					if(index < prod.length && index >= prod.length/2){
						text = "Max " + Resource.toStringType(index) + " Production: " + prod[index];
					}
				}
			}
			if(source.getOwner() != null){
				if(rx > width/2 - mh && rx < width/2 + mh && ry > 0 && ry < height){
					text = "Owned by " + source.getOwner().getFormalName();
				}
			}
			if(text == null){
				return null;
			}else{
				BufferedImage imago = new BufferedImage((int) (h * 5), (int) (h * 0.4), BufferedImage.TYPE_INT_ARGB);
				Graphics g = imago.getGraphics();
				int[] size = bubbleText(text, imago.getWidth(), imago.getHeight(), 0, 0, g);
				return imago.getSubimage(0, 0, size[0], size[1]);
			}
		}

		@Override
		public void paint(int WIDTH, int HEIGHT, int STARTX, int STARTY, Space source, Graphics g, int MX, int MY) {
			this.width = WIDTH;
			this.height = width/6;
			this.xStart = STARTX;
			this.yStart = STARTY;
			this.source = source;
			if(source.getOwner() != null){
				active = true;
				BufferedImage insignia = source.getOwner().getInsignia(width - 4 * height);
				g.drawImage(insignia, xStart + width/2 - insignia.getWidth()/2, yStart, null);
			}
			if(source.getIdentifier() == 1){
				active = true;
				Planet p = (Planet) source;
				int[] prod = p.getProduction();
				g.setColor(Color.black);
				for(int i = 0; i < prod.length; i++){
					BufferedImage bi = SolarWarfare4_2.il.findImage("Resource" + i).getFit(height, height);
					if(i < prod.length/2){
						g.drawImage(bi, xStart + i * height, yStart, null);
						String amount = Integer.toString(prod[i]);
						g.setFont(g.getFont().deriveFont(TextPackage.idealFont(amount, height, height, 1, g)));
						g.setFont(g.getFont().deriveFont(Font.BOLD));
						int swidth = g.getFontMetrics().stringWidth(amount);
						int sheight = g.getFontMetrics().getHeight();
						g.drawString(amount, (int) (xStart + (0.5+i)*height - swidth/2), (int)(yStart+height-sheight/4));
					}else{
						g.drawImage(bi, xStart+width-height*prod.length/2 + (i-prod.length/2) * height, yStart, null);
						String amount = Integer.toString(prod[i]);
						g.setFont(g.getFont().deriveFont(TextPackage.idealFont(amount, height, height, 1, g)));
						g.setFont(g.getFont().deriveFont(Font.BOLD));
						int swidth = g.getFontMetrics().stringWidth(amount);
						int sheight = g.getFontMetrics().getHeight();
						g.drawString(amount, (int) (xStart+width-height*prod.length/2 + (0.5+i - prod.length/2)*height - swidth/2), (int)(yStart+height-sheight/4));
					}
				}
				if(source.getOwner() != null){
					this.height = width - 4 * height;
				}
			}
		}

		@Override
		public void click(int x, int y, double h, General g, int clicktype) {}
	}
}