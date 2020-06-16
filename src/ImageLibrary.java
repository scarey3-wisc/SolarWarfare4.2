import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ImageLibrary{
	public static final String saveFilepath = "ImageData.txt";
	private ArrayList<ArrayList<NamedImage>> images;
	public ImageLibrary(int slots){
		images = new ArrayList<ArrayList<NamedImage>>();
		for(int i = 0; i < slots; i++){
			ArrayList<NamedImage> nova = new ArrayList<NamedImage>();
			images.add(nova);
		}
		ArrayList<String> read = Toolbox.load(saveFilepath);
		for(int i = 0; i < read.size(); i++){
			double progress = 1.0 * i / read.size();
			int percent = (int) (100 * progress);
			System.out.println("Loading Images: " + percent + "%");
			String s = read.get(i);
			String[] split = s.split("!");
			String name = split[0];
			int width = Integer.parseInt(split[1]);
			int height = Integer.parseInt(split[2]);
			NamedImage ni = findImage(name);
			ni.createFit(width, height);
		}
	}
	public void save(boolean encode){
		ArrayList<String> write = new ArrayList<String>();
		for(ArrayList<NamedImage> alni: images){
			for(NamedImage ni: alni){
				for(int i = 0; i < ni.getSizes().size(); i++){
					if(ni.getUsed().get(i)){
						String nova = ni.getName() + "!" + ni.getSizes().get(i).getWidth() + "!" + ni.getSizes().get(i).getHeight();
						write.add(nova);
					}
				}
			}
		}
		Toolbox.save(write, saveFilepath, encode);
	}
	public NamedImage getImage(String name){
		int index = hashtag(name);
		for(NamedImage ni: images.get(index)){
			if(ni.getName().equals(name)){
				return ni;
			}
		}
		return null;
	}
	private String getFilePath(String name){
		return "SW4_2Data/Images/" + name + ".png";
	}
	public static BufferedImage LoadImage(String filename){
		BufferedImage bill = null;
		try {
			bill = ImageIO.read(new File(filename));
		} catch (IOException e) {
			System.out.println(filename);
			e.printStackTrace();
		}
		return bill;
	}
	public NamedImage findImage(String name){
		int index = hashtag(name);
		NamedImage found = getImage(name);
		if(found == null){
			String path = getFilePath(name);
			found = new NamedImage(LoadImage(path), name);
			if(found != null){
				images.get(index).add(found);
			}
		}
		return found;
	}
	public int hashtag(String name){
		int askiiSum = 0;
		for(char c: name.toCharArray()){
			askiiSum += c;
		}
		return askiiSum%images.size();
	}
}