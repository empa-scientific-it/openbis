package ch.ethz.sis.shared.log;

public interface LogFactory {
    <T> Logger getLogger(Class<T> clazz);
    void configure(String pathToConfigurationFile);
}