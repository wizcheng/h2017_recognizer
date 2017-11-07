package org.hackathon2017.transcriber.api;


import com.google.common.collect.ImmutableMap;
import org.hackathon2017.transcriber.transcriber.SphinxTranscriber;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class TranscriberApi {

    @RequestMapping(value = "/transcribe", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseBody
    Map<String, Object> transcribe(@RequestParam MultipartFile file) throws IOException, UnsupportedAudioFileException {

        Map<String, Object> sourceInfo = ImmutableMap.of(
                "length", file.getSize(),
                "name", file.getOriginalFilename(),
                "contentType", file.getContentType()
        );

        long start = System.currentTimeMillis();
        List<Map<String, Object>> transcribe = SphinxTranscriber.transcribe(file.getInputStream());
        long takenMs = System.currentTimeMillis() - start;

        Map<String, Object> response = toResponse(transcribe, takenMs, sourceInfo);

        return response;
    }

    private Map<String, Object> toResponse(Object transcribe, long takenMs, Map<String, Object> sourceInfo) {
        return ImmutableMap.of(
                    "status", "success",
                    "transcriber", "sphinx",
                    "durationMs", takenMs,
                    "output", transcribe,
                    "source", sourceInfo
            );
    }


}
