package ch.systemsx.cisd.etlserver.registrator;

import net.lemnik.eodsql.DynamicTransactionQuery;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;

/**
 * An object that provides the context for a data set registration.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationContext
{
    public static interface IHolder
    {
        DataSetRegistrationContext getRegistrationContext();
    }

    private final DataSetRegistrationPersistentMap persistentMap;

    private final TopLevelDataSetRegistratorGlobalState globalState;

    private final String userSessionToken;

    public DataSetRegistrationContext(DataSetRegistrationPersistentMap persistentMap,
            TopLevelDataSetRegistratorGlobalState globalState, String userSessionToken)
    {
        this.persistentMap = persistentMap;
        this.globalState = globalState;
        this.userSessionToken = userSessionToken;
    }

    public DataSetRegistrationPersistentMap getPersistentMap()
    {
        return persistentMap;
    }

    public TopLevelDataSetRegistratorGlobalState getGlobalState()
    {
        return globalState;
    }

    public String getUserSessionToken()
    {
        return userSessionToken;
    }

    DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
    {
        return globalState.getDynamicTransactionQueryFactory().createDynamicTransactionQuery(
                dataSourceName);
    }

}
