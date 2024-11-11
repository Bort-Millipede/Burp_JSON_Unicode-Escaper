package bort.millipede.burp.payloadprocessing;

import burp.api.montoya.intruder.PayloadProcessor;
import burp.api.montoya.intruder.PayloadProcessingResult;
import burp.api.montoya.intruder.PayloadData;
import burp.api.montoya.core.ByteArray;

import bort.millipede.burp.JsonEscaper;

import java.nio.charset.StandardCharsets;

import org.json.JSONException;

public class UnescapePayloadProcessor implements PayloadProcessor {
	public UnescapePayloadProcessor() {
	
	}
	
	@Override
	public String displayName() {
		return JsonEscaper.UNESCAPE_LABEL;
	}
	
	@Override
	public PayloadProcessingResult processPayload(PayloadData payloadData) {
		ByteArray currentPayload = payloadData.currentPayload();
		PayloadProcessingResult payloadProcessingResult = null;
		try {
			payloadProcessingResult = PayloadProcessingResult.usePayload(ByteArray.byteArray(JsonEscaper.unescapeAllChars(new String(currentPayload.getBytes(),StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8)));
		} catch(JSONException jsonE) {
			payloadProcessingResult = PayloadProcessingResult.usePayload(currentPayload);
		}
		return payloadProcessingResult;
	}
}

