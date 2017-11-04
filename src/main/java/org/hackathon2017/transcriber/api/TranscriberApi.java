package org.hackathon2017.transcriber.api;


import com.google.common.collect.ImmutableMap;
import org.hackathon2017.transcriber.transcriber.TranscriberDemo;
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

        int length = file.getBytes().length;

        long start = System.currentTimeMillis();
        List<String> transcribe = TranscriberDemo.transcribe(file.getInputStream());
        long takenMs = System.currentTimeMillis() - start;

        return ImmutableMap.of(
                "status", "success",
                "transcriber", "sphinx",
                "output", ImmutableMap.of(
                        "message", transcribe,
                        "durationMs", takenMs
                ),
                "source", ImmutableMap.of(
                        "lengthBytes", length
                )
        );
    }




}
