package kij_chat_server;

import java.io.IOException;
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
        private ArrayList<Client> clientList;
        ObjectOutputStream out;
        ObjectInputStream ois;
        
	public Client(Socket s, ArrayList<Pair<Socket, String>> _loginlist, ArrayList<Pair<String, String>> _userlist, 
                ArrayList<Pair<String, String>> _grouplist, ArrayList<Pair<String, PublicKey>> _publicKey, ArrayList<Client> clientList)
	{
		socket = s;//INSTANTIATE THE SOCKET)
                this._loginlist = _loginlist;
                this._userlist = _userlist;
                this._grouplist = _grouplist;
                this._publicKey=_publicKey;
                this.clientList=clientList;
	}

    
	
	@Override
	public void run() //(IMPLEMENTED FROM THE RUNNABLE INTERFACE)
	{
		try //HAVE TO HAVE THIS FOR THE in AND out VARIABLES
		{
			//Scanner in = new Scanner(socket.getInputStream());//GET THE SOCKETS INPUT STREAM (THE STREAM THAT YOU WILL GET WHAT THEY TYPE FROM)
			//PrintWriter out = new PrintWriter(socket.getOutputStream());//GET THE SOCKETS OUTPUT STREAM (THE STREAM YOU WILL SEND INFORMATION TO THEM FROM)
			out = new ObjectOutputStream(socket.getOutputStream());
                        ois = new ObjectInputStream(socket.getInputStream());
                        
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
                                                login=false;
                                                clientList.remove(this);
                                            } else {
                                                out.writeObject("FAIL logout");
                                                out.flush();
                                            }
                                            
                                            if(login==false){
                                                for(Pair<String,PublicKey> iter:_publicKey){
                                                    if(iter.getFirst().equals(this.username)){
                                                        removePublicKey(this.username, iter.getSecond());
                                                        _publicKey.remove(iter);
                                                        break;
                                                    }
                                                }
                                                for(Pair<String,PublicKey> iter:_publicKey){
                                                    System.out.println(iter.getFirst());
                                                    System.out.println(iter.getSecond());
                                                    System.out.println("another key?");
                                                }
                                                this.socket.close();
                                                break;
                                            }
                                        }
                                        
                                        //public Key
                                        if (input.split(" ")[0].toLowerCase().equals("publickey") == true ) {
                                            //out.writeObject("ok");
                                            Object inputObject = ois.readObject();
                                            PublicKey publicKey = (PublicKey) inputObject;
                                            System.out.println(publicKey);
                                            justLoggedIn();
                                            _publicKey.add(new Pair(this.username,publicKey));
                                            broadcastPublicKey(this.username, publicKey);
                                            for(Pair<String,PublicKey> iter:_publicKey){
                                                System.out.println(iter.getFirst());
                                                System.out.println(iter.getSecond());
                                                System.out.println("another key?");
                                            }
                                        }
                                        
                                        // param PM <userName dst> <message>
                                        if (input.split(" ")[0].toLowerCase().equals("pm") == true) {
                                            Object inputObject;
                                            synchronized(ois){ //biar langsung dobel
                                                inputObject = ois.readObject();  
                                            }
                                            String[] vals = input.split(" ");
                                            
                                            Client destination = null;
                                            boolean exist = false;
                                            
                                            for(Pair<Socket, String> cur : _loginlist) {
                                                if (cur.getSecond().equals(vals[1])) {
                                                    //ObjectOutputStream outDest = new ObjectOutputStream(cur.getFirst().getOutputStream());   ///////IKI COK SING BERMASALAH
                                                    for(Client iter:clientList)
                                                    {
                                                        if(iter.username.equals(cur.getSecond())){
                                                            destination=iter;
                                                            exist=true;
                                                            break;      
                                                        }
                                                    }
                                                    String messageOut = "";
                                                    for (int j = 2; j<vals.length; j++) {
                                                        messageOut += vals[j] + " ";
                                                    }
                                                    System.out.println(this.username + " messaged " + vals[1]);
                                                    if(exist){
                                                        destination.sendToClient(this.username + " : ");
                                                        destination.sendToClient(inputObject);
                                                    }
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
                                                                ObjectOutputStream outDest = new ObjectOutputStream(cur.getFirst().getOutputStream());
                                                                String messageOut = "";
                                                                for (int j = 2; j<vals.length; j++) {
                                                                    messageOut += vals[j] + " ";
                                                                }
                                                                System.out.println(this.username + " to " + vals[1] + " group: " + messageOut);
                                                                outDest.writeObject(this.username + " @ " + vals[1] + " group: " + messageOut);
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
                                            String messageOut = "";
                                            for (int j = 1; j<vals.length; j++) {
                                                messageOut += vals[j] + " ";
                                            }
                                            for(Client iter:clientList){
                                                if(iter.username.equals(this.username)){}
                                                else{
                                                    iter.sendToClient(this.username + " <BROADCAST> : " + messageOut);
                                                    System.out.println("this");
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
        
        public void broadcastPublicKey(String ID,PublicKey key) throws IOException
        {
            for(Client iter:clientList){
                if(iter!=null){
                    if(iter.username.equals(this.username)){}
                    else{
                        iter.sendToClient("publicKey Add "+ ID);
                        iter.sendToClient(key);
                    }
                }
            }
        }
        public void removePublicKey(String ID,PublicKey key) throws IOException
        {
            for(Client iter:clientList){
                if(iter!=null){
                    if(iter.username.equals(this.username)){}
                    else{
                        iter.sendToClient("publicKey remove "+ ID);
                        iter.sendToClient(key);
                    }
                }
            }
        }
        public void sendToClient(Object object) throws IOException{
            out.writeObject(object);
            out.flush();
        }
        public void justLoggedIn() throws IOException{
            for(Pair<String,PublicKey> iter:_publicKey){
                this.sendToClient("publicKey Add "+ iter.getFirst());
                this.sendToClient(iter.getSecond());
            }
        }

}


