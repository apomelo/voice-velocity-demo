package com.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Created by C on 2018/8/9.
 */
public class WavFormatHead {

    public static final int TOTAL_LEN = 44;

    private String chunkId = "RIFF";    // 4字节，文件头标识，一般就是" RIFF" 四个字母
    private int chunkSize;              // 4字节，整个数据文件的大小，不包括上面ID和Size本身
    private String format = "WAVE";     // 4字节，一般就是" WAVE" 四个字母

    private String subChunk1Id = "fmt ";// 4字节，格式说明块，本字段一般就是"fmt "
    private int subChunk1Size = 16;     // 4字节，本数据块的大小，不包括ID和Size字段本身
    private short audioFormat = 1;      // 2字节，音频的格式说明，PCM = 1 （比如，线性采样），如果是其它值的话，则可能是一些压缩形式
    private short numChannels = 1;      // 2字节，声道数，1 => 单声道  |  2 => 双声道
    private int sampleRate = 8000;      // 4字节，采样率，如 8000，44100 等值
    private int byteRate = 16000;       // 4字节，比特率，每秒所需要的字节数，等于： SampleRate * numChannels * BitsPerSample / 8
    private short blockAlign = 2;       // 2字节，数据块对齐单元，等于：numChannels * BitsPerSample / 8
    private short bitRerSample = 16;    // 2字节，采样时模数转换的分辨率

    private String subChunk2Id = "data";// 4字节，真正的声音数据块，本字段一般是"data"
    private int subChunk2size;          // 4字节，本数据块的大小，不包括ID和Size字段本身

    public byte[] toBytes() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(44)) {
            byteArrayOutputStream.write(chunkId.getBytes());
            byteArrayOutputStream.write(int2byte(chunkSize, ByteOrder.LITTLE_ENDIAN));

            byteArrayOutputStream.write(format.getBytes());
            byteArrayOutputStream.write(subChunk1Id.getBytes());
            byteArrayOutputStream.write(int2byte(subChunk1Size, ByteOrder.LITTLE_ENDIAN));
            byteArrayOutputStream.write(short2byte(audioFormat, ByteOrder.LITTLE_ENDIAN));
            byteArrayOutputStream.write(short2byte(numChannels, ByteOrder.LITTLE_ENDIAN));
            byteArrayOutputStream.write(int2byte(sampleRate, ByteOrder.LITTLE_ENDIAN));
            byteArrayOutputStream.write(int2byte(byteRate, ByteOrder.LITTLE_ENDIAN));
            byteArrayOutputStream.write(short2byte(blockAlign, ByteOrder.LITTLE_ENDIAN));
            byteArrayOutputStream.write(short2byte(bitRerSample, ByteOrder.LITTLE_ENDIAN));

            byteArrayOutputStream.write(subChunk2Id.getBytes());
            byteArrayOutputStream.write(int2byte(subChunk2size, ByteOrder.LITTLE_ENDIAN));

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] short2byte(short i, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return new byte[] {
                    (byte) ((i >> 8) & 0xff),
                    (byte) ((i >> 0) & 0xff)
            };
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return new byte[] {
                    (byte) ((i >> 0) & 0xff),
                    (byte) ((i >> 8) & 0xff)
            };
        } else {
            return new byte[2];
        }
    }

    public short byte2short(byte[] bytes, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (short) (((bytes[0]) & 0xff << 8) | ((bytes[1] & 0xff) << 0));
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return (short) (((bytes[0]) & 0xff << 0) | ((bytes[1] & 0xff) << 8));
        } else {
            return 0;
        }
    }

    public byte[] int2byte(int i, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return new byte[] {
                    (byte) ((i >> 24) & 0xff),
                    (byte) ((i >> 16) & 0xff),
                    (byte) ((i >> 8) & 0xff),
                    (byte) ((i >> 0) & 0xff)
            };
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return new byte[] {
                    (byte) ((i >> 0) & 0xff),
                    (byte) ((i >> 8) & 0xff),
                    (byte) ((i >> 16) & 0xff),
                    (byte) ((i >> 24) & 0xff)
            };
        } else {
            return new byte[4];
        }
    }

    public int byte2int(byte[] bytes, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return ((bytes[0]) & 0xff << 24)
                    | ((bytes[1] & 0xff) << 16)
                    | ((bytes[2] & 0xff) << 8)
                    | ((bytes[3] & 0xff) << 0);
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return ((bytes[0]) & 0xff << 0)
                    | ((bytes[1] & 0xff) << 8)
                    | ((bytes[2] & 0xff) << 16)
                    | ((bytes[3] & 0xff) << 24);
        } else {
            return 0;
        }
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setNumChannels(short numChannels) {
        this.numChannels = numChannels;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setByteRate(int byteRate) {
        this.byteRate = byteRate;
    }

    public void setBlockAlign(short blockAlign) {
        this.blockAlign = blockAlign;
    }

    public void setBitRerSample(short bitRerSample) {
        this.bitRerSample = bitRerSample;
    }

    public void setSubChunk2size(int subChunk2size) {
        this.subChunk2size = subChunk2size;
    }

    public static byte[] removeWavHead(byte[] srcData) {
        byte[] destData = new byte[srcData.length - WavFormatHead.TOTAL_LEN];
        System.arraycopy(srcData, WavFormatHead.TOTAL_LEN, destData, 0, destData.length);
        return destData;
    }
}
