import java.awt.image.BufferedImage;

public abstract class ButtonAction implements Change{
	public abstract BufferedImage getImage(int aw, int ah, int mx, int my);
	public abstract String getTooltipText();
	public abstract int getWidth(int aw, int ah);
	public abstract int getHeight(int aw, int ah);
	public abstract int[] cost();
}