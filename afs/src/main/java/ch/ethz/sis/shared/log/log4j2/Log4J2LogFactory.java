package ch.ethz.sis.shared.log.log4j2;

import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

public class Log4J2LogFactory implements LogFactory {
    @Override
    public <T> Logger getLogger(Class<T> clazz) {
        return new Log4JLogger(LogManager.getLogger(clazz));
    }

    @Override
    public void configure(String pathToConfigurationFile) {
        if (pathToConfigurationFile != null) {
            Configurator.initialize(null, pathToConfigurationFile);
        }
    }
}