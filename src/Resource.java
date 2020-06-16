import java.util.ArrayList;

public class Resource extends Index{
	private int type;
	private Space location;
	private General owner;
	private General creator;
	public static final int numResources = 5;
	public Resource(int type, Space location, General creator){
		this.type = type;
		this.location = location;
		this.owner = creator;
		this.creator = creator;
	}
	public Resource(String[] desc, ArrayList<General> grl, ArrayList<Space> loc){
		type = Integer.parseInt(desc[0]);
		location = loc.get(Integer.parseInt(desc[1]));
		owner = grl.get(Integer.parseInt(desc[2]));
		creator = grl.get(Integer.parseInt(desc[3]));
	}
	public String getDescription(){
		String desc = "";
		desc += type + "!";
		desc += location.getIndex() + "!";
		desc += owner.getIndex() + "!";
		desc += creator.getIndex() + "!";
		return desc;
	}
	public General getOwner() {
		return owner;
	}
	public void setOwner(General owner) {
		this.owner = owner;
	}
	public int getType() {
		return type;
	}
	public Space getLocation() {
		return location;
	}
	public General getCreator() {
		return creator;
	}
	public static int toType(String name){
		switch(name){
		case "Hydrogen": return 0;
		case "Iron": return 1;
		case "Gold": return 2;
		case "Silicon": return 3;
		case "Missile": return 4;
		default: System.out.println(name); Toolbox.breakThings(); return -1;
		}
	}
	public static int[] toIndexedArray(ArrayList<Resource> contents){
		int[] result = new int[Resource.numResources];
		for(Resource r: contents){
			result[r.getType()]++;
		}
		return result;
	}
	public static String toStringType(int type){
		switch(type){
		case 0: return "Hydrogen";
		case 1: return "Iron";
		case 2: return "Gold";
		case 3: return "Silicon";
		case 4: return "Missile";
		default: Toolbox.breakThings();
			return "Unknown resource type; this is coming from Resource.toStringType(int)";
		}
	}
	public String stringType(){
		switch(type){
		case 0: return "Hydrogen";
		case 1: return "Iron";
		case 2: return "Gold";
		case 3: return "Silicon";
		case 4: return "Missile";
		default: Toolbox.breakThings();
			return "Unknown resource type; this is coming from Resource.stringType()";
		}
	}
}