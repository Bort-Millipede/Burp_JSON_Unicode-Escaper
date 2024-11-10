package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.intruder.PayloadProcessor;
import burp.api.montoya.intruder.PayloadProcessingResult;
import burp.api.montoya.intruder.PayloadData;
import burp.api.montoya.core.ByteArray;

import bort.millipede.burp.JsonEscaper;

import java.nio.charset.StandardCharsets;

public class EscapeKeyCharsPayloadProcessor implements PayloadProcessor {
	public EscapeKeyCharsPayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.ESCAPE_KEY_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		String escapedPayload = JsonEscaper.escapeKeyChars(new String(payloadData.currentPayload().getBytes(),StandardCharsets.UTF_8));
		return PayloadProcessingResult.usePayload(ByteArray.byteArray(escapedPayload.getBytes(StandardCharsets.UTF_8)));
	}
}

