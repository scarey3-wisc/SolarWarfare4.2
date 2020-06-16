import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Toolbox{
	private static boolean inUse;
	public static void delay(){
		while(inUse){
			sleep(10);
		}
	}
	public static void release(){
		inUse = false;
	}
	public static void lock(){
		inUse = true;
	}
	public static void breakThings(){
		int i = 1/0;
		i = i + 1;
	}
	public static void sleep(long amount){
		try{
			Thread.sleep(amount);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static boolean even(int i){
		return i%2 == 0;
	}
	public static void save(ArrayList<String> info, String filepath, boolean encode){
		if(encode){
			saveAndEncode(info, filepath);
		}else{
			save(info, filepath);
		}
	}
	private static void saveAndEncode(ArrayList<String> info, String filepath){
		String code = encode(info);
		ArrayList<String> codeCont = new ArrayList<String>();
		codeCont.add("ENCODED:4L7D");
		codeCont.add(code);
		save(codeCont, filepath);
	}
	private static void save(ArrayList<String> info, String filepath){
		try{
			BufferedWriter w = new BufferedWriter(new FileWriter(filepath));
			for(int i = 0; i < info.size(); i++){
				w.write(info.get(i));
				if(i != info.size() - 1){
					w.newLine();
				}
			}
			w.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public static ArrayList<ArrayList<String>> loadSplit(String filepath){
		ArrayList<String> loaded = load(filepath);
		if(loaded.size() == 0){
			return null;
		}
		ArrayList<ArrayList<String>> alas = new ArrayList<ArrayList<String>>();
		ArrayList<String> temp = new ArrayList<String>();
		for(String s: loaded){
			if(s.equals("*")){
				alas.add(temp);
				temp = new ArrayList<String>();
			}else{
				temp.add(s);
			}
		}
		alas.add(temp);
		return alas;
	}
	public static ArrayList<String> load(String filepath){
		return readAndDecode(filepath);
	}
	public static ArrayList<String> readAndDecode(String filepath){
		ArrayList<String> input = read(filepath);
		if(input.size() == 0){
			return input;
		}else{
			if(input.get(0).equals("ENCODED:4L7D")){
				input.remove(0);
				if(input.size() == 0){
					return new ArrayList<String>();
				}else{
					return decode(input.get(0));
				}
			}else{
				return input;
			}
		}
	}
	public static ArrayList<String> read(String filepath){
		ArrayList<String> strings = new ArrayList<String>();
		try {
			BufferedReader r = new BufferedReader(new FileReader(filepath));
			while(r.ready()){
				String s = r.readLine();
				if(!s.startsWith("//")){
					strings.add(s);
				}
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strings;
	}
	public static ArrayList<String> decode(String secret){
		ArrayList<String> blocks = split(secret, 8);
		String combo = "";
		for(String s: blocks){
			int convert = Integer.parseInt(s);
			String re = reverseEngineer(convert);
			combo += re;
		}
		ArrayList<String> decode = split(combo, "&");
		ArrayList<String> finishedProgress = new ArrayList<String>();
		for(String s: decode){
			if(!s.startsWith("//")){
				finishedProgress.add(s);
			}
		}
		return finishedProgress;
	}
	public static void decodeFile(String filepath){
		save(readAndDecode(filepath), filepath);
	}
	public static void encodeFile(String filepath){
		saveAndEncode(readAndDecode(filepath), filepath);
	}
	public static String encode(ArrayList<String> list){
		String s = combine(list, "&");
		ArrayList<String> blocks = split(s, 3);
		String finale = "";
		for(String b: blocks){
			finale += convertInt(calculateValue(b), 8);
		}
		return finale;
	}
	public static double distance(double X0, double Y0, double X1, double Y1){
		return Math.sqrt(Math.pow(X1 - X0, 2) + Math.pow(Y1 - Y0, 2));
	}
	public static double closestMod(double i, double j){
		if(i <= 0){
			return positiveMod(i, j);
		}else{
			return negativeMod(i, j);
		}
	}
	public static double negativeMod(double i, double j){
		return ((i%j) - j)%j;
	}
	public static double positiveMod(double i, double j){
		return ((i%j) + j)%j;
	}
	public static double versatileMod(double input, double lower, double length){
		double mod = input%length;
		while(mod < lower || mod > lower + length){
			if(mod < lower){
				mod += length;
			}else if(mod > lower + length){
				mod -= length;
			}
		}
		return mod;
	}
	public static int roundDown(double t){
		if(t > 0){
			return (int) (t);
		}else if(t == (int) t){
			return (int) t;
		}else{
			return (int) (t - 1);
		}
	}
	private static String convertInt(int input, int dMagnitude){
		//For 3 characters per block, and the full askii table, the greatest number (255 + 255 * 256 + 255 * 256 * 256) = 16777215
		//this number has a magnitude of 8.
		int fMagnitude = getMagnitude(input);
		String bob = Integer.toString(input);
		for(int i = 0; i < dMagnitude - fMagnitude; i++){
			bob = "0" + bob;
		}
		return bob;
	}
	private static int getMagnitude(int input){
		if(input == 0){
			return 1;
		}
		boolean surrender = false;
		int suggestedMagnitude = 1;
		while(!surrender){
			int biggest = (int) Math.pow(10, suggestedMagnitude);
			if(input < biggest && (input * 10) > suggestedMagnitude){
				return suggestedMagnitude;
			}else{
				suggestedMagnitude ++;
			}
			if(suggestedMagnitude > 10){
				surrender = true;
			}
		}
		return -1;
	}
	private static String reverseEngineer(int input){
		LinkedList<Integer> allDigits = new LinkedList<Integer>();
		while(input > 0){
			int mod = input%256;
			input/=256;
			allDigits.add(mod);
		}
		char[] split = new char[allDigits.size()];
		for(int i = 0; i < allDigits.size(); i++){
			int inte = allDigits.get(i);
			char b = (char) inte;
			split[i] = b;
		}
		return new String(split);
	}
	private static int calculateValue(String s){
		int total = 0;
		char[] splitt = s.toCharArray();
		for(int i = 0; i < splitt.length; i++){
			total += splitt[i] * Math.pow(256, i);
		}
		return total;
	}
	private static ArrayList<String> split(String string, int amount){
		ArrayList<String> strings = new ArrayList<String>();
		while(string.length() > amount){
			String one = string.substring(0, amount);
			strings.add(one);
			string = string.substring(amount);
		}
		if(string.length() > 0){
			strings.add(string);
		}
		return strings;
	}
	private static ArrayList<String> split(String strings, String deliniator){
		String[] split = strings.split(deliniator);
		ArrayList<String> newStrings = new ArrayList<String>();
		for(int i = 0; i < split.length; i++){
			newStrings.add(split[i]);
		}
		return newStrings;
	}
	private static String combine(ArrayList<String> strings, String deliniator){
		String fine = "";
		for(int i = 0; i < strings.size(); i++){
			fine += strings.get(i);
			if(i != strings.size() - 1){
				fine += deliniator;
			}
		}
		return fine;
	}
}