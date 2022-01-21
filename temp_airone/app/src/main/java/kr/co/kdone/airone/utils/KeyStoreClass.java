package kr.co.kdone.airone.utils;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * 아이디 및 패스워드 로컬 저장을 위한 KeyStore
 */
public class KeyStoreClass {
    private static String LOCAL_TAG = "KeyStoreClass";
    private static boolean debug = false;
    private static String alias = "KDONE Keystore";

    public static void createNewKeys(Context context) {
        KeyStore keyStore;

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
                if(debug) {
                    Log.d(LOCAL_TAG, "Create key store!!!!");
                }
            } else {
                if(debug) {
                    Log.d(LOCAL_TAG, "Exist key store!!!!");
                }
            }
        } catch (Exception e) {
            Log.e(LOCAL_TAG, Log.getStackTraceString(e));
        }
    }

    public static String encryptString(String input) {
        KeyStore keyStore;
        String result = "";

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            java.security.KeyStore.PrivateKeyEntry privateKeyEntry = (java.security.KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            if(input.isEmpty()) {
                return result;
            }

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(input.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();
            result= Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(LOCAL_TAG, Log.getStackTraceString(e));
        }
        if(debug) {
            Log.d(LOCAL_TAG, "encryptString = " + result);
        }
        return result;
    }

    public static String decryptString(String cipherText) {
        KeyStore keyStore;
        String finalText = "";
        try {
            /*
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            Key privateKey = (Key) privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);
            */

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            java.security.KeyStore.PrivateKeyEntry privateKeyEntry = (java.security.KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            finalText = new String(bytes, 0, bytes.length, "UTF-8");
        } catch (Exception e) {
            Log.e(LOCAL_TAG, Log.getStackTraceString(e));
        }
        if(debug) {
            Log.e(LOCAL_TAG, "decryptString = " + finalText);
        }
        return finalText;
    }
}
