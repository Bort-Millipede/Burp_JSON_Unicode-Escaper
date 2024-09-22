package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.editor.RawEditor;

import java.nio.charset.StandardCharsets;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

public class EscaperPopup extends JFrame {
	private JPanel panel;
	private RawEditor rawEditor;
	private ByteArray contents;
	private JButton copyButton;
	private boolean error;
	private Logging logger;
	
	public EscaperPopup(RawEditor re,ByteArray inContents,boolean inError) {
		this(re,inContents,inError,null);
	}
	
	public EscaperPopup(RawEditor re,ByteArray inContents,boolean inError,Logging inLogger) {
		super(String.format("%s: Converted Text",JsonEscaper.EXTENSION_NAME));
		rawEditor = re;
		contents = inContents;
		error = inError;
		logger = inLogger;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(600,250);
		this.setLocationRelativeTo(null);
		rawEditor.setContents(contents);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		if(error) {
			JLabel errorLabel = new JLabel("Error occurred when processing input text!!!");
			errorLabel.setForeground(Color.RED);
			panel.add(errorLabel);
		}
		panel.add(rawEditor.uiComponent());
		copyButton = new JButton("Copy Full Value To Clipboard");
		copyButton.addActionListener(new CopyPasteListener());
		panel.add(copyButton);
		this.getContentPane().add(panel);
	}
	
	private class CopyPasteListener implements ActionListener {
		CopyPasteListener() {
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String contents = new String(rawEditor.getContents().getBytes(),StandardCharsets.UTF_8);
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(contents);
			cb.setContents(ss,ss);
			
			if(logger!=null) {
				logger.logToOutput(String.format("%s value from pop-up copied to clipboard")); //todo: add timestamps to logs?
			}
		}
	}
}
