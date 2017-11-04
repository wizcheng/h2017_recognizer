
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TranscriberDemo {

    public static void main(String[] args) throws Exception {

        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

//        configuration.setSampleRate(8000);

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
//        InputStream stream = new FileInputStream(new File("10001-90210-01803.wav"));
//        InputStream stream = new FileInputStream(new File("voice_samples/OSR_us_000_0034_8k.wav"));

        // http://www.loyalbooks.com/book/pride-and-prejudice-by-jane-austen
        // test conversion of audio using online audio converter
//        InputStream stream = new FileInputStream(new File("voice_samples/prideandprejudice_01_austen_64kb_16000Hx.wav"));

        InputStream stream = new FileInputStream(new File("voice_samples/prideandprejudice_01_austen_64kb.mp3.wav"));


        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
        }
        recognizer.stopRecognition();
    }
}