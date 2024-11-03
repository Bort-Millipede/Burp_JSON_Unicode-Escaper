package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.core.ByteArray;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;

import org.json.JSONException;

public class JsonEscaperTab extends JPanel {
	private MontoyaApi mApi;
	
	private EscaperUnescaperTab escaperUnescaperTab;
	private EscaperSettingsTab settingsTab;
	
	public JsonEscaperTab(MontoyaApi api) {
		super(new GridLayout(1,1));
		mApi = api;
		JTabbedPane tabbedPane = new JTabbedPane();
		
		escaperUnescaperTab = new EscaperUnescaperTab(mApi);
		tabbedPane.addTab("Manual Escaper/Unescaper",new JScrollPane(escaperUnescaperTab));
		
		settingsTab = new EscaperSettingsTab(mApi);
		tabbedPane.addTab("Settings",new JScrollPane(settingsTab));
		
		this.add(tabbedPane);
	}
	
	public void setManualInputArea(ByteArray contents) {
		RawEditor inputArea = escaperUnescaperTab.getInputArea();
		inputArea.setContents(ByteArray.byteArrayOfLength(0));
		inputArea.setContents(contents);
	}
	
	public void clearManualOutputArea() {
		escaperUnescaperTab.getOutputArea().setContents(ByteArray.byteArrayOfLength(0));
	}
}
