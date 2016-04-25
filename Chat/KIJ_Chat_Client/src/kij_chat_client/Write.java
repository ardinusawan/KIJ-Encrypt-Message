/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
    Buat nerima input dari CMD
*/
package kij_chat_client;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

/**
 *
 * @author santen-suru
 */
public class Write implements Runnable {
    
	private Scanner chat;
        private PrintWriter out;
        boolean keepGoing = true;
        ArrayList<String> log;
	
	public Write(Scanner chat, PrintWriter out, ArrayList<String> log)
	{
		this.chat = chat;
                this.out = out;
                this.log = log;
	}
	
	@Override
	public void run()//INHERIT THE RUN METHOD FROM THE Runnable INTERFACE
	{
		try
		{
			while (keepGoing)//WHILE THE PROGRAM IS RUNNING
			{						
				String input = chat.nextLine();	//SET NEW VARIABLE input TO THE VALUE OF WHAT THE CLIENT TYPED IN
				//Start Hash
                                
                                //int hashCode = input.hashCode();
                                //System.out.println("input hash code = " + hashCode);
                                
                                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                                messageDigest.update(input.getBytes());
                                String hashedString = new String(messageDigest.digest());
                                System.out.println(hashedString); //print hash
                                //End Hash
                                //input=input+hashedString;//msg+hash
                                
                                //Start RSA encrypt
                                // Get an instance of the RSA key generator
                                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                                // Generate the keys — might take sometime on slow computers
                                KeyPair myPair = kpg.generateKeyPair();
                                
                                // Get an instance of the Cipher for RSA encryption/decryption
                                Cipher c = Cipher.getInstance("RSA");
                                // Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
                                c.init(Cipher.ENCRYPT_MODE, myPair.getPrivate()); 
                                
                                // Create a secret message
                                // String myMessage = new String("Secret Message");
                                // Encrypt that message using a new SealedObject and the Cipher we created before
                                SealedObject myEncryptedMessage= new SealedObject( input, c);
                                //End Start RSA encrypt
                                //String cipherText=(String) myEncryptedMessage;
                                System.out.println(myEncryptedMessage);
                                //out.println(myEncryptedMessage);
                                //out.flush();
                                //Start RSA decrypt
                                // Get an instance of the Cipher for RSA encryption/decryption
                                Cipher dec = Cipher.getInstance("RSA");
                                // Initiate the Cipher, telling it that it is going to Decrypt, giving it the private key
                                dec.init(Cipher.DECRYPT_MODE, myPair.getPublic());
                                
                                // Tell the SealedObject we created before to decrypt the data and return it
                                String message = (String) myEncryptedMessage.getObject(dec);
                                System.out.println("foo = "+myPair.getPublic());
                                //End RSA decrypt

                                //
                                
                                
                                out.println(input);//SEND IT TO THE SERVER
				out.flush();//FLUSH THE STREAM
                                
                                if (input.contains("logout")) {
                                    if (log.contains("true"))
                                        keepGoing = false;
                                    
                                }
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();//MOST LIKELY WONT BE AN ERROR, GOOD PRACTICE TO CATCH THOUGH
		} 
	}

}
