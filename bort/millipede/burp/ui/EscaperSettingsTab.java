package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

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
	private Preferences preferences; //to be removed
	private JsonEscaperSettings settings;
	private byte inputFormat;
	
	//"Characters to JSON Unicode-escape" settings
	private JRadioButton charsFormatButton;
	private JRadioButton decRangesFormatButton;
	private JRadioButton hexRangesFormatButton;
	private JLabel charsToEscapeLabel;
	private JTextArea charsToEscapeField;
	private JButton pasteButton;
	private JButton deduplicateButton;
	private JCheckBox includeKeyCharsCheckbox;
	
	//Global settings
	private JCheckBox fineTuneUnescapingCheckbox;
	private JCheckBox verboseLoggingCheckbox;
	
	//Constants
	private static final byte CHARS_INPUT_FORMAT = 0;
	private static final byte DECIMAL_INPUT_FORMAT = 1;
	private static final byte HEXADECIMAL_INPUT_FORMAT = 2;
	
	EscaperSettingsTab(MontoyaApi api) {
		super();
		mApi = api;
		preferences = mApi.persistence().preferences();
		
		settings = JsonEscaperSettings.getInstance();
		inputFormat = CHARS_INPUT_FORMAT;
		
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel,BoxLayout.Y_AXIS));
		
		//Start creating "escape custom chars" settings panel
		innerPanel.add(new JLabel(String.format("\"%s\" Settings:",JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL)));
		JPanel innerSettingsPanel = new JPanel(new GridLayout(4,2));
		innerSettingsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		innerSettingsPanel.add(new JLabel("Define characters for escaping as:",SwingConstants.RIGHT));
		
		//format radio buttons
		charsFormatButton = new JRadioButton("Charaters",true);
		//charsFormatButton.setActionCommand("CHARS");
		///charsFormatButton.addActionListener(this);
		decRangesFormatButton = new JRadioButton("Decimal numbers/ranges",false);
		//decRangesFormatButton.setActionCommand("DEC");
		///decRangesFormatButton.addActionListener(this);
		decRangesFormatButton.setEnabled(false);
		hexRangesFormatButton = new JRadioButton("Hexdecimal numbers/ranges",false);
		//hexRangesFormatButton.setActionCommand("HEX");
		///hexRangesFormatButton.addActionListener(this);
		hexRangesFormatButton.setEnabled(false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(charsFormatButton);
		bg.add(decRangesFormatButton);
		bg.add(hexRangesFormatButton);
		JPanel formatButtonPanel = new JPanel(new GridLayout(3,1));
		formatButtonPanel.add(charsFormatButton);
		formatButtonPanel.add(decRangesFormatButton);
		formatButtonPanel.add(hexRangesFormatButton);
		innerSettingsPanel.add(formatButtonPanel);
		
		//all remaining elements
		charsToEscapeLabel = new JLabel("Characters to JSON Unicode-escape:",SwingConstants.RIGHT);
		innerSettingsPanel.add(charsToEscapeLabel);
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
		deduplicateButton = new JButton("Deduplicate");
		deduplicateButton.addActionListener(this);
		buttonPanel.add(deduplicateButton);
		innerSettingsPanel.add(buttonPanel);
		innerSettingsPanel.add(new JLabel("Automatically include JSON key characters:",SwingConstants.RIGHT));
		includeKeyCharsCheckbox = new JCheckBox("",true);
		includeKeyCharsCheckbox.addActionListener(this);
		innerSettingsPanel.add(includeKeyCharsCheckbox);
		innerPanel.add(innerSettingsPanel);
		
		//separators between settings panels
		innerPanel.add(new JLabel(" "));
		innerPanel.add(new JLabel(" "));
		
		//global settings panel
		innerPanel.add(new JLabel("Global Settings:"));
		JPanel innerGlobalPanel = new JPanel(new GridLayout(2,2));
		innerGlobalPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		innerGlobalPanel.add(new JLabel("Fine-tune unescaping to avoid errors:",SwingConstants.RIGHT));
		fineTuneUnescapingCheckbox = new JCheckBox("",settings.getFineTuneUnescaping());
		fineTuneUnescapingCheckbox.addActionListener(this);
		innerGlobalPanel.add(fineTuneUnescapingCheckbox);
		innerGlobalPanel.add(new JLabel("Verbose logging in Extension \"Output\" tab:",SwingConstants.RIGHT));
		verboseLoggingCheckbox = new JCheckBox("",settings.getVerboseLogging());
		verboseLoggingCheckbox.addActionListener(this);
		innerGlobalPanel.add(verboseLoggingCheckbox);
		innerPanel.add(innerGlobalPanel);
		
		this.add(innerPanel);
	}
	
	//ActionListener method
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == charsFormatButton) {
			inputFormat = CHARS_INPUT_FORMAT;
			charsToEscapeLabel.setText("Characters to JSON Unicode-escape:");
			deduplicateButton.setEnabled(true);
		} else if(source == decRangesFormatButton) {
			inputFormat = DECIMAL_INPUT_FORMAT;
			charsToEscapeLabel.setText("Character Decimal Ranges to JSON Unicode-escape:");
			deduplicateButton.setEnabled(false);
		} else if (source == hexRangesFormatButton) {
			inputFormat = HEXADECIMAL_INPUT_FORMAT;
			charsToEscapeLabel.setText("Character Hexadecimal Ranges to JSON Unicode-escape:");
			deduplicateButton.setEnabled(false);
		} else if(source == pasteButton) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = cb.getContents(null);
			try {
				charsToEscapeField.setText(charsToEscapeField.getText()+(String) t.getTransferData(DataFlavor.stringFlavor));
			} catch(Exception ex) {
				//No data in clipboard, or data is not text: do nothing
			}
		} else if(source == deduplicateButton) {
			if(inputFormat == CHARS_INPUT_FORMAT)
				deduplicateEscapeChars();
		} else if(source == includeKeyCharsCheckbox) {
			if(inputFormat == CHARS_INPUT_FORMAT) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
			}
		} else if(source == fineTuneUnescapingCheckbox) {
			settings.setFineTuneUnescaping(fineTuneUnescapingCheckbox.isSelected());
		} else if(source == verboseLoggingCheckbox) {
			settings.setVerboseLogging(verboseLoggingCheckbox.isSelected());
		}
	}
	
	//DocumentListener methods
	@Override
	public void changedUpdate(DocumentEvent e) {
		//preferences.setString(JsonEscaper.CHARS_TO_ESCAPE_KEY,charsToEscapeField.getText());
		if(inputFormat == CHARS_INPUT_FORMAT) {
			settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
		}
		
		//mApi.logging().logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (changedUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		if(inputFormat == CHARS_INPUT_FORMAT) {
			settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
		}
		//mApi.logging().logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (insertUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	@Override	
	public void removeUpdate(DocumentEvent e) {
		if(inputFormat == CHARS_INPUT_FORMAT) {
			settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
		}
		//mApi.logging().logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (removeUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	private void deduplicateEscapeChars() {
		if(inputFormat != CHARS_INPUT_FORMAT) return;
		
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
