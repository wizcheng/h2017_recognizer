package org.hackathon2017.transcriber.transcriber;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TranscriberDemo {

    public static void main(String[] args) throws Exception {

        InputStream stream = new FileInputStream(new File("voice_samples/10001-90210-01803.wav"));
//        InputStream stream = new FileInputStream(new File("voice_samples/OSR_us_000_0034_8k.wav"));

        // http://www.loyalbooks.com/book/pride-and-prejudice-by-jane-austen
        // test conversion of audio using online audio converter
//        InputStream stream = new FileInputStream(new File("voice_samples/prideandprejudice_01_austen_64kb_16000Hx.wav"));


            transcribe(stream);
    }

    private static class AudioInfo {

        private boolean compatiable;
        private int sampleRate;
        private String reason;

        public AudioInfo(boolean compatiable, int sampleRate, String reason) {
            this.compatiable = compatiable;
            this.sampleRate = sampleRate;
            this.reason = reason;
        }

        public static AudioInfo notCompatiable(String reason){
            return new AudioInfo(false, 0, reason);
        }

        public static AudioInfo compatiable(int sampleRate){
            return new AudioInfo(true, sampleRate, null);
        }
    }

    public static AudioInfo check(BufferedInputStream bis) throws IOException, UnsupportedAudioFileException {
        AudioFileFormat audioInputStream = AudioSystem.getAudioFileFormat(bis);
        AudioFormat format = audioInputStream.getFormat();
        System.out.printf("format.Channels = " + format.getChannels());
        System.out.printf("format.SampleRate = " + format.getSampleRate());
        System.out.printf("format.Encoding = " + format.getEncoding());
        System.out.printf("format.FrameRate = " + format.getFrameRate());
        System.out.printf("format.FrameSize = " + format.getFrameSize());
        System.out.printf("format.SampleSizeInBits = " + format.getSampleSizeInBits());
        System.out.printf("format.Encoding = " + format.getEncoding());

        if (format.getSampleSizeInBits()!=16)
            return AudioInfo.notCompatiable("Sample size must be 16bit, was " + format.getSampleSizeInBits());

        if (format.getChannels()!=1)
            return AudioInfo.notCompatiable("Must have only 1 channel, aka mono, channel size was " + format.getChannels());

        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
            return AudioInfo.notCompatiable("Encoding must be PCM_SIGNED, was " + format.getEncoding());

        if (format.getSampleRate() != 8000.0 && format.getSampleRate() != 16000.0)
            return AudioInfo.notCompatiable("Sample rate must be 8K or 16K, was " + format.getSampleRate());

        return AudioInfo.compatiable((int) format.getSampleRate());
    }



    public static List<String> transcribe(InputStream stream) throws IOException, UnsupportedAudioFileException {

        try(BufferedInputStream bis = new BufferedInputStream(stream)){

            AudioInfo audioInfo = check(bis);
            if (!audioInfo.compatiable){
                throw new IllegalArgumentException("Audio transcribe un-support, reason: " + audioInfo.reason);
            }
//            AudioInfo audioInfo = new AudioInfo(true, 16000, "supported");

            List<String> transcribedResult = new ArrayList<>();

            Configuration configuration = new Configuration();

            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            configuration.setSampleRate(audioInfo.sampleRate);

//        Please note that the audio for this decoding must have one of the following formats:
//
//        RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 16000 Hz
//                or
//
//        RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz

            // requirement of the audio
            // 16000 Hz is much better than 8000 Hz
            // PCM Signed 16-bit
            // little-endian
            //


            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);


            recognizer.startRecognition(stream);
            SpeechResult result;
            while ((result = recognizer.getResult()) != null) {
                transcribedResult.add(result.getHypothesis());
                System.out.format("Hypothesis: %s\n", result.getHypothesis());
            }
            recognizer.stopRecognition();

            return transcribedResult;

        }


    }
}
