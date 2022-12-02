package ch.ethz.sis.afsserver.worker;

public class Pair<KEY,VALUE> {
    private final KEY key;
    private final VALUE value;

    public Pair(KEY key, VALUE value) {
        this.key = key;
        this.value = value;
    }

    public KEY getKey() {
        return key;
    }

    public VALUE getValue() {
        return value;
    }
}
