import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class General extends Index{
	private String formalName;
	private String informalName;
	private String password;
	private ArrayList<Relation> relations;
	private ArrayList<String> shipNames;
	private boolean active;
	public General(String fn, String in, String pa){
		formalName = fn;
		informalName = in;
		password = pa;
		relations = new ArrayList<Relation>();
		shipNames = new ArrayList<String>();
		shipNames.add("Raider");
		shipNames.add("Cruiser");
		shipNames.add("Juggernaut");
		shipNames.add("General");
		active = false;
	}
	public General(String fn, String in, String pn, String pa){
		formalName = fn;
		informalName = in;
		password = pa;
		relations = new ArrayList<Relation>();
		shipNames = new ArrayList<String>();
		shipNames.add("Fury");
		shipNames.add("Hunter");
		shipNames.add("Leviathan");
		shipNames.add(pn);
		active = false;
	}
	public General(String[] desc){
		formalName = desc[0];
		informalName = desc[1];
		password = desc[2];
		shipNames = new ArrayList<String>();
		relations = new ArrayList<Relation>();
		for(int i = 3; i < desc.length; i++){
			shipNames.add(desc[i]);
		}
	}
	public String getDescription(){
		String form = formalName + "!" + informalName + "!" + password;;
		for(String s: shipNames){
			form += "!" + s;
		}
		return form;
	}
	public static ArrayList<String> fullDescription(ArrayList<General> list){
		ArrayList<String> save = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++){
			General g = list.get(i);
			g.setIndex(i);
			save.add(g.getDescription());
		}
		return save;
	}
	public static void saveGenerals(ArrayList<General> list, String filepath){
		ArrayList<String> save = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++){
			General g = list.get(i);
			g.setIndex(i);
			save.add(g.getDescription());
		}
		Toolbox.save(save, filepath, false);
	}
	public static ArrayList<General> loadGenerals(String filepath){
		ArrayList<String> info = Toolbox.load(filepath);
		ArrayList<General> g = new ArrayList<General>();
		int index = 0;
		for(String s: info){
			General nova = new General(s.split("!"));
			g.add(nova);
			nova.setIndex(index);
			index++;
		}
		return g;
	}
	public String getNameFor(int shiptype){
		return shipNames.get(shiptype);
	}
	public BufferedImage getInsignia(int dim){
		return SolarWarfare4_2.il.findImage(getInformalName()).getFit(dim, dim);
	}
	public void setRelation(General g, int opinion){
		accessRelation(g).setOpinion(opinion);
	}
	public Relation accessRelation(General g){
		for(Relation r: relations){
			if(r.getTarget() == g){
				return r;
			}
		}
		Relation r;
		if(g == this){
			r = new Relation(this, g, 1);
		}else{
			r = new Relation(this, g, 0);
		}
		relations.add(r);
		return r;
	}
	public int lookupRelation(General g){
		return accessRelation(g).getOpinion();
	}
	public void addRelation(Relation r){
		relations.add(r);
	}
	public String getFormalName(){
		return formalName;
	}
	public String getInformalName(){
		return informalName;
	}
	public boolean checkPassword(String test){
		return password.equals(test);
	}
	public boolean isActive(){
		return active;
	}
	public void activate(){
		active = true;
	}
	public void deactivate(){
		active = false;
	}
}