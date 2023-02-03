package ch.ethz.sis.shared.collection;

import java.util.Collections;

public class List {
    public static <E> java.util.List<E> safe(java.util.List<E> listOrNull) {
        if(listOrNull == null) {
            return Collections.EMPTY_LIST;
        } else {
            return listOrNull;
        }
    }
}
