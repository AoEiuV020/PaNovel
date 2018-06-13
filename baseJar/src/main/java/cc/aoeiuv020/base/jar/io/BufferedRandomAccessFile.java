package cc.aoeiuv020.base.jar.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * https://gist.githubusercontent.com/angerman/248605/raw/204620bac623bfe43b269c6386df4f9aa31c1ead/BufferedRandomAccessFile.java
 */
@SuppressWarnings("all")
public class BufferedRandomAccessFile extends RandomAccessFile {

    private byte[] bytebuffer;
    private int bufferlength;
    private int maxread;
    private int buffpos;
    private ByteArrayOutputStream input;

    public BufferedRandomAccessFile(File file, String mode) throws
            FileNotFoundException {
        super(file, mode);
        bufferlength = 65536;
        bytebuffer = new byte[bufferlength];
        maxread = 0;
        buffpos = 0;
        input = new ByteArrayOutputStream();
    }

    public int getbuffpos() {
        return buffpos;
    }

    @Override
    public int read() throws IOException {
        if (buffpos >= maxread) {
            maxread = readchunk();
            if (maxread == -1) {
                return -1;
            }
        }
        buffpos++;
        return bytebuffer[buffpos - 1];
    }

    public String readLine(String charset) throws IOException {
        input.reset();
        int c = -1;
        boolean eol = false;
        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input.write(c);
                    break;
            }
        }

        if ((c == -1) && (input.size() == 0)) {
            return null;
        }
        return input.toString(charset);
    }

    @Override
    public long getFilePointer() throws IOException {
        return super.getFilePointer() + buffpos;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (maxread != -1 && pos < (super.getFilePointer() + maxread) && pos > super.getFilePointer()) {
            Long diff = (pos - super.getFilePointer());
            if (diff < Integer.MAX_VALUE) {
                buffpos = diff.intValue();
            } else {
                throw new IOException("something wrong w/ seek");
            }
        } else {
            buffpos = 0;
            super.seek(pos);
            maxread = readchunk();
        }
    }

    private int readchunk() throws IOException {
        long pos = super.getFilePointer() + buffpos;
        super.seek(pos);
        int read = super.read(bytebuffer);
        super.seek(pos);
        buffpos = 0;
        return read;
    }
}