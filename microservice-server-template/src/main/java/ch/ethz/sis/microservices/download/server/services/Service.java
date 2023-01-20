package ch.ethz.sis.microservices.download.server.services;

import javax.servlet.http.HttpServlet;

import ch.ethz.sis.microservices.download.api.configuration.ServiceConfig;

public abstract class Service extends HttpServlet
{
    private ServiceConfig serviceConfig;

    public ServiceConfig getServiceConfig()
    {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig)
    {
        this.serviceConfig = serviceConfig;
    }

}
