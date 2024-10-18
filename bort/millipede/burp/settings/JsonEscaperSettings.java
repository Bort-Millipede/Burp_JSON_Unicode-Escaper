package bort.millipede.burp.settings;

import java.util.stream.IntStream;

public class JsonEscaperSettings {
	//Singleton references
	private static JsonEscaperSettings instance;
	private static Object mutex = new Object();
	
	//"Unicode-Escape Custom Chars" settings fields
	private int[] charsToEscape;
	//private IntStream charsToEscape;
	//private byte charsInputFormat;
	
	//global settings fields
	private boolean fineTuneUnescaping;
	private boolean verboseLogging;
	
	//Constants
	public static final String KEY_CHARS = "\000\001\002\003\004\005\006\007\010\011\012\013\014\015\016\017\020\021\022\023\024\025\026\027\030\031\032\033\034\035\036\037\"\\";
	/*public static final byte CHARS_INPUT_FORMAT = 0;
	public static final byte DECIMAL_INPUT_FORMAT = 1;
	public static final byte HEXADECIMAL_INPUT_FORMAT = 2;*/
	public static final String CHARS_TO_ESCAPE_FORMAT_KEY = "JsonEscaper.charsToEscapeFormat";
	public static final String CHARS_TO_ESCAPE_KEY = "JsonEscaper.charsToEscape";
	public static final String INCLUDE_KEY_CHARS_KEY = "JsonEscaper.includeKeyChars";
	
	private JsonEscaperSettings() {
		//charsToEscape = IntStream.empty();
		charsToEscape = new int[0];
		//charsInputFormat = CHARS_INPUT_FORMAT;
		
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
	public void setCharsToEscape(String strCharsToEscape,boolean includeKeyChars) {
		if(includeKeyChars)
			strCharsToEscape = KEY_CHARS.concat(strCharsToEscape);
		charsToEscape = strCharsToEscape.chars().distinct().sorted().toArray();
	}
	
	/*public void setInputFormat(byte input) {
		charsInputFormat = input;
	}
	
	public void setCharsInputFormat() {
		charsInputFormat = CHARS_INPUT_FORMAT;
	}
	
	public void setDecimalInputFormat() {
		charsInputFormat = DECIMAL_INPUT_FORMAT;
	}
	
	public void setHexadecimalInputFormat() {
		charsInputFormat = HEXADECIMAL_INPUT_FORMAT;
	}*/
	
	public void setFineTuneUnescaping(boolean input) {
		fineTuneUnescaping = input;
	}
	
	public void setVerboseLogging(boolean input) {
		verboseLogging = input;
	}
	//END mutator methods
}
