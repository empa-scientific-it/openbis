/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogFactoryFactory;
import ch.ethz.sis.shared.log.LogManager;

import java.io.File;


public class AbstractTest {
    static {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void init() throws Exception {
        System.out.println("Current Working Directory: " + (new File("")).getCanonicalPath());
        // Initializing LogManager
        LogFactoryFactory logFactoryFactory = new LogFactoryFactory();
        LogFactory logFactory = logFactoryFactory.create(ServerClientEnvironmentFS.getInstance().getDefaultServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.logFactoryClass));
        LogManager.setLogFactory(logFactory);
    }
}
