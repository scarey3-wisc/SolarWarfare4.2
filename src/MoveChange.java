import java.util.ArrayList;

public class MoveChange implements Change{
	private ArrayList<Space> path;
	private ArrayList<Ship> moving;
	private int maxDistance;
	private boolean legal;
	private ArrayList<Double> previousTheta;
	public MoveChange(ArrayList<Ship> ships, Space start, Space finish){
		previousTheta = new ArrayList<Double>();
		moving = ships;
		legal = true;
		if(moving.size() == 0){
			legal = false;
		}
		if(start == finish){
			legal = false;
		}
		General owner = moving.get(0).getOwner();
		maxDistance = moving.get(0).maxMove();
		for(Ship s: moving){
			if(s.maxMove() < maxDistance){
				maxDistance = s.maxMove();
			}
			if(s.getOwner() != owner){
				legal = false;
			}
			previousTheta.add(s.getTheta());
		}
		if(!owner.isActive()){
			legal = false;
		}
		path = Map.getPath(start, finish, owner, maxDistance);
		if(path == null){
			legal = false;
		}
		if(path != null && path.size() - 1 > maxDistance){
			legal = false;
		}
	}
	public boolean noMovement(){
		return maxDistance == 0;
	}
	@Override
	public void makeChange() {
		int distance = path.size()-1;
		Space start = path.get(0);
		Space finis = path.get(path.size() - 1);
		Space semiFinis = path.get(path.size()-2);
		double theta = calculateTheta(semiFinis, finis);
		for(Ship s: moving){
			start.getFleet().remove(s);
			finis.getFleet().add(s);
			s.setLocation(finis);
			s.changeAmountMoved(distance);
			s.setTheta(theta);
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
	public boolean legalChange(){
		return legal;
	}
	@Override
	public void undoChange() {
		int distance = 1-path.size();
		Space finis = path.get(0);
		Space start = path.get(path.size() - 1);
		for(int i = 0; i < moving.size(); i++){
			Ship s = moving.get(i);
			start.getFleet().remove(s);
			finis.getFleet().add(s);
			s.setLocation(finis);
			s.changeAmountMoved(distance);	
			s.setTheta(previousTheta.get(i));
		}
	}

	@Override
	public ArrayList<String> getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
}