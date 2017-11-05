package org.hackathon2017.transcriber.api;

import org.hackathon2017.transcriber.transformer.Mp3ToWavTransformer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
public class TransformApi {


    @RequestMapping(value = "/transform", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseBody
    void transform(@RequestParam MultipartFile file, HttpServletResponse response) throws Exception {

        String outputFileName = file.getOriginalFilename() + ".wav";
        byte[] wavBytes = Mp3ToWavTransformer.mp3towav(file.getBytes());
        response.setContentType("application/x-download");
        response.setHeader("Content-Disposition", "attachment; filename="+outputFileName);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.getOutputStream().write(wavBytes);

    }
}
