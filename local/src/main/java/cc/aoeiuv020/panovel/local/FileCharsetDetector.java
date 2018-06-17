package cc.aoeiuv020.panovel.local;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * http://www.cnblogs.com/yejg1212/p/3402322.html?spm=a2c4e.11153940.blogcont59514.3.15a85e805Oo7MG
 */
@SuppressWarnings("all")
public class FileCharsetDetector {
    public static final int ALL = nsPSMDetector.ALL;
    public static final int JAPANESE = nsPSMDetector.JAPANESE;
    public static final int CHINESE = nsPSMDetector.CHINESE;
    public static final int SIMPLIFIED_CHINESE = nsPSMDetector.SIMPLIFIED_CHINESE;
    public static final int TRADITIONAL_CHINESE = nsPSMDetector.TRADITIONAL_CHINESE;
    public static final int KOREAN = nsPSMDetector.KOREAN;
    public static final int NO_OF_LANGUAGES = nsPSMDetector.NO_OF_LANGUAGES;
    private boolean found = false;
    private String encoding = null;

    /**
     * 传入一个输入流，检查文件编码
     *
     * @param inputStream 任意输入流，没有关闭，
     * @return 文件编码，eg：UTF-8,GBK,GB2312形式(不确定的时候，返回可能的字符编码序列)；若无，则返回null
     */
    public static String guessStreamEncoding(InputStream inputStream) throws FileNotFoundException, IOException {
        return guessStreamEncoding(inputStream, ALL);
    }

    /**
     * 判断输入流的编码，
     *
     * @param inputStream  任意输入流，没有关闭，
     * @param languageHint 语言提示区域代码 @see #nsPSMDetector ,取值如下：
     *                     1 : Japanese
     *                     2 : Chinese
     *                     3 : Simplified Chinese
     *                     4 : Traditional Chinese
     *                     5 : Korean
     *                     6 : Dont know(default)
     * @return 文件编码，eg：UTF-8,GBK,GB2312形式(不确定的时候，返回可能的字符编码序列)；若无，则返回null
     */
    public static String guessStreamEncoding(InputStream inputStream, int languageHint) throws FileNotFoundException, IOException {
        return new FileCharsetDetector().guessStreamEncoding(inputStream, new nsDetector(languageHint));
    }

    /**
     * 判断输入流的编码，
     *
     * @param inputStream 任意输入流，没有关闭，
     */
    private String guessStreamEncoding(InputStream imp, nsDetector det) throws FileNotFoundException, IOException {
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                encoding = charset;
                found = true;
            }
        });

        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = false;

        // 每次读1k数据，不需要缓冲，
        while ((len = imp.read(buf, 0, buf.length)) != -1) {
            // Check if the stream is only ascii.
            isAscii = det.isAscii(buf, len);
            if (isAscii) {
                break;
            }
            // DoIt if non-ascii and not done yet.
            done = det.DoIt(buf, len, false);
            if (done) {
                break;
            }
        }
        det.DataEnd();

        if (isAscii) {
            encoding = "ASCII";
            found = true;
        }

        if (!found) {
            String[] prob = det.getProbableCharsets();
            //这里将可能的字符集组合起来返回
            for (int i = 0; i < prob.length; i++) {
                if (i == 0) {
                    encoding = prob[i];
                } else {
                    encoding += "," + prob[i];
                }
            }

            if (prob.length > 0) {
                // 在没有发现情况下,也可以只取第一个可能的编码,这里返回的是一个可能的序列
                return encoding;
            } else {
                return null;
            }
        }
        return encoding;
    }
}