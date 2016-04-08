package ek1535_lab3;

/**
 * Created by Aedo on 4/6/16.
 */
public class Resource {
    int type;
    int unit;

    public Resource(int unit) {
        this.unit = unit;
    }

    public Resource(int type, int unit) {
        this.type = type;
        this.unit = unit;
    }
}
