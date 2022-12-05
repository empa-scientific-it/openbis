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

package ch.ethz.sis.afsserver.startup;

import ch.ethz.sis.afsserver.server.Server;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.shared.startup.Configuration;

import java.io.File;
import java.util.List;

public class Main {

    public static List getParameterClasses() {
        return List.of(AtomicFileSystemServerParameter.class);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Current Working Directory: " + (new File("")).getCanonicalPath());
        Configuration configuration = new Configuration(getParameterClasses(), "../afs-server/src/main/resources/afs-server-config.properties");
        DummyServerObserver dummyServerObserver = new DummyServerObserver();
        Server server = new Server(configuration, dummyServerObserver, dummyServerObserver);
        Thread.currentThread().join();
    }
}
