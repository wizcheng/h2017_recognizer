import org.apache.commons.io.IOUtils;

import javax.sound.sampled.*;
import java.io.*;

public class Mp3ToWavConversionDemo {


    public static void main(String[] args) throws Exception {

        byte[] bytesOutput;
        String inputFileName = "voice_samples/prideandprejudice_01_austen_64kb.mp3";
        String outputFileName = inputFileName + ".wav";
        AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 1, 8000, false);

        try(FileInputStream fileInputStream = new FileInputStream(inputFileName)){
            byte[] bytesInput = IOUtils.toByteArray(fileInputStream);
            bytesOutput = getAudioDataBytes(bytesInput, convertFormat);
        }

        try (FileOutputStream output = new FileOutputStream(outputFileName)){
            IOUtils.write(bytesOutput, output);
        }

    }

    public static byte [] getAudioDataBytes(byte [] sourceBytes, AudioFormat audioFormat) throws UnsupportedAudioFileException, IllegalArgumentException, Exception {
        if(sourceBytes == null || sourceBytes.length == 0 || audioFormat == null){
            throw new IllegalArgumentException("Illegal Argument passed to this method");
        }

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(sourceBytes);
             final AudioInputStream sourceAIS = AudioSystem.getAudioInputStream(bais)) {
            AudioFormat sourceFormat = sourceAIS.getFormat();
            AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(), sourceFormat.getChannels()*2, sourceFormat.getSampleRate(), false);
            try (final AudioInputStream convert1AIS = AudioSystem.getAudioInputStream(convertFormat, sourceAIS);
                 final AudioInputStream convert2AIS = AudioSystem.getAudioInputStream(audioFormat, convert1AIS);
                 final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte [] buffer = new byte[8192];
                while(true){
                    int readCount = convert2AIS.read(buffer, 0, buffer.length);
                    if(readCount == -1){
                        break;
                    }
                    baos.write(buffer, 0, readCount);
                }
                return baos.toByteArray();
            }
        }
    }

}
