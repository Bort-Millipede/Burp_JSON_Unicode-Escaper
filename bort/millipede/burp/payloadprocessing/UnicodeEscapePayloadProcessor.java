package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.intruder.PayloadProcessor;
import burp.api.montoya.intruder.PayloadProcessingResult;
import burp.api.montoya.intruder.PayloadData;
import burp.api.montoya.core.ByteArray;

import bort.millipede.burp.JsonEscaper;
import bort.millipede.burp.settings.JsonEscaperSettings;

import java.nio.charset.StandardCharsets;

public class UnicodeEscapePayloadProcessor implements PayloadProcessor {
	private JsonEscaperSettings settings;
	
	public UnicodeEscapePayloadProcessor() {
		settings = JsonEscaperSettings.getInstance();
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String escapedPayload = JsonEscaper.unicodeEscapeChars(new String(payloadData.currentPayload().getBytes(),StandardCharsets.UTF_8),settings.getCharsToEscape());
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload.getBytes(StandardCharsets.UTF_8)));
	}
}

