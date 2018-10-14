package com.syh.bigfiletransfer.util;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.nio.channels.Channels;

public class FileUtil {

    public static InputStream getFileStream(File file, long start, long end) throws IOException {
        RandomAccessFile rFile = new RandomAccessFile(file, "r");
        rFile.seek(start);
        long count = end - start + 1;
        return ByteStreams.limit(Channels.newInputStream(rFile.getChannel()), count);
    }
}
