/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afsserver;

import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.shared.log.log4j2.Log4J2LogFactory;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.HashMap;
import java.util.Map;

public class AFSServerEnvironment {
    public static Configuration getDefaultAFSConfig() {
        Map<Enum, String> configuration = new HashMap<>();
        configuration.put(AtomicFileSystemParameter.logFactoryClass,  Log4J2LogFactory.class.getName());
//        configuration.put(AtomicFileSystemServerParameter.logConfigFile,  "objectfs-afs-config-log4j2.xml");
        configuration.put(AtomicFileSystemParameter.jsonObjectMapperClass, JacksonObjectMapper.class.getName());
        configuration.put(AtomicFileSystemParameter.writeAheadLogRoot, "./target/tests/transactions");
        configuration.put(AtomicFileSystemParameter.storageRoot, "./target/tests/storage");
        configuration.put(AtomicFileSystemServerParameter.port, "1010");
        configuration.put(AtomicFileSystemServerParameter.maxContentLength, "1024");
        configuration.put(AtomicFileSystemServerParameter.uri, "/fileserver");
        return new Configuration(configuration);
    }
}
