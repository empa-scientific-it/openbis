/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.microservices.download.server.logging;

public class LogManager {
    private static LogFactory factory;

    private static boolean isNotInitialized() {
        return factory == null;
    }

    public static void setLogFactory(LogFactory logFactory) {
        if (isNotInitialized()) {
            factory = logFactory;
        }
    }

    public static <T> Logger getLogger(Class<T> clazz) {
        if (isNotInitialized()) {
            throw new RuntimeException("LogFactory not initialized.");
        }
        return factory.getLogger(clazz);
    }
}
