package org.hackathon2017.transcriber.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class StorageService {


    public static final String META_FILENAME = "filename";

    private String folder;
    private ObjectMapper objectMapper = new ObjectMapper();

    public StorageService(String folder) {
        this.folder = folder;
    }

    public void storeRaw(String voiceId, byte[] bytes){
        store(voiceId, bytes, "raw");
    }

    public void storeTransformedWav(String voiceId, byte[] bytes){
        store(voiceId, bytes, "wav");
    }

    public byte[] loadRaw(String voiceId) {
        return load(voiceId, "raw");
    }

    public byte[] loadTransformedWav(String voiceId){
        return load(voiceId, "wav");
    }

    public void storeMeta(String voiceId, String fileName){
        try {
            String meta = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ImmutableMap.of(
                    META_FILENAME, fileName
            ));
            FileUtils.write(new File(new File(folder, "meta"), voiceId + ".json"), meta, "UTF-8");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> loadMeta(String voiceId){
        try {
            File metaFile = new File(new File(folder, "meta"), voiceId + ".json");
            String meta = FileUtils.readFileToString(metaFile, "UTF-8");
            return objectMapper.readValue(meta, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void store(String voiceId, byte[] bytes, String subFolder) {
        try {
            File file = new File(folder);
            File subFolderFile = new File(file, subFolder);
            File fileToSave = new File(subFolderFile, voiceId);
            System.out.println("File will be save to " + fileToSave.getAbsolutePath());
            FileUtils.writeByteArrayToFile(fileToSave, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] load(String voiceId, String subFolder) {
        try {
            File file = new File(folder);
            File subFolderFile = new File(file, subFolder);
            File fileToRead = new File(subFolderFile, voiceId);
            System.out.println("File will be read form " + fileToRead.getAbsolutePath());
            return FileUtils.readFileToByteArray(fileToRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
