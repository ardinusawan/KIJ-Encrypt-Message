package kij_chat_server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.xml.bind.DatatypeConverter;

/** original ->http://www.dreamincode.net/forums/topic/262304-simple-client-and-server-chat-program/
 * 
 * @author santen-suru
 */


public class Client implements Runnable{

	private Socket socket;//SOCKET INSTANCE VARIABLE
        private String username;
        private boolean login = false;
        private boolean hasLogin=false;
        private int count=0;
        private String keyTmp;
        //Main father;
        
        private ArrayList<Pair<Socket,String>> _loginlist;
        private ArrayList<Pair<String,String>> _userlist;
        private ArrayList<Pair<String,String>> _grouplist;
        private ArrayList<Pair<String,PublicKey>> _publicKey;
        //private ArrayList<Client> clientList;
	
	public Client(Socket s, ArrayList<Pair<Socket,String>> _loginlist, ArrayList<Pair<String,String>> _userlist, ArrayList<Pair<String,String>> _grouplist, ArrayList<Pair<String,PublicKey>> _publicKey)
	{
		socket = s;//INSTANTIATE THE SOCKET)
                this._loginlist = _loginlist;
                this._userlist = _userlist;
                this._grouplist = _grouplist;
                this._publicKey=_publicKey;
	}
	
	@Override
	public void run() //(IMPLEMENTED FROM THE RUNNABLE INTERFACE)
	{
		try //HAVE TO HAVE THIS FOR THE in AND out VARIABLES
		{
			//Scanner in = new Scanner(socket.getInputStream());//GET THE SOCKETS INPUT STREAM (THE STREAM THAT YOU WILL GET WHAT THEY TYPE FROM)
			//PrintWriter out = new PrintWriter(socket.getOutputStream());//GET THE SOCKETS OUTPUT STREAM (THE STREAM YOU WILL SEND INFORMATION TO THEM FROM)
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        
			while (true)//WHILE THE PROGRAM IS RUNNING
			{		
                                Object inputTmp=ois.readObject();
				if (inputTmp!=null)
				{
					String input = (String) inputTmp;//IF THERE IS INPUT THEN MAKE A NEW VARIABLE input AND READ WHAT THEY TYPED
//					System.out.println("Client Said: " + input);//PRINT IT OUT TO THE SCREEN
//					out.println("You Said: " + input);//RESEND IT TO THE CLIENT
//					out.flush();//FLUSH THE STREAM
                                        System.out.println("Receiving ="+input);
                                        // param LOGIN <userName> <pass>
                                        if (input.split(" ")[0].toLowerCase().equals("login") == true) {
                                            String[] vals = input.split(" ");
                                            hasLogin=false;
                                            for (Pair<Socket, String> selGroup : _loginlist) {
                                                if (selGroup.getSecond().equals(vals[1])) {
                                                    hasLogin=true;
                                                }
                                            }
                                            if (this._userlist.contains(new Pair(vals[1], vals[2])) == true) {
                                                if (this.login == false && this.hasLogin==false) {
                                                    this._loginlist.add(new Pair(this.socket, vals[1]));
                                                    this.username = vals[1];
                                                    this.login = true;
                                                    System.out.println("Users count: " + this._loginlist.size());
                                                    out.writeObject("SUCCESS login");
                                                    out.flush();
                                                } else {
                                                    out.writeObject("FAIL login");
                                                    out.flush();
                                                }
                                            } else {
                                                out.writeObject("FAIL login");
                                                out.flush();
                                            }
                                        }
                                        
                                        // param LOGOUT
                                        if (input.split(" ")[0].toLowerCase().equals("logout") == true) {
                                            String[] vals = input.split(" ");
                                            
                                            if (this._loginlist.contains(new Pair(this.socket, this.username)) == true) {
                                                this._loginlist.remove(new Pair(this.socket, this.username));
                                                System.out.println(this._loginlist.size());
                                                out.writeObject("SUCCESS logout");
                                                out.flush();
                                                this.socket.close();
                                                break;
                                            } else {
                                                out.writeObject("FAIL logout");
                                                out.flush();
                                            }
                                        }
                                        
                                        //public Key
                                        if (input.split(" ")[0].toLowerCase().equals("publickey") == true ) {
                                            out.writeObject("ok");
                                            Object inputObject = ois.readObject();
                                            PublicKey publicKey = (PublicKey) inputObject;
                                            System.out.println(publicKey);
                                            _publicKey.add(new Pair(this.username,publicKey));
                                            Object inputObject2 = ois.readObject();
                                            SealedObject cipherText = (SealedObject)inputObject2;
                                            System.out.println(cipherText);
                                            System.out.println(inputObject2);
                                            //Start RSA decrypt
                                            // Get an instance of the Cipher for RSA encryption/decryption
                                            Cipher dec = Cipher.getInstance("RSA");
                                            // Initiate the Cipher, telling it that it is going to Decrypt, giving it the private key
                                            dec.init(Cipher.DECRYPT_MODE, publicKey);

                                            // Tell the SealedObject we created before to decrypt the data and return it
                                            String message = (String) cipherText.getObject(dec);
                                            System.out.println("foo = "+message);
                                            //End RSA decrypt
                                            
                                        }
                                        
                                        // param PM <userName dst> <message>
                                        if (input.split(" ")[0].toLowerCase().equals("pm") == true) {
                                            String[] vals = input.split(" ");
                                            
                                            boolean exist = false;
                                            
                                            for(Pair<Socket, String> cur : _loginlist) {
                                                if (cur.getSecond().equals(vals[1])) {
                                                    ObjectOutputStream outDest = new ObjectOutputStream(cur.getFirst().getOutputStream());
                                                    String messageOut = "";
                                                    for (int j = 2; j<vals.length; j++) {
                                                        messageOut += vals[j] + " ";
                                                    }
                                                    System.out.println(this.username + " to " + vals[1] + " : " + messageOut);
                                                    outDest.writeObject(this.username + ": " + messageOut);
                                                    outDest.flush();
                                                    exist = true;
                                                }
                                            }
                                            
                                            if (exist == false) {
                                                System.out.println("pm to " + vals[1] + " by " + this.username + " failed.");
                                                out.writeObject("FAIL pm");
                                                out.flush();
                                            }
                                        }
                                        
                                        // param CG <groupName>
                                        if (input.split(" ")[0].toLowerCase().equals("cg") == true) {
                                            String[] vals = input.split(" ");
                                            
                                            boolean exist = false;
                                            
                                            for(Pair<String, String> selGroup : _grouplist) {
                                                if (selGroup.getFirst().equals(vals[1])) {
                                                    exist = true;
                                                }
                                            }
                                            
                                            if(exist == false) {
                                                Group group = new Group();
                                                int total = group.updateGroup(vals[1], this.username, _grouplist);
                                                System.out.println("total group: " + total);
                                                System.out.println("cg " + vals[1] + " by " + this.username + " successed.");
                                                out.writeObject("SUCCESS cg");
                                                out.flush();
                                            } else {
                                                System.out.println("cg " + vals[1] + " by " + this.username + " failed.");
                                                out.writeObject("FAIL cg");
                                                out.flush();
                                            }
                                        }
                                        
                                        // param GM <groupName> <message>
                                        if (input.split(" ")[0].toLowerCase().equals("gm") == true) {
                                            String[] vals = input.split(" ");
                                            
                                            boolean exist = false;
                                            
                                            for(Pair<String, String> selGroup : _grouplist) {
                                                if (selGroup.getSecond().equals(this.username)) {
                                                    exist = true;
                                                }
                                            }
                                            
                                            if (exist == true) {
                                                for(Pair<String, String> selGroup : _grouplist) {
                                                    if (selGroup.getFirst().equals(vals[1])) {
                                                        for(Pair<Socket, String> cur : _loginlist) {
                                                            if (cur.getSecond().equals(selGroup.getSecond()) && !cur.getFirst().equals(socket)) {
                                                                PrintWriter outDest = new PrintWriter(cur.getFirst().getOutputStream());
                                                                String messageOut = "";
                                                                for (int j = 2; j<vals.length; j++) {
                                                                    messageOut += vals[j] + " ";
                                                                }
                                                                System.out.println(this.username + " to " + vals[1] + " group: " + messageOut);
                                                                outDest.println(this.username + " @ " + vals[1] + " group: " + messageOut);
                                                                outDest.flush();
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                System.out.println("gm to " + vals[1] + " by " + this.username + " failed.");
                                                out.writeObject("FAIL gm");
                                                out.flush();
                                            }
                                        }
                                        
                                        // param BM <message>
                                        if (input.split(" ")[0].toLowerCase().equals("bm") == true) {
                                            String[] vals = input.split(" ");
                                            
                                            for(Pair<Socket, String> cur : _loginlist) {
                                                if (!cur.getFirst().equals(socket)) {
                                                    PrintWriter outDest = new PrintWriter(cur.getFirst().getOutputStream());
                                                    String messageOut = "";
                                                    for (int j = 1; j<vals.length; j++) {
                                                        messageOut += vals[j] + " ";
                                                    }
                                                    System.out.println(this.username + " to alls: " + messageOut);
                                                    outDest.println(this.username + " <BROADCAST>: " + messageOut);
                                                    outDest.flush();
                                                }
                                            }
                                        }
				}
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();//MOST LIKELY THERE WONT BE AN ERROR BUT ITS GOOD TO CATCH
		}	
	}

}


