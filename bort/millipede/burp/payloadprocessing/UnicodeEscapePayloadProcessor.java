package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.persistence.Preferences;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import java.nio.charset.StandardCharsets;

public class UnicodeEscapePayloadProcessor implements PayloadProcessor {
	private MontoyaApi mApi;
	private JsonEscaperSettings settings;
	
	public UnicodeEscapePayloadProcessor(MontoyaApi api) {
		mApi = api;
		settings = JsonEscaperSettings.getInstance();
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String escapedPayload = JsonEscaper.unicodeEscapeChars(payloadData.currentPayload().toString(),settings.getCharsToEscape());
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload.getBytes(StandardCharsets.UTF_8)));
	}
}

