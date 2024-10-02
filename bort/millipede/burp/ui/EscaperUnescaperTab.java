package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.EditorOptions;

import java.nio.charset.StandardCharsets;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

import org.json.JSONException;

class EscaperUnescaperTab extends JPanel implements ActionListener {
	private MontoyaApi mApi;
	//private RawEditor inputArea;
	private JTextArea inputArea;
	private JComboBox<String> optionDropdown;
	private JButton escapeUnescapeButton;
	private JButton copyClipboardButton;
	private JButton clearOutputButton;
	private JLabel errorLabel;
	private Color errorLabelColor;
	//private RawEditor outputArea;
	private JTextArea outputArea;
	
	private JPanel settingsPanel;
	
	EscaperUnescaperTab(MontoyaApi api) {
		super();
		mApi = api;
		//setup "Manual Escaper/Unescaper" Tab
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//this.setLayout(new GridLayout(3,1));
		//inputArea = mApi.userInterface().createRawEditor(EditorOptions.WRAP_LINES);
		//inputArea = mApi.userInterface().createRawEditor(EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		inputArea = new JTextArea();
		//inputArea.setSize(inputArea.getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight());
		inputArea.setLineWrap(true);
		inputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		//escaperUnescaperPanel.add(inputArea.uiComponent());
		this.add(inputArea);
		JPanel middlePanel = new JPanel();
		optionDropdown = new JComboBox<String>();
		optionDropdown.addItem(JsonEscaper.UNESCAPE_LABEL);
		optionDropdown.addItem(JsonEscaper.ESCAPE_KEY_LABEL);
		optionDropdown.addItem(JsonEscaper.UNICODE_ESCAPE_KEY_LABEL);
		optionDropdown.addItem(JsonEscaper.UNICODE_ESCAPE_ALL_LABEL);
		//optionDropdown.addItem(JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL);
		optionDropdown.setEditable(false);
		optionDropdown.addActionListener(this);
		
		escapeUnescapeButton = new JButton("Unescape");
		escapeUnescapeButton.addActionListener(this);
		copyClipboardButton = new JButton("Copy Unescaped Value To Clipboard");
		copyClipboardButton.addActionListener(this);
		clearOutputButton = new JButton("Clear Output");
		clearOutputButton.addActionListener(this);
		errorLabel = new JLabel(new String(""));
		errorLabelColor = errorLabel.getForeground();
		
		middlePanel.add(optionDropdown);
		middlePanel.add(escapeUnescapeButton);
		middlePanel.add(copyClipboardButton);
		middlePanel.add(clearOutputButton);
		middlePanel.add(errorLabel);
		this.add(middlePanel);
		
		//outputArea = mApi.userInterface().createRawEditor(EditorOptions.READ_ONLY,EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		outputArea = new JTextArea();
		outputArea.setLineWrap(true);
		outputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		//this.add(outputArea.uiComponent());
		this.add(outputArea);
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
					copyClipboardButton.setText("Copy Unescaped Value To Clipboard");
					break;
				case JsonEscaper.ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_ALL_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
					escapeUnescapeButton.setText("Escape");
					copyClipboardButton.setText("Copy Escaped Value To Clipboard");
					break;
			}
		} else if(source==escapeUnescapeButton) { //Escaper/Unescape button
			errorLabel.setText("");
			errorLabel.setForeground(errorLabelColor);
			//String outputVal = new String(inputArea.getContents().copy().getBytes(),StandardCharsets.UTF_8);
			String outputVal = inputArea.getText();
			if(outputVal.length()==0) {
				//outputArea.setContents(ByteArray.byteArray(""));
				outputArea.setText("");
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
					outputVal = JsonEscaper.unicodeEscapeChars(outputVal,null);
					break;
			}
			
			mApi.logging().logToOutput("EscaperTab outputVal: "+outputVal);
			//outputArea.setContents(ByteArray.byteArray(outputVal.getBytes(StandardCharsets.UTF_8)));
			outputArea.setText(outputVal);
		} else if(source==copyClipboardButton) { //Copy to Clipboard button
			//ByteArray contents = outputArea.getContents();
			String contents = outputArea.getText();
			if(contents.length()>0) {
				String strContents = new String(contents.getBytes(),StandardCharsets.UTF_8);
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(strContents);
				cb.setContents(ss,ss);
			}
		} else if(source==clearOutputButton) { //Clear Output button
			//outputArea.setContents(ByteArray.byteArray(""));
			outputArea.setText("");
		}
	}
}
