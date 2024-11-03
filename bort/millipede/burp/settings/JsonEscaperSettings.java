package bort.millipede.burp.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import burp.api.montoya.core.Range;

public class JsonEscaperSettings {
	//Singleton references
	private static JsonEscaperSettings instance;
	private static Object mutex = new Object(); //added for thread-safety: https://www.digitalocean.com/community/tutorials/thread-safety-in-java-singleton-classes
	
	//"Unicode-Escape Custom Chars" settings fields
	private int[] charsToEscape;
	
	//global settings fields
	private boolean fineTuneUnescaping;
	private boolean verboseLogging;
	
	//Constants
	public static final String KEY_CHARS = "\000\001\002\003\004\005\006\007\010\011\012\013\014\015\016\017\020\021\022\023\024\025\026\027\030\031\032\033\034\035\036\037\"\\";
	public static final int[] KEY_CHARS_INT = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,34,92};
	public static final String CHARS_TO_ESCAPE_FORMAT_KEY = "JsonEscaper.charsToEscapeFormat";
	public static final String CHARS_TO_ESCAPE_KEY = "JsonEscaper.charsToEscape";
	public static final String INCLUDE_KEY_CHARS_KEY = "JsonEscaper.includeKeyChars";
	
	private JsonEscaperSettings() {
		charsToEscape = KEY_CHARS_INT;
		
		fineTuneUnescaping = true;
		verboseLogging = false;
	}
	
	public static JsonEscaperSettings getInstance() {
		JsonEscaperSettings result = instance;
		if(instance == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new JsonEscaperSettings();
			}
		}
		return instance;
	}
	
	//START accessor methods
	public int[] getCharsToEscape() {
		return charsToEscape;
	}
	
	/*public byte getCharsInputFormat() {
		return charsInputFormat;
	}*/
	
	public boolean getFineTuneUnescaping() {
		return fineTuneUnescaping;
	}
	
	public boolean getVerboseLogging() {
		return verboseLogging;
	}
	//END accessor methods
	
	//START mutator methods
	public synchronized void setCharsToEscape(String strCharsToEscape,boolean includeKeyChars) {
		if(strCharsToEscape != null && strCharsToEscape.length() !=0) {
			if(includeKeyChars) {
				strCharsToEscape = KEY_CHARS.concat(strCharsToEscape);
			}
		} else {
			if(includeKeyChars) {
				strCharsToEscape = KEY_CHARS;
			} else {
				strCharsToEscape = "";
			}
		}
		
		if(strCharsToEscape.length()>0) {
			charsToEscape = strCharsToEscape.chars().distinct().sorted().toArray();
		} else {
			charsToEscape = new int[0];
		}
	}
	
	public synchronized void setCharsToEscape(Range[] ranges,boolean includeKeyChars) {
		if(ranges==null) {
			if(includeKeyChars) {
				charsToEscape = KEY_CHARS_INT;
			} else {
				charsToEscape = new int[0];
			}
			return;
		}
		if(ranges.length==0) {
			if(includeKeyChars) {
				charsToEscape = KEY_CHARS_INT;
			} else {
				charsToEscape = new int[0];
			}
			return;
		}
		
		int outArrLength = 0;
		
		int i=0;
		while(i<ranges.length) {
			if(ranges[i]!=null) {
				outArrLength += (ranges[i].endIndexExclusive()-ranges[i].startIndexInclusive());
			}
			i++;
		}
		if(includeKeyChars)
			outArrLength+=KEY_CHARS_INT.length;
		
		int[] outArr = new int[outArrLength];
		int outIndex = 0;
		i=0;
		while(i<ranges.length) {
			int[] rangeArr = IntStream.range(ranges[i].startIndexInclusive(),ranges[i].endIndexExclusive()).toArray();
			int j=0;
			while(j<rangeArr.length) {
				outArr[outIndex] = rangeArr[j];
				outIndex++;
				j++;
			}
			i++;
		}
		if(includeKeyChars) {
			i=0;
			while(i<KEY_CHARS_INT.length) {
				outArr[outIndex] = KEY_CHARS_INT[i];
				outIndex++;
				i++;
			}
		}
		
		charsToEscape = IntStream.of(outArr).distinct().sorted().toArray();
	}
	
	public void setFineTuneUnescaping(boolean input) {
		fineTuneUnescaping = input;
	}
	
	public void setVerboseLogging(boolean input) {
		verboseLogging = input;
	}
	//END mutator methods
}

