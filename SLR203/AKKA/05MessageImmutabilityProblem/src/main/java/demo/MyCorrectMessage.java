package demo;

import java.io.Serializable;
import java.util.ArrayList;

public class MyCorrectMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final ArrayList<Integer> list;
    public final int i;
    public final Person p;
    public final String s;

    public MyCorrectMessage(ArrayList<Integer> list, int i, Person p, String s) {
        this.list = org.apache.commons.lang3.SerializationUtils.clone(list);
        this.i = i;
        this.p = org.apache.commons.lang3.SerializationUtils.clone(p);
        this.s = s;
    }

  }