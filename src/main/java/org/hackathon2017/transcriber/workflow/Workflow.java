package org.hackathon2017.transcriber.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.hackathon2017.transcriber.ExternalConfig;

import java.io.*;
import java.net.URLEncoder;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Workflow {

    private ExternalConfig externalConfig;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Clock clock = Clock.systemUTC();

    public Workflow(ExternalConfig externalConfig) {
        this.externalConfig = externalConfig;
    }

    public static void main(String[] args) throws IOException {


//        Request.Post("http://somehost/do-stuff")
//                .useExpectContinue()
//                .version(HttpVersion.HTTP_1_1)
//                .bodyForm(NamedVale)
//                .bodyString("Important stuff", ContentType.DEFAULT_TEXT)
//                .execute().returnContent().asBytes();


//        String filePath = "voice_samples/10001-90210-01803.wav";
        String filePath = "voice_samples/prideandprejudice_01_austen_64kb.mp3.wav";


        File file = new File(filePath);
        String fileName = file.getName();
        FileInputStream inputStream = new FileInputStream(filePath);
        String voiceId = UUID.randomUUID().toString();


        new Workflow(new ExternalConfig()).process(org.apache.commons.io.IOUtils.toByteArray(inputStream), fileName, voiceId);


    }

    public void process(byte[] bytes, String fileName, String voiceId) {

        try {

            updateMainProcess(voiceId, "processing", "");

            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpResponse dummyResponse = submitFile(httpClient, externalConfig.getStorageService().toPath("/store/raw/" + voiceId + "/" + URLEncoder.encode(fileName)), new ByteArrayInputStream(bytes), fileName);
            EntityUtils.consumeQuietly(dummyResponse.getEntity());

            updateMainProcess(voiceId, "processing", "transform");
            updateProcess(voiceId, "Transform (to Specific Wav)");
            HttpResponse transform = submitFile(httpClient, externalConfig.getTransformer().toPath("/transform"), new ByteArrayInputStream(bytes), fileName);
            byte[] transformedBytes = EntityUtils.toByteArray(transform.getEntity());

            HttpResponse dummyResponse2 = submitFile(httpClient, externalConfig.getStorageService().toPath("/store/wav/" + voiceId + "/" + URLEncoder.encode(fileName)), new ByteArrayInputStream(bytes), fileName);
            EntityUtils.consumeQuietly(dummyResponse2.getEntity());

            updateMainProcess(voiceId, "processing", "transcribing");
            updateProcess(voiceId, "Transcribing (Sphinx)");
            HttpResponse httpResponse = submitFile(httpClient, externalConfig.getSphinxTranscriber().toPath("/transcribe"), new ByteArrayInputStream(transformedBytes), fileName);
            String transcribed = EntityUtils.toString(httpResponse.getEntity());

            System.out.println("transcribed: " + transcribed);

            updateMainProcess(voiceId, "processing", "saving");
            updateProcess(voiceId, "Save Result (Elasticsearch)");
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(transcribed, Map.class);


            List<Map<String, Object>> output = (List<Map<String, Object>>) map.get("output");


            List<HashMap<String, Object>> breakdowns = output.stream()
                    .map(o -> {
                        HashMap<String, Object> breakdown = new HashMap<>(o);
                        breakdown.put("voiceId", voiceId);
                        breakdown.put("fileName", fileName);
                        breakdown.put("transcriber", "sphinx");
                        return breakdown;
                    })
                    .collect(Collectors.toList());


            breakdowns.forEach(m -> {
                try {
                    String elkResponse = Request.Post(externalConfig.getSearch().toPath("/transcript/sphinx_breakdown"))
                            .useExpectContinue()
                            .version(HttpVersion.HTTP_1_1)
                            .bodyString(objectMapper.writeValueAsString(m), ContentType.APPLICATION_JSON)
                            .execute().returnContent().asString();

                    System.out.println(elkResponse);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


            String combinedMessage = output.stream()
                    .map(o -> (String) o.get("message"))
                    .collect(Collectors.joining(", "));

            Map<Object, Object> combined = ImmutableMap.builder()
                    .put("combinedMessage", combinedMessage)
                    .put("fileName", fileName)
                    .put("voiceId", voiceId)
                    .build();

            String elkResponse = Request.Post(externalConfig.getSearch().toPath("/transcript/sphinx_combined/" + voiceId))
                    .useExpectContinue()
                    .version(HttpVersion.HTTP_1_1)
                    .bodyString(objectMapper.writeValueAsString(combined), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();

            System.out.println(elkResponse);

            updateProcess(voiceId, "Completed");
            updateMainProcess(voiceId, "completed", "success");

        } catch (Exception ex) {

            ex.printStackTrace();
            updateProcess(voiceId, "Error: " + ex.getMessage());
            updateMainProcess(voiceId, "completed", "failed");

        } finally {

        }


    }

    private HttpResponse submitFile(HttpClient httpClient, String path, InputStream inputStream, String fileName) throws IOException {
        System.out.println("Submit file to " + path);
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addBinaryBody("file", inputStream, ContentType.create("application/octet-stream"), fileName)
                .build();
        HttpPost httpPost = new HttpPost(path);
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpClient.execute(httpPost);

        if (httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(EntityUtils.toString(httpResponse.getEntity()));
        }

        return httpResponse;
    }

    public void updateMainProcess(String voiceId, String status, String result) {
        try {
            ImmutableMap<String, String> log = ImmutableMap.of(
                    "voiceId", voiceId,
                    "status", status,
                    "result", result,
                    "time", OffsetDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );

            String elkResponse = Request.Post(externalConfig.getSearch().toPath("/process/status/" + voiceId))
                    .useExpectContinue()
                    .version(HttpVersion.HTTP_1_1)
                    .bodyString(objectMapper.writeValueAsString(log), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();

            System.out.println(elkResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateProcess(String voiceId, String message) {
        try {
            ImmutableMap<String, String> log = ImmutableMap.of(
                    "voiceId", voiceId,
                    "message", message,
                    "time", OffsetDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );

            String elkResponse = Request.Post(externalConfig.getSearch().toPath("/process/status_detail"))
                    .useExpectContinue()
                    .version(HttpVersion.HTTP_1_1)
                    .bodyString(objectMapper.writeValueAsString(log), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();

            System.out.println(elkResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void cancel(String voiceId) {
        updateProcess(voiceId, "cancelled");
        updateMainProcess(voiceId, "completed", "cancelled");
    }
}
