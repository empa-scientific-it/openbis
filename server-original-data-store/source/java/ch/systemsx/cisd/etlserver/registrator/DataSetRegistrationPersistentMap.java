package ch.systemsx.cisd.etlserver.registrator;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container for a jython dropbox that can be used to store information during the registration process. This is the suggested way for the users of
 * dropboxes to share some data beetween the dropbox scripts and and the functions like post_registration, post_storage etc.
 * 
 * @author jakubs
 */
public class DataSetRegistrationPersistentMap implements Serializable
{

    private static final long serialVersionUID = 1L;

    private HashMap<String, Serializable> persistentMap;

    public DataSetRegistrationPersistentMap()
    {
        persistentMap = new HashMap<String, Serializable>();
    }

    public int size()
    {
        return persistentMap.size();
    }

    public boolean isEmpty()
    {
        return persistentMap.isEmpty();
    }

    public Serializable get(Object key)
    {
        return persistentMap.get(key);
    }

    public boolean containsKey(Object key)
    {
        return persistentMap.containsKey(key);
    }

    /**
     * @param value object to put into the map. Method accepts Object instead of Serializable, so that it can fail with informative message if the
     *            jython script calls the method with inappropriate type
     * @throws IllegalArgumentException if the value is not Serializable.
     */
    public Serializable put(String key, Object value)
    {
        if (false == (value instanceof Serializable))
            throw new IllegalArgumentException(
                    "Provided non-serializable argument to persistent map. Argument type is "
                            + value.getClass());
        return persistentMap.put(key, (Serializable) value);

    }

    /**
     * Add all entries from other persistent map.
     */
    public void putAll(DataSetRegistrationPersistentMap other)
    {
        for (Entry<String, Serializable> item : other.persistentMap.entrySet())
        {
            this.persistentMap.put(item.getKey(), item.getValue());
        }
    }

    public Serializable remove(Object key)
    {
        return persistentMap.remove(key);
    }

    public void clear()
    {
        persistentMap.clear();
    }

    public boolean containsValue(Object value)
    {
        return persistentMap.containsValue(value);
    }

    public Set<String> keySet()
    {
        return persistentMap.keySet();
    }

    public Collection<Serializable> values()
    {
        return persistentMap.values();
    }

}
