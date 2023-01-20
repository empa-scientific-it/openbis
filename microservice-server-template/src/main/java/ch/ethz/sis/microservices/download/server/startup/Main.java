/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.microservices.download.server.startup;

import ch.ethz.sis.microservices.download.api.configuration.Config;
import ch.ethz.sis.microservices.download.server.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.Logger;
import ch.ethz.sis.microservices.download.server.logging.log4j.Log4J2LogFactory;

import java.io.File;
import java.io.FileInputStream;

public class Main
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(final String[] args) throws Exception
    {
        LOGGER.info("Current Workspace: " + (new File("").getAbsolutePath()));

        final File configFile;
        if (args.length < 1)
        {
            configFile = new File("config.json");
            LOGGER.info("No arguments given, starting with default config file: " + (configFile.getAbsolutePath()));
        } else
        {
            configFile = new File(args[0]);
        }

        final Config config = JacksonObjectMapper.getInstance().readValue(new FileInputStream(configFile),
                Config.class);
        final ServerLauncher servicesStarter = new ServerLauncher(config);
        servicesStarter.getServer().join();
    }

}
