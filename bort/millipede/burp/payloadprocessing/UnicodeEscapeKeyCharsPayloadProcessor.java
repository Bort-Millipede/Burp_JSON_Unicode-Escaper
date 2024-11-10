package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.intruder.PayloadProcessor;
import burp.api.montoya.intruder.PayloadProcessingResult;
import burp.api.montoya.intruder.PayloadData;
import burp.api.montoya.core.ByteArray;

import bort.millipede.burp.JsonEscaper;

import java.nio.charset.StandardCharsets;

public class UnicodeEscapeKeyCharsPayloadProcessor implements PayloadProcessor {
	public UnicodeEscapeKeyCharsPayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_KEY_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String escapedPayload = JsonEscaper.unicodeEscapeKeyChars(new String(payloadData.currentPayload().getBytes(),StandardCharsets.UTF_8));
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload.getBytes(StandardCharsets.UTF_8)));
	}
}

