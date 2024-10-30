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

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Dimension;
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
	private JButton pasteFileButton;
	private JButton copyClipboardButton;
	private JButton copyFileButton;
	private JButton clearOutputButton;
	private JLabel errorLabel;
	//private Color errorLabelColor;
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
		Component inputAreaComponent = inputArea.uiComponent();
		//inputAreaComponent.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight())); //Need to add this in to prevent window from going crazy on large pastes
		this.add(inputAreaComponent);
		
		//middle button panel
		JPanel middlePanel = new JPanel();
		errorLabel = new JLabel(new String(""));
		//errorLabelColor = errorLabel.getForeground();
		errorLabel.setForeground(Color.RED);
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
		pasteClipboardButton = new JButton("Paste From Clipboard");
		pasteClipboardButton.addActionListener(this);
		pasteFileButton = new JButton("Paste From File");
		pasteFileButton.addActionListener(this);
		copyClipboardButton = new JButton("Copy Unescaped Output To Clipboard");
		copyClipboardButton.addActionListener(this);
		copyFileButton = new JButton("Copy Unescaped Output To File");
		copyFileButton.addActionListener(this);
		clearOutputButton = new JButton("Clear Output");
		clearOutputButton.addActionListener(this);
		middlePanel.add(errorLabel);
		middlePanel.add(optionDropdown);
		middlePanel.add(escapeUnescapeButton);
		middlePanel.add(new JLabel("    "));
		middlePanel.add(clearInputButton);
		middlePanel.add(pasteClipboardButton);
		middlePanel.add(pasteFileButton);
		middlePanel.add(copyClipboardButton);
		middlePanel.add(copyFileButton);
		middlePanel.add(clearOutputButton);
		this.add(middlePanel);
		
		//output area
		JPanel outputLabelPanel = new JPanel(new GridLayout(1,2));
		outputLabelPanel.add(new JLabel("Output:",SwingConstants.LEFT));
		outputLabelPanel.add(new JLabel());
		this.add(outputLabelPanel);
		outputArea = mApi.userInterface().createRawEditor(EditorOptions.READ_ONLY,EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		Component outputAreaComponent = outputArea.uiComponent();
		//outputAreaComponent.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight())); //Need to add this in to prevent window from going crazy on large pastes
		this.add(outputAreaComponent);
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
					copyFileButton.setText("Copy Unescaped Output To File");
					break;
				case JsonEscaper.ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_ALL_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
					escapeUnescapeButton.setText("Escape");
					copyClipboardButton.setText("Copy Escaped Output To Clipboard");
					copyFileButton.setText("Copy Escaped Output To File");
					break;
			}
		} else if(source==escapeUnescapeButton) { //Escape/Unescape button
			errorLabel.setText("");
			//errorLabel.setForeground(errorLabelColor);
			String outputVal = new String(inputArea.getContents().getBytes(),StandardCharsets.UTF_8);
			if(outputVal.length()==0) {
				outputArea.setContents(ByteArray.byteArrayOfLength(0));
				return;
			}
			mApi.logging().logToOutput("EscaperTab actionPerformed inputArea contents: "+outputVal);
			String selectedItem = (String) optionDropdown.getSelectedItem();
			new Thread(new Runnable() {
				public void run() {
					String innerOutputVal = outputVal;
					switch(selectedItem) {
						case JsonEscaper.UNESCAPE_LABEL:
							try {
								outputArea.setContents(ByteArray.byteArray("Unescaping..."));
								innerOutputVal = JsonEscaper.unescapeAllChars(innerOutputVal);
							} catch(JSONException jsonE) {
								innerOutputVal = "";
								errorLabel.setText("Error occurred when unescaping input!!!");
							}
							break;
						case JsonEscaper.ESCAPE_KEY_LABEL:
							outputArea.setContents(ByteArray.byteArray("Escaping..."));
							innerOutputVal = JsonEscaper.escapeKeyChars(innerOutputVal);
							break;
						case JsonEscaper.UNICODE_ESCAPE_KEY_LABEL:
							outputArea.setContents(ByteArray.byteArray("Escaping..."));
							innerOutputVal = JsonEscaper.unicodeEscapeKeyChars(innerOutputVal);
							break;
						case JsonEscaper.UNICODE_ESCAPE_ALL_LABEL:
							outputArea.setContents(ByteArray.byteArray("Escaping..."));
							innerOutputVal = JsonEscaper.unicodeEscapeAllChars(innerOutputVal);
							break;
						case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
							outputArea.setContents(ByteArray.byteArray("Escaping..."));
							innerOutputVal = JsonEscaper.unicodeEscapeChars(innerOutputVal,settings.getCharsToEscape());
							break;
					}
					
					mApi.logging().logToOutput("EscaperTab outputVal: "+innerOutputVal);
					outputArea.setContents(ByteArray.byteArrayOfLength(0));
					outputArea.setContents(ByteArray.byteArray(innerOutputVal.getBytes(StandardCharsets.UTF_8)));
				}
			}).start();
		} else if(source==clearInputButton) { //Clear Input button
			inputArea.setContents(ByteArray.byteArrayOfLength(0));
			errorLabel.setText("");
		} else if(source==pasteClipboardButton) { //Paste From Clipboard button
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

