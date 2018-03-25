package cc.aoeiuv020.panovel.check;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

import java.security.MessageDigest;

/**
 * https://gist.github.com/scottyab/b849701972d57cf9562e
 */
@SuppressWarnings("All")
public class SignatureUtil {

    public static String getAppSignature(Context context) {

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            StringBuilder sb = new StringBuilder();
            for (Signature signature : packageInfo.signatures) {
                // SHA1 the signature
                String sha1 = getSHA1(signature.toByteArray());
                sb.append(sha1);
                // check is matches hardcoded value
            }
            return sb.toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    //computed the sha1 hash of the signature
    public static String getSHA1(byte[] sig) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1", "BC");
            digest.update(sig);
            byte[] hashtext = digest.digest();
            return bytesToHex(hashtext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //util method to convert byte array to hex string
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}