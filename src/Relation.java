import java.util.ArrayList;

public class Relation{
	private General source;
	private General target;
	private int opinion;
	public Relation(General s, General t, int o){
		source = s;
		target = t;
		opinion = o;
	}
	public static ArrayList<String> fullDescription(ArrayList<General> g){
		ArrayList<String> save = new ArrayList<String>();
		for(int i = 0; i < g.size(); i++){
			String info = "";
			for(int j = 0; j < g.size(); j++){
				info += "" + g.get(i).lookupRelation(g.get(j));
				if(j != g.size() - 1){
					info += "!";
				}
			}
			save.add(info);
		}
		return save;
	}
	public static void saveRelationMatrix(ArrayList<General> g, String filepath){
		ArrayList<String> save = new ArrayList<String>();
		for(int i = 0; i < g.size(); i++){
			String info = "";
			for(int j = 0; j < g.size(); j++){
				info += "" + g.get(i).lookupRelation(g.get(j));
				if(j != g.size() - 1){
					info += "!";
				}
			}
			save.add(info);
		}
		Toolbox.save(save, filepath, false);
	}
	public static void loadRelationMatrix(ArrayList<General> g, String filepath){
		ArrayList<String> info = Toolbox.load(filepath);
		for(int i = 0; i < info.size(); i++){
			String[] desc = info.get(i).split("!");
			for(int j = 0; j < desc.length; j++){
				g.get(i).setRelation(g.get(j), Integer.parseInt(desc[j]));
			}
		}
	}
	public static void createRelationMatrix(ArrayList<General> g, ArrayList<String> info){
		for(int i = 0; i < info.size(); i++){
			String[] desc = info.get(i).split("!");
			for(int j = 0; j < desc.length; j++){
				g.get(i).setRelation(g.get(j), Integer.parseInt(desc[j]));
			}
		}
	}
	public int getOpinion() {
		return opinion;
	}
	public void setOpinion(int opinion) {
		this.opinion = opinion;
	}
	public General getSource() {
		return source;
	}
	public General getTarget() {
		return target;
	}
	public String toString(){
		if(opinion == 0){
			return source.getFormalName() + " is neutral towards " + target.getFormalName();
		}else if(opinion == 1){
			return source.getFormalName() + " is friendly towards " + target.getFormalName(); 
		}else if(opinion == -1){
			return source.getFormalName() + " is antagonistic towards " + target.getFormalName();
		}
		Toolbox.breakThings();
		return "Relation unknown, please check for errors";
	}
}