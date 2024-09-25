package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;

import java.nio.charset.StandardCharsets;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;

import org.json.JSONException;

public class EscaperTab extends JPanel implements ActionListener {
	private MontoyaApi mApi;
	
	//"Manual Escaper/Unescaper" Tab
	private JPanel escaperUnescaperPanel;
	private RawEditor inputArea;
	private JComboBox<String> optionDropdown;
	private JButton escapeUnescapeButton;
	private JButton copyClipboardButton;
	private JButton clearOutputButton;
	private JLabel errorLabel;
	private RawEditor outputArea;
	
	private JPanel settingsPanel;
	
	public EscaperTab(MontoyaApi api) {
		super(new GridLayout(1,1));
		mApi = api;
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//setup "Manual Escaper/Unescaper" Tab
		escaperUnescaperPanel = new JPanel();
		escaperUnescaperPanel.setLayout(new BoxLayout(escaperUnescaperPanel, BoxLayout.Y_AXIS));
		inputArea = mApi.userInterface().createRawEditor(EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		escaperUnescaperPanel.add(inputArea.uiComponent());
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
		
		middlePanel.add(optionDropdown);
		middlePanel.add(escapeUnescapeButton);
		middlePanel.add(copyClipboardButton);
		middlePanel.add(clearOutputButton);
		middlePanel.add(errorLabel);
		
		escaperUnescaperPanel.add(middlePanel);
		outputArea = mApi.userInterface().createRawEditor(EditorOptions.READ_ONLY,EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		escaperUnescaperPanel.add(outputArea.uiComponent());
		
		tabbedPane.addTab("Manual Escaper/Unescaper",new JScrollPane(escaperUnescaperPanel));
		
		
		//setup "Settings" Tab
		settingsPanel = new JPanel();
		settingsPanel.add(new JLabel("NOT IMPLEMENTED YET!!!"));
		tabbedPane.addTab("Settings",new JScrollPane(settingsPanel));
		
		this.add(tabbedPane);
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
			String outputVal = new String(inputArea.getContents().getBytes(),StandardCharsets.UTF_8);
			if(outputVal.length()==0) {
				outputArea.setContents(ByteArray.byteArray(""));
				return;
			}
			String selectedItem = (String) optionDropdown.getSelectedItem();
			switch(selectedItem) {
				case JsonEscaper.UNESCAPE_LABEL:
					try {
						outputVal = JsonEscaper.unescapeAllChars(outputVal);
					} catch(JSONException jsonE) {
						outputVal = "";
						errorLabel.setText("Error occurred when unescaping input!!!"); //TODO: make label red too
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
			outputArea.setContents(ByteArray.byteArray(outputVal.getBytes(StandardCharsets.UTF_8)));
		} else if(source==copyClipboardButton) { //Copy to Clipboard button
			ByteArray contents = outputArea.getContents();
			if(contents.length()>0) {
				String strContents = new String(contents.getBytes(),StandardCharsets.UTF_8);
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(strContents);
				cb.setContents(ss,ss);
			}
		} else if(source==clearOutputButton) { //Clear Output button
			outputArea.setContents(ByteArray.byteArray(""));
		}
	}
}
