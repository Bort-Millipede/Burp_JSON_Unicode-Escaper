package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.persistence.Preferences;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Color;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

class EscaperSettingsTab extends JPanel implements ActionListener,DocumentListener {
	private MontoyaApi mApi;
	private Preferences preferences;
	
	//"Characters to JSON Unicode-escape" settings
	private JTextArea charsToEscapeField;
	private JButton pasteButton;
	private JButton deduplicateButton;
	private JCheckBox includeKeyCharsCheckbox;
	
	//Global settings
	private JCheckBox fineTuneUnescapingCheckbox;
	private JCheckBox verboseLoggingCheckbox;
	
	EscaperSettingsTab(MontoyaApi api) {
		super();
		mApi = api;
		preferences = mApi.persistence().preferences();
		
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel,BoxLayout.Y_AXIS));
		
		//"escape custom chars" settings panel
		innerPanel.add(new JLabel(String.format("\"%s\" Settings:",JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL)));
		JPanel innerSettingsPanel = new JPanel(new GridLayout(3,2));
		innerSettingsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		innerSettingsPanel.add(new JLabel("Characters to JSON Unicode-escape:",SwingConstants.RIGHT));
		charsToEscapeField = new JTextArea("");
		charsToEscapeField.setLineWrap(true);
		charsToEscapeField.getDocument().addDocumentListener(this);
		preferences.setString(JsonEscaper.CHARS_TO_ESCAPE_KEY,charsToEscapeField.getText());
		innerSettingsPanel.add(new JScrollPane(charsToEscapeField));
		innerSettingsPanel.add(new JLabel());
		JPanel buttonPanel = new JPanel(new GridLayout(1,2));
		pasteButton = new JButton("Paste");
		pasteButton.addActionListener(this);
		buttonPanel.add(pasteButton);
		deduplicateButton = new JButton("Deduplicate characters");
		deduplicateButton.addActionListener(this);
		buttonPanel.add(deduplicateButton);
		innerSettingsPanel.add(buttonPanel);
		innerSettingsPanel.add(new JLabel("Automatically include JSON key characters:",SwingConstants.RIGHT));
		includeKeyCharsCheckbox = new JCheckBox("",true);
		includeKeyCharsCheckbox.addActionListener(this);
		innerSettingsPanel.add(includeKeyCharsCheckbox);
		preferences.setBoolean(JsonEscaper.INCLUDE_KEY_CHARS_KEY,true);
		innerPanel.add(innerSettingsPanel);
		
		//separators
		innerPanel.add(new JLabel(" "));
		innerPanel.add(new JLabel(" "));
		
		//global settings panel
		innerPanel.add(new JLabel("Global Settings:"));
		JPanel innerGlobalPanel = new JPanel(new GridLayout(2,2));
		innerGlobalPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		innerGlobalPanel.add(new JLabel("Fine-tune unescaping to avoid errors:",SwingConstants.RIGHT));
		fineTuneUnescapingCheckbox = new JCheckBox("",true);
		fineTuneUnescapingCheckbox.addActionListener(this);
		innerGlobalPanel.add(fineTuneUnescapingCheckbox);
		preferences.setBoolean(JsonEscaper.FINE_TUNE_UNESCAPING_KEY,true);
		innerGlobalPanel.add(new JLabel("Verbose logging in Extension \"Output\" tab:",SwingConstants.RIGHT));
		verboseLoggingCheckbox = new JCheckBox("",false);
		verboseLoggingCheckbox.addActionListener(this);
		verboseLoggingCheckbox.setEnabled(false);
		innerGlobalPanel.add(verboseLoggingCheckbox);
		preferences.setBoolean(JsonEscaper.VERBOSE_LOGGING_KEY,false);
		innerPanel.add(innerGlobalPanel);
		
		this.add(innerPanel);
	}
	
	//ActionListener method
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == pasteButton) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = cb.getContents(null);
			try {
				charsToEscapeField.setText(charsToEscapeField.getText()+(String) t.getTransferData(DataFlavor.stringFlavor));
			} catch(Exception ex) {
				//No data in clipboard, or data is not text: do nothing
			}
		} else if(source == deduplicateButton) {
			deduplicateEscapeChars();
		} else if(source == includeKeyCharsCheckbox) {
			preferences.setBoolean(JsonEscaper.INCLUDE_KEY_CHARS_KEY,includeKeyCharsCheckbox.isSelected());
		} else if(source == fineTuneUnescapingCheckbox) {
			preferences.setBoolean(JsonEscaper.FINE_TUNE_UNESCAPING_KEY,fineTuneUnescapingCheckbox.isSelected());
		} else if(source == verboseLoggingCheckbox) {
			preferences.setBoolean(JsonEscaper.VERBOSE_LOGGING_KEY,verboseLoggingCheckbox.isSelected());
		}
	}
	
	//DocumentListener methods
	@Override
	public void changedUpdate(DocumentEvent e) {
		preferences.setString(JsonEscaper.CHARS_TO_ESCAPE_KEY,charsToEscapeField.getText());
		//mApi.logging().logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (changedUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		preferences.setString(JsonEscaper.CHARS_TO_ESCAPE_KEY,charsToEscapeField.getText());
		//mApi.logging().logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (insertUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	@Override	
	public void removeUpdate(DocumentEvent e) {
		preferences.setString(JsonEscaper.CHARS_TO_ESCAPE_KEY,charsToEscapeField.getText());
		//mApi.logging().logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (removeUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	private void deduplicateEscapeChars() {
		String charsToEscape = charsToEscapeField.getText();
		
		//Remove duplicate characters
		if(charsToEscape!=null && charsToEscape.length()!=0) {
			String uniqEscapeChars = "";
			for(int l=0;l<charsToEscape.length();l++) {
				String ch = String.valueOf(charsToEscape.charAt(l));
				if(!uniqEscapeChars.contains(ch))
					uniqEscapeChars += ch;
			}
			charsToEscapeField.setText(uniqEscapeChars);
			preferences.setString(JsonEscaper.CHARS_TO_ESCAPE_KEY,uniqEscapeChars);
		}
	}
}
