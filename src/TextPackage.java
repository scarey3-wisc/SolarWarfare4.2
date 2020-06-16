import java.awt.Graphics;
import java.util.ArrayList;

public class TextPackage{
	private float fontValue;
	private String[] result;
	public TextPackage(String input, int width, int height, double delta, Graphics g, int minimum) {
		String[] test = bestSplit(input, minimum, g);
		String[] maxSplit = input.split(" ");
		float max = 0f;
		boolean insideBounds = true;
		while (insideBounds) {
			g.setFont(g.getFont().deriveFont(max));
			int resultantHeight = g.getFontMetrics().getHeight();
			int resultantWidth = maxResultantWidth(g, test);
			if (resultantHeight * test.length < height && resultantWidth < width) {
				max += delta;
			} else {
				if(test.length < maxSplit.length && (resultantHeight * (test.length + 1) < height)){
					String[] proposal = bestSplit(input, test.length + 1, g);
					if(maxResultantWidth(g, proposal) < width){
						test = proposal;
					}else{
						insideBounds = false;
						max -= delta;
					}
				}else{
					insideBounds = false;
					max -= delta;
				}
			}
		}
		fontValue = max;
		result = test;
	}
	public TextPackage(String input, int width, int height, double delta, Graphics g) {
		String[] test = new String[]{input};
		String[] maxSplit = input.split(" ");
		float max = 0f;
		boolean insideBounds = true;
		while (insideBounds) {
			g.setFont(g.getFont().deriveFont(max));
			int resultantHeight = g.getFontMetrics().getHeight();
			int resultantWidth = maxResultantWidth(g, test);
			if (resultantHeight * test.length < height && resultantWidth < width) {
				max += delta;
			} else {
				if(test.length < maxSplit.length && (resultantHeight * (test.length + 1) < height)){
					String[] proposal = bestSplit(input, test.length + 1, g);
					if(maxResultantWidth(g, proposal) < width){
						test = proposal;
					}else{
						insideBounds = false;
						max -= delta;
					}
				}else{
					insideBounds = false;
					max -= delta;
				}
			}
		}
		fontValue = max;
		result = test;
	}
	public TextPackage(String input, int width, int height, double delta, Graphics g, float smax) {
		String[] test = new String[]{input};
		String[] maxSplit = input.split(" ");
		float max = 0f;
		boolean insideBounds = true;
		while (insideBounds) {
			g.setFont(g.getFont().deriveFont(max));
			int resultantHeight = g.getFontMetrics().getHeight();
			int resultantWidth = maxResultantWidth(g, test);
			if (resultantHeight * test.length < height && resultantWidth < width) {
				max+=delta;
			} else {
				if(test.length < maxSplit.length && (resultantHeight * (test.length + 1) < height)){
					String[] proposal = bestSplit(input, test.length + 1, g);
					if(maxResultantWidth(g, proposal) < width){
						test = proposal;
					}else{
						insideBounds = false;
						max -= delta;
					}
				}else{
					insideBounds = false;
					max -= delta;
				}
			}
		}
		fontValue = max;
		result = test;
	}
	
	public float getFontValue() {
		return fontValue;
	}

	public String[] getResult() {
		return result;
	}
	public static float idealFont(String input, int width, int height, double delta, Graphics g){
		float max = 0f;
		boolean insideBounds = true;
		while (insideBounds) {
			g.setFont(g.getFont().deriveFont(max));
			int resultantHeight = g.getFontMetrics().getHeight();
			int resultantWidth = g.getFontMetrics().stringWidth(input);
			if (resultantHeight < height && resultantWidth < width) {
				max += delta;
			} else {
				insideBounds = false;
				max -= delta;
			}
		}
		return max;
	}
	public static String[] bestSplit(String s, float f, Graphics g, int width){
		g.setFont(g.getFont().deriveFont(f));
		ArrayList<String> finale = new ArrayList<String>();
		String[] splitBeta = s.split(" ");
		String addition = splitBeta[0];
		for(int i = 1; i < splitBeta.length; i++){
			String test = addition + " " + splitBeta[i];
			if(g.getFontMetrics().stringWidth(test) > width){
				finale.add(addition);
				addition = splitBeta[i];
			}else{
				addition = test;
			}
		}
		finale.add(addition);
		String[] done = new String[finale.size()];
		for(int i = 0; i < done.length; i++){
			done[i] = finale.get(i);
		}
		return done;
	}
	private String[] bestSplit(String s, int rows, Graphics g){
		String[] result = new String[rows];
		result[0] = s;
		for(int i = 1; i < result.length; i++){
			result[i] = "";
		}
		boolean changeMade = true;
		while(changeMade){
			changeMade = false;
			for(int i = 0; i < result.length - 1; i++){
				String[] comparison = result.clone();
				String[] at1 = comparison[i].split(" ");
				if(at1.length > 1){
					if(!comparison[i + 1].equals("")){
						comparison[i + 1] = at1[at1.length - 1] + " " + comparison[i + 1];
					}else{
						comparison[i + 1] = at1[at1.length - 1];
					}
					comparison[i] = "";
					for(int j = 0; j < at1.length - 1; j++){
						comparison[i] += at1[j];
						if(j != at1.length - 2){
							comparison[i] += " ";
						}
					}
					if(maxResultantWidth(g, comparison) < maxResultantWidth(g, result)){
						result = comparison;
						changeMade = true;
					}
				}
			}
		}
		return result;
	}
	private int maxResultantWidth(Graphics g, String[] test){
		int max = 0;
		for(String s: test){
			int result = g.getFontMetrics().stringWidth(s);
			if(result > max){
				max = result;
			}
		}
		return max;
	}
}