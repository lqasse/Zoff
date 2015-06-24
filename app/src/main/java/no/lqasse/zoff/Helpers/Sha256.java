package no.lqasse.zoff.Helpers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lassedrevland on 21.06.15.
 */
public class Sha256 {

    public static String getHash(String stringToBeHashed){
        MessageDigest md = null;
        try{
            md = MessageDigest.getInstance("SHA-256");

            md.update(stringToBeHashed.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();

            return bytesToHex(digest);




        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();

        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }



        return "";

    }
    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
