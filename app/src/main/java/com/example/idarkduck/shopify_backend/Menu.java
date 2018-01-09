package com.example.idarkduck.shopify_backend;

import java.util.ArrayList;

/**
 * Created by iDarkDuck on 1/8/18.
 */

public class Menu {


    String id;
    String data;
    ArrayList<String> childIDList;

    Menu(String id, String data){
        this.id=id; this.data=data;
        childIDList =  new ArrayList<String>();
    }
    Menu(String id, String data, ArrayList<String> childID){
        this.id=id; this.data=data; this.childIDList=childID;
    }
    public void setId(String id){
        this.id=id;
    }
    public void setData(String data){
        this.data=data;
    }
    public void setChildID(ArrayList<String> childIDList){
        this.childIDList=childIDList;
    }
    public void addChildID(String childID){
        childIDList.add(childID);
    }
    public String getId(){
        return id;
    }
    public String getData(){
        return data;
    }
    public ArrayList<String> getChildIDList(){
        return childIDList;
    }
}
