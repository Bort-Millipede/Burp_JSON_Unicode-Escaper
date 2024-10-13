package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.persistence.Preferences;

import bort.millipede.burp.JsonEscaper;

import java.nio.charset.StandardCharsets;

public class UnicodeEscapePayloadProcessor implements PayloadProcessor {
	private MontoyaApi mApi;
	private Preferences mPreferences;
	
	public UnicodeEscapePayloadProcessor(MontoyaApi api) {
		mApi = api;
		mPreferences = mApi.persistence().preferences();
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String charsToEscape = mPreferences.getString(JsonEscaper.CHARS_TO_ESCAPE_KEY);
		if(mPreferences.getBoolean(JsonEscaper.INCLUDE_KEY_CHARS_KEY))
			charsToEscape = JsonEscaper.KEY_CHARS.concat(charsToEscape);
		
		String escapedPayload = JsonEscaper.unicodeEscapeChars(payloadData.currentPayload().toString(),charsToEscape);
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload.getBytes(StandardCharsets.UTF_8)));
	}
}

