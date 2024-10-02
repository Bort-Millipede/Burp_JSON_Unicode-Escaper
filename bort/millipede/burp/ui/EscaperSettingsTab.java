package bort.millipede.burp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.EditorOptions;

import java.awt.GridLayout;
import javax.swing.*;

class EscaperSettingsTab extends JPanel {
	private MontoyaApi mApi;
	
	EscaperSettingsTab(MontoyaApi api) {
		super();
		mApi = api;
		
		//setup "Settings" Tab
		JPanel innerPanel = new JPanel(new GridLayout(3,2));
		innerPanel.add(new JLabel("Characters to JSON Unicode-escape:",SwingConstants.RIGHT));
		//innerPanel.add(new JTextField());
		innerPanel.add(new JScrollPane(mApi.userInterface().createRawEditor(EditorOptions.SHOW_NON_PRINTABLE_CHARACTERS,EditorOptions.WRAP_LINES).uiComponent()));
		innerPanel.add(new JLabel());
		JPanel buttonPanel = new JPanel(new GridLayout(2,1));
		buttonPanel.add(new JButton("Paste"));
		buttonPanel.add(new JButton("Deduplicate characters"));
		innerPanel.add(buttonPanel);
		innerPanel.add(new JLabel("Automatically include JSON key characters:",SwingConstants.RIGHT));
		JCheckBox includeKeyChars = new JCheckBox();
		innerPanel.add(includeKeyChars);
		
		this.add(innerPanel);
	}
}
