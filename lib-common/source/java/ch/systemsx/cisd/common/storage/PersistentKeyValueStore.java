/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
