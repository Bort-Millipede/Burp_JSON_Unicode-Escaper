package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

import bort.millipede.burp.JsonEscaper;

public class UnicodeEscapeKeyCharsPayloadProcessor implements PayloadProcessor {
	public UnicodeEscapeKeyCharsPayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_KEY_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String payload = payloadData.currentPayload().toString();
		String escapedPayload = JsonEscaper.unicodeEscapeKeyChars(payload);
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload));
	}
}
