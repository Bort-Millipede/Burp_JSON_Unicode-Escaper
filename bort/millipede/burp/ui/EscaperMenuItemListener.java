package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.*;
import burp.api.montoya.ui.*;
import burp.api.montoya.ui.contextmenu.*;
import burp.api.montoya.http.message.*;
import burp.api.montoya.http.message.requests.*;
import burp.api.montoya.http.message.responses.*;
import burp.api.montoya.logging.Logging;

import java.nio.charset.StandardCharsets;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;

import org.json.JSONException;

public class EscaperMenuItemListener implements ActionListener {
	private MontoyaApi mApi;
	private ContextMenuEvent event;
	private Logging mLogging;
	
	public EscaperMenuItemListener(MontoyaApi api,ContextMenuEvent inEvent) {
		mApi = api;
		event = inEvent;
		mLogging = mApi.logging();
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
			EscaperPopup popup = new EscaperPopup(mApi,ByteArray.byteArray(outputVal.getBytes(StandardCharsets.UTF_8)),unescapeError);
			mApi.userInterface().applyThemeToComponent(popup);
			popup.setVisible(true);
		}
	}
}

