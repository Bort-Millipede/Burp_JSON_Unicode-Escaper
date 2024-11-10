package bort.millipede.burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.Extension;
import burp.api.montoya.intruder.Intruder;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.logging.Logging;

import bort.millipede.burp.payloadprocessing.UnescapePayloadProcessor;
import bort.millipede.burp.payloadprocessing.EscapeKeyCharsPayloadProcessor;
import bort.millipede.burp.payloadprocessing.UnicodeEscapeKeyCharsPayloadProcessor;
import bort.millipede.burp.payloadprocessing.UnicodeEscapeAllCharsPayloadProcessor;
import bort.millipede.burp.payloadprocessing.UnicodeEscapePayloadProcessor;
import bort.millipede.burp.ui.JsonEscaperTab;
import bort.millipede.burp.ui.EscaperMenuItemListener;
import bort.millipede.burp.settings.JsonEscaperSettings;

import java.util.List;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.json.JSONException;

public class JsonEscaper implements BurpExtension,ContextMenuItemsProvider {
	//BurpExtension variables
	private MontoyaApi mApi;
	private Extension bExtension;
	private Intruder bIntruder;
	private UserInterface bUI;
	
	//static BurpExtension variables;
	private static Logging mLogging;
	
	//ContextMenuItems
	private JMenuItem unescapeMenuItem;
	private JMenuItem escapeKeyMenuItem;
	private JMenuItem unicodeEscapeKeyMenuItem;
	private JMenuItem unicodeEscapeAllMenuItem;
	private JMenuItem unicodeEscapeMenuItem;
	private JMenuItem sendToManualTabItem;
	
	//Settings
	private JsonEscaperSettings settings;
	
	//Custom tab
	private JsonEscaperTab escaperTab;
	
	//START constants
	//general
	public static final String EXTENSION_NAME = "JSON Unicode-Escaper";
	public static final String EXTENSION_VERSION = "0.1";
	//ContextMenuItems labels
	public static final String UNESCAPE_LABEL = "JSON-unescape";
	public static final String ESCAPE_KEY_LABEL = "JSON-escape key chars";
	public static final String UNICODE_ESCAPE_KEY_LABEL = "JSON Unicode-escape key chars";
	public static final String UNICODE_ESCAPE_ALL_LABEL = "JSON Unicode-escape all chars";
	public static final String UNICODE_ESCAPE_CUSTOM_LABEL = "JSON Unicode-escape custom chars";
	public static final String SEND_TO_MANUAL_TAB = "Send to Manual Escaper/Unescaper";
	//JSON processing constants
	public static final String INLINE_JSON_KEY = "input";
	//END constants
	
	@Override
	public void initialize(MontoyaApi api) {
		mApi = api;
		bExtension = mApi.extension();
		bExtension.setName(EXTENSION_NAME);
		
		bIntruder = mApi.intruder();
		bIntruder.registerPayloadProcessor(new UnescapePayloadProcessor());
		bIntruder.registerPayloadProcessor(new EscapeKeyCharsPayloadProcessor());
		bIntruder.registerPayloadProcessor(new UnicodeEscapeKeyCharsPayloadProcessor());
		bIntruder.registerPayloadProcessor(new UnicodeEscapeAllCharsPayloadProcessor());
		bIntruder.registerPayloadProcessor(new UnicodeEscapePayloadProcessor());
		
		bUI = mApi.userInterface();
		
		bUI.registerContextMenuItemsProvider(this);
		unescapeMenuItem = new JMenuItem(UNESCAPE_LABEL);
		escapeKeyMenuItem = new JMenuItem(ESCAPE_KEY_LABEL);
		unicodeEscapeKeyMenuItem = new JMenuItem(UNICODE_ESCAPE_KEY_LABEL);
		unicodeEscapeAllMenuItem = new JMenuItem(UNICODE_ESCAPE_ALL_LABEL);
		unicodeEscapeMenuItem = new JMenuItem(UNICODE_ESCAPE_CUSTOM_LABEL);
		sendToManualTabItem = new JMenuItem(SEND_TO_MANUAL_TAB);
		
		settings = JsonEscaperSettings.getInstance();
		
		escaperTab = new JsonEscaperTab(mApi);
		bUI.applyThemeToComponent(escaperTab);
		bUI.registerSuiteTab(EXTENSION_NAME,escaperTab);
		mLogging = mApi.logging();
		mLogging.logToOutput(String.format("%s v%s initialized.",EXTENSION_NAME,EXTENSION_VERSION));
	}
	
	@Override
	public List<Component> provideMenuItems(ContextMenuEvent event) {

		if(event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST,InvocationType.MESSAGE_EDITOR_RESPONSE,InvocationType.MESSAGE_VIEWER_REQUEST,InvocationType.MESSAGE_VIEWER_RESPONSE)) {
			//Remove previous ActionListeners containing old event data
			ActionListener[] listeners = unescapeMenuItem.getActionListeners();
			int i=0;
			while(i<listeners.length) {
				unescapeMenuItem.removeActionListener(listeners[i]);
				i++;
			}
			listeners = escapeKeyMenuItem.getActionListeners();
			i=0;
			while(i<listeners.length) {
				escapeKeyMenuItem.removeActionListener(listeners[i]);
				i++;
			}
			listeners = unicodeEscapeKeyMenuItem.getActionListeners();
			i=0;
			while(i<listeners.length) {
				unicodeEscapeKeyMenuItem.removeActionListener(listeners[i]);
				i++;
			}
			listeners = unicodeEscapeAllMenuItem.getActionListeners();
			i=0;
			while(i<listeners.length) {
				unicodeEscapeAllMenuItem.removeActionListener(listeners[i]);
				i++;
			}
			listeners = unicodeEscapeMenuItem.getActionListeners();
			i=0;
			while(i<listeners.length) {
				unicodeEscapeMenuItem.removeActionListener(listeners[i]);
				i++;
			}
			listeners = sendToManualTabItem.getActionListeners();
			i=0;
			while(i<listeners.length) {
				sendToManualTabItem.removeActionListener(listeners[i]);
				i++;
			}
			
			//Add listener for in-place escape/unescape menu items
			EscaperMenuItemListener listener = new EscaperMenuItemListener(mApi,event);
			unescapeMenuItem.addActionListener(listener);
			escapeKeyMenuItem.addActionListener(listener);
			unicodeEscapeKeyMenuItem.addActionListener(listener);
			unicodeEscapeAllMenuItem.addActionListener(listener);
			unicodeEscapeMenuItem.addActionListener(listener);
			
			//Add listener for send to manual escaper/unescaper menu item
			EscaperMenuItemListener sendToManualListener = new EscaperMenuItemListener(mApi,event,escaperTab);
			sendToManualTabItem.addActionListener(sendToManualListener);
			
			JMenu manualSubMenu = new JMenu("Manual Escaper/Unescaper");
			manualSubMenu.add(sendToManualTabItem);
			
			return List.of(unescapeMenuItem,escapeKeyMenuItem,unicodeEscapeKeyMenuItem,unicodeEscapeAllMenuItem,unicodeEscapeMenuItem,new JSeparator(),manualSubMenu);
		}
		return List.of();
	}
	
	//START Unescaper/Escaper methods
	//un-JSON-escape all characters
	public static String unescapeAllChars(String input) throws JSONException {
		if(input==null) return null;
		if(input.length()==0) return input;
		if(!input.contains("\\")) return input; //do not process input not containing \ characters (implying no escaping in input)
		
		//If "Fine-tune unescaping to avoid errors" enabled in Settings menu: Attempt to prevent errors when processing text that is not actually escaped.
		String sanitizedInput = input;
		if(JsonEscaperSettings.getInstance().getFineTuneUnescaping()) {
			if(sanitizedInput.contains("\n")) sanitizedInput = sanitizedInput.replace("\n","\\u000a"); //escape raw newline characters if present
			if(sanitizedInput.contains("\r")) sanitizedInput = sanitizedInput.replace("\r","\\u000d"); //escape raw newline characters if present
			if(sanitizedInput.contains("\"")) { //" characters in string to potentially unescape: properly escape " and \ characters for inline JSON if necessary
				int i=sanitizedInput.length()-1;
				while(i>=0) {
					if(sanitizedInput.charAt(i)=='\"') { //" character found
						int end = i;
						int backslashCount = 0;
						if(i>0) { //count \ characters preceding "
							i--;
							char prev = sanitizedInput.charAt(i);
							while(i>=0 && prev=='\\') {
								backslashCount++;
								i--;
								prev = sanitizedInput.charAt(i);
							}
							i++;
						}
						
						String quoteBackslashReplace = "";
						int j=0;
						while(j<(backslashCount/2)) { //replace escaped \ characters with unicode-escaped \ characters
							quoteBackslashReplace = quoteBackslashReplace.concat("\\u005c");
							j++;
						}
						quoteBackslashReplace = quoteBackslashReplace.concat("\\u0022"); //unicode-escape " character
						
						String prefix = sanitizedInput.substring(0,i);
						String suffix = "";
						if(sanitizedInput.length()!=end+1) {
							suffix = sanitizedInput.substring(end+1,sanitizedInput.length());
						}
						sanitizedInput = prefix.concat(quoteBackslashReplace).concat(suffix); //replace original " and \ characters discovered above with unicode-escape characters
					}
					i--;
				}
				
				if(JsonEscaperSettings.getInstance().getVerboseLogging()) mLogging.logToOutput("sanitizedInput: "+sanitizedInput);
			}
		}

		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(String.format("{\"%s\":\"%s\"}",INLINE_JSON_KEY,sanitizedInput)); //Create input JSON inline because unicode-escapes (\\uxxxx) are not interpreted correctly any other way
		} catch(JSONException jsonE) { //JSON string contains invalid value(s) (either invalid escape(s), or characters that should be escaped when placed into inline JSON): print stack trace to Extension->Errors tab and throw exception to notify other functions of error
			if(JsonEscaperSettings.getInstance().getVerboseLogging()) mLogging.logToError(input);
			if(JsonEscaperSettings.getInstance().getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
			throw jsonE;
		}
		return (String) jsonObj.get(INLINE_JSON_KEY);
	}
	
	//JSON-escape only minimum characters required by JSON RFCs using JSON-Java library
	public static String escapeKeyChars(String input) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		String escapedInput = JSONWriter.valueToString(input);
		escapedInput = escapedInput.substring(1,escapedInput.length()-1);
		return escapedInput;
	}
	
	//JSON Unicode-escape only minimum characters required by JSON RFCs
	public static String unicodeEscapeKeyChars(String input) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		String escapedInput = JSONWriter.valueToString(input);
		escapedInput = escapedInput.substring(1,escapedInput.length()-1);
		
		//replace escaped characters with unicode-escaped characters
		escapedInput = escapedInput.replace("\\\\","\\u005c"); //backslash
		escapedInput = escapedInput.replace("\\b","\\u0008"); //backspace
		escapedInput = escapedInput.replace("\\t","\\u0009"); //tab
		escapedInput = escapedInput.replace("\\n","\\u000a"); //newline
		escapedInput = escapedInput.replace("\\f","\\u000c"); //form feed
		escapedInput = escapedInput.replace("\\r","\\u000d"); //carriage return
		escapedInput = escapedInput.replace("\\\"","\\u0022"); //double quote
		return escapedInput;
	}
	
	//JSON Unicode-escape all characters in input
	public static String unicodeEscapeAllChars(String input) {
		return unicodeEscapeChars(input,null);
	}
	
	//JSON Unicode-escape characters passed in charsToEscape.
	//If charsToEscape is null or empty: Unicode-escape everything
	public static String unicodeEscapeChars(String input,int[] charsToEscape) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		String[] inputArr = new String[input.length()];
		int i=0;
		while(i<inputArr.length) {
			inputArr[i] = String.valueOf(input.charAt(i));
			i++;
		}
		
		i=0;
		while(i<inputArr.length) {
			String escaped = null;
			if(charsToEscape!=null && charsToEscape.length!=0) {
				for(int j=0;j<charsToEscape.length;j++) {
					if(inputArr[i].charAt(0) == (char) charsToEscape[j]) {
						escaped = Integer.toHexString(inputArr[i].charAt(0));
						while(escaped.length()<4) {
							escaped = "0".concat(escaped);
						}
						inputArr[i] = String.format("\\u%s",escaped);
						break;
					}
				}
			} else {
				escaped = Integer.toHexString(inputArr[i].charAt(0));
				while(escaped.length()<4) {
					escaped = "0".concat(escaped);
				}
				inputArr[i] = String.format("\\u%s",escaped);
			}
			i++;
		}
		
		return String.join("",inputArr);
	}
	//END Unescaper/Escaper methods
}

