package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Range;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.logging.Logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;
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
	private JLabel errorLabel;
	private JButton applyButton;
	private JButton pasteButton;
	private JButton deduplicateSortButton;
	private JCheckBox includeKeyCharsCheckbox;
	
	//Global settings
	private JCheckBox fineTuneUnescapingCheckbox;
	private JCheckBox verboseLoggingCheckbox;
	
	//Constants
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
	//text constants
	private static final String EX_MESSAGE_HEAD = "For input string: \"";
	
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
		//hexRangesFormatButton.setEnabled(false);
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
		
		this.add(innerPanel);
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
	
	//Convert escape character list to array of Ranges
	//For use when:
	//	- switching from characters to hexadecimal ranges
	//	- pasting text: converting text to hexadecimal ranges
	private Range[] convertCharsToRanges(String inText) {
		if(inText==null) return new Range[0];
		if(inText.length()==0) return new Range[0];
		
		//Remove duplicate characters
		String uniqChars = "";
		for(int l=0;l<inText.length();l++) {
			String ch = String.valueOf(inText.charAt(l));
			if(!uniqChars.contains(ch))
				uniqChars += ch;
		}
		inText = uniqChars;
		
		//sort characters
		char[] inTextChars = inText.toCharArray();
		Arrays.sort(inTextChars);
		inText = new String(inTextChars);
		
		String[] inputArr = new String[inText.length()];
		int i=0;
		while(i<inputArr.length) {
			inputArr[i] = String.valueOf(inText.charAt(i));
			i++;
		}
		
		ArrayList<Range> outputList = new ArrayList<Range>();
		i=0;
		while(i<inputArr.length) {
			char curr = inputArr[i].charAt(0);
			int j=1;
			char next = curr;
			while((i+j)<inputArr.length) {
				next = inputArr[i+j].charAt(0);
				if(next-curr!=j) {
					next = inputArr[i+j-1].charAt(0);
					break;
				}
				j++;
			}
			
			if(next == curr) {
				outputList.add(Range.range((int) curr,(int) curr+1));
			} else {
				outputList.add(Range.range((int) curr,(int) next+1));
				i+=j-1;
			}
			i++;
		}
		
		Range[] outArr = new Range[outputList.size()];
		return outputList.toArray(outArr);
	}
	
	//Convert array of Ranges to range text
	//For use when:
	//	- switching from characters to hexadecimal ranges
	//	- pasting text: converting text to hexadecimal ranges
	private String convertRangesToText(Range[] inRanges) {
		if(inRanges==null) return "";
		if(inRanges.length==0) return "";
		
		ArrayList<String> outputList = new ArrayList<String>();
		for(int i=0;i<inRanges.length;i++) {
			if(inRanges[i] != null) {
				int start = inRanges[i].startIndexInclusive();
				int end = inRanges[i].endIndexExclusive()-1;
				if(start>end) { //may not even be necessary, but included for good measure
					start = inRanges[i].endIndexExclusive();
					end = inRanges[i].startIndexInclusive()-1;
				}
				
				String escaped = Integer.toHexString(start);
				while(escaped.length()<4) {
						escaped = "0".concat(escaped);
				}
				
				if(start==end) {
					outputList.add(escaped);
				} else {
					String nextEscaped = Integer.toHexString(end);
					while(nextEscaped.length()<4) {
						nextEscaped = "0".concat(nextEscaped);
					}
					
					outputList.add(String.format("%s-%s",escaped,nextEscaped));
				}
			}
		}
		
		return String.join(",",outputList.toArray(new String[outputList.size()]));
	}
	
	//convert ranges text to array of Ranges
	//For use when:
	//	- updating hexadecimal ranges
	//	- toggling key chars when using hexadecimal ranges
	private Range[] convertRangesTextToRanges(String inText) throws UnsupportedEncodingException,NumberFormatException {
		if(inText == null) return new Range[0];
		if(inText.length() == 0) return new Range[0];
		
		ArrayList<Range> outputList = new ArrayList<Range>();
		inText = inText.strip();
		String[] inTextSplit = inText.split(",");
		String output = "";
		for(int i=0;i<inTextSplit.length;i++) {
			String hex = inTextSplit[i].strip();
			if(hex.length()!=0) {
				if(hex.contains("-")) {
					String[] range = hex.split("-",2);
					
					//hex beyond 16-bit range (4 digits) is unsupported:
					if(range[0].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[0]));
					} else if(range[1].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[1]));
					}
					
					int start = Integer.parseInt(range[0],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					int end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
						
					if(start>end) {
						start = end;
						end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					}
					
					outputList.add(Range.range((int) start,(int) end+1));
				} else {
					int start = Integer.parseInt(hex,16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					outputList.add(Range.range(start,start+1));
				}
			}
		}
		
		Range[] outArr = new Range[outputList.size()];
		return outputList.toArray(outArr);
	}
	
	//Convert array of Ranges to escape character list
	//For use when switching from hexadecimal ranges to characters
	private String convertRangesToChars(String inText) throws UnsupportedEncodingException,NumberFormatException {
		if(inText==null) return null;
		if(inText.length()==0) return inText;
		
		inText = inText.strip();
		String[] inTextSplit = inText.split(",");
		String output = "";
		for(int i=0;i<inTextSplit.length;i++) {
			String hex = inTextSplit[i].strip();
			if(hex.length()!=0) {
				if(hex.contains("-")) {
					String[] range = hex.split("-",2);
					
					//hex beyond 16-bit range (4 digits) is unsupported:
					if(range[0].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[0]));
					} else if(range[1].length()>4) {
						throw new UnsupportedEncodingException(String.format("%s%s\"",EX_MESSAGE_HEAD,range[1]));
					}
					
					int start = Integer.parseInt(range[0],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					int end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					if(start>end) {
						start = end;
						end = Integer.parseInt(range[1],16); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
					}
					String[] outChars = new String[(end-start)+1];
					int j=0;
					while(start<=end) {
						outChars[j] = String.valueOf((char) start);
						start++;
						j++;
					}
					output = output.concat(String.join("",outChars));
				} else {
					if(hex.length()>4) {
						throw new IllegalArgumentException();
					}
					output = output.concat(String.valueOf((char) Integer.parseInt(hex,16))); //java.lang.NumberFormatException thrown when invalid non-hex data entered into ranges field.
				}
			}
		}
		
		//remove duplicate characters
		String uniqChars = "";
		for(int l=0;l<output.length();l++) {
			String ch = String.valueOf(output.charAt(l));
			if(!uniqChars.contains(ch))
				uniqChars += ch;
		}
		output = uniqChars;
		
		return output;
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
							charsToEscapeField.setText(convertRangesToChars(fieldText));
						} catch(UnsupportedEncodingException|NumberFormatException ex) {
							String message = ex.getMessage();
							//mLogging.logToError(message,ex);
							highlightHexError(fieldText,message);
							hexRangesFormatButton.setSelected(true);
							charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_RANGES_TEXT);
							return;
						}
						inputFormat = CHARS_INPUT_FORMAT;
						settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
						mLogging.logToOutput("Chars to escape field set to: "+charsToEscapeField.getText());
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
				System.err.println("charsToEscapeLabel.setText(ESCAPE_CHARS_LABEL_RANGES_TEXT);");
				String fieldText = charsToEscapeField.getText();
				System.err.println("String fieldText = charsToEscapeField.getText();");
				charsToEscapeField.setText(CONVERT_CHARS_TO_RANGES_TEXT);
				System.err.println("charsToEscapeField.setText(CONVERT_CHARS_TO_RANGES_TEXT);");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Range[] ranges = convertCharsToRanges(fieldText);
						System.err.println("Range[] ranges = convertCharsToRanges(fieldText); == "+Arrays.toString(ranges));
						String rangesText = convertRangesToText(ranges);
						System.err.println("String rangesText = convertRangesToText(ranges); == \""+rangesText+"\"");
						//mLogging.logToOutput("ranges: "+Arrays.toString(ranges));
						settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
						System.err.println("settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());");
						inputFormat = HEXADECIMAL_INPUT_FORMAT;
						System.err.println("inputFormat = HEXADECIMAL_INPUT_FORMAT;");
						charsToEscapeField.setText(rangesText);
						System.err.println("charsToEscapeField.setText(rangesText);");
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
							ranges = convertRangesTextToRanges(fieldText);
						} catch(UnsupportedEncodingException|NumberFormatException ex) {
							String message = ex.getMessage();
							//mLogging.logToError(message,ex);
							highlightHexError(fieldText,message);
							return;
						}
						mLogging.logToOutput("ranges: "+Arrays.toString(ranges));
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
							Range[] ranges = convertCharsToRanges(pastedData);
							//mLogging.logToOutput("Pasted ranges: "+Arrays.toString(ranges));
							String pastedRangesText = convertRangesToText(ranges);
							//mLogging.logToOutput("Pasted ranges text: "+pastedRangesText);
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
					Range[] ranges = convertRangesTextToRanges(fieldText);
					settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
				} catch(UnsupportedEncodingException|NumberFormatException ex) {
					String message = ex.getMessage();
					//mLogging.logToError(message,ex);
					highlightHexError(fieldText,message);
					includeKeyCharsCheckbox.setSelected(!selected);
				}
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
	
	private void highlightHexError(String fieldText,String message) {
		if(message.indexOf(EX_MESSAGE_HEAD)==0) {
			charsToEscapeField.setText(fieldText);
			String input = message.substring(EX_MESSAGE_HEAD.length());
			input = input.substring(0,input.lastIndexOf('\"'));
			mLogging.logToOutput("invalid input: "+input);
			Highlighter highlighter = charsToEscapeField.getHighlighter();
			HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
			int p0 = fieldText.indexOf(input);
			int p1 = p0 + input.length();
			try {
				highlighter.addHighlight(p0,p1,painter);
			} catch(Exception e) {
				mLogging.logToError(e.getMessage(),e);
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
				mLogging.logToOutput("Chars to escape field set in removeUpdate() to: "+charsToEscapeField.getText());
			}
		} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
			charsToEscapeField.getHighlighter().removeAllHighlights();
			errorLabel.setText("");
			applyButton.setEnabled(true);
		}
		//mLogging.logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field%s",JsonEscaper.CHARS_TO_ESCAPE_KEY,String.format(" (%s())",method)));
		System.err.println(String.format("%s updated from \"Characters to JSON Unicode-escape\" field%s",JsonEscaper.CHARS_TO_ESCAPE_KEY,String.format(" (%s())",method)));
	}
}

