package bort.millipede.burp.ui;

import burp.api.montoya.core.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;

class EscaperUIHelpers {
	//text constants
	static final String EX_MESSAGE_HEAD = "For input string: \"";
	
	//Convert escape character list to array of Ranges
	static Range[] convertCharsToRanges(String inText) {
		if(inText==null) return new Range[0];
		if(inText.length()==0) return new Range[0];
		
		//Remove duplicate characters
		String uniqChars = "";
		for(int l=0;l<inText.length();l++) {
			String ch = String.valueOf(inText.charAt(l));
			if(!uniqChars.contains(ch))
				uniqChars += ch;
		}
		inText = uniqChars;
		
		//sort characters
		char[] inTextChars = inText.toCharArray();
		Arrays.sort(inTextChars);
		inText = new String(inTextChars);
		
		String[] inputArr = new String[inText.length()];
		int i=0;
		while(i<inputArr.length) {
			inputArr[i] = String.valueOf(inText.charAt(i));
			i++;
		}
		
		ArrayList<Range> outputList = new ArrayList<Range>();
		i=0;
		while(i<inputArr.length) {
			char curr = inputArr[i].charAt(0);
			int j=1;
			char next = curr;
			while((i+j)<inputArr.length) {
				next = inputArr[i+j].charAt(0);
				if(next-curr!=j) {
					next = inputArr[i+j-1].charAt(0);
					break;
				}
				j++;
			}
			
			if(next == curr) {
				outputList.add(Range.range((int) curr,(int) curr+1));
			} else {
				outputList.add(Range.range((int) curr,(int) next+1));
				i+=j-1;
			}
			i++;
		}
		
		Range[] outArr = new Range[outputList.size()];
		return outputList.toArray(outArr);
	}
	
	//Convert array of Ranges to range text
	static String convertRangesToText(Range[] inRanges) {
		if(inRanges==null) return "";
		if(inRanges.length==0) return "";
		
		ArrayList<String> outputList = new ArrayList<String>();
		for(int i=0;i<inRanges.length;i++) {
			if(inRanges[i] != null) {
				int start = inRanges[i].startIndexInclusive();
				int end = inRanges[i].endIndexExclusive()-1;
				if(start>end) { //may not even be necessary, but included for good measure
					start = inRanges[i].endIndexExclusive();
					end = inRanges[i].startIndexInclusive()-1;
				}
				
				String escaped = Integer.toHexString(start);
				while(escaped.length()<4) {
						escaped = "0".concat(escaped);
				}
				
				if(start==end) {
					outputList.add(escaped);
				} else {
					String nextEscaped = Integer.toHexString(end);
					while(nextEscaped.length()<4) {
						nextEscaped = "0".concat(nextEscaped);
					}
					
					outputList.add(String.format("%s-%s",escaped,nextEscaped));
				}
			}
		}
		
		return String.join(",",outputList.toArray(new String[outputList.size()]));
	}
	
	//convert ranges text to array of Ranges
	static Range[] convertRangesTextToRanges(String inText) throws UnsupportedEncodingException,NumberFormatException {
		if(inText == null) return new Range[0];
		if(inText.length() == 0) return new Range[0];
		
		ArrayList<Range> outputList = new ArrayList<Range>();
		inText = inText.strip();
		String[] inTextSplit = inText.split(",");
		String output = "";
		for(int i=0;i<inTextSplit.length;i++) {
			String hex = inTextSplit[i].strip();
			if(hex.length()!=0) {
				if(hex.contains("-")) {
					String[] range = hex.split("-",2);
					
					//hex beyond 16-bit range (4 digits) is unsupported:
					if(range[0].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[0]));
					} else if(range[1].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[1]));
					}
					
					int start = Integer.parseInt(range[0],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					int end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
						
					if(start>end) {
						start = end;
						end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					}
					
					outputList.add(Range.range((int) start,(int) end+1));
				} else {
					if(hex.length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,hex));
					}
					int start = Integer.parseInt(hex,16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					outputList.add(Range.range(start,start+1));
				}
			}
		}
		
		Range[] outArr = new Range[outputList.size()];
		return outputList.toArray(outArr);
	}
	
	//Convert array of Ranges to escape character list
	static String convertRangesToChars(String inText) throws UnsupportedEncodingException,NumberFormatException {
		if(inText==null) return null;
		if(inText.length()==0) return inText;
		
		inText = inText.strip();
		String[] inTextSplit = inText.split(",");
		String output = "";
		for(int i=0;i<inTextSplit.length;i++) {
			String hex = inTextSplit[i].strip();
			if(hex.length()!=0) {
				if(hex.contains("-")) {
					String[] range = hex.split("-",2);
					
					//hex beyond 16-bit range (4 digits) is unsupported:
					if(range[0].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[0]));
					} else if(range[1].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[1]));
					}
					
					int start = Integer.parseInt(range[0],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					int end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					if(start>end) {
						start = end;
						end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					}
					String[] outChars = new String[(end-start)+1];
					int j=0;
					while(start<=end) {
						outChars[j] = String.valueOf((char) start);
						start++;
						j++;
					}
					output = output.concat(String.join("",outChars));
				} else {
					if(hex.length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,hex));
					}
					output = output.concat(String.valueOf((char) Integer.parseInt(hex,16))); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
				}
			}
		}
		
		//remove duplicate characters
		String uniqChars = "";
		for(int l=0;l<output.length();l++) {
			String ch = String.valueOf(output.charAt(l));
			if(!uniqChars.contains(ch))
				uniqChars += ch;
		}
		output = uniqChars;
		
		return output;
	}
}
