package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Preferences;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.Selection;

import java.util.Optional;
import java.nio.charset.StandardCharsets;

import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

import org.json.JSONException;

class EscaperUnescaperTab extends JPanel implements ActionListener {
	private MontoyaApi mApi;
	private Preferences mPreferences;
	
	private JsonEscaperSettings settings;
	
	private RawEditor inputArea;
	private JComboBox<String> optionDropdown;
	private JButton escapeUnescapeButton;
	private JButton clearInputButton;
	private JButton pasteClipboardButton;
	private JButton copyClipboardButton;
	private JButton clearOutputButton;
	private JLabel errorLabel;
	private Color errorLabelColor;
	private RawEditor outputArea;
	
	private JPanel settingsPanel;
	
	EscaperUnescaperTab(MontoyaApi api) {
		super();
		mApi = api;
		mPreferences = mApi.persistence().preferences();
		
		settings = JsonEscaperSettings.getInstance();
		
		//input area
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel inputLabelPanel = new JPanel(new GridLayout(1,2));
		inputLabelPanel.add(new JLabel("Input:",SwingConstants.LEFT));
		inputLabelPanel.add(new JLabel());
		this.add(inputLabelPanel);
		inputArea = mApi.userInterface().createRawEditor(EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		inputArea.setContents(ByteArray.byteArrayOfLength(0));
		this.add(inputArea.uiComponent());
		
		//middle button panel
		JPanel middlePanel = new JPanel();
		errorLabel = new JLabel(new String(""));
		errorLabelColor = errorLabel.getForeground();
		optionDropdown = new JComboBox<String>();
		optionDropdown.addItem(JsonEscaper.UNESCAPE_LABEL);
		optionDropdown.addItem(JsonEscaper.ESCAPE_KEY_LABEL);
		optionDropdown.addItem(JsonEscaper.UNICODE_ESCAPE_KEY_LABEL);
		optionDropdown.addItem(JsonEscaper.UNICODE_ESCAPE_ALL_LABEL);
		optionDropdown.addItem(JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL);
		optionDropdown.setEditable(false);
		optionDropdown.addActionListener(this);
		escapeUnescapeButton = new JButton("Unescape");
		escapeUnescapeButton.addActionListener(this);
		clearInputButton = new JButton("Clear Input");
		clearInputButton.addActionListener(this);
		pasteClipboardButton = new JButton("Paste Value From Clipboard");
		pasteClipboardButton.addActionListener(this);
		copyClipboardButton = new JButton("Copy Unescaped Output To Clipboard");
		copyClipboardButton.addActionListener(this);
		clearOutputButton = new JButton("Clear Output");
		clearOutputButton.addActionListener(this);
		middlePanel.add(errorLabel);
		middlePanel.add(optionDropdown);
		middlePanel.add(escapeUnescapeButton);
		middlePanel.add(new JLabel("    "));
		middlePanel.add(clearInputButton);
		middlePanel.add(pasteClipboardButton);
		middlePanel.add(copyClipboardButton);
		middlePanel.add(clearOutputButton);
		this.add(middlePanel);
		
		//output area
		JPanel outputLabelPanel = new JPanel(new GridLayout(1,2));
		outputLabelPanel.add(new JLabel("Output:",SwingConstants.LEFT));
		outputLabelPanel.add(new JLabel());
		this.add(outputLabelPanel);
		outputArea = mApi.userInterface().createRawEditor(EditorOptions.READ_ONLY,EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		this.add(outputArea.uiComponent());
	}
	
	RawEditor getInputArea() {
		return inputArea;
	}
	
	//ActionListener method
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source==optionDropdown) { //Escaper/Unescaper dropdown
			String selectedItem = (String) optionDropdown.getSelectedItem();
			switch(selectedItem) {
				case JsonEscaper.UNESCAPE_LABEL:
					escapeUnescapeButton.setText("Unescape");
					copyClipboardButton.setText("Copy Unescaped Output To Clipboard");
					break;
				case JsonEscaper.ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_ALL_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
					escapeUnescapeButton.setText("Escape");
					copyClipboardButton.setText("Copy Escaped Output To Clipboard");
					break;
			}
		} else if(source==escapeUnescapeButton) { //Escape/Unescape button
			errorLabel.setText("");
			errorLabel.setForeground(errorLabelColor);
			String outputVal = new String(inputArea.getContents().getBytes(),StandardCharsets.UTF_8);
			if(outputVal.length()==0) {
				outputArea.setContents(ByteArray.byteArrayOfLength(0));
				return;
			}
			mApi.logging().logToOutput("EscaperTab actionPerformed inputArea contents: "+outputVal);
			String selectedItem = (String) optionDropdown.getSelectedItem();
			switch(selectedItem) {
				case JsonEscaper.UNESCAPE_LABEL:
					try {
						outputVal = JsonEscaper.unescapeAllChars(outputVal);
					} catch(JSONException jsonE) {
						outputVal = "";
						errorLabel.setText("Error occurred when unescaping input!!!");
						errorLabel.setForeground(Color.RED);
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
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
					outputVal = JsonEscaper.unicodeEscapeChars(outputVal,settings.getCharsToEscape());
					break;
			}
			
			mApi.logging().logToOutput("EscaperTab outputVal: "+outputVal);
			outputArea.setContents(ByteArray.byteArrayOfLength(0));
			outputArea.setContents(ByteArray.byteArray(outputVal.getBytes(StandardCharsets.UTF_8)));
		} else if(source==pasteClipboardButton) { //Paste Value button
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = cb.getContents(null);
			String textToPaste = null;
			try {
				textToPaste = (String) t.getTransferData(DataFlavor.stringFlavor);
				mApi.logging().logToOutput("Text to paste: "+textToPaste);
			} catch(Exception ex) {
				//No data in clipboard, or data is not text: do nothing
			}
			
			ByteArray contents = inputArea.getContents();
			String prefix = "";
			String suffix = "";
			Optional<Selection> selectionOptional = inputArea.selection();
			
			if(selectionOptional.isEmpty()) { //no text selected
				int caretPosition = inputArea.caretPosition();
				if(caretPosition == 0) {
					suffix = new String(contents.getBytes(),StandardCharsets.UTF_8);
				} else if(caretPosition == contents.length()) {
					prefix = new String(contents.getBytes(),StandardCharsets.UTF_8);
				} else {
					prefix = new String(contents.subArray(0,caretPosition).getBytes(),StandardCharsets.UTF_8);
					suffix = new String(contents.subArray(caretPosition,contents.length()).getBytes(),StandardCharsets.UTF_8);
				}
			} else {
				Selection selection = selectionOptional.get();
				int selectionStart = selection.offsets().startIndexInclusive();
				int selectionEnd = selection.offsets().endIndexExclusive();
				if(selectionStart != 0) {
					prefix = new String(contents.subArray(0,selectionStart).getBytes(),StandardCharsets.UTF_8);
				}
				if(selectionEnd != contents.length()) {
					suffix = new String(contents.subArray(selectionEnd,contents.length()).getBytes(),StandardCharsets.UTF_8);
				}
			}
			contents = ByteArray.byteArray(prefix.concat(textToPaste).concat(suffix).getBytes(StandardCharsets.UTF_8));
			inputArea.setContents(ByteArray.byteArrayOfLength(0));
			inputArea.setContents(contents);
			
		} else if(source==copyClipboardButton) { //Copy to Clipboard button
			ByteArray contents = outputArea.getContents();
			if(contents.length()>0) {
				String strContents = new String(contents.getBytes(),StandardCharsets.UTF_8);
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(strContents);
				cb.setContents(ss,ss);
			}
		} else if(source==clearOutputButton) { //Clear Output button
			outputArea.setContents(ByteArray.byteArrayOfLength(0));
		}
	}
}

