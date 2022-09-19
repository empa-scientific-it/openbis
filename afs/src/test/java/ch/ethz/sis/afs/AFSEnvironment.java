package ch.ethz.sis.afs;

import ch.ethz.sis.shared.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.shared.startup.Configuration;
import ch.ethz.sis.shared.log.log4j2.Log4J2LogFactory;
import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;

import java.util.HashMap;
import java.util.Map;

public class AFSEnvironment {
    public static Configuration getDefaultAFSConfig() {
        Map<Enum, String> configuration = new HashMap<>();
        configuration.put(AtomicFileSystemParameter.logFactoryClass,  Log4J2LogFactory.class.getName());
//        configuration.put(AtomicFileSystemParameter.logConfigFile,  "objectfs-afs-config-log4j2.xml");
        configuration.put(AtomicFileSystemParameter.jsonObjectMapperClass, JacksonObjectMapper.class.getName());
        configuration.put(AtomicFileSystemParameter.writeAheadLogRoot, "./target/tests/transactions");
        configuration.put(AtomicFileSystemParameter.storageRoot, "./target/tests/storage");
        return new Configuration(configuration);
    }
}
