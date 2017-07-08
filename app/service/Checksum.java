package service;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fincash on 03-06-2017.
 */
public class Checksum {

    Logger logger = LoggerFactory.getLogger(getClass());
    private static int pswdIterations = 1000;
    private static int keySize = 128;
    private String IV_VALUE = "";
    public Checksum(String  ivvalue){
        this.IV_VALUE = ivvalue;
    }

    public String Encrypt(String strToEncrypt, String saltValue, String password) {

        byte[] inputString = strToEncrypt.getBytes();      //  Base64.decodeBase64(strToEncrypt);
        byte[] iv = IV_VALUE.getBytes();
        byte[] salt = saltValue.getBytes();
        byte[] pwd = password.getBytes();//.decode(encryptionKey);
        String returnval = "";

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec( password.toCharArray(), saltValue.getBytes(), pswdIterations, keySize);
            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            byte[] enctyptedIVSting = cipher.doFinal(inputString);
            returnval = new Base64().encodeAsString(enctyptedIVSting);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnval;
    }


    public String Decrypt(String strToDecrypt, String saltValue, String password) {
        byte[] ivstring = strToDecrypt.getBytes();      //  Base64.decodeBase64(strToEncrypt);
        byte[] salt = saltValue.getBytes();
        byte[] pwd = password.getBytes();//.decode(encryptionKey);
        String returnval = "";

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec( password.toCharArray(), salt, pswdIterations, keySize);
            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
//            HMACParameterSpec hspec = new HMACParameterSpec(16)
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] enctyptedIVSting = cipher.doFinal(ivstring);
            returnval = new Base64().encodeAsString(enctyptedIVSting);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnval;
    }


    public String GenerateSaltValue(String inputString, String SaltValue, String timestamp) {
        List<Integer> newTimeStamp = SplitByLength(timestamp, 2);
        StringBuilder key = new StringBuilder();
        int len = inputString.length();
        for (int x : newTimeStamp) {
            if (len > x) {
                if (x % 2 == 0) {
                    key.append(Character.toString(inputString.charAt(x)));
                } else {
                    key.append(Character.toString(inputString.charAt(len - 1 - x)));
                }

            } else {
                int newLen = inputString.length();
                String newStr = inputString;
                while (newLen - 1 < x) {
                    newStr = newStr + newStr;
                    newLen = newStr.length();
                }
                key.append(Character.toString(newStr.charAt(x)));
            }
        }
        return key.toString().substring(0, (newTimeStamp.size() / 2)) + SaltValue + key.toString().substring(newTimeStamp.size() / 2);
    }


    public static List<Integer> SplitByLength(String x, int maxLength) {
        List<Integer> a = new ArrayList<>();
        for (int i = 0; i < x.length(); i += maxLength) {
            if ((i + maxLength) < x.length())
                a.add(Integer.parseInt(x.substring(i, i + maxLength)) % 10);
            else
                a.add(Integer.parseInt(x.substring(i)) % 10);
        }
        return a;
    }
}
