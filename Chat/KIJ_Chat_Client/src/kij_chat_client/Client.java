package kij_chat_client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import sun.misc.BASE64Encoder;

/** original ->http://www.dreamincode.net/forums/topic/262304-simple-client-and-server-chat-program/
 * 
 * @author santen-suru
 */

public class Client implements Runnable {

	private Socket socket;//MAKE SOCKET INSTANCE VARIABLE
        public boolean hasLogin=false;
        ObjectOutputStream out;
        ObjectInputStream in;
        KeyPair myPair ;
        PrivateKey privateKey;
        ArrayList<Pair<String,PublicKey>> _publicKey;
        
        // use arraylist -> arraylist dapat diparsing as reference
        volatile ArrayList<String> log = new ArrayList<>();
        
	public Client(Socket s)
	{
		socket = s;//INSTANTIATE THE INSTANCE VARIABLE
                log.add(String.valueOf("false"));
	}
	
	@Override
	public void run()//INHERIT THE RUN METHOD FROM THE Runnable INTERFACE
	{
		try
		{
			Scanner chat = new Scanner(System.in);//GET THE INPUT FROM THE CMD
			in = new ObjectInputStream(socket.getInputStream());//GET THE CLIENTS INPUT STREAM (USED TO READ DATA SENT FROM THE SERVER)
			out = new ObjectOutputStream(socket.getOutputStream());//GET THE CLIENTS OUTPUT STREAM (USED TO SEND DATA TO THE SERVER)
                        _publicKey=new ArrayList();
			
//			while (true)//WHILE THE PROGRAM IS RUNNING
//			{						
//				String input = chat.nextLine();	//SET NEW VARIABLE input TO THE VALUE OF WHAT THE CLIENT TYPED IN
//				out.println(input);//SEND IT TO THE SERVER
//				out.flush();//FLUSH THE STREAM
//				
//				if(in.hasNext())//IF THE SERVER SENT US SOMETHING
//					System.out.println(in.nextLine());//PRINT IT OUT
//			}
                        
                        Read reader = new Read(in, out, log,this,_publicKey); //socket.in
			
			Thread tr = new Thread(reader);
			tr.start();
                        
                        Write writer = new Write(chat, out, log,this);//socket.out
			
			Thread tw = new Thread(writer);
			tw.start();
                        
//                        System.out.println(tr.isAlive());
                        while (tr.isAlive() == true) {
                            if (tr.isAlive() == false && tw.isAlive() == false) {
                                socket.close();
                            }
                        }
		}
		catch (Exception e)
		{
			e.printStackTrace();//MOST LIKELY WONT BE AN ERROR, GOOD PRACTICE TO CATCH THOUGH
		} 
	}
        
        public void hasLogin() throws IOException{
            try {
                this.hasLogin=true;
                //Start RSA encrypt
                // Get an instance of the RSA key generator
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                // Generate the keys — might take sometime on slow computers
                KeyPair myPair = kpg.generateKeyPair();
                
                PublicKey publicKey = myPair.getPublic();
                privateKey = myPair.getPrivate();
                //dari http://janiths-codes.blogspot.co.id/2009/11/how-to-convert-publickey-as-string-and.html
                
                // Send the public key bytes to the other party...
                this.out.writeObject("publickey");
                this.out.flush();
                //this.in.readObject();
                
                this.out.writeObject(publicKey);
                this.out.flush();
                System.out.println("Public key Saya Adalah "+publicKey);
                //ngirim public key
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

}

