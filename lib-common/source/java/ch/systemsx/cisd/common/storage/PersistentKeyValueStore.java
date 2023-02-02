package ch.systemsx.cisd.common.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Generic API for storing values in a file.
 */
public class PersistentKeyValueStore
{

    private String keyStorePath;

    private ConcurrentMap<String, Serializable> keyStore = new ConcurrentHashMap<>();

    /**
     * @param keyStorePath - Path to binary file storing values.
     */
    public PersistentKeyValueStore(String keyStorePath) throws IOException, ClassNotFoundException
    {
        this.keyStorePath = keyStorePath;
        load();
    }

    public synchronized void put(String key, Serializable value) throws IOException
    {
        keyStore.put(key, value);
        save();
    }

    public synchronized Serializable get(String key)
    {
        return keyStore.get(key);
    }

    public synchronized void remove(String key) throws IOException
    {
        keyStore.remove(key);
        save();
    }

    public synchronized boolean containsKey(String key)
    {
        return keyStore.containsKey(key);
    }

    private void save() throws IOException
    {
        File file = new File(keyStorePath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(keyStorePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(keyStore);
        oos.close();
    }

    @SuppressWarnings("unchecked")
    private void load() throws IOException, ClassNotFoundException
    {
        if (new File(keyStorePath).exists())
        {
            FileInputStream fis = new FileInputStream(keyStorePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            keyStore = (ConcurrentMap<String, Serializable>) ois.readObject();
            ois.close();
        }
    }

}
