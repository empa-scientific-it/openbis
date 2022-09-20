package ch.ethz.sis.shared.log;

public class LogFactoryFactory {
    public LogFactory create(String logFactoryClass) throws Exception {
        return (LogFactory) Class.forName(logFactoryClass).newInstance();
    }
}
