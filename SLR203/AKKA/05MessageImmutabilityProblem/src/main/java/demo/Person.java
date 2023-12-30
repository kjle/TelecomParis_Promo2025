package demo;

import java.io.Serializable;

public class Person implements Serializable {
    private static final long serialVersionUID = -8962538611373217667L;
    private String name;
    public Person(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public void changeName(String newName){
        this.name=newName;
    }
}