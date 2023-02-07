package ch.ethz.sis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;


public class PersistentKeyValueStore {

	private static final String KEY_STORE_FILE;
	private static ConcurrentMap<String, Serializable> keyStore = new ConcurrentHashMap<>();
	
	static {
		Properties properties = DataStoreServer.getConfigParameters().getProperties();
		String storerootDir = properties.getProperty("storeroot-dir");
		KEY_STORE_FILE = storerootDir + "/" +  "PersistentKeyValueStore.bin";
		load();
	}

	//
	// Public API
	//
	public synchronized static void put(String key, Serializable value) {
		keyStore.put(key, value);
		save();
		print();
	}

	public synchronized static Serializable get(String key) {
		print();
		return keyStore.get(key);
	}

	public synchronized static void remove(String key) {
		keyStore.remove(key);
		save();
	}

	public synchronized static boolean containsKey(String key) {
		print();
		return keyStore.containsKey(key);
	}

	private synchronized static void print() {
		for (String key : keyStore.keySet()) {
			System.out.println(key + " - " + keyStore.get(key));
		}
	}
	
	//
	// save / load
	//
	private static void save() {
		try (FileOutputStream fos = new FileOutputStream(KEY_STORE_FILE))
		{
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(keyStore);
			oos.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void load() {
		if (new File(KEY_STORE_FILE).exists()) {
			try (FileInputStream fis = new FileInputStream(KEY_STORE_FILE)) {
		        ObjectInputStream ois = new ObjectInputStream(fis);
		        keyStore = (ConcurrentMap<String, Serializable>) ois.readObject();
		        ois.close();
			} catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

}
