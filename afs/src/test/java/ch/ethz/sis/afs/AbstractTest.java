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

package ch.ethz.sis.afs;

import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.log4j2.Log4J2LogFactory;


public class AbstractTest {

    static {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void init() throws Exception {
        // Initializing LogManager
        LogFactory logFactory = new Log4J2LogFactory();
        LogManager.setLogFactory(logFactory);
    }

}

