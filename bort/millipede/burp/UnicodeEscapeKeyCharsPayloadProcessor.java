package bort.millipede.burp;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

class UnicodeEscapeKeyCharsPayloadProcessor implements PayloadProcessor {
	UnicodeEscapeKeyCharsPayloadProcessor() {
	
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

