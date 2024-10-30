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
	private JButton deduplicateButton;
	private JCheckBox includeKeyCharsCheckbox;
	
	//Global settings
	private JCheckBox fineTuneUnescapingCheckbox;
	private JCheckBox verboseLoggingCheckbox;
	
	//Constants
	private static final byte CHARS_INPUT_FORMAT = 0;
	private static final byte HEXADECIMAL_INPUT_FORMAT = 1;
	
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
		charsToEscapeLabel = new JLabel("Characters to JSON Unicode-escape:",SwingConstants.RIGHT);
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
		applyButton = new JButton("Changes Auto-Applied");
		applyButton.addActionListener(this);
		applyButton.setEnabled(false);
		buttonPanel.add(applyButton);
		pasteButton = new JButton("Paste");
		pasteButton.addActionListener(this);
		buttonPanel.add(pasteButton);
		deduplicateButton = new JButton("Deduplicate & Sort");
		deduplicateButton.addActionListener(this);
		buttonPanel.add(deduplicateButton);
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
	
	private void deduplicateEscapeChars() {
		if(inputFormat != CHARS_INPUT_FORMAT) return;
		
		String charsToEscape = charsToEscapeField.getText();
		if(charsToEscape == null) return;
		if(charsToEscape.length() == 0) return;
		
		pasteButton.setEnabled(false);
		deduplicateButton.setEnabled(false);
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
		deduplicateButton.setEnabled(true);
	}
	
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
	
	private String convertRangesToText(Range[] inRanges) {
		if(inRanges==null) return "";
		
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
	
	private Range[] convertRangesTextToRanges(String inText) throws NumberFormatException {
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
					//need to eventually do error handling here for invalid hex
					String[] range = hex.split("-",2);
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
	
	private String convertRangesToChars(String inText) throws NumberFormatException {
		if(inText==null) return null;
		if(inText.length()==0) return inText;
		
		inText = inText.strip();
		String[] inTextSplit = inText.split(",");
		String output = "";
		for(int i=0;i<inTextSplit.length;i++) {
			String hex = inTextSplit[i].strip();
			if(hex.length()!=0) {
				if(hex.contains("-")) {
					//need to eventually do error handling here for invalid hex
					String[] range = hex.split("-",2);
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
				charsToEscapeLabel.setText("Characters to JSON Unicode-escape:");
				String fieldText = charsToEscapeField.getText();
				charsToEscapeField.setText("Converting Hexadecimal Numbers/Ranges to Characters...");
				new Thread(new Runnable() {
					public void run() {
						String escapeChars = null;
						try {
							charsToEscapeField.setText(convertRangesToChars(fieldText));
						} catch(NumberFormatException nfe) {
							String message = nfe.getMessage();
							//mLogging.logToError(message,nfe);
							if(message.indexOf("For input string: \"")==0) {
								charsToEscapeField.setText(fieldText);
								String input = message.substring("For input string: \"".length());
								input = input.substring(0,input.indexOf("\" under radix 16"));
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
								errorLabel.setText("Error updating escape characters: invalid range identified!");
								hexRangesFormatButton.setSelected(true);
							}
							return;
						}
						inputFormat = CHARS_INPUT_FORMAT;
						settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
						mLogging.logToOutput("Chars to escape field set to: "+charsToEscapeField.getText());
						applyButton.setText("Escape Chars Auto-Updated");
						applyButton.setEnabled(false);
						pasteButton.setText("Paste");
						deduplicateButton.setEnabled(true);
					}
				}).start();
			}
		} else if (source == hexRangesFormatButton) {
			if(inputFormat != HEXADECIMAL_INPUT_FORMAT) {
				charsToEscapeLabel.setText("Character Hexadecimal Ranges to JSON Unicode-escape:");
				String fieldText = charsToEscapeField.getText();
				charsToEscapeField.setText("Converting Characters to Hexadecimal Numbers/Ranges...");
				new Thread(new Runnable() {
					public void run() {
						Range[] ranges = convertCharsToRanges(fieldText);
						mLogging.logToOutput("ranges: "+Arrays.toString(ranges));
						settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
						charsToEscapeField.setText(convertRangesToText(ranges));
						inputFormat = HEXADECIMAL_INPUT_FORMAT;
						applyButton.setText("Update Escape Chars");
						pasteButton.setText("Paste Text as Range(s)");
						deduplicateButton.setEnabled(false);
					}
				}).start();
				
			}
		} else if(source == applyButton) {
			if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
				String fieldText = charsToEscapeField.getText();
				new Thread(new Runnable() {
					public void run() {
						Range[] ranges = null;
						try {
							ranges = convertRangesTextToRanges(fieldText);
						} catch(NumberFormatException nfe) {
							String message = nfe.getMessage();
							//mLogging.logToError(message,nfe);
							if(message.indexOf("For input string: \"")==0) {
								String input = message.substring("For input string: \"".length());
								input = input.substring(0,input.indexOf("\" under radix 16"));
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
								
								errorLabel.setText("Error updating escape characters: invalid range identified!");
							}
							return;
						}
						mLogging.logToOutput("ranges: "+Arrays.toString(ranges));
						settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
						applyButton.setEnabled(false);
					}
				}).start();
			}
		} else if(source == pasteButton) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = cb.getContents(null);
			new Thread(new Runnable() {
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
							mLogging.logToOutput("Pasted ranges: "+Arrays.toString(ranges));
							String pastedRangesText = convertRangesToText(ranges);
							mLogging.logToOutput("Pasted ranges text: "+pastedRangesText);
							String fieldTextStripped = fieldText.stripTrailing();
							if((fieldTextStripped.length()!=0) && (fieldTextStripped.charAt(fieldTextStripped.length()-1)!=','))
								pastedRangesText = ",".concat(pastedRangesText);
							pastedData = pastedRangesText;
						}
						charsToEscapeField.setText(fieldText+pastedData);
						if(inputFormat == CHARS_INPUT_FORMAT) {
							pasteButton.setText("Paste");
						} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
							pasteButton.setText("Paste Text as Ranges");
						}
						pasteButton.setEnabled(true);
					}
				}
			}).start();
		} else if(source == deduplicateButton) {
			if(inputFormat == CHARS_INPUT_FORMAT) {
				new Thread(new Runnable() {
					public void run() {
						deduplicateEscapeChars();
					}
				}).start();
			}
		} else if(source == includeKeyCharsCheckbox) {
			if(inputFormat == CHARS_INPUT_FORMAT) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
			} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
				Range[] ranges = convertRangesTextToRanges(charsToEscapeField.getText());
				settings.setCharsToEscape(ranges,includeKeyCharsCheckbox.isSelected());
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
		if(inputFormat == CHARS_INPUT_FORMAT) {
			applyButton.setText("Changes Auto-Applied");
			applyButton.setEnabled(false);
			if(includeKeyCharsCheckbox.isSelected()) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
				mLogging.logToOutput("Chars to escape set to: "+charsToEscapeField.getText());
			}
		} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
			charsToEscapeField.getHighlighter().removeAllHighlights();
			errorLabel.setText("");
			applyButton.setEnabled(true);
		}
		//mLogging.logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (changedUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		if(inputFormat == CHARS_INPUT_FORMAT) {
			applyButton.setText("Changes Auto-Applied");
			applyButton.setEnabled(false);
			if(includeKeyCharsCheckbox.isSelected()) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
				mLogging.logToOutput("Chars to escape set to: "+charsToEscapeField.getText());
			}
		} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
			charsToEscapeField.getHighlighter().removeAllHighlights();
			errorLabel.setText("");
			applyButton.setEnabled(true);
		}
		//mLogging.logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (insertUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
	
	@Override	
	public void removeUpdate(DocumentEvent e) {
		if(inputFormat == CHARS_INPUT_FORMAT) {
			applyButton.setText("Changes Auto-Applied");
			applyButton.setEnabled(false);
			if(includeKeyCharsCheckbox.isSelected()) {
				settings.setCharsToEscape(charsToEscapeField.getText(),includeKeyCharsCheckbox.isSelected());
				mLogging.logToOutput("Chars to escape set to: "+charsToEscapeField.getText());
			}
		} else if(inputFormat == HEXADECIMAL_INPUT_FORMAT) {
			charsToEscapeField.getHighlighter().removeAllHighlights();
			errorLabel.setText("");
			applyButton.setEnabled(true);
		}
		//mLogging.logToOutput(String.format("%s updated from \"Characters to JSON Unicode-escape\" field (removeUpdate())",JsonEscaper.CHARS_TO_ESCAPE_KEY));
	}
}
