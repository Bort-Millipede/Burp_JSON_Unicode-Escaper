package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.RawEditor;

//import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

public class EscaperPopup extends JFrame {
	private JPanel panel;
	private RawEditor rawEditor;
	private ByteArray contents;
	private JButton copyButton;
	
	public EscaperPopup(RawEditor re,ByteArray inContents) {
		super(String.format("%s: Converted text",JsonEscaper.EXTENSION_NAME));
		rawEditor = re;
		contents = inContents;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(600,250);
		this.setLocationRelativeTo(null);
		rawEditor.setContents(contents);
		//panel = new JPanel(new GridLayout(2,1));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(rawEditor.uiComponent());
		copyButton = new JButton("copy to clipboard (NOT IMPLEMENTED)");
		copyButton.setEnabled(false);
		panel.add(copyButton);
		this.getContentPane().add(panel);
	}
}
