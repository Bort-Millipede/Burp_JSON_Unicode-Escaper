package bort.millipede.burp;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

class UnescapePayloadProcessor implements PayloadProcessor {
	UnescapePayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNESCAPE_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String payload = payloadData.currentPayload().toString();
		String escapedPayload = JsonEscaper.unescapeAllChars(payload);
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload));
	}
}

