package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache;

public interface ICache<V>
{

    void put(String key, V value);

    V get(String key);

    void remove(String key);

    boolean contains(String key);

    void clear();

    void clearOld(long time);

}
