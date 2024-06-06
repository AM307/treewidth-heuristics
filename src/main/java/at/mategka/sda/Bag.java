package at.mategka.sda;

import java.util.Collection;
import java.util.HashSet;

public class Bag<V> extends HashSet<V> {

    private String name;

    public Bag(String name) {
        this.name = name;
    }

    public Bag(String name, Collection<? extends V> c) {
        super(c);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
