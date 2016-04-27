/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
    Buat nerima input dari socket.in
    dari inet cok
*/
package kij_chat_client;

/*import java.net.Socket;*/
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

/**
 *
 * @author santen-suru
 */
public class Read implements Runnable {
        
        private ObjectInputStream in;//MAKE SOCKET INSTANCE VARIABLE
        private ObjectOutputStream out;
        String input;
        boolean keepGoing = true;
        ArrayList<String> log;
        Client father;
        ArrayList<Pair<String,PublicKey>> _publicKey;
	
	public Read(ObjectInputStream in, ObjectOutputStream out, ArrayList<String> log,Client father,ArrayList<Pair<String,PublicKey>> _publicKey)
	{
		this.in = in;
                this.log = log;
                this.father=father;
                this._publicKey=_publicKey;
                this.out=out;
	}
    
        @Override
	public void run()//INHERIT THE RUN METHOD FROM THE Runnable INTERFACE
	{
		try
                    //INHERIT THE RUN METHOD FROM THE Runnable INTERFACE
                {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                    KeyPair myPair = kpg.generateKeyPair();
                    try
                    {
                        while (keepGoing)//WHILE THE PROGRAM IS RUNNING
                        {
                            Object inputObject= in.readObject();
                            if(inputObject!=null) {
                                input=(String)inputObject;
                                //IF THE SERVER SENT US SOMETHING
                                //input = this.in.nextLine();
                                System.out.println(input);//PRINT IT OUT
                                if (input.split(" ")[0].toLowerCase().equals("success")) {
                                    if (input.split(" ")[1].toLowerCase().equals("logout")) {
                                        keepGoing = false;
                                    } else if (input.split(" ")[1].toLowerCase().equals("login")) {
                                        log.clear();
                                        log.add("true");
                                        father.myPair=myPair;
                                        father.hasLogin();
                                        //out.writeObject("klol");
                                    }
                                }
                                else if (input.split(" ")[0].toLowerCase().equals("publickey")) {
                                    if (input.split(" ")[1].toLowerCase().equals("add")) {
                                        String ID=input.split(" ")[2];
                                        inputObject= in.readObject();
                                        PublicKey publicKey=(PublicKey)inputObject;
                                        _publicKey.add(new Pair(ID,publicKey));
                                        //
                                        for(Pair<String,PublicKey>iter:_publicKey){
                                            System.out.println(iter.getFirst());
                                            System.out.println(iter.getSecond());
                                        }
                                        System.out.println("done adding this round");
                                    }
                                    else if (input.split(" ")[1].toLowerCase().equals("remove")) {
                                        String ID=input.split(" ")[2];
                                        inputObject= in.readObject();
                                        PublicKey publicKey=(PublicKey)inputObject;
                                        for(Pair<String,PublicKey> iter: _publicKey){
                                            if(iter.getFirst().equals(ID)){
                                               _publicKey.remove(iter);
                                               break;
                                            }
                                        }
                                        for(Pair<String,PublicKey>iter:_publicKey){
                                            System.out.println(iter.getFirst());
                                            System.out.println(iter.getSecond());
                                        }
                                        System.out.println("done removing this round");
                                    }
                                }
                                else if (input.toLowerCase().equals("fail login")) {
                                }
                                else{
                                    //System.out.println(input);
                                    inputObject= in.readObject();
                                    SealedObject encryptedMessage = (SealedObject)inputObject;
                                    System.out.println(encryptedMessage);
                                    String ID = input.split(" ")[0];
                                    
                                    // Get an instance of the Cipher for RSA encryption/decryption
                                    Cipher dec = Cipher.getInstance("RSA");
                                    // Initiate the Cipher, telling it that it is going to Decrypt, giving it the private key
                                    dec.init(Cipher.DECRYPT_MODE, father.privateKey);
                                    // Tell the SealedObject we created before to decrypt the data and return it
                                    String message = (String) encryptedMessage.getObject(dec);
                                    System.out.println(ID+" decrypted message: "+message);
                                }
                            }
                            
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();//MOST LIKELY WONT BE AN ERROR, GOOD PRACTICE TO CATCH THOUGH
                    }
                }
		catch (NoSuchAlgorithmException ex)
		{
			Logger.getLogger(Read.class.getName()).log(Level.SEVERE, null, ex);
		} 
	}
}
