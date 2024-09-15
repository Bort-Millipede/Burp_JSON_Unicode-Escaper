package bort.millipede.burp;

import java.util.List;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.JMenuItem;

import burp.api.montoya.*;
import burp.api.montoya.extension.Extension;
import burp.api.montoya.intruder.*;
import burp.api.montoya.ui.*;
import burp.api.montoya.ui.contextmenu.*;
import burp.api.montoya.core.*;
import burp.api.montoya.http.message.*;
import burp.api.montoya.http.message.requests.*;
import burp.api.montoya.http.message.responses.*;

import org.json.JSONObject;
import org.json.JSONWriter;

public class JsonEscaper implements BurpExtension,ContextMenuItemsProvider {
	private MontoyaApi mApi;
	private Extension bExtension;
	private Intruder bIntruder;
	private UserInterface bUI;
	
	static final String UNESCAPE_LABEL = "JSON-unescape";
	static final String ESCAPE_KEY_LABEL = "JSON-escape key chars";
	static final String UNICODE_ESCAPE_KEY_LABEL = "JSON Unicode-escape key chars";
	static final String UNICODE_ESCAPE_ALL_LABEL = "JSON Unicode-escape all chars";
	static final String UNICODE_ESCAPE_CUSTOM_LABEL = "JSON Unicode-escape custom chars";
	
	@Override
	public void initialize(MontoyaApi api) {
		mApi = api;
		bExtension = mApi.extension();
		bExtension.setName("JSON Unicode Escaper");
		bIntruder = mApi.intruder();
		bIntruder.registerPayloadProcessor(new UnescapePayloadProcessor());
		bIntruder.registerPayloadProcessor(new EscapeKeyCharsPayloadProcessor());
		bIntruder.registerPayloadProcessor(new UnicodeEscapeKeyCharsPayloadProcessor());
		bIntruder.registerPayloadProcessor(new UnicodeEscapeAllCharsPayloadProcessor());
		bIntruder.registerPayloadProcessor(new UnicodeEscapePayloadProcessor());
		bUI = mApi.userInterface();
		bUI.registerContextMenuItemsProvider(this);
	}
	
	@Override
	public List<Component> provideMenuItems(ContextMenuEvent event) {
		if(event.messageEditorRequestResponse().isPresent()) {
			MenuItemListener listener = new MenuItemListener(event);
			JMenuItem unescapeMenuItem = new JMenuItem(UNESCAPE_LABEL);
			unescapeMenuItem.addActionListener(listener);
			JMenuItem escapeKeyMenuItem = new JMenuItem(ESCAPE_KEY_LABEL);
			escapeKeyMenuItem.addActionListener(listener);
			JMenuItem unicodeEscapeKeyMenuItem = new JMenuItem(UNICODE_ESCAPE_KEY_LABEL);
			unicodeEscapeKeyMenuItem.addActionListener(listener);
			JMenuItem unicodeEscapeAllMenuItem = new JMenuItem(UNICODE_ESCAPE_ALL_LABEL);
			unicodeEscapeAllMenuItem.addActionListener(listener);
			JMenuItem unicodeEscapeMenuItem = new JMenuItem(UNICODE_ESCAPE_CUSTOM_LABEL);
			unicodeEscapeMenuItem.addActionListener(listener);
			return List.of(unescapeMenuItem,escapeKeyMenuItem,unicodeEscapeKeyMenuItem,unicodeEscapeAllMenuItem,unicodeEscapeMenuItem);
		}
		return null;
	}
	
	//un-JSON-escape all characters
	static String unescapeAllChars(String input) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		JSONObject jsonObj = new JSONObject(String.format("{\"input\":\"%s\"}",input));
		return (String) jsonObj.get("input");
	}
	
	//JSON-escape only minimum characters required by JSON RFCs using JSON-Java library
	static String escapeKeyChars(String input) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		String escapedInput = JSONWriter.valueToString(input);
		escapedInput = escapedInput.substring(1,escapedInput.length()-1);
		return escapedInput;
	}
	
	//JSON Unicode-escape only minimum characters required by JSON RFCs
	static String unicodeEscapeKeyChars(String input) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
		String escapedInput = JSONWriter.valueToString(input);
		escapedInput = escapedInput.substring(1,escapedInput.length()-1);
		
		escapedInput = escapedInput.replace("\\b","\\u0008"); //backspace
		escapedInput = escapedInput.replace("\\t","\\u0009"); //tab
		escapedInput = escapedInput.replace("\\n","\\u000a"); //newline
		escapedInput = escapedInput.replace("\\f","\\u000c"); //form feed
		escapedInput = escapedInput.replace("\\r","\\u000d"); //carriage return
		escapedInput = escapedInput.replace("\\\"","\\u0022"); //double quote
		escapedInput = escapedInput.replace("\\\\","\\u005c"); //backslash
		return escapedInput;
	}
	
	//JSON Unicode-escape all characters in input
	static String unicodeEscapeAllChars(String input) {
		return unicodeEscapeChars(input,null);
	}
	
	//JSON Unicode-escape characters passed in charsToEscape.
	//If charsToEscape is null: Unicode-escape everything
	static String unicodeEscapeChars(String input,String charsToEscape) {
		if(input==null) return null;
		if(input.length()==0) return input;
		
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
			MessageEditorHttpRequestResponse meHttpRequestResponse = event.messageEditorRequestResponse().get();
			Range selectionOffsets = meHttpRequestResponse.selectionOffsets().get();
			HttpRequestResponse requestResponse = meHttpRequestResponse.requestResponse();
			
			String outputVal = "";
			ByteArray msgBytes = null;
			if(event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST,InvocationType.MESSAGE_VIEWER_REQUEST)) {
				HttpRequest req = requestResponse.request();
				msgBytes = req.toByteArray();
				ByteArray selectedVal = msgBytes.subArray(selectionOffsets);
				outputVal = selectedVal.toString();
			} else if(event.isFrom(InvocationType.MESSAGE_EDITOR_RESPONSE,InvocationType.MESSAGE_VIEWER_RESPONSE)) {
				HttpResponse resp = requestResponse.response();
				msgBytes = resp.toByteArray();
				ByteArray selectedVal = msgBytes.subArray(selectionOffsets);
				outputVal = selectedVal.toString();
			}
			
			String menuItemText = menuItem.getText();
			switch(menuItemText) {
				case JsonEscaper.UNESCAPE_LABEL:
					outputVal = JsonEscaper.unescapeAllChars(outputVal);
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
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
					outputVal = JsonEscaper.unicodeEscapeChars(outputVal,null);
					break;
			}
			
			mApi.logging().logToOutput(String.format("%s: %s\r\n",menuItemText,outputVal));
			
			int startIndex = selectionOffsets.startIndexInclusive();
			ByteArray updatedMsg = msgBytes.subArray(0,startIndex);
			updatedMsg = updatedMsg.withAppended(ByteArray.byteArray(outputVal));
			updatedMsg = updatedMsg.withAppended(msgBytes.subArray(selectionOffsets.endIndexExclusive(),msgBytes.length()));
			if(event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST)) {
				meHttpRequestResponse.setRequest(HttpRequest.httpRequest(updatedMsg));
			} else if(event.isFrom(InvocationType.MESSAGE_EDITOR_RESPONSE)) {
				meHttpRequestResponse.setResponse(HttpResponse.httpResponse(updatedMsg));
			}
		}
	}
}

