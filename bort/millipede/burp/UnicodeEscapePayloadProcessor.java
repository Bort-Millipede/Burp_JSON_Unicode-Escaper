package bort.millipede.burp;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

class UnicodeEscapePayloadProcessor implements PayloadProcessor {
	UnicodeEscapePayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNICODE_ESCAPE_CUSTOM_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String payload = payloadData.currentPayload().toString();
		
		return null;
	}
}

