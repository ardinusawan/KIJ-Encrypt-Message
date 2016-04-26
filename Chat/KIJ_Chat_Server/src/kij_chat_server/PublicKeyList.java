package kij_chat_server;


import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import kij_chat_server.Pair;
import static sun.security.krb5.Confounder.bytes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
}
