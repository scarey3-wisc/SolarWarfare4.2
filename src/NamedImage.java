import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NamedImage{
	private String name;
	private BufferedImage image;
	private ArrayList<BufferedImage> sizes;
	private ArrayList<Boolean> used;
	public NamedImage(BufferedImage ii, String name) {
		this.name = name;
		this.image = ii;
		sizes = new ArrayList<BufferedImage>();
		used = new ArrayList<Boolean>();
	}
	public BufferedImage getFit(int width, int height){
		BufferedImage found = null;
		for(int i = 0; i < sizes.size(); i++){
			BufferedImage bi = sizes.get(i);
			if(tighestFit(bi, width, height)){
				found = bi;
				used.set(i, true);
				break;
			}
		}
		if(found == null){
			found = generateTighestFit(width, height, true);
		}
		return found;
	}
	private BufferedImage toCompatibleImage(BufferedImage image)
	{
	    // obtain the current system graphical settings
	    GraphicsConfiguration gfx_config = GraphicsEnvironment.
	        getLocalGraphicsEnvironment().getDefaultScreenDevice().
	        getDefaultConfiguration();

	    /*
	     * if image is already compatible and optimized for current system 
	     * settings, simply return it
	     */
	    if (image.getColorModel().equals(gfx_config.getColorModel()))
	        return image;

	    // image is not optimized, so create a new image that is
	    BufferedImage new_image = gfx_config.createCompatibleImage(
	            image.getWidth(), image.getHeight(), image.getTransparency());

	    // get the graphics context of the new image to draw the old image on
	    Graphics2D g2d = (Graphics2D) new_image.getGraphics();

	    // actually draw the image and dispose of context no longer needed
	    g2d.drawImage(image, 0, 0, null);
	    g2d.dispose();

	    // return the new optimized image
	    return new_image; 
	}
	public void createFit(int width, int height){
		generateTighestFit(width, height, false);
	}
	private BufferedImage generateTighestFit(int availableWidth, int availableHeight, boolean defaultUsed){
		double horizontalRatio = 1.0 * availableWidth / image.getWidth();
		double verticalRatio = 1.0 * availableHeight / image.getHeight();
		int resultantHeight = (int) (image.getHeight() * horizontalRatio);
		int resultantWidth = (int) (image.getWidth() * verticalRatio);
		if(availableWidth == -1){
			availableWidth = resultantWidth + 1;
			horizontalRatio = 1.0 * availableWidth / image.getWidth();
			resultantHeight = (int) (image.getHeight() * horizontalRatio);
		}
		if(availableHeight == -1){
			availableHeight = resultantHeight + 1;
			verticalRatio = 1.0 * availableHeight / image.getHeight();
			resultantWidth = (int) (image.getWidth() * verticalRatio);
		}
		int finalHeight = image.getHeight();
		int finalWidth = image.getWidth();
		if((resultantHeight <= availableHeight)){
			finalHeight = (int) (horizontalRatio * image.getHeight());
			finalWidth = (int) (horizontalRatio * image.getWidth());
		}else if(resultantWidth <= availableWidth){
			finalHeight = (int) (verticalRatio * image.getHeight());
			finalWidth = (int) (verticalRatio * image.getWidth());
		}else{
			System.out.println("CRITICAL ERROR in NamedImage.generateTightestFit");
			System.out.println("printint and/or debugger.");
			System.out.println("Here is some helpful information:");
			System.out.println("Available Dimensions: " + availableWidth + ", " + availableHeight);
			System.out.println("Image Dimensions: " + image.getWidth() + ", " + image.getHeight());
			System.out.println("Ratios: " + horizontalRatio + ", " + verticalRatio);
			System.out.println("Resultant Dimensions: " + resultantWidth + ", " + resultantHeight);
			Toolbox.breakThings();
		}
		if(finalHeight < 1){
			finalHeight = 1;
		}
		if(finalWidth < 1){
			finalWidth = 1;
		}
		Image temporary = image.getScaledInstance(finalWidth, finalHeight, BufferedImage.SCALE_SMOOTH);
		BufferedImage nova = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_4BYTE_ABGR);
		nova.getGraphics().drawImage(temporary, 0, 0, null);
		nova = toCompatibleImage(nova);
		boolean existing = false;
		for(BufferedImage bi: sizes){
			if(bi.getWidth() == nova.getWidth() && bi.getHeight() == nova.getHeight()){
				existing = true;
			}
		}
		if(!existing){
			sizes.add(nova);
			used.add(defaultUsed);
		}
		return nova;
	}
	private boolean tighestFit(BufferedImage bi, int maxWidth, int maxHeight){
		if(bi == null){
			return false;
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		if(width == maxWidth && (height <= maxHeight || maxHeight == -1)){
			return true;
		}
		if(height == maxHeight && (width <= maxWidth || maxWidth == -1)){
			return true;
		}
		return false;
	}
	public BufferedImage getImage(){
		return image;
	}
	public String getName(){
		return name;
	}
	public ArrayList<BufferedImage> getSizes() {
		return sizes;
	}
	public void setSizes(ArrayList<BufferedImage> sizes) {
		this.sizes = sizes;
	}
	public ArrayList<Boolean> getUsed() {
		return used;
	}
	public void setUsed(ArrayList<Boolean> used) {
		this.used = used;
	}
}