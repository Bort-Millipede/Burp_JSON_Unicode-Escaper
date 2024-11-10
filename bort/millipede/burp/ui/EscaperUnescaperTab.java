package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.logging.Logging;

import java.util.Optional;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.json.JSONException;

class EscaperUnescaperTab extends JPanel implements ActionListener {
	private MontoyaApi mApi;
	private Logging mLogging;
	
	private JsonEscaperSettings settings;
	
	private RawEditor inputArea;
	private JComboBox<String> optionDropdown;
	private JButton escapeUnescapeButton;
	private JButton clearInputButton;
	private JButton pasteClipboardButton;
	private JButton pasteFileButton;
	private JFileChooser pasteFileChooser;
	private JButton copyClipboardButton;
	private JButton copyFileButton;
	private JFileChooser copyFileChooser;
	private JButton clearOutputButton;
	private JLabel errorLabel;
	private RawEditor outputArea;
	
	private JPanel settingsPanel;
	
	EscaperUnescaperTab(MontoyaApi api) {
		super();
		mApi = api;
		mLogging = mApi.logging();
		
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
		this.add(inputAreaComponent);
		
		//middle button panel
		JPanel middlePanel = new JPanel();
		errorLabel = new JLabel(new String(""));
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
		pasteFileChooser = new JFileChooser();
		pasteFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		copyClipboardButton = new JButton("Copy Unescaped Output To Clipboard");
		copyClipboardButton.addActionListener(this);
		copyFileButton = new JButton("Copy Unescaped Output To File");
		copyFileButton.addActionListener(this);
		copyFileChooser = new JFileChooser();
		copyFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
		this.add(outputAreaComponent);
	}
		
	RawEditor getInputArea() {
		return inputArea;
	}
	
	RawEditor getOutputArea() {
		return outputArea;
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
					outputArea.setContents(ByteArray.byteArrayOfLength(0));
					break;
				case JsonEscaper.ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_KEY_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_ALL_LABEL:
				case JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL:
					escapeUnescapeButton.setText("Escape");
					copyClipboardButton.setText("Copy Escaped Output To Clipboard");
					copyFileButton.setText("Copy Escaped Output To File");
					outputArea.setContents(ByteArray.byteArrayOfLength(0));
					break;
			}
		} else if(source==escapeUnescapeButton) { //Escape/Unescape button
			errorLabel.setText("");
			String outputVal = new String(inputArea.getContents().getBytes(),StandardCharsets.UTF_8);
			if(outputVal.length()==0) {
				outputArea.setContents(ByteArray.byteArrayOfLength(0));
				return;
			}
			if(settings.getVerboseLogging()) mLogging.logToOutput("EscaperTab actionPerformed inputArea contents: "+outputVal);
			String selectedItem = (String) optionDropdown.getSelectedItem();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
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
					
					if(settings.getVerboseLogging()) mLogging.logToOutput("EscaperTab outputVal: "+innerOutputVal);
					outputArea.setContents(ByteArray.byteArrayOfLength(0));
					outputArea.setContents(ByteArray.byteArray(innerOutputVal.getBytes(StandardCharsets.UTF_8)));
				}
			});
		} else if(source==clearInputButton) { //Clear Input button
			inputArea.setContents(ByteArray.byteArrayOfLength(0));
			errorLabel.setText("");
		} else if(source==pasteClipboardButton) { //Paste From Clipboard button: considering removing this button
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = cb.getContents(null);
			String textToPaste = null;
			try {
				textToPaste = (String) t.getTransferData(DataFlavor.stringFlavor);
				if(settings.getVerboseLogging()) mLogging.logToOutput("Text to paste: "+textToPaste);
			} catch(Exception ex) {
				//No data in clipboard, or data is not text: do nothing
				return;
			}
			
			if(textToPaste!=null && textToPaste.length()!=0) {
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
			}
		} else if(source==pasteFileButton) { //Paste from file button
			int res = pasteFileChooser.showOpenDialog(null);
			if(res == JFileChooser.APPROVE_OPTION) {
				File srcFile = pasteFileChooser.getSelectedFile();
				
				ByteArrayOutputStream baos = null;
				FileInputStream fis = null;
				try {
					byte[] buffer = new byte[4096];
					baos = new ByteArrayOutputStream();
					fis = new FileInputStream(srcFile);
					int read = 0;
					while ((read = fis.read(buffer)) != -1) {
						baos.write(buffer,0,read);
					}
				} catch(IOException ioe) {
					String absPath = srcFile.getAbsolutePath();
					errorLabel.setText(String.format("Error reading from file %s!",absPath));
					mLogging.logToError(String.format("Error reading from file %s!",absPath));
					return;
				}
				
				try {
					baos.close();
				} catch (Exception ex) {
					
				}
				try {
					fis.close();
				} catch (Exception ex) {
					
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
				contents = ByteArray.byteArray(prefix.concat(new String(baos.toByteArray())).concat(suffix).getBytes(StandardCharsets.UTF_8));
				inputArea.setContents(ByteArray.byteArrayOfLength(0));
				inputArea.setContents(contents);
			}
		} else if(source==copyClipboardButton) { //Copy output to Clipboard button
			ByteArray contents = outputArea.getContents();
			if(contents.length()>0) {
				String strContents = new String(contents.getBytes(),StandardCharsets.UTF_8);
				Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(strContents);
				cb.setContents(ss,ss);
			}
		} else if(source==copyFileButton) { //Copy Output to File button
			int res = copyFileChooser.showOpenDialog(null);
			if(res == JFileChooser.APPROVE_OPTION) {
				File destFile = copyFileChooser.getSelectedFile();
				String absPath = destFile.getAbsolutePath();
				ByteArray contents = outputArea.getContents();
				
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(destFile);
					fos.write(contents.getBytes());
					fos.flush();
				} catch(Exception ex) {
					errorLabel.setText(String.format("Error writing to file %s!",absPath));
					mLogging.logToError(String.format("Error writing to file %s!",absPath));
					return;
				}
				
				try {
					fos.close();
				} catch(Exception ex) {
					
				}
				mLogging.logToOutput(String.format("Output saved to file %s",absPath));
			}
		} else if(source==clearOutputButton) { //Clear Output button
			outputArea.setContents(ByteArray.byteArrayOfLength(0));
			errorLabel.setText("");
		}
	}
}

