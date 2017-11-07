package org.hackathon2017.transcriber.api;

import com.google.common.collect.ImmutableMap;
import org.hackathon2017.transcriber.ExternalConfig;
import org.hackathon2017.transcriber.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController("storage")
public class StorageApi {

    private ExternalConfig externalConfig;

    @Autowired
    public StorageApi(ExternalConfig externalConfig) {
        this.externalConfig = externalConfig;
    }

    @RequestMapping(value = "/store/{type}/{voiceId}/{fileName:.+}", method = RequestMethod.POST, consumes = "multipart/form-data")
    Object store(
            @RequestParam MultipartFile file,
            @PathVariable("type") String type,
            @PathVariable("voiceId") String voiceId,
            @PathVariable("fileName") String fileName
            ) throws Exception {

        StorageService storageService = new StorageService(externalConfig.getStorage().getUrl());
        if ("raw".equals(type)) {
            storageService.storeRaw(voiceId, file.getBytes());
            storageService.storeMeta(voiceId, fileName);
        } else if ("wav".equals(type)) {
            storageService.storeTransformedWav(voiceId, file.getBytes());
            storageService.storeMeta(voiceId, fileName);
        } else {
            throw new RuntimeException("Unexpected type " + type + ", expecting raw/wav");
        }

        return ImmutableMap.of("status", "success");
    }

    @RequestMapping(value = "/load/{type}/{voiceId}", method = RequestMethod.GET)
    @ResponseBody
    void load(
            @PathVariable("type") String type,
            @PathVariable("voiceId") String voiceId,
            HttpServletResponse response) throws Exception {

        StorageService storageService = new StorageService(externalConfig.getStorage().getUrl());

        String fileName = voiceId;
        Map<String, Object> meta = storageService.loadMeta(voiceId);
        if (meta != null) {
            fileName = (String) meta.get(StorageService.META_FILENAME);
        }

        byte[] bytes = null;
        if ("raw".equals(type)) {
            bytes = storageService.loadRaw(voiceId);
        } else if ("wav".equals(type)) {
            bytes = storageService.loadTransformedWav(voiceId);
            fileName += ".wav";
        } else {
            throw new RuntimeException("Unexpected type " + type + ", expecting raw/wav");
        }

        writeBytes(response, fileName, bytes);

    }

    private void writeBytes(HttpServletResponse response, String outputFileName, byte[] bytes) throws IOException {
        response.setContentType("application/x-download");
        response.setHeader("Content-Disposition", "attachment; filename="+outputFileName);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.getOutputStream().write(bytes);
    }
}
