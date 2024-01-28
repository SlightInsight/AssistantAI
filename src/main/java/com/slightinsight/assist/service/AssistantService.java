package com.slightinsight.assist.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slightinsight.assist.model.Prompt;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

@Service
public class AssistantService {

    private static final String CLAUDE = "anthropic.claude-v2";
    private static final String TITAN = "amazon.titan-embed-text-v1";

    @Autowired
    private BedrockRuntimeClient bedrockClient;

    @Autowired
    private BedrockRuntimeAsyncClient bedrockAsyncClient;

    public String askAssistant(Prompt prompt) {
        String response = "";
        // Claude requires you to enclose the prompt as follows:
        String enclosedPrompt = "Human: " + prompt.getQuestion() + "\n\nAssistant:";

        if (prompt.getResponseType().equals("async"))
            response = asyncResponse(enclosedPrompt);
        else if (prompt.getResponseType().equals("sync"))
            response = syncResponse(enclosedPrompt);

        return response;
    }

    /*
     * * Synchronous call to AI for text response
     */
    private String syncResponse(String enclosedPrompt) {

        String payload = new JSONObject().put("prompt", enclosedPrompt)
                .put("max_tokens_to_sample", 200)
                .put("temperature", 0.5)
                .put("stop_sequences", List.of("\n\nHuman:")).toString();

        InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(CLAUDE)
                .contentType("application/json")
                .accept("application/json").build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);

        JSONObject responseBody = new JSONObject(response.body().asUtf8String());

        String generatedText = responseBody.getString("completion");

        System.out.println("Generated text: " + generatedText);

        return generatedText;
    }

    /*
     * * Streaming call to AI for text response
     */
    private String asyncResponse(String enclosedPrompt) {
        var finalCompletion = new AtomicReference<>("");
        var silent = false;

        var payload = new JSONObject().put("prompt", enclosedPrompt).put("temperature", 0.8)
                .put("max_tokens_to_sample", 300).toString();

        var request = InvokeModelWithResponseStreamRequest.builder().body(SdkBytes.fromUtf8String(payload))
                .modelId(CLAUDE).contentType("application/json").accept("application/json").build();

        var visitor = InvokeModelWithResponseStreamResponseHandler.Visitor.builder().onChunk(chunk -> {
            var json = new JSONObject(chunk.bytes().asUtf8String());
            var completion = json.getString("completion");
            finalCompletion.set(finalCompletion.get() + completion);
            if (!silent) {
                System.out.print(completion);
            }
        }).build();

        var handler = InvokeModelWithResponseStreamResponseHandler.builder()
                .onEventStream(stream -> stream.subscribe(event -> event.accept(visitor))).onComplete(() -> {
                }).onError(e -> System.out.println("\n\nError: " + e.getMessage())).build();

        bedrockAsyncClient.invokeModelWithResponseStream(request, handler).join();

        return finalCompletion.get();
    }

    public String saveEmbeddings(Prompt prompt) {
        String payload = new JSONObject().put("inputText", prompt.getQuestion()).toString();

        InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload)).modelId(TITAN)
                .contentType("application/json").accept("application/json").build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);

        JSONObject responseBody = new JSONObject(response.body().asUtf8String());

        return responseBody.getJSONArray("embedding").toString();
    }

}
