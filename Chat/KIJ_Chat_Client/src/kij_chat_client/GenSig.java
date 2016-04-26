/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kij_chat_client;

import java.io.*;
import java.security.*;

class GenSig {

    public static void main(String[] args) {

        /* Generate a DSA signature */

        if (args.length != 1) {
            System.out.println("Usage: GenSig nameOfFileToSign");
        }
        else try {

        // the rest of the code goes here
            
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
            FileInputStream fis = new FileInputStream(args[0]);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            };
            bufin.close();
            
            /* Generate the Signature */
            byte[] realSig = dsa.sign();
            
            //Step 3 Save the Signature and the Public Key in Files */
            
            /* Save the signature in a file */
            FileOutputStream sigfos = new FileOutputStream("sig");
            sigfos.write(realSig);
            sigfos.close();
            
            /* Save the public key in a file */
            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = new FileOutputStream("suepk");
            keyfos.write(key);
            keyfos.close();

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
    }
}
    
