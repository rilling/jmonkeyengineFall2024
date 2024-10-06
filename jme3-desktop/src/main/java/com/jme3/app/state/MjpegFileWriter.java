/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.app.state;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Released under BSD License
 * @author monceaux, normenhansen, entrusC
 */
public class MjpegFileWriter implements AutoCloseable {

    int width = 0;
    int height = 0;
    double framerate = 0;
    int numFrames = 0;
    File aviFile = null;
    OutputStream aviOutput = null;
    long riffOffset = 0;
    long aviMovieOffset = 0;
    long position = 0;
    AVIIndexList indexlist = null;

    public MjpegFileWriter(File aviFile, int width, int height, double framerate) throws Exception {
        this(aviFile, width, height, framerate, 0);
    }

    public MjpegFileWriter(File aviFile, int width, int height, double framerate, int numFrames) throws Exception {
        this.aviFile = aviFile;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.numFrames = numFrames;
        FileOutputStream fos = new FileOutputStream(aviFile);
        aviOutput = new BufferedOutputStream(fos);

        RIFFHeader rh = new RIFFHeader();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        baos.write(rh.toBytes());
        baos.write(new AVIMainHeader().toBytes());
        baos.write(new AVIStreamList().toBytes());
        baos.write(new AVIStreamHeader().toBytes());
        baos.write(new AVIStreamFormat().toBytes());
        baos.write(new AVIJunk().toBytes());
        byte[] headerBytes = baos.toByteArray();
        aviOutput.write(headerBytes);
        aviMovieOffset = headerBytes.length;
        byte[] listBytes = new AVIMovieList().toBytes();
        aviOutput.write(listBytes);
        indexlist = new AVIIndexList();

        position = headerBytes.length + listBytes.length;
    }

    public void addImage(Image image) throws Exception {
        addImage(image, 0.8f);
    }

    public void addImage(Image image, float quality) throws Exception {
        addImage(writeImageToBytes(image, quality));
    }

    public void addImage(byte[] imageData) throws Exception {
        byte[] fcc = new byte[]{'0', '0', 'd', 'b'};
        int useLength = imageData.length;
        int extra = (useLength + (int) position) % 4;
        if (extra > 0) {
            useLength = useLength + extra;
        }

        indexlist.addAVIIndex((int) position, useLength);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(fcc.length + 4 + useLength);
        baos.write(fcc);
        baos.write(intBytes(swapInt(useLength)));
        baos.write(imageData);
        if (extra > 0) {
            for (int i = 0; i < extra; i++) {
                baos.write(0);
            }
        }
        byte[] data = baos.toByteArray();
        aviOutput.write(data);
        imageData = null;

        numFrames++; //add a frame
        position += data.length;
    }

    public void finishAVI() throws IOException {
        byte[] indexlistBytes = indexlist.toBytes();
        aviOutput.write(indexlistBytes);
        aviOutput.close();
        int fileSize = (int) aviFile.length();
        int listSize = (int) (fileSize - 8 - aviMovieOffset - indexlistBytes.length);

        //add header and length by writing the headers again
        //with the now available information
        try (SeekableByteChannel sbc = Files.newByteChannel(aviFile.toPath(), StandardOpenOption.WRITE);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(new RIFFHeader(fileSize).toBytes());
            baos.write(new AVIMainHeader().toBytes());
            baos.write(new AVIStreamList().toBytes());
            baos.write(new AVIStreamHeader().toBytes());
            baos.write(new AVIStreamFormat().toBytes());
            baos.write(new AVIJunk().toBytes());
            baos.write(new AVIMovieList(listSize).toBytes());

            sbc.write(ByteBuffer.wrap(baos.toByteArray()));
        }
    }

    // public void writeAVI(File file) throws Exception
    // {
    // OutputStream os = new FileOutputStream(file);
    //
    // // RIFFHeader
    // // AVIMainHeader
    // // AVIStreamList
    // // AVIStreamHeader
    // // AVIStreamFormat
    // // write 00db and image bytes...
    // }
    public static int swapInt(int v) {
        return (v >>> 24) | (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
    }

    public static short swapShort(short v) {
        return (short) ((v >>> 8) | (v << 8));
    }

    public static byte[] intBytes(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i >>> 24);
        b[1] = (byte) ((i >>> 16) & 0x000000FF);
        b[2] = (byte) ((i >>> 8) & 0x000000FF);
        b[3] = (byte) (i & 0x000000FF);

        return b;
    }

    public static byte[] shortBytes(short i) {
        byte[] b = new byte[2];
        b[0] = (byte) (i >>> 8);
        b[1] = (byte) (i & 0x000000FF);

        return b;
    }

    @Override
    public void close() throws Exception {
        finishAVI();
    }

    private class RIFFHeader {

        public byte[] fcc = new byte[]{'R', 'I', 'F', 'F'};
        public int fileSize = 0;
        public byte[] fcc2 = new byte[]{'A', 'V', 'I', ' '};
        public byte[] fcc3 = new byte[]{'L', 'I', 'S', 'T'};
        public int listSize = 200;
        public byte[] fcc4 = new byte[]{'h', 'd', 'r', 'l'};

        public RIFFHeader() {
        }

        public RIFFHeader(int fileSize) {
            this.fileSize = fileSize;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(fileSize)));
            baos.write(fcc2);
            baos.write(fcc3);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc4);

            return baos.toByteArray();
        }
    }

    private class AVIMainHeader {
        /*
         *
         * FOURCC fcc; DWORD cb; DWORD dwMicroSecPerFrame; DWORD
         * dwMaxBytesPerSec; DWORD dwPaddingGranularity; DWORD dwFlags; DWORD
         * dwTotalFrames; DWORD dwInitialFrames; DWORD dwStreams; DWORD
         * dwSuggestedBufferSize; DWORD dwWidth; DWORD dwHeight; DWORD
         * dwReserved[4];
         */

        public byte[] fcc = new byte[]{'a', 'v', 'i', 'h'};
        public int cb = 56;
        public int dwMicroSecPerFrame = 0;                                // (1
        // /
        // frames
        // per
        // sec)
        // *
        // 1,000,000
        public int dwMaxBytesPerSec = 10000000;
        public int dwPaddingGranularity = 0;
        public int dwFlags = 65552;
        public int dwTotalFrames = 0;                                // replace
        // with
        // correct
        // value
        public int dwInitialFrames = 0;
        public int dwStreams = 1;
        public int dwSuggestedBufferSize = 0;
        public int dwWidth = 0;                                // replace
        // with
        // correct
        // value
        public int dwHeight = 0;                                // replace
        // with
        // correct
        // value
        public int[] dwReserved = new int[4];

        public AVIMainHeader() {
            dwMicroSecPerFrame = (int) ((1.0 / framerate) * 1000000.0);
            dwWidth = width;
            dwHeight = height;
            dwTotalFrames = numFrames;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(dwMicroSecPerFrame)));
            baos.write(intBytes(swapInt(dwMaxBytesPerSec)));
            baos.write(intBytes(swapInt(dwPaddingGranularity)));
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwTotalFrames)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwStreams)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwWidth)));
            baos.write(intBytes(swapInt(dwHeight)));
            baos.write(intBytes(swapInt(dwReserved[0])));
            baos.write(intBytes(swapInt(dwReserved[1])));
            baos.write(intBytes(swapInt(dwReserved[2])));
            baos.write(intBytes(swapInt(dwReserved[3])));

            return baos.toByteArray();
        }
    }

    private class AVIStreamList {

        public byte[] fcc = new byte[]{'L', 'I', 'S', 'T'};
        public int size = 124;
        public byte[] fcc2 = new byte[]{'s', 't', 'r', 'l'};

        public AVIStreamList() {
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(fcc2);

            return baos.toByteArray();
        }
    }

    private class AVIStreamHeader {
        /*
         * FOURCC fcc; DWORD cb; FOURCC fccType; FOURCC fccHandler; DWORD
         * dwFlags; WORD wPriority; WORD wLanguage; DWORD dwInitialFrames; DWORD
         * dwScale; DWORD dwRate; DWORD dwStart; DWORD dwLength; DWORD
         * dwSuggestedBufferSize; DWORD dwQuality; DWORD dwSampleSize; struct {
         * short int left; short int top; short int right; short int bottom; }
         * rcFrame;
         */

        public byte[] fcc = new byte[]{'s', 't', 'r', 'h'};
        public int cb = 64;
        public byte[] fccType = new byte[]{'v', 'i', 'd', 's'};
        public byte[] fccHandler = new byte[]{'M', 'J', 'P', 'G'};
        public int dwFlags = 0;
        public short wPriority = 0;
        public short wLanguage = 0;
        public int dwInitialFrames = 0;
        public int dwScale = 0;                                // microseconds
        // per
        // frame
        public int dwRate = 1000000;                          // dwRate
        // /
        // dwScale
        // =
        // frame
        // rate
        public int dwStart = 0;
        public int dwLength = 0;                                // num
        // frames
        public int dwSuggestedBufferSize = 0;
        public int dwQuality = -1;
        public int dwSampleSize = 0;
        public int left = 0;
        public int top = 0;
        public int right = 0;
        public int bottom = 0;

        public AVIStreamHeader() {
            dwScale = (int) ((1.0 / framerate) * 1000000.0);
            dwLength = numFrames;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(fccType);
            baos.write(fccHandler);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(shortBytes(swapShort(wPriority)));
            baos.write(shortBytes(swapShort(wLanguage)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwScale)));
            baos.write(intBytes(swapInt(dwRate)));
            baos.write(intBytes(swapInt(dwStart)));
            baos.write(intBytes(swapInt(dwLength)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwQuality)));
            baos.write(intBytes(swapInt(dwSampleSize)));
            baos.write(intBytes(swapInt(left)));
            baos.write(intBytes(swapInt(top)));
            baos.write(intBytes(swapInt(right)));
            baos.write(intBytes(swapInt(bottom)));

            return baos.toByteArray();
        }
    }

    private class AVIStreamFormat {
        /*
         * FOURCC fcc; DWORD cb; DWORD biSize; LONG biWidth; LONG biHeight; WORD
         * biPlanes; WORD biBitCount; DWORD biCompression; DWORD biSizeImage;
         * LONG biXPelsPerMeter; LONG biYPelsPerMeter; DWORD biClrUsed; DWORD
         * biClrImportant;
         */

        public byte[] fcc = new byte[]{'s', 't', 'r', 'f'};
        public int cb = 40;
        public int biSize = 40;                               // same
        // as
        // cb
        public int biWidth = 0;
        public int biHeight = 0;
        public short biPlanes = 1;
        public short biBitCount = 24;
        public byte[] biCompression = new byte[]{'M', 'J', 'P', 'G'};
        public int biSizeImage = 0;                                // width
        // x
        // height
        // in
        // pixels
        public int biXPelsPerMeter = 0;
        public int biYPelsPerMeter = 0;
        public int biClrUsed = 0;
        public int biClrImportant = 0;

        public AVIStreamFormat() {
            biWidth = width;
            biHeight = height;
            biSizeImage = width * height;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(biSize)));
            baos.write(intBytes(swapInt(biWidth)));
            baos.write(intBytes(swapInt(biHeight)));
            baos.write(shortBytes(swapShort(biPlanes)));
            baos.write(shortBytes(swapShort(biBitCount)));
            baos.write(biCompression);
            baos.write(intBytes(swapInt(biSizeImage)));
            baos.write(intBytes(swapInt(biXPelsPerMeter)));
            baos.write(intBytes(swapInt(biYPelsPerMeter)));
            baos.write(intBytes(swapInt(biClrUsed)));
            baos.write(intBytes(swapInt(biClrImportant)));

            return baos.toByteArray();
        }
    }

    private class AVIMovieList {

        public byte[] fcc = new byte[]{'L', 'I', 'S', 'T'};
        public int listSize = 0;
        public byte[] fcc2 = new byte[]{'m', 'o', 'v', 'i'};

        // 00db size jpg image data ...
        public AVIMovieList() {
        }

        public AVIMovieList(int listSize) {
            this.listSize = listSize;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc2);

            return baos.toByteArray();
        }
    }

    private class AVIIndexList {

        public byte[] fcc = new byte[]{'i', 'd', 'x', '1'};
        public int cb = 0;
        public List<AVIIndex> ind = new ArrayList<>();

        public AVIIndexList() {
        }

        @SuppressWarnings("unused")
        public void addAVIIndex(AVIIndex ai) {
            ind.add(ai);
        }

        public void addAVIIndex(int dwOffset, int dwSize) {
            ind.add(new AVIIndex(dwOffset, dwSize));
        }

        public byte[] toBytes() throws IOException {
            cb = 16 * ind.size();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            for (int i = 0; i < ind.size(); i++) {
                AVIIndex in = ind.get(i);
                baos.write(in.toBytes());
            }

            return baos.toByteArray();
        }
    }

    private class AVIIndex {

        public byte[] fcc = new byte[]{'0', '0', 'd', 'b'};
        public int dwFlags = 16;
        public int dwOffset = 0;
        public int dwSize = 0;

        public AVIIndex(int dwOffset, int dwSize) {
            this.dwOffset = dwOffset;
            this.dwSize = dwSize;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwOffset)));
            baos.write(intBytes(swapInt(dwSize)));

            return baos.toByteArray();
        }
    }

    private static class AVIJunk {

        public static final byte[] fcc = new byte[] { 'J', 'U', 'N', 'K' };
        public static final int size = 1808;
        public static final byte[] data = new byte[size];
        public AVIJunk() {
            Arrays.fill(data, (byte) 0);
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(data);

            return baos.toByteArray();
        }
    }

    public byte[] writeImageToBytes(Image image, float quality) throws Exception {
        BufferedImage bi;
        if (image instanceof BufferedImage && ((BufferedImage) image).getType() == BufferedImage.TYPE_INT_RGB) {
            bi = (BufferedImage) image;
        } else {
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bi.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
        try (ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(baos)) {
            imgWrtr.setOutput(imgOutStrm);

            ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
            jpgWrtPrm.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWrtPrm.setCompressionQuality(quality);
            imgWrtr.write(null, new IIOImage(bi, null, null), jpgWrtPrm);
        }

        return baos.toByteArray();
    }
}
