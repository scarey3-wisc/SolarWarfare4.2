import java.util.ArrayList;

public interface Change{
	public void makeChange();
	public void undoChange();
	public boolean legalChange();
	public ArrayList<String> getDescription();
}