/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kij_chat_client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

/**
 *
 * @author dhanarp
 */
public class PublicKeyList {
    private ArrayList<Pair<String,PublicKey>> _publicKey = new ArrayList<>();
    
    PublicKeyList() {
    }
    
    public void addPublicKey(String ID,PublicKey publicKey,ArrayList<Pair<String,PublicKey>> _publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException{

        _publicKey.add(new Pair(ID, publicKey));
        
        this._publicKey.add(new Pair(ID, publicKey));
    }
    public ArrayList<Pair<String,PublicKey>> getPublicKey(){
        return _publicKey;
    }
    public void removePublicKey(String ID){
        for(Pair<String,PublicKey> iter:_publicKey){
            if(iter.getFirst().equals(ID))
                _publicKey.remove(iter);
        }
    }
}
