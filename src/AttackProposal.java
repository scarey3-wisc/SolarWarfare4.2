import java.util.ArrayList;

public class AttackProposal{
	private ArrayList<Ship> dba;
	//defenders before attack;
	private ArrayList<Ship> daa;
	//defenders after attack;
	private ArrayList<Ship> dead;
	//things killed by the attack;
	public AttackProposal(ArrayList<Ship> defenders, ArrayList<Ship> attackers, int number){
		dba = defenders;
		daa = new ArrayList<Ship>();
		dead = new ArrayList<Ship>();
		ArrayList<Integer> health = new ArrayList<Integer>();
		for(Ship s: defenders){
			health.add(s.getD());
		}
		for(int i = 0; i < attackers.size(); i++){
			int damage = attackers.get(i).getA();
			int index = number%defenders.size();
			number/=defenders.size();
			health.set(index, health.get(index) - damage);
		}
		for(int i = 0; i < defenders.size(); i++){
			if(health.get(i) <= 0){
				dead.add(defenders.get(i));
			}else{
				daa.add(defenders.get(i));
			}
		}
	}
	public static AttackProposal Optimize(ArrayList<Ship> a, ArrayList<Ship> d){
		return discernBest(generateAllProposals(d, a), normalPriority());
	}
	public static AttackProposal discernBest(ArrayList<AttackProposal> list, ArrayList<Integer> priority){
		if(list.size() == 0){
			Toolbox.breakThings();
			return null;
		}
		int optimus = 0;
		AttackProposal best = list.get(0);
		for(AttackProposal ap: list){
			if(ap.evaluate(priority) > optimus){
				optimus = ap.evaluate(priority);
				best = ap;
			}
		}
		return best;
	}
	public static ArrayList<AttackProposal> generateAllProposals(ArrayList<Ship> d, ArrayList<Ship> a){
		int max = (int) (Math.pow(d.size(), a.size()));
		ArrayList<AttackProposal> list = new ArrayList<AttackProposal>();
		for(int i = 0; i < max; i++){
			list.add(new AttackProposal(d,a,i));
		}
		return list;
	}
	public ArrayList<Ship> getKilled(){
		return dead;
	}
	public ArrayList<Ship> getSurvivors(){
		return daa;
	}
	public ArrayList<Ship> getOriginal(){
		return dba;
	}
	public int comparativeEvaluation(int maxFleetSize){
		return evaluate(normalPriority(), maxFleetSize);
	}
	public int standardEvaluation(){
		return evaluate(normalPriority());
	}
	public int evaluate(ArrayList<Integer> priorities, int base){
		int value = 0;
		for(int i = 0; i < normalPriority().size(); i++){
			int type = normalPriority().get(i);
			value += numberKilled(type) * Math.pow(base, i);
		}
		return value;
	}
	public int evaluate(ArrayList<Integer> priorities){
		int value = 0;
		for(int i = 0; i < normalPriority().size(); i++){
			int type = normalPriority().get(i);
			value += numberKilled(type) * Math.pow(dba.size(), i);
		}
		return value;
	}
	public int numberKilled(int type){
		int total = 0;
		for(Ship s: dead){
			if(type == -1 || s.getType() == type){
				total++;
			}
		}
		return total;
	}
	public static ArrayList<Integer> normalPriority(){
		/*
		 * lists how much we want to kill each type. So generals (type 3) get an index of zero, meaning
		 * we don't want to kill them very much. Juggernauts (type 2) get an index of 3, meaning we REALLY
		 * want to kill them. -1 simply means "the number of things killed" and has the highest priority.
		 * In essence, that means it'll select killing a raider and a cruiser over killing a juggernaut.
		 */
		ArrayList<Integer> ali = new ArrayList<Integer>();
		ali.add(3);
		ali.add(0);
		ali.add(1);
		ali.add(2);
		ali.add(-1);
		return ali;
	}
}