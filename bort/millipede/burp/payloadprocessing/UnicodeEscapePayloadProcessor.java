package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.intruder.*;
import burp.api.montoya.core.ByteArray;

import bort.millipede.burp.JsonEscaper;

import java.nio.charset.StandardCharsets;

public class UnicodeEscapePayloadProcessor implements PayloadProcessor {
	public UnicodeEscapePayloadProcessor() {
	
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

