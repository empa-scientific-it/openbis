package ch.ethz.sis.shared.pool;

public interface Factory<CONFIGURATION_PARAMETERS, FACTORY_PARAMETERS, ELEMENT> {

    /*
     * To be used for factories that need an initial configuration or keep some state, ignore otherwise
     */
    void init(CONFIGURATION_PARAMETERS configurationParameters) throws Exception;

    /*
     * To be implemented by all factories
     */
    ELEMENT create(FACTORY_PARAMETERS factoryParameters) throws Exception;

    /*
     * To be implemented for elements that can be closed/destroyed to recover resources, ignore otherwise
     */
    void destroy(ELEMENT element) throws Exception;

}
