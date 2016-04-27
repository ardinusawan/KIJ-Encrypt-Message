/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kij_chat_client;

import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class DigitalSignature {

    public static String Generete(String _message) {
        try {
            //Step 1 Generate Public and Private Keys
                    
            /* Create a Key Pair Generator */
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            
            /* Initialize the Key Pair Generator */
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            
            /* Generate the Pair of Keys */
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();
            
            //Step 2 Sign the Data
            
            /* Get a Signature Object */
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            
            /* Initialize the Signature Object */
            dsa.initSign(priv);
            
            /* Supply the Signature Object the Data to Be Signed */
            //FileInputStream fis = new FileInputStream(args[0]);
            FileInputStream fis = new FileInputStream(_message);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            };
            bufin.close();
            
            /* Generate the Signature */
            byte[] realSig = dsa.sign();
            
            return base64Encode(realSig); //signature dalam bentuk string
        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
            return _message;
        }
    
    }
    
    public static void Verify(String _publickey, String _signature, String _message) {
        try {
            /* import encoded public key */
            
            //Input and Convert the Encoded Public Key Bytes
            
            /*  Read in the encoded public key bytes */
            FileInputStream keyfis = new FileInputStream(_publickey);
            byte[] encKey = new byte[keyfis.available()];  
            keyfis.read(encKey);
 
            keyfis.close();
 
            /* Key specification */
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
 
            /* KeyFactory object to do the conversion */
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            
            /* Use the KeyFactory object to generate a PublicKey from the key specification */
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
 
            
            // Input the signature bytes
            
            /* Input the signature bytes from the file specified as the second command line argument */
            
            FileInputStream sigfis = new FileInputStream(_signature);
            byte[] sigToVerify = new byte[sigfis.available()]; 
            sigfis.read(sigToVerify );
 
            sigfis.close();
 
            
            //Verify the Signature
            
            /* Create a Signature object and initialize it with the public key */
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            
            /* Initialize the Signature Object for Verification. The initialization method for verification requires the public key. */
            sig.initVerify(pubKey);
            
            /* Update and verify the data */
            
            /* Supply the Signature Object With the Data to be Verified */
            FileInputStream datafis = new FileInputStream(_message);
            BufferedInputStream bufin = new BufferedInputStream(datafis);
 
            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                sig.update(buffer, 0, len);
                };
 
            bufin.close();
 
            /* Verify the Signature */
            boolean verifies = sig.verify(sigToVerify);
 
            System.out.println("signature verifies: " + verifies);
            
        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
    }
    }
    
    private static final String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    private static byte[] base64Decode(String input)    {
        if (input.length() % 4 != 0)    {
            throw new IllegalArgumentException("Invalid base64 input");
        }
        byte decoded[] = new byte[((input.length() * 3) / 4) - (input.indexOf('=') > 0 ? (input.length() - input.indexOf('=')) : 0)];
        char[] inChars = input.toCharArray();
        int j = 0;
        int b[] = new int[4];
        for (int i = 0; i < inChars.length; i += 4)     {
            // This could be made faster (but more complicated) by precomputing these index locations.
            b[0] = CODES.indexOf(inChars[i]);
            b[1] = CODES.indexOf(inChars[i + 1]);
            b[2] = CODES.indexOf(inChars[i + 2]);
            b[3] = CODES.indexOf(inChars[i + 3]);
            decoded[j++] = (byte) ((b[0] << 2) | (b[1] >> 4));
            if (b[2] < 64)      {
                decoded[j++] = (byte) ((b[1] << 4) | (b[2] >> 2));
                if (b[3] < 64)  {
                    decoded[j++] = (byte) ((b[2] << 6) | b[3]);
	        }
            }
        }

        return decoded;
    }

    private static String base64Encode(byte[] in)       {
        StringBuilder out = new StringBuilder((in.length * 4) / 3);
        int b;
        for (int i = 0; i < in.length; i += 3)  {
            b = (in[i] & 0xFC) >> 2;
            out.append(CODES.charAt(b));
            b = (in[i] & 0x03) << 4;
            if (i + 1 < in.length)      {
                b |= (in[i + 1] & 0xF0) >> 4;
                out.append(CODES.charAt(b));
                b = (in[i + 1] & 0x0F) << 2;
                if (i + 2 < in.length)  {
                    b |= (in[i + 2] & 0xC0) >> 6;
                    out.append(CODES.charAt(b));
                    b = in[i + 2] & 0x3F;
                    out.append(CODES.charAt(b));
                } else  {
                    out.append(CODES.charAt(b));
                    out.append('=');
                }
            } else      {
                out.append(CODES.charAt(b));
                out.append("==");
            }
        }

        return out.toString();
    }
}
