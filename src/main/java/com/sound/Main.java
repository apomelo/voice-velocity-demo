package com.sound;

import org.apache.commons.io.FileUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by C on 2018/8/9.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        File file1 = new File("samples/123.raw");
        File file2 = new File("samples/234.wav");


        byte[] srcData1 = FileUtils.readFileToByteArray(file1);
        byte[] destData1 = pcmAudioConvert(0.7f, srcData1);
        destData1 = addWavHead(destData1);
        FileUtils.writeByteArrayToFile(new File("samples/test1.wav"), destData1);

        byte[] srcData2 = FileUtils.readFileToByteArray(file2);
        byte[] destData2 = wavAudioConvert(0.7f, srcData2);
        FileUtils.writeByteArrayToFile(new File("samples/test2.wav"), destData2);
    }

    // Run sonic.
    private static void runSonic(AudioInputStream audioStream, SourceDataLine line,float speed, float pitch, float rate, float volume, boolean emulateChordPitch, int quality, int sampleRate, int numChannels) throws IOException
    {
        Sonic sonic = new Sonic(sampleRate, numChannels);
        int bufferSize = line.getBufferSize();
        byte inBuffer[] = new byte[bufferSize];
        byte outBuffer[] = new byte[bufferSize];
        int numRead, numWritten;

        sonic.setSpeed(speed);
        sonic.setPitch(pitch);
        sonic.setRate(rate);
        sonic.setVolume(volume);
        sonic.setChordPitch(emulateChordPitch);
        sonic.setQuality(quality);
        do {
            numRead = audioStream.read(inBuffer, 0, bufferSize);
            if(numRead <= 0) {
                sonic.flushStream();
            } else {
                sonic.writeBytesToStream(inBuffer, numRead);
            }
            do {
                numWritten = sonic.readBytesFromStream(outBuffer, bufferSize);
                if(numWritten > 0) {
                    line.write(outBuffer, 0, numWritten);
                }
            } while(numWritten > 0);
        } while(numRead > 0);
    }

    public static void audioConvertAndPlay(float speed, byte[] srcData) throws IOException, LineUnavailableException {
        float pitch = 1.0f;
        float rate = 1.0f;
        float volume = 1.0f;
        boolean emulateChordPitch = false;
        int quality = 0;

        AudioInputStream stream;
        AudioFormat format;
        int sampleRate;
        int numChannels;
        try {
            stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(srcData));
            format = stream.getFormat();
            sampleRate = (int) format.getSampleRate();
            numChannels = format.getChannels();
        } catch (UnsupportedAudioFileException e) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0f, 16, 1, 2, 8000.0f, false);
            stream = new AudioInputStream(new ByteArrayInputStream(srcData), format, srcData.length);
            sampleRate = (int) format.getSampleRate();
            numChannels = format.getChannels();
        }
        SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format,
                ((int) stream.getFrameLength() * format.getFrameSize()));
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        runSonic(stream, line, speed, pitch, rate, volume, emulateChordPitch, quality,
                sampleRate, numChannels);
        line.drain();
        line.stop();
    }

    public static byte[] runSonic(byte[] srcData, float speed, float pitch, float rate, float volume, boolean emulateChordPitch, int quality, int sampleRate, int numChannels) throws IOException
    {
        Sonic sonic = new Sonic(sampleRate, numChannels);
        int numWritten;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 8000;
        byte outBuffer[] = new byte[bufferSize];

        sonic.setSpeed(speed);
        sonic.setPitch(pitch);
        sonic.setRate(rate);
        sonic.setVolume(volume);
        sonic.setChordPitch(emulateChordPitch);
        sonic.setQuality(quality);

        if(srcData == null || srcData.length <= 0) {
            sonic.flushStream();
        } else {
            sonic.writeBytesToStream(srcData, srcData.length);
        }
        do {
            numWritten = sonic.readBytesFromStream(outBuffer, bufferSize);
            if(numWritten > 0) {
                byteArrayOutputStream.write(outBuffer, 0, numWritten);
            }
        } while(numWritten > 0);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] pcmAudioConvert(float speed, byte[] srcData) throws IOException {
        float pitch = 1.0f;
        float rate = 1.0f;
        float volume = 1.0f;
        boolean emulateChordPitch = false;
        int quality = 0;

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0f, 16, 1, 2, 8000.0f, false);;
        int sampleRate = (int) format.getSampleRate();
        int numChannels = format.getChannels();

        byte[] destData = runSonic(srcData, speed, pitch, rate, volume, emulateChordPitch, quality,
                sampleRate, numChannels);

        return destData;
    }

    public static byte[] wavAudioConvert(float speed, byte[] srcData) throws IOException {
        float pitch = 1.0f;
        float rate = 1.0f;
        float volume = 1.0f;
        boolean emulateChordPitch = false;
        int quality = 0;

        AudioInputStream stream = null;
        try {
            stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(srcData));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            return null;
        }
        AudioFormat format = stream.getFormat();
        int sampleRate = (int) format.getSampleRate();
        int numChannels = format.getChannels();

        srcData = removeWavHead(srcData);
        byte[] destData = runSonic(srcData, speed, pitch, rate, volume, emulateChordPitch, quality,
                sampleRate, numChannels);
        destData = addWavHead(destData);

        return destData;
    }

    public static byte[] addWavHead(byte[] srcData) {
        int srcLen = srcData.length;
        byte[] destData = new byte[srcLen + WavFormatHead.TOTAL_LEN];
        WavFormatHead head = new WavFormatHead();
        head.setChunkSize(srcLen + 36);
        head.setSubChunk2size(srcLen);
        byte[] headBytes = head.toBytes();
        System.arraycopy(headBytes, 0, destData, 0, WavFormatHead.TOTAL_LEN);
        System.arraycopy(srcData, 0, destData, WavFormatHead.TOTAL_LEN, srcLen);
        return destData;
    }

    public static byte[] removeWavHead(byte[] srcData) {
        byte[] destData = new byte[srcData.length - WavFormatHead.TOTAL_LEN];
        System.arraycopy(srcData, WavFormatHead.TOTAL_LEN, destData, 0, destData.length);
        return destData;
    }
}
