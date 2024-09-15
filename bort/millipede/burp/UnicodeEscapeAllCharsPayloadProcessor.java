package bort.millipede.burp;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

class UnicodeEscapeAllCharsPayloadProcessor implements PayloadProcessor {
	UnicodeEscapeAllCharsPayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_ALL_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String payload = payloadData.currentPayload().toString();
		String escapedPayload = JsonEscaper.unicodeEscapeAllChars(payload);
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload));
	}
}

