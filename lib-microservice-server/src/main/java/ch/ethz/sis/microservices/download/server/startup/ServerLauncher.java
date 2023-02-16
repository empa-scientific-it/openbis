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
package ch.ethz.sis.microservices.download.server.startup;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ch.ethz.sis.microservices.download.api.configuration.Config;
import ch.ethz.sis.microservices.download.api.configuration.ServiceConfig;
import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.Logger;
import ch.ethz.sis.microservices.download.server.services.Service;

public class ServerLauncher
{
    private final Logger logger = LogManager.getLogger(ServerLauncher.class);

    private final Server server;

    public ServerLauncher(Config config) throws Exception
    {
        server = new Server(config.getPort());
        ServiceConfig[] services = config.getServices();
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        for (ServiceConfig serviceConfig : services)
        {
            logger.info("Loading Service: " + serviceConfig.getClassName() + " URL: " + serviceConfig.getUrl());
            Service service = (Service) Class.forName(serviceConfig.getClassName()).getConstructor().newInstance();
            service.setServiceConfig(serviceConfig);
            ServletHolder servletHolder = new ServletHolder(service);
            handler.addServletWithMapping(servletHolder, serviceConfig.getUrl());
        }

        logger.info("Server Starting");
        server.start();
        logger.info("Server Started");
    }

    public Server getServer()
    {
        return server;
    }

}
