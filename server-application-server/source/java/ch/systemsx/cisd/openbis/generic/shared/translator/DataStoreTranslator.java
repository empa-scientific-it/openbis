/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author Franz-Josef Elmer
 */
public class DataStoreTranslator
{
    public static DataStore translate(DataStorePE dataStorePE)
    {
        DataStore dataStore = new DataStore();
        String downloadUrl = dataStorePE.getDownloadUrl();
        dataStore.setHostUrl(downloadUrl);
        downloadUrl = translateDownloadUrl(downloadUrl);
        dataStore.setDownloadUrl(downloadUrl);
        dataStore.setCode(dataStorePE.getCode());
        return dataStore;
    }

    public static List<DataStore> translate(List<DataStorePE> dataStorePEs)
    {
        List<DataStore> result = new ArrayList<DataStore>();
        for (DataStorePE dataStorePE : dataStorePEs)
        {
            result.add(translate(dataStorePE));
        }
        return result;
    }

    public static String translateDownloadUrl(String downloadUrl)
    {
        return downloadUrl + "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME;
    }
}
