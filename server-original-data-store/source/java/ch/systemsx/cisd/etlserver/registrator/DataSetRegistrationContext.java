/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
