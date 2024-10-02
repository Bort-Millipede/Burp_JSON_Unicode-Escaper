package bort.millipede.burp.ui;

import bort.millipede.burp.JsonEscaper;

import burp.api.montoya.MontoyaApi;

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
}
