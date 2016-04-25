package kij_chat_client;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/** original ->http://www.dreamincode.net/forums/topic/262304-simple-client-and-server-chat-program/
 * 
 * @author santen-suru
 */

public class Client implements Runnable {

	private Socket socket;//MAKE SOCKET INSTANCE VARIABLE
        public boolean hasLogin=false;
        PrintWriter out;
        
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
			Scanner in = new Scanner(socket.getInputStream());//GET THE CLIENTS INPUT STREAM (USED TO READ DATA SENT FROM THE SERVER)
			out = new PrintWriter(socket.getOutputStream());//GET THE CLIENTS OUTPUT STREAM (USED TO SEND DATA TO THE SERVER)
			
//			while (true)//WHILE THE PROGRAM IS RUNNING
//			{						
//				String input = chat.nextLine();	//SET NEW VARIABLE input TO THE VALUE OF WHAT THE CLIENT TYPED IN
//				out.println(input);//SEND IT TO THE SERVER
//				out.flush();//FLUSH THE STREAM
//				
//				if(in.hasNext())//IF THE SERVER SENT US SOMETHING
//					System.out.println(in.nextLine());//PRINT IT OUT
//			}
                        
                        Read reader = new Read(in, log,this); //socket.in
			
			Thread tr = new Thread(reader);
			tr.start();
                        
                        Write writer = new Write(chat, out, log);//socket.out
			
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
        
        public void hasLogin(){
            try {
                this.hasLogin=true;
                //Start RSA encrypt
                // Get an instance of the RSA key generator
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                // Generate the keys — might take sometime on slow computers
                KeyPair myPair = kpg.generateKeyPair();
                
                out.println("publicKey "+myPair.getPublic());
                //ngirim public key
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

}

