package bort.millipede.burp;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

class EscapeKeyCharsPayloadProcessor implements PayloadProcessor {
	EscapeKeyCharsPayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.ESCAPE_KEY_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String payload = payloadData.currentPayload().toString();
		String escapedPayload = JsonEscaper.escapeKeyChars(payload);
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload));
	}
}

