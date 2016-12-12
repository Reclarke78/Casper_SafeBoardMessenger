package ru.olgathebest.casper;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.R.id.accessibilityActionShowOnScreen;
import static android.R.id.message;

/**
 * Created by Ольга on 12.12.2016.
 */

public class EncriptionTest {
public static RSA rsa = new RSA(1024);

    public static void main(String[] args) {
        String str = "Оля";
        String enc= "";
        byte[] encrypt = new byte[0];
        try {
           // enc = encrypt(str,rsa.getN());
          //  enc = rsa.rsaEncrypt(str,rsa.getN().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
      // encrypt = UTF8.encode(enc);
      //  enc = UTF8.decode(encrypt);
        String decrypt = null;
        try {
         //   decrypt = rsa.rsaDecrypt(enc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(str);
        System.out.println(decrypt);
        System.out.println(str.equals(decrypt));
    }
    public static String encrypt(String str, BigInteger pk) throws Exception {
        return rsa.encrypt(str,pk);

    }
    public static String decrypt(String EncData){
        return rsa.decrypt(EncData);
    }
//    public static byte[] encrypt(String str) throws Exception{
//        AES aes;
//        String msgEnripted ="";
//
//        byte[] keyEnc;
//        String keyEnripted;
//        byte[] dataEncripted;
//        aes = new AES();
//        try {
//            msgEnripted = aes.encrypt(str);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(new String(aes.getSecretKey()));
//        keyEnripted = rsa.encrypt(aes.getSecretKey());
//        keyEnc = UTF8.hexToBytes(keyEnripted);
//        dataEncripted = new byte[256+msgEnripted.getBytes().length];
//        for (int i = 0; i < 256; i++){
//            if (i < 256 - keyEnc.length)
//                dataEncripted[i] = 0;
//            else
//            dataEncripted[i] = keyEnc[i-256+keyEnc.length];
//
//        }
//        for (int i = 0; i < msgEnripted.getBytes().length; i++){
//            dataEncripted[i+256] = msgEnripted.getBytes()[i];
//        }
//        return dataEncripted;
//    }
//    public static String decrypt(byte [] EncData, BigInteger pk){
//        AES aes;
//        byte[] Enckey = new byte[256];
//        for (int i = 0; i < 256; i++){
//            if ( EncData[i] !=0)
//            Enckey[i] = EncData[i];
//        }
//        byte[] EncMsg = new byte[EncData.length - 256];
//        for (int i = 256; i < EncData.length; i++){
//            EncMsg[i-256] = EncData[i];
//        }
//        String msgDecripted = "";
//        String keyDecripted = rsa.decrypt(EncMsg,pk);
//        System.out.println(new String(keyDecripted.getBytes()));
//        aes = new AES();
//        try {
//            msgDecripted = aes.decrypt(new String(EncMsg), keyDecripted);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return msgDecripted;
//    }
}
