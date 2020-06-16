import java.util.ArrayList;
public class AttackChange implements Change{
	private boolean legal;
	private Space drt; //place defenders run to
	private Space ats; //this'll be both the place where attackers start and where they retreat to.
	private Space btg; //place of fighting
	private Space ogs; //this is where the ships started
	private int mDist; //this is the distance missile users travel
	private int fDist; //this is the distance everyone else travels. 
	/*
	 * Cool story about the way we're coding this - Buffed-Missile ships will count as missile ships for the
	 * purpose of calculating distance, but the intuition about a pre-emptive strike is that the ship sped
	 * into the midst of the fight, guns blazing, before anyone could react. So in terms of general skill,
	 * what does a pre-emptive strike mean? The general flies his ship at unbelievable speeds into the heart
	 * of the enemy fleet, blows up an unexpecting fool, reverses directions at 8 gs, flying safely out of
	 * range before anyone can shoot him, performs another skew flip which leaves him at rest in the same
	 * location as the rear guard of his fleet, and starts raining down missiles - all for less fuel than
	 * the people who simply flew forward.
	 */
	private ArrayList<Ship> ddA; //attackers who died
	private ArrayList<Ship> ddD; //defenders who died
	private ArrayList<Ship> mua; //attackers who use missiles
	private ArrayList<Ship> rtA; //attackers who retreat
	private ArrayList<Ship> rtD; //defenders who retreat
	private ArrayList<Ship> cqA; //indicates attackers who ended on the battleground
	private ArrayList<Ship> vtD; //indicates defenders who ended on the battleground
	private ArrayList<Ship> ogD; //indicates original defenders on the battleground;
	private ArrayList<Ship> ogA; //indicates original list of attackers moving the full distance
	private ArrayList<Double> ogAT; //indicates the original theta of all the attackers.
	private ArrayList<Double> ogDT; //indicates the original theta of all the defenders.
	private int backwardOpinion;
	private int forwardOpinion;
	private General owner;
	private General eg;
	public AttackChange(ArrayList<Ship> ships, Space source, Space target){
		ogs = source;
		btg = target;
		legal = true;
		if(ships.size() == 0){
			legal = false;
			return;
		}
		if(source == target){
			legal = false;
			return;
		}
		owner = ships.get(0).getOwner();
		int maxDistance = ships.get(0).maxMove();
		for(Ship s: ships){
			if(s.maxMove() < maxDistance){
				maxDistance = s.maxMove();
			}
			if(s.getOwner() != owner){
				legal = false;
				return;
			}
			if(s.isAttackedYet()){
				legal = false;
				return;
			}
		}
		if(!owner.isActive()){
			legal = false;
			return;
		}
		ArrayList<Space> path = Map.getPath(source, target, owner, maxDistance);
		if(path == null){
			legal = false;
			return;
		}
		fDist = path.size() - 1;
		mDist = path.size() - 1;
		if(path != null && (path.size() - 1 > maxDistance||path.size() < 2)){
			legal = false;
			return;
		}
		/*
		 * thus far, all we've done is movement setup: we've saved our attacker's theta, found their path,
		 * and confirmed that they can fly that path, as well as attack. We've also set up the return-to thetas.
		 */
		
		if(btg.getFleet().size() == 0){
			legal = false;
			return;
		}
		Ship ezero = btg.getFleet().get(0);
		eg = ezero.getOwner();
		for(Ship s: btg.getFleet()){
			if(s.getOwner() == owner){
				legal = false;
				return;
			}
			if(s.getOwner() != eg){
				legal = false;
				return;
			}
		}
		/*
		 * Here we check the battleground to make sure everything is okay - there are foreigners, and they're
		 * all under the same banner, etc.
		 */
		
		forwardOpinion = owner.lookupRelation(eg);
		backwardOpinion = eg.lookupRelation(owner);
		/*
		 * We also set up the relationship changing
		 */
		
		ats = path.get(path.size() - 2);
		drt = null;
		if(allAllied(btg.getOpposite(ats), eg)){
			drt = btg.getOpposite(ats);
		}else{
			for(Space s: btg.getTriangle(ats)){
				if(allAllied(s, eg)){
					drt = s;
				}
			}
			if(drt == null){
				for(Space s: btg.getFullSide(ats)){
					if(allAllied(s, eg)){
						drt = s;
					}
				}
			}
		}
		
		/*
		 * now, we're finding the retreat points. For the attackers, their retreat point is just the semifinal
		 * location on the path. That'll also be where we put the missile users, after the space - but we'll
		 * do different things with their thetas. For the defenders, we'll first check the space opposite
		 * the attack retreat points, then the triangles, and then finally the adjacency points.
		 */
		
		ogA = new ArrayList<Ship>();
		ArrayList<Ship> activeAttackers = new ArrayList<Ship>();
		ogAT = new ArrayList<Double>();
		for(Ship s: ships){
			ogA.add(s);
			ogAT.add(s.getTheta());
			activeAttackers.add(s);
		}
		ogD = new ArrayList<Ship>();
		ArrayList<Ship> activeDefenders = new ArrayList<Ship>();
		ogDT = new ArrayList<Double>();
		for(Ship s: btg.getFleet()){
			ogD.add(s);
			ogDT.add(s.getTheta());
			activeDefenders.add(s);
		}
		/*
		 * Now we start worrying about the ships themselves. First, we create our ogA and ogD lists; these are
		 * the things we'll reset to when undoing stuff. Note that we already went through the ogA list to check
		 * on movement, but we're going through it again here for clarity rather than conciseness. We also create
		 * "Active" attacker and defender lists; these will be used as we run through the actual battle, creating
		 * our dead and retreated lists. They'll start the same as ogA and ogD, but will quickly be modified.
		 */
		ddD = new ArrayList<Ship>();
		ArrayList<Ship> buffedAttackers = new ArrayList<Ship>();
		for(Ship s: activeAttackers){
			if(s.isBuffed()){
				buffedAttackers.add(s);
			}
		}
		if(buffedAttackers.size() > 0){
			AttackProposal preemptive = AttackProposal.Optimize(buffedAttackers, activeDefenders);
			for(Ship s: preemptive.getKilled()){
				ddD.add(s);
				activeDefenders.remove(s);
			}
		}
		/*
		 * With all the setup accomplished, we start the actual battle calculations. First, we separate the buffed
		 * ships out from the attackers, and have them run a pre-emptive strike round on the enemies. We look at
		 * the dead defenders, add them to the dead list, and remove them from the active list. We don't need to
		 * worry about thetas for now.
		 */
		
		if(activeDefenders.size() == 0){
			mua = new ArrayList<Ship>();
			ddA = new ArrayList<Ship>();
			rtA = new ArrayList<Ship>();
			rtD = new ArrayList<Ship>();
			cqA = activeAttackers;
			vtD = new ArrayList<Ship>();
			return;
		}
		/*
		 * next, we check if the battle was simply over after the buff strike.
		 */
		
		mua = new ArrayList<Ship>();
		ddA = new ArrayList<Ship>();
		AttackProposal attack = AttackProposal.Optimize(activeAttackers, activeDefenders);
		for(int i = 0; i < activeAttackers.size(); i++){
			Ship s = activeAttackers.get(i);
			if(s.isMissileActive()){
				activeAttackers.remove(i);
				i--;
				mua.add(s);
			}
		}
		if(activeAttackers.size() > 0){
			AttackProposal defend = AttackProposal.Optimize(activeDefenders, activeAttackers);
			for(Ship s: attack.getKilled()){
				ddD.add(s);
				activeDefenders.remove(s);
			}
			for(Ship s: defend.getKilled()){
				ddA.add(s);
				activeAttackers.remove(s);
			}
		}else{
			for(Ship s: attack.getKilled()){
				ddD.add(s);
				activeDefenders.remove(s);
			}
		}
		
		/*
		 * Next we do round one. Here's how we are going to do it: First, we have everyone in the attackers shoot
		 * at the enemies (using an attack proposal) and save the list of people who will die. Then, we remove
		 * the missile users from active attackers and put them in MUA. Finally, we have the defenders throw an
		 * attack proposal at the remaining active attackers, and save the list of people who will die. Then we
		 * remove both lists of dead guys from the active list, and add them to the dead lists.
		 */
		
		if(activeAttackers.size() == 0 || activeDefenders.size() == 0){
			rtA = new ArrayList<Ship>();
			rtD = new ArrayList<Ship>();
			if(activeAttackers.size() == 0){
				vtD = activeDefenders;
				cqA = new ArrayList<Ship>();
			}
			if(activeDefenders.size() == 0){
				cqA = activeAttackers;
				vtD = new ArrayList<Ship>();
			}
			return;
		}
		/*
		 * Again, we check to see if we're just done.
		 */
		
		AttackProposal aAttack = AttackProposal.Optimize(activeAttackers, activeDefenders);
		AttackProposal dAttack = AttackProposal.Optimize(activeDefenders, activeAttackers);
		int maxSize = activeAttackers.size();
		if(activeDefenders.size() > maxSize){
			maxSize = activeDefenders.size();
		}
		boolean living = true;
		while(aAttack.comparativeEvaluation(maxSize) == dAttack.comparativeEvaluation(maxSize) && living && (aAttack.standardEvaluation() > 0 || dAttack.standardEvaluation() > 0)){
			for(Ship s: aAttack.getKilled()){
				ddD.add(s);
				activeDefenders.remove(s);
			}
			for(Ship s: dAttack.getKilled()){
				ddA.add(s);
				activeAttackers.remove(s);
			}
			if(activeAttackers.size() == 0 || activeDefenders.size() == 0){
				living = false;
			}else{
				aAttack = AttackProposal.Optimize(activeAttackers, activeDefenders);
				dAttack = AttackProposal.Optimize(activeDefenders, activeAttackers);
				maxSize = activeAttackers.size();
				if(activeDefenders.size() > maxSize){
					maxSize = activeDefenders.size();
				}
			}
		}
		
		/*
		 * Now we get to the fun part - we're going to have attack proposals for both sides, things
		 * that they would do on the next round. We'll loop through, checking whether each side wants
		 * the fight. Basically, if one of them is more enthusiastic than the other, the other will
		 * retreat rather than committing to another round. But I expect their heuristics to often
		 * be identical, so retreating won't happen in that case. Once we've confirmed that the loop
		 * is going to happen again, we'll kill the dead, and then check if people survived the
		 * round. If they didn't, we'll break the loop, if they did, we'll generate new attack
		 * proposals.
		 */
		
		if(!living){
			if(activeAttackers.size() == 0){
				vtD = activeDefenders;
				cqA = new ArrayList<Ship>();
			}
			if(activeDefenders.size() == 0){
				cqA = activeAttackers;
				vtD = new ArrayList<Ship>();
			}
		}else{
			if(aAttack.comparativeEvaluation(maxSize) > dAttack.comparativeEvaluation(maxSize)){
				cqA = activeAttackers;
				vtD = new ArrayList<Ship>();
				rtD = activeDefenders;
				rtA = new ArrayList<Ship>();
			}else if(aAttack.comparativeEvaluation(maxSize) < dAttack.comparativeEvaluation(maxSize)){
				cqA = new ArrayList<Ship>();
				vtD = activeDefenders;
				rtD = new ArrayList<Ship>();
				rtA = activeAttackers;
			}else if(aAttack.standardEvaluation() == 0){
				cqA = new ArrayList<Ship>();
				vtD = new ArrayList<Ship>();
				rtD = activeDefenders;
				rtA = activeAttackers;
			}else{
				System.out.println("THEY SHOULD HAVE KEPT FIGHTING IF THEY WANTED TO AND COULD.");
				Toolbox.breakThings();
			}
		}
		
		/*
		 * At this point, we're out of the loop and ready for cleanup. The first thing we do is see
		 * whether it was a retreat or whether someone died. If someone died, we make the other guy
		 * a winner and put him in the victory list. If someone retreated, we need to check whether
		 * that is because:
		 * 1) Someone gave up; put them in the retreat lists, the other guys in the conquer lists.
		 * 2) Both gave up, which means the evaluations were zero, neither conquer, both retreat
		 */
	}
	private boolean allAllied(Space s, General g){
		for(Ship ss: s.getFleet()){
			if(ss.getOwner() != g){
				return false;
			}
		}
		return true;
	}
	@Override
	public void makeChange() {
		for(Ship s: ogD){
			btg.removeShip(s);
		}
		for(Ship s: ogA){
			ogs.removeShip(s);
		}
		for(Ship s: ddA){
			//System.out.println("Requiescat in Pacem, " + s.getOwner().getNameFor(s.getType()) + " belonging to " + s.getOwner().getFormalName());
			s.setLocation(null);
		}
		for(Ship s: ddD){
			//System.out.println("Requiescat in Pacem, " + s.getOwner().getNameFor(s.getType()) + " belonging to " + s.getOwner().getFormalName());
			s.setLocation(null);
		}
		for(Ship s: mua){
			ats.addShip(s);
			s.setLocation(ats);
			s.changeAmountMoved(mDist);
			s.setTheta(calculateTheta(ats, btg));
			s.setActiveMissile(false);
			s.setAttackedYet(true);
		}
		for(Ship s: rtA){
			ats.addShip(s);
			s.setLocation(ats);
			s.changeAmountMoved(fDist);
			s.setTheta(calculateTheta(btg, ats));
			s.setAttackedYet(true);
		}
		for(Ship s: rtD){
			if(drt != null){
				drt.addShip(s);
				s.setLocation(drt);
				s.setTheta(calculateTheta(btg, drt));
			}
		}
		for(Ship s: cqA){
			btg.addShip(s);
			s.setLocation(btg);
			s.changeAmountMoved(fDist);
			s.setTheta(calculateTheta(ats, btg));
			s.setAttackedYet(true);
		}
		for(Ship s: vtD){
			btg.addShip(s);
			s.setLocation(btg);
			s.setTheta(calculateTheta(btg, ats));
		}
		owner.setRelation(eg, -1);
		eg.setRelation(owner, -1);
	}

	@Override
	public void undoChange() {
		eg.setRelation(owner, backwardOpinion);
		owner.setRelation(eg, forwardOpinion);
		for(Ship s: vtD){
			btg.removeShip(s);
		}
		for(Ship s: cqA){
			btg.removeShip(s);
			s.changeAmountMoved(fDist * -1);
			s.setAttackedYet(false);
		}
		for(Ship s: rtD){
			if(drt != null){
				drt.removeShip(s);
			}
		}
		for(Ship s: rtA){
			ats.removeShip(s);
			s.changeAmountMoved(fDist * -1);
			s.setAttackedYet(false);
		}
		for(Ship s: mua){
			ats.removeShip(s);
			s.changeAmountMoved(mDist * -1);
			s.setActiveMissile(true);
			s.setAttackedYet(false);
		}
		/*for(Ship s: ddA){
			System.out.println("Welcome back, " + s.getOwner().getNameFor(s.getType()) + " belonging to " + s.getOwner().getFormalName());
		}
		for(Ship s: ddD){
			System.out.println("Welcome, " + s.getOwner().getNameFor(s.getType()) + " belonging to " + s.getOwner().getFormalName());
		}*/
		for(int i = 0; i < ogA.size(); i++){
			Ship s = ogA.get(i);
			ogs.addShip(s);
			s.setLocation(ogs);
			s.setTheta(ogAT.get(i));
		}
		for(int i = 0; i < ogD.size(); i++){
			Ship s = ogD.get(i);
			btg.addShip(s);
			s.setLocation(btg);
			s.setTheta(ogDT.get(i));
		}
	}
	private double calculateTheta(Space one, Space two){
		if(one.getR() == two){
			return Math.PI;
		}else if(one.getUR() == two){
			return 4*Math.PI/3;
		}else if(one.getUL() == two){
			return 5*Math.PI/3;
		}else if(one.getL() == two){
			return 0;
		}else if(one.getDL() == two){
			return 1 * Math.PI/3;
		}else if(one.getDR() == two){
			return 2 * Math.PI/3;
		}else{
			Toolbox.breakThings();
			return 0;
		}
	}
	@Override
	public boolean legalChange() {
		return legal;
	}

	@Override
	public ArrayList<String> getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}