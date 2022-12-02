package ch.ethz.sis.shared.pool;

public abstract class AbstractFactory<CONFIGURATION_PARAMETERS, FACTORY_PARAMETERS, ELEMENT>
        implements Factory<CONFIGURATION_PARAMETERS, FACTORY_PARAMETERS, ELEMENT> {
    @Override
    public void init(CONFIGURATION_PARAMETERS configurationParameters) throws Exception {

    }

    @Override
    public void destroy(ELEMENT element) throws Exception {

    }
}
