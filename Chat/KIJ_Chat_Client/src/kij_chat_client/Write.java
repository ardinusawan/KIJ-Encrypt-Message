/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
    Buat nerima input dari CMD
*/
package kij_chat_client;

import java.io.ObjectOutputStream;
import java.security.PublicKey;
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
        private ObjectOutputStream out;
        boolean keepGoing = true;
        ArrayList<String> log;
	int flag=0;
        Client father;
        ArrayList<Pair<String,PublicKey>> _publicKey;
        String ID;
        PublicKey publicKey = null;
        
	public Write(Scanner chat, ObjectOutputStream out, ArrayList<String> log,Client father, ArrayList<Pair<String,PublicKey>> _publicKey)
	{
		this.chat = chat;
                this.out = out;
                this.log = log;
                this.father = father;
                this._publicKey=_publicKey;
	}
	
	@Override
	public void run()//INHERIT THE RUN METHOD FROM THE Runnable INTERFACE
	{
		try
		{
			while (keepGoing)//WHILE THE PROGRAM IS RUNNING
			{						
				String input = chat.nextLine();	//SET NEW VARIABLE input TO THE VALUE OF WHAT THE CLIENT TYPED IN
                              
                                //Start RSA encrypt
                                // Get an instance of the RSA key generator
                                //KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                                // Generate the keys — might take sometime on slow computers
                                //KeyPair myPair = kpg.generateKeyPair();
                                
                          
        
        
                                
                                
                                
                                //out.println(myEncryptedMessage);
                                //out.flush();
//                                //Start RSA decrypt
//                                // Get an instance of the Cipher for RSA encryption/decryption
//                                Cipher dec = Cipher.getInstance("RSA");
//                                // Initiate the Cipher, telling it that it is going to Decrypt, giving it the private key
//                                dec.init(Cipher.DECRYPT_MODE, myPair.getPublic());
//                                
//                                // Tell the SealedObject we created before to decrypt the data and return it
//                                String message = (String) myEncryptedMessage.getObject(dec);
//                                System.out.println("foo = "+message);
//                                //End RSA decrypt

                               
                                
                                if(flag==0){
                                    System.out.println("kirim plaintext");
                                    if(input.startsWith("gm"))//broadcast gausah
                                    {
                                        flag=1;  
                                        ID=input.split(" ")[1];
                                    }
                                    else if(input.startsWith("pm")){
                                        flag=1;  
                                        ID=input.split(" ")[1];
                                        boolean notFound=true;
                                        for(Pair<String,PublicKey>iter:_publicKey){
                                            if(iter.getFirst().equals(ID))
                                            {
                                                publicKey=iter.getSecond();
                                                notFound=false;
                                                break;
                                            }
                                        }
                                        if(notFound){
                                            System.out.println("Destination ID not found or offline");
                                            flag=0;
                                            continue;
                                        }
                                        else{
                                            out.writeObject(input);//SEND IT TO THE SERVER
                                            out.flush();//FLUSH THE STREAM
                                        }
                                    }
                                    else{
                                        out.writeObject(input);//SEND IT TO THE SERVER
                                        out.flush();//FLUSH THE STREAM  
                                    }
                                        
                                }
                                else{
                                    
                                    //RSA Encrypt
                                    
                                    
                                    // Get an instance of the Cipher for RSA encryption/decryption
                                    Cipher c = Cipher.getInstance("RSA");
                                    // Initiate the Cipher, telling it that it is going to Encrypt, giving it the public key
                                    c.init(Cipher.ENCRYPT_MODE, publicKey); 

                                    // Create a secret message
                                    // String myMessage = new String("Secret Message");
                                    // Encrypt that message using a new SealedObject and the Cipher we created before
                                    SealedObject myEncryptedMessage= new SealedObject( input, c);
                                    //End Start RSA encrypt
                                    //String cipherText=(String) myEncryptedMessage;
                                    System.out.println(myEncryptedMessage);
                                    System.out.println("kirim cipher");
                                    out.writeObject(myEncryptedMessage);//SEND IT TO THE SERVER 
                                    out.flush();
                                    flag=0;
                                }
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
