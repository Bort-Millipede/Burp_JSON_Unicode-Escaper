package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Range;
import burp.api.montoya.logging.Logging;

import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

class EscaperSettingsTab extends JPanel implements ActionListener,DocumentListener {
	private MontoyaApi mApi;
	private Logging mLogging;
	private JsonEscaperSettings settings;
	private byte inputFormat;
	
	//"Characters to JSON Unicode-escape" settings
	private JRadioButton charsFormatButton;
	private JRadioButton hexRangesFormatButton;
	private JLabel charsToEscapeLabel;
	private JTextArea charsToEscapeField;
	private Highlighter charsToEscapeFieldHighlighter;
	private HighlightPainter errorHighlightPainter;
	private JLabel errorLabel;
	private JButton applyButton;
	private JButton pasteButton;
	private JButton deduplicateSortButton;
	private JCheckBox includeKeyCharsCheckbox;
	
	//Global settings
	private JCheckBox fineTuneUnescapingCheckbox;
	private JCheckBox verboseLoggingCheckbox;
	
	//Settings buttons and file choosers
	private JButton importSettingsButton;
	private JFileChooser importSettingsChooser;
	private JButton exportSettingsButton;
	private JFileChooser exportSettingsChooser;
	private JButton resetSettingsButton;
	private JLabel settingsErrorLabel;
	
	//START constants
	//Input format constants
	private static final byte CHARS_INPUT_FORMAT = 0;
	private static final byte HEXADECIMAL_INPUT_FORMAT = 1;
	//UI text constants
	private static final String ESCAPE_CHARS_LABEL_CHARS_TEXT = "Characters to JSON Unicode-escape:";
	private static final String ESCAPE_CHARS_LABEL_RANGES_TEXT = "Character Hexadecimal Ranges to JSON Unicode-escape:";
	private static final String ERROR_LABEL_TEXT = "Error updating escape characters: invalid range identified!";
	private static final String APPLY_BUTTON_CHARS_TEXT = "Escape Chars Auto-Updated";
	private static final String APPLY_BUTTON_RANGES_TEXT = "Update Escape Chars";
	private static final String PASTE_BUTTON_CHARS_TEXT = "Paste";
	private static final String PASTE_BUTTON_RANGES_TEXT = "Paste Text as Range(s)";
	private static final String CONVERT_CHARS_TO_RANGES_TEXT = "Converting Characters to Hexadecimal Numbers/Ranges...";
	private static final String CONVERT_RANGES_TO_CHARS_TEXT = "Converting Hexadecimal Numbers/Ranges to Characters...";
	private static final String IMPORTING_ESCAPE_CHARS_TEXT = "Importing escape characters...";
	private static final String IMPORTING_ESCAPE_RANGES_TEXT = "Importing escape character ranges...";
	private static final String IMPORT_BUTTON_ENABLED_TEXT = "Import Settings";
	private static final String IMPORT_BUTTON_DISABLED_TEXT = "Importing Settings...";
	//settings JSON constants
	private static final String INPUT_FORMAT_JSON_KEY = "inputFormat";
	private static final String ESCAPE_CHARS_JSON_KEY = "charsToEscape";
	private static final String INCLUDE_KEY_CHARS_JSON_KEY = "includeKeyChars";
	private static final String FINE_TUNE_UNESCAPING_JSON_KEY = "fineTuneUnescaping";
	private static final String VERBOSE_LOGGING_JSON_KEY = "verboseLogging";
	//END constants
	
	EscaperSettingsTab(MontoyaApi api) {
		super();
		mApi = api;
		mLogging = mApi.logging();
		
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
		charsFormatButton = new JRadioButton("Characters",true);
		charsFormatButton.addActionListener(this);
		hexRangesFormatButton = new JRadioButton("Hexdecimal numbers/ranges",false);
		hexRangesFormatButton.addActionListener(this);
		ButtonGroup bg = new ButtonGroup();
		bg.add(charsFormatButton);
		bg.add(hexRangesFormatButton);
		JPanel formatButtonPanel = new JPanel(new GridLayout(2,1));
		formatButtonPanel.add(charsFormatButton);
		formatButtonPanel.add(hexRangesFormatButton);
		innerSettingsPanel.add(formatButtonPanel);
		
		//all remaining "escape custom chars" elements
		charsToEscapeLabel = new JLabel(ESCAPE_CHARS_LABEL_CHARS_TEXT,SwingConstants.RIGHT);
		innerSettingsPanel.add(charsToEscapeLabel);
		charsToEscapeField = new JTextArea("");
		charsToEscapeField.setLineWrap(true);
		charsToEscapeField.getDocument().addDocumentListener(this);
		charsToEscapeField.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight()));
		innerSettingsPanel.add(new JScrollPane(charsToEscapeField));
		charsToEscapeFieldHighlighter = charsToEscapeField.getHighlighter();
		errorHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
		errorLabel = new JLabel("",SwingConstants.RIGHT);
		errorLabel.setForeground(Color.RED);
		innerSettingsPanel.add(errorLabel);
		JPanel buttonPanel = new JPanel(new GridLayout(1,3));
		applyButton = new JButton(APPLY_BUTTON_CHARS_TEXT);
		applyButton.addActionListener(this);
		applyButton.setEnabled(false);
		buttonPanel.add(applyButton);
		pasteButton = new JButton(PASTE_BUTTON_CHARS_TEXT);
		pasteButton.addActionListener(this);
		buttonPanel.add(pasteButton);
		deduplicateSortButton = new JButton("Deduplicate & Sort");
		deduplicateSortButton.addActionListener(this);
		buttonPanel.add(deduplicateSortButton);
		innerSettingsPanel.add(buttonPanel);
		innerSettingsPanel.add(new JLabel("Automatically include JSON key characters:",SwingConstants.RIGHT));
		includeKeyCharsCheckbox = new JCheckBox("",true);
		includeKeyCharsCheckbox.addActionListener(this);
		innerSettingsPanel.add(includeKeyCharsCheckbox);
		innerSettingsPanel.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight()));
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
		innerGlobalPanel.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight()));
		innerPanel.add(innerGlobalPanel);
		
		//separaters between global settings and buttons
		innerPanel.add(new JLabel(" "));
		innerPanel.add(new JLabel(" "));
		
		//settings buttons
		JPanel settingsButtonsPanel = new JPanel(new GridLayout(1,3));
		importSettingsButton = new JButton(IMPORT_BUTTON_ENABLED_TEXT);
		importSettingsButton.addActionListener(this);
		importSettingsChooser = new JFileChooser();
		importSettingsChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		exportSettingsButton = new JButton("Export Settings");
		exportSettingsButton.addActionListener(this);
		exportSettingsChooser = new JFileChooser();
		exportSettingsChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		resetSettingsButton = new JButton("Reset Defaults");
		resetSettingsButton.addActionListener(this);
		settingsButtonsPanel.add(importSettingsButton);
		settingsButtonsPanel.add(exportSettingsButton);
		settingsButtonsPanel.add(resetSettingsButton);
		settingsButtonsPanel.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight()));
		innerPanel.add(settingsButtonsPanel);
		settingsErrorLabel = new JLabel("",SwingConstants.CENTER);
		settingsErrorLabel.setForeground(Color.RED);
		settingsErrorLabel.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight()));
		innerPanel.add(settingsErrorLabel);
		
		this.add(innerPanel);
		this.setMaximumSize(new Dimension(mApi.userInterface().swingUtils().suiteFrame().getWidth(),mApi.userInterface().swingUtils().suiteFrame().getHeight()));
	}
	
	private void switchEscapeUIElements(byte format) {
		if(format==CHARS_INPUT_FORMAT) {
			charsFormatButton.setSelected(true);
			charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_CHARS_TEXT);
			applyButton.setText(APPLY_BUTTON_CHARS_TEXT);
			applyButton.setEnabled(false);
			pasteButton.setText(PASTE_BUTTON_CHARS_TEXT);
			deduplicateSortButton.setEnabled(true);
		} else if(format==HEXADECIMAL_INPUT_FORMAT) {
			hexRangesFormatButton.setSelected(true);
			charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_RANGES_TEXT);
			applyButton.setText(APPLY_BUTTON_RANGES_TEXT);
			pasteButton.setText(PASTE_BUTTON_RANGES_TEXT);
			deduplicateSortButton.setEnabled(false);
		}
	}
	
	private void deduplicateSortEscapeChars() {
		if(inputFormat != CHARS_INPUT_FORMAT) return;
		
		String charsToEscape = charsToEscapeField.getText();
		if(charsToEscape == null) return;
		if(charsToEscape.length() == 0) return;
		
		pasteButton.setEnabled(false);
		deduplicateSortButton.setEnabled(false);
		charsToEscapeField.setEditable(false);
		charsToEscapeField.setText("Deduplicating and Sorting...");
		
		//Remove duplicate characters
		if(charsToEscape!=null && charsToEscape.length()!=0) {
			String uniqEscapeChars = "";
			for(int l=0;l<charsToEscape.length();l++) {
				String ch = String.valueOf(charsToEscape.charAt(l));
				if(!uniqEscapeChars.contains(ch))
					uniqEscapeChars += ch;
			}
			char[] uniqChars = uniqEscapeChars.toCharArray();
			Arrays.sort(uniqChars);
			uniqEscapeChars = new String(uniqChars);
			
			charsToEscapeField.setCaretPosition(0);
			charsToEscapeField.setText("");
			charsToEscapeField.setText(uniqEscapeChars);

			settings.setCharsToEscape(uniqEscapeChars,includeKeyCharsCheckbox.isSelected());
		}
		
		charsToEscapeField.setEditable(true);
		pasteButton.setEnabled(true);
		deduplicateSortButton.setEnabled(true);
	}
	
		private void highlightHexError(String fieldText,String message) {
		if(message.indexOf(EscaperUIHelpers.EX_MESSAGE_HEAD)==0) {
			charsToEscapeField.setText(fieldText);
			String input = message.substring(EscaperUIHelpers.EX_MESSAGE_HEAD.length());
			input = input.substring(0,input.lastIndexOf('\"'));
			if(settings.getVerboseLogging()) mLogging.logToError("invalid input: "+input);
			int highlightStart = fieldText.indexOf(input);
			int highlightEnd = highlightStart + input.length();
			try {
				charsToEscapeFieldHighlighter.addHighlight(highlightStart,highlightEnd,errorHighlightPainter);
			} catch(Exception e) {
				if(settings.getVerboseLogging()) mLogging.logToError(e.getMessage(),e);
			}
		}
		errorLabel.setText(ERROR_LABEL_TEXT);
	}
	
	private void documentListenerUpdates(String method) {
		if(inputFormat == CHARS_INPUT_FORMAT) {
			applyButton.setText(APPLY_BUTTON_CHARS_TEXT);
			applyButton.setEnabled(false);
			if(includeKeyCharsCheckbox.isSelected()) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
				if(settings.getVerboseLogging()) mLogging.logToOutput("Chars to escape field set in "+method+"() to: "+charsToEscapeField.getText());
			}
		} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
			charsToEscapeField.getHighlighter().removeAllHighlights();
			errorLabel.setText("");
			applyButton.setEnabled(true);
		}
		if(settings.getVerboseLogging()) mLogging.logToOutput(String.format("Escape Characters updated from \"Characters to JSON Unicode-escape\" field%s",String.format(" (%s())",method)));
	}
	
	//ActionListener method
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == charsFormatButton) {
			if(inputFormat != CHARS_INPUT_FORMAT) {
				charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_CHARS_TEXT);
				String fieldText = charsToEscapeField.getText();
				charsToEscapeField.setText(CONVERT_RANGES_TO_CHARS_TEXT);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String escapeChars = null;
						try {
							charsToEscapeField.setText(EscaperUIHelpers.convertRangesToChars(fieldText));
						} catch(UnsupportedEncodingException|NumberFormatException ex) {
							String message = ex.getMessage();
							if(settings.getVerboseLogging()) mLogging.logToError(message,ex);
							highlightHexError(fieldText,message);
							hexRangesFormatButton.setSelected(true);
							charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_RANGES_TEXT);
							return;
						}
						inputFormat = CHARS_INPUT_FORMAT;
						settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
						if(settings.getVerboseLogging()) mLogging.logToOutput("Chars to escape field set to: "+charsToEscapeField.getText());
						applyButton.setText(APPLY_BUTTON_CHARS_TEXT);
						applyButton.setEnabled(false);
						pasteButton.setText(PASTE_BUTTON_CHARS_TEXT);
						deduplicateSortButton.setEnabled(true);
					}
				});
			}
		} else if (source == hexRangesFormatButton) {
			if(inputFormat != HEXADECIMAL_INPUT_FORMAT) {
				charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_RANGES_TEXT);
				String fieldText = charsToEscapeField.getText();
				charsToEscapeField.setText(CONVERT_CHARS_TO_RANGES_TEXT);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Range[] ranges = EscaperUIHelpers.convertCharsToRanges(fieldText);
						String rangesText = EscaperUIHelpers.convertRangesToText(ranges);
						settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
						inputFormat = HEXADECIMAL_INPUT_FORMAT;
						charsToEscapeField.setText(rangesText);
						applyButton.setText(APPLY_BUTTON_RANGES_TEXT);
						pasteButton.setText(PASTE_BUTTON_RANGES_TEXT);
						deduplicateSortButton.setEnabled(false);
						applyButton.setEnabled(false);
					}
				});
			}
		} else if(source == applyButton) {
			if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
				String fieldText = charsToEscapeField.getText();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Range[] ranges = null;
						try {
							ranges = EscaperUIHelpers.convertRangesTextToRanges(fieldText);
						} catch(UnsupportedEncodingException|NumberFormatException ex) {
							String message = ex.getMessage();
							if(settings.getVerboseLogging()) mLogging.logToError(message,ex);
							highlightHexError(fieldText,message);
							return;
						}
						settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
						applyButton.setEnabled(false);
					}
				});
			}
		} else if(source == pasteButton) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = cb.getContents(null);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String pastedData = null;
					try {
						pastedData = (String) t.getTransferData(DataFlavor.stringFlavor);
					} catch(Exception ex) {
						//No data in clipboard, or data is not text: do nothing
					}
					if(pastedData != null) {
						pasteButton.setEnabled(false);
						pasteButton.setText("Pasting...");
						String fieldText = charsToEscapeField.getText();
						if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
							Range[] ranges = EscaperUIHelpers.convertCharsToRanges(pastedData);
							String pastedRangesText = EscaperUIHelpers.convertRangesToText(ranges);
							String fieldTextStripped = fieldText.stripTrailing();
							if((fieldTextStripped.length()!=0) && (fieldTextStripped.charAt(fieldTextStripped.length()-1)!=','))
								pastedRangesText = ",".concat(pastedRangesText);
							pastedData = pastedRangesText;
						}
						charsToEscapeField.setText(fieldText+pastedData);
						if(inputFormat == CHARS_INPUT_FORMAT) {
							pasteButton.setText(PASTE_BUTTON_CHARS_TEXT);
						} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
							pasteButton.setText(PASTE_BUTTON_RANGES_TEXT);
						}
						pasteButton.setEnabled(true);
					}
				}
			});
		} else if(source == deduplicateSortButton) {
			if(inputFormat == CHARS_INPUT_FORMAT) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						deduplicateSortEscapeChars();
					}
				});
			}
		} else if(source == includeKeyCharsCheckbox) {
			if(inputFormat == CHARS_INPUT_FORMAT) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
			} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
				String fieldText = charsToEscapeField.getText();
				boolean selected = includeKeyCharsCheckbox.isSelected();
				try {
					Range[] ranges = EscaperUIHelpers.convertRangesTextToRanges(fieldText);
					settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
				} catch(UnsupportedEncodingException|NumberFormatException ex) {
					String message = ex.getMessage();
					if(settings.getVerboseLogging()) mLogging.logToError(message,ex);
					highlightHexError(fieldText,message);
					includeKeyCharsCheckbox.setSelected(!selected);
				}
			}
		} else if(source == fineTuneUnescapingCheckbox) {
			settings.setFineTuneUnescaping(fineTuneUnescapingCheckbox.isSelected());
		} else if(source == verboseLoggingCheckbox) {
			settings.setVerboseLogging(verboseLoggingCheckbox.isSelected());
		} else if(source == importSettingsButton) {
			int res = importSettingsChooser.showOpenDialog(null);
			if(res == JFileChooser.APPROVE_OPTION) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						importSettingsButton.setText(IMPORT_BUTTON_DISABLED_TEXT);
						importSettingsButton.setEnabled(false);
						settingsErrorLabel.setText("");
						
						File srcFile = importSettingsChooser.getSelectedFile();
						String absPath = srcFile.getAbsolutePath();
						
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
							settingsErrorLabel.setText(String.format("Error reading from file %s!",absPath));
							mLogging.logToError(String.format("Error reading from file %s!",absPath));
							importSettingsButton.setText(IMPORT_BUTTON_ENABLED_TEXT);
							importSettingsButton.setEnabled(true);
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
						
						JSONObject inSettings = null;
						try {
							inSettings = new JSONObject(new String(baos.toByteArray(),StandardCharsets.UTF_8));
						} catch(JSONException jsonE) {
							settingsErrorLabel.setText(String.format("Error parsing JSON read from file %s!",absPath));
							mLogging.logToError(String.format("Error parsing JSON read from file %s!",absPath));
							if(settings.getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
							importSettingsButton.setText(IMPORT_BUTTON_ENABLED_TEXT);
							importSettingsButton.setEnabled(true);
							return;
						}
						
						try { //parse verbose logging
							boolean verbose = inSettings.getBoolean(VERBOSE_LOGGING_JSON_KEY);
							settings.setVerboseLogging(verbose);
							verboseLoggingCheckbox.setSelected(verbose);
						} catch(JSONException jsonE) {
							mLogging.logToError(String.format("Verbose logging setting absent from %s or invalid: skipping import",absPath));
							if(settings.getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
						}
						
						try { //parse fine-tune unescaping
							boolean fineTune = inSettings.getBoolean(FINE_TUNE_UNESCAPING_JSON_KEY);
							settings.setFineTuneUnescaping(fineTune);
							fineTuneUnescapingCheckbox.setSelected(fineTune);
						} catch(JSONException jsonE) {
							mLogging.logToError(String.format("Fine-tune unescaping setting absent from %s or invalid: skipping import",absPath));
							if(settings.getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
						}
						
						try { //parse input format
							byte format = (byte) inSettings.getInt(INPUT_FORMAT_JSON_KEY);
							if(format==HEXADECIMAL_INPUT_FORMAT) {
								inputFormat = HEXADECIMAL_INPUT_FORMAT;
							} else {
								inputFormat = CHARS_INPUT_FORMAT;
							}
							switchEscapeUIElements(inputFormat);
						} catch(JSONException jsonE) { //if error, set characters format
							inputFormat = CHARS_INPUT_FORMAT;
							switchEscapeUIElements(inputFormat);
							mLogging.logToError(String.format("Input format setting absent from %s or invalid: setting character input format",absPath));
							if(settings.getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
						}
						
						//parse include key characters and characters to escape
						String fieldText = charsToEscapeField.getText();
						if(inputFormat == CHARS_INPUT_FORMAT) {
							charsToEscapeField.setText(IMPORTING_ESCAPE_CHARS_TEXT);
						} else {
							charsToEscapeField.setText(IMPORTING_ESCAPE_RANGES_TEXT);
						}
						charsToEscapeField.setEditable(false);
						boolean keyChars = false;
						String escapeChars = null;
						try { 
							keyChars = inSettings.getBoolean(INCLUDE_KEY_CHARS_JSON_KEY);
							includeKeyCharsCheckbox.setSelected(keyChars);
						} catch(JSONException jsonE) {
							mLogging.logToError(String.format("Include Key Characters setting absent from %s or invalid: setting to false",absPath));
							if(settings.getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
						}
						JSONArray escapeCharsJsonArray = null;
						try {
							escapeCharsJsonArray = inSettings.getJSONArray(ESCAPE_CHARS_JSON_KEY);
						} catch(JSONException jsonE) {
							//no character list found, error reported below
							if(settings.getVerboseLogging()) mLogging.logToError(jsonE.getMessage(),jsonE);
						}
						if(escapeCharsJsonArray!=null && escapeCharsJsonArray.length()!=0) {
							int i=0;
							boolean error = false;
							while(i<escapeCharsJsonArray.length()) {
								try {
									int num = escapeCharsJsonArray.getInt(i);
									if(num<0 || num>65535) throw new NumberFormatException(String.format("%i is outside of character range",num));
								} catch(JSONException|NumberFormatException ex) {
									mLogging.logToError(ex.getMessage(),ex);
									if(settings.getVerboseLogging()) mLogging.logToError(ex.getMessage(),ex);
									error=true;
									break;
								}
								i++;
							}
							
							if(!error) {
								if(escapeCharsJsonArray.length()>0) {
									String[] escapeCharsArray = new String[escapeCharsJsonArray.length()];
									i=0;
									while(i<escapeCharsJsonArray.length()) {
										escapeCharsArray[i] = new String(new char[] {(char) escapeCharsJsonArray.getInt(i)});
										i++;
									}
									escapeChars = String.join("",escapeCharsArray);
								} else {
									escapeChars = "";
								}
							} else {
								mLogging.logToError(String.format("Escape Character list read from %s contains invalid value(s): skipping import",absPath));
								charsToEscapeField.setText(fieldText);
							}
						} else {
							mLogging.logToError(String.format("Invalid or empty escape character list read from %s, or escape character list empty: skipping import",absPath));
							charsToEscapeField.setText(fieldText);
						}
						if(escapeChars != null) {
							settings.setCharsToEscape(escapeChars,keyChars);
							if(escapeChars.length()>0) {
								if(inputFormat==HEXADECIMAL_INPUT_FORMAT) {
									charsToEscapeField.setText(EscaperUIHelpers.convertRangesToText(EscaperUIHelpers.convertCharsToRanges(escapeChars)));
									applyButton.setEnabled(false);
								} else if(inputFormat==CHARS_INPUT_FORMAT) {
									charsToEscapeField.setText(escapeChars);
								}
							}
						}
						
						charsToEscapeField.setEditable(true);
						mLogging.logToOutput(String.format("%s settings imported from file %s",JsonEscaper.EXTENSION_NAME,absPath));
						importSettingsButton.setText(IMPORT_BUTTON_ENABLED_TEXT);
						importSettingsButton.setEnabled(true);
					}
				});
			}
		} else if(source == exportSettingsButton) {
			int res = exportSettingsChooser.showOpenDialog(null);
			if(res == JFileChooser.APPROVE_OPTION) {
				File destFile = exportSettingsChooser.getSelectedFile();
				String absPath = destFile.getAbsolutePath();
				
				JSONObject outSettings = new JSONObject();
				outSettings.put(INPUT_FORMAT_JSON_KEY,inputFormat);
				outSettings.put(ESCAPE_CHARS_JSON_KEY,settings.getCharsToEscape());
				outSettings.put(INCLUDE_KEY_CHARS_JSON_KEY,includeKeyCharsCheckbox.isSelected());
				outSettings.put(FINE_TUNE_UNESCAPING_JSON_KEY,settings.getFineTuneUnescaping());
				outSettings.put(VERBOSE_LOGGING_JSON_KEY,settings.getVerboseLogging());
				
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(destFile);
					fos.write(outSettings.toString(1).getBytes(StandardCharsets.UTF_8));
					fos.flush();
				} catch(Exception ex) {
					mLogging.logToError(String.format("Error writing to file %s!",absPath));
					return;
				}
				try {
					fos.close();
				} catch(Exception ex) {
					
				}
				
				mLogging.logToOutput(String.format("%s settings exported to file %s",JsonEscaper.EXTENSION_NAME,absPath));
			}
		} else if(source == resetSettingsButton) {
			int res = JOptionPane.showConfirmDialog(null,String.format("Restore %s Settings to Defaults?",JsonEscaper.EXTENSION_NAME),"Restore Default Settings",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
			if(res == JOptionPane.YES_OPTION) {
				settings.setDefaultSettings();
				inputFormat = CHARS_INPUT_FORMAT;
				switchEscapeUIElements(inputFormat);
				charsToEscapeField.setText("");
				includeKeyCharsCheckbox.setSelected(true);
				fineTuneUnescapingCheckbox.setSelected(true);
				verboseLoggingCheckbox.setSelected(false);
				mLogging.logToOutput(JsonEscaper.EXTENSION_NAME+" settings restored to defaults.");
			}
		}
	}
	
	//DocumentListener methods
	@Override
	public void changedUpdate(DocumentEvent e) {
		documentListenerUpdates("changedUpdate");
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		documentListenerUpdates("insertUpdate");
	}
	
	@Override	
	public void removeUpdate(DocumentEvent e) {
		documentListenerUpdates("removeUpdate");
	}
}

