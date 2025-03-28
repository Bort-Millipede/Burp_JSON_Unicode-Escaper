package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;

import java.nio.charset.StandardCharsets;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

public class EscaperPopup extends JFrame implements ActionListener {
	private MontoyaApi mApi;
	private JPanel panel;
	private RawEditor rawEditor;
	private ByteArray contents;
	private JButton copyButton;
	private boolean error;
	private Logging mLogging;
	
	public EscaperPopup(MontoyaApi api,ByteArray inContents,boolean inError) {
		super(String.format("%s: Converted Text",JsonEscaper.EXTENSION_NAME));
		mApi = api;
		rawEditor = mApi.userInterface().createRawEditor(EditorOptions.READ_ONLY,EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES);
		contents = inContents;
		error = inError;
		mLogging = mApi.logging();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(600,250);
		this.setLocationRelativeTo(null);
		rawEditor.setContents(contents);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		if(error) {
			JLabel errorLabel = new JLabel("Error occurred when processing input text!!!");
			errorLabel.setForeground(Color.RED);
			panel.add(errorLabel);
		}
		panel.add(rawEditor.uiComponent());
		JPanel buttonPanel = new JPanel(new GridLayout(1,1));
		copyButton = new JButton("Copy Full Value To Clipboard");
		copyButton.addActionListener(this);
		buttonPanel.add(copyButton);
		panel.add(buttonPanel);
		this.getContentPane().add(panel);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection ss = new StringSelection(new String(rawEditor.getContents().getBytes(),StandardCharsets.UTF_8));
		cb.setContents(ss,ss);
		if(JsonEscaperSettings.getInstance().getVerboseLogging()) mLogging.logToOutput(String.format("%s value from pop-up copied to clipboard"));
	}
}
