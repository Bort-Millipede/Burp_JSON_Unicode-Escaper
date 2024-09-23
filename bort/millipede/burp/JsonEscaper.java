package bort.millipede.burp;

import burp.api.montoya.*;
import burp.api.montoya.extension.Extension;
import burp.api.montoya.intruder.*;
import burp.api.montoya.ui.*;
import burp.api.montoya.ui.contextmenu.*;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.core.*;
import burp.api.montoya.http.message.*;
import burp.api.montoya.http.message.requests.*;
import burp.api.montoya.http.message.responses.*;
import burp.api.montoya.logging.*;

import bort.millipede.burp.payloadprocessing.*;
import bort.millipede.burp.ui.*;

import java.util.List;
import java.nio.charset.StandardCharsets;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.JMenuItem;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.json.JSONException;

public class JsonEscaper implements BurpExtension,ContextMenuItemsProvider {
	private MontoyaApi mApi;
	private Extension bExtension;
	private Intruder bIntruder;
	private UserInterface bUI;
	
	static Logging mLogging;
	
	private JMenuItem unescapeMenuItem;
	private JMenuItem escapeKeyMenuItem;
	private JMenuItem unicodeEscapeKeyMenuItem;
	private JMenuItem unicodeEscapeAllMenuItem;
	private JMenuItem unicodeEscapeMenuItem;
	
	public static final String EXTENSION_NAME = "JSON Unicode-Escaper";
	public static final String UNESCAPE_LABEL = "JSON-unescape";
	public static final String ESCAPE_KEY_LABEL = "JSON-escape key chars";
	public static final String UNICODE_ESCAPE_KEY_LABEL = "JSON Unicode-escape key chars";
	public static final String UNICODE_ESCAPE_ALL_LABEL = "JSON Unicode-escape all chars";
	public static final String UNICODE_ESCAPE_CUSTOM_LABEL = "JSON Unicode-escape custom chars";
	public static final String INLINE_JSON_KEY = "input";
	
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
		//bIntruder.registerPayloadProcessor(new UnicodeEscapePayloadProcessor());
		
		bUI = mApi.userInterface();
		bUI.registerContextMenuItemsProvider(this);
		unescapeMenuItem = new JMenuItem(UNESCAPE_LABEL);
		escapeKeyMenuItem = new JMenuItem(ESCAPE_KEY_LABEL);
		unicodeEscapeKeyMenuItem = new JMenuItem(UNICODE_ESCAPE_KEY_LABEL);
		unicodeEscapeAllMenuItem = new JMenuItem(UNICODE_ESCAPE_ALL_LABEL);
		unicodeEscapeMenuItem = new JMenuItem(UNICODE_ESCAPE_CUSTOM_LABEL+" [NOT FULLY IMPLEMENTED]");
		
		mLogging = mApi.logging();
	}
	
	@Override
	public List<Component> provideMenuItems(ContextMenuEvent event) {

		if(event.isFrom(InvocationType.INTRUDER_PAYLOAD_POSITIONS,InvocationType.MESSAGE_EDITOR_REQUEST,InvocationType.MESSAGE_EDITOR_RESPONSE,InvocationType.MESSAGE_VIEWER_REQUEST,InvocationType.MESSAGE_VIEWER_RESPONSE)) {
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
						
			MenuItemListener listener = new MenuItemListener(event);
			unescapeMenuItem.addActionListener(listener);
			escapeKeyMenuItem.addActionListener(listener);
			unicodeEscapeKeyMenuItem.addActionListener(listener);
			unicodeEscapeAllMenuItem.addActionListener(listener);
			unicodeEscapeMenuItem.setEnabled(false);
			//unicodeEscapeMenuItem.addActionListener(listener);
			if(event.isFrom(InvocationType.INTRUDER_PAYLOAD_POSITIONS)) { //Intruder in-place edit not yet implemented, so disable buttons for now
				unescapeMenuItem.setEnabled(false);
				escapeKeyMenuItem.setEnabled(false);
				unicodeEscapeKeyMenuItem.setEnabled(false);
				unicodeEscapeAllMenuItem.setEnabled(false);
			}
			
			return List.of(unescapeMenuItem,escapeKeyMenuItem,unicodeEscapeKeyMenuItem,unicodeEscapeAllMenuItem,unicodeEscapeMenuItem);
		}
		return List.of();
	}
	
	//un-JSON-escape all characters
	public static String unescapeAllChars(String input) throws JSONException {
		if(input==null) return null;
		if(input.length()==0) return input;
		if(!input.contains("\\")) return input;
		
		String sanitizedInput = input;
		if(sanitizedInput.contains("\"")) { //" characters in string to unescape: properly escape " and \ characters for inline JSON if necessary
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
			
			//todo: add timestamps to logs?
			mLogging.logToOutput("sanitizedInput: "+sanitizedInput);
		}

		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(String.format("{\"%s\":\"%s\"}",INLINE_JSON_KEY,sanitizedInput)); //Create input JSON inline because unicode-escapes (\\uxxxx) are not interpreted correctly any other way
		} catch(JSONException jsonE) { //JSON string contains invalid value(s) (likely invalid escape(s)): print stack trace to Extension->Errors tab and throw exception to notify other functions of error
			mLogging.logToError(input);
			mLogging.logToError(jsonE.getMessage(),jsonE);
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
	//If charsToEscape is null: Unicode-escape everything
	public static String unicodeEscapeChars(String input,String charsToEscape) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		//Remove duplicate characters from charsToEscape
		if(charsToEscape!=null && charsToEscape.length()!=0) {
			String uniqEscapeChars = "";
			for(int l=0;l<charsToEscape.length();l++) {
				String ch = String.valueOf(charsToEscape.charAt(l));
				if(!uniqEscapeChars.contains(ch))
					uniqEscapeChars += ch;
			}
			charsToEscape = uniqEscapeChars;
		}
		
		String[] inputArr = new String[input.length()];
		int i=0;
		while(i<inputArr.length) {
			inputArr[i] = String.valueOf(input.charAt(i));
			i++;
		}
		
		i=0;
		while(i<inputArr.length) {
			String escaped = null;
			if(charsToEscape!=null && charsToEscape.length()!=0) {
				for(int j=0;j<charsToEscape.length();j++) {
					if(inputArr[i].equals(charsToEscape.substring(j,j+1))) {
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
	
	private class MenuItemListener implements ActionListener {
		private ContextMenuEvent event;
		
		MenuItemListener(ContextMenuEvent inEvent) {
			event = inEvent;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem menuItem = (JMenuItem) e.getSource();
			HttpRequestResponse requestResponse = null;
			MessageEditorHttpRequestResponse meHttpRequestResponse = null;
			Range selectionOffsets = null;
			if(event.messageEditorRequestResponse().isPresent()) {
				mLogging.logToOutput("MessageEditorHttpRequestResponse present");
				
				meHttpRequestResponse = event.messageEditorRequestResponse().get();
				
				if(meHttpRequestResponse.selectionOffsets().isEmpty()) {
					return; //no text highlighted: do nothing
				}
				selectionOffsets = meHttpRequestResponse.selectionOffsets().get();
				requestResponse = meHttpRequestResponse.requestResponse();
			} else {
				mLogging.logToOutput("MessageEditorHttpRequestResponse NOT present");
				mLogging.logToOutput("selectedRequestResponses() result set length: "+event.selectedRequestResponses().size());
			}
			
			String outputVal = "";
			String strMsg = null;
			if(event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST,InvocationType.MESSAGE_VIEWER_REQUEST)) {
				strMsg = new String(requestResponse.request().toByteArray().getBytes(),StandardCharsets.UTF_8);
				outputVal = strMsg.substring(selectionOffsets.startIndexInclusive(),selectionOffsets.endIndexExclusive());
			} else if(event.isFrom(InvocationType.MESSAGE_EDITOR_RESPONSE,InvocationType.MESSAGE_VIEWER_RESPONSE)) {
				strMsg = new String(requestResponse.response().toByteArray().getBytes(),StandardCharsets.UTF_8);
				outputVal = strMsg.substring(selectionOffsets.startIndexInclusive(),selectionOffsets.endIndexExclusive());
			} else {
				InvocationType invocationType = event.invocationType();
				mLogging.logToOutput("containsHttpMessage(): "+invocationType.containsHttpMessage());
				mLogging.logToOutput("containsHttpRequestResponses(): "+invocationType.containsHttpRequestResponses());
				return;
			}
			
			boolean unescapeError = false;
			String menuItemText = menuItem.getText();
			switch(menuItemText) {
				case JsonEscaper.UNESCAPE_LABEL:
					try {
						outputVal = JsonEscaper.unescapeAllChars(outputVal);
					} catch(JSONException jsonE) {
						unescapeError = true;
					}
					break;
				case JsonEscaper.ESCAPE_KEY_LABEL:
					outputVal = JsonEscaper.escapeKeyChars(outputVal);
					break;
				case JsonEscaper.UNICODE_ESCAPE_KEY_LABEL:
					outputVal = JsonEscaper.unicodeEscapeKeyChars(outputVal);
					break;
				case JsonEscaper.UNICODE_ESCAPE_ALL_LABEL:
					outputVal = JsonEscaper.unicodeEscapeAllChars(outputVal);
					break;
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL: //todo: implement escaping only specific chars here.
					outputVal = JsonEscaper.unicodeEscapeChars(outputVal,null);
					break;
			}
			
			mLogging.logToOutput(String.format("%s: %s\r\n",menuItemText,outputVal)); //todo: add timestamps to logs? or remove this altogether
			
			if(event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST,InvocationType.MESSAGE_EDITOR_RESPONSE)) {
				if(!unescapeError) {
					String updatedMsgStr = strMsg.substring(0,selectionOffsets.startIndexInclusive());
					updatedMsgStr = updatedMsgStr.concat(outputVal);
					if(selectionOffsets.endIndexExclusive()!=strMsg.length()-1)
						updatedMsgStr = updatedMsgStr.concat(strMsg.substring(selectionOffsets.endIndexExclusive()));
					ByteArray updatedMsg = ByteArray.byteArray(updatedMsgStr.getBytes(StandardCharsets.UTF_8));
					if(event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST)) {
						meHttpRequestResponse.setRequest(HttpRequest.httpRequest(updatedMsg));
					} else if(event.isFrom(InvocationType.MESSAGE_EDITOR_RESPONSE)) {
						meHttpRequestResponse.setResponse(HttpResponse.httpResponse(updatedMsg));
					}
				}
			} else if(event.isFrom(InvocationType.INTRUDER_PAYLOAD_POSITIONS)) {
				//TODO: implement this if possible
			} else {
				EscaperPopup popup = new EscaperPopup(bUI.createRawEditor(EditorOptions.READ_ONLY,EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES),ByteArray.byteArray(outputVal.getBytes(StandardCharsets.UTF_8)),unescapeError,mLogging);
				bUI.applyThemeToComponent(popup);
				popup.setVisible(true);
			}
		}
	}
}

