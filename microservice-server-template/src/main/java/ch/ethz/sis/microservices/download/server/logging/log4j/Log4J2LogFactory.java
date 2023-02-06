package ch.ethz.sis.microservices.download.server.logging.log4j;

import org.apache.logging.log4j.LogManager;

import ch.ethz.sis.microservices.download.server.logging.LogFactory;
import ch.ethz.sis.microservices.download.server.logging.Logger;

public class Log4J2LogFactory implements LogFactory
{
    @Override
    public <T> Logger getLogger(Class<T> clazz)
    {
        return new Log4JLogger(LogManager.getLogger(clazz));
    }
}