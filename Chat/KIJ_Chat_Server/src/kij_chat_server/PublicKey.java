package kij_chat_server;


import java.util.ArrayList;
import kij_chat_server.Pair;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dhanarp
 */
public class PublicKey {
    private ArrayList<Pair<String,String>> _publicKey = new ArrayList<>();
    
    PublicKey() {
    }
    
    public void addPublicKey(String ID,String publicKey){
        _publicKey.add(new Pair(ID, publicKey));
    }
    public ArrayList<Pair<String,String>> getPublicKey(){
        return _publicKey;
    }
}
