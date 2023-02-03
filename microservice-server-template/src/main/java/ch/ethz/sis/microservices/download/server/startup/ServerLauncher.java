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
