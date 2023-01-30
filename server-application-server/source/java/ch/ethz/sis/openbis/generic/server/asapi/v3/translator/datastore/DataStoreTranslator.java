/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.datastore;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class DataStoreTranslator extends AbstractCachingTranslator<Long, DataStore, DataStoreFetchOptions> implements
        IDataStoreTranslator
{

    @Autowired
    private IDataStoreBaseTranslator baseTranslator;

    @Override
    protected DataStore createObject(TranslationContext context, Long dataStoreId, DataStoreFetchOptions fetchOptions)
    {
        DataStore dataStore = new DataStore();
        dataStore.setFetchOptions(new DataStoreFetchOptions());
        return dataStore;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> dataStoreIds, DataStoreFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IDataStoreBaseTranslator.class, baseTranslator.translate(context, dataStoreIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataStoreId, DataStore result, Object objectRelations,
            DataStoreFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        DataStoreBaseRecord baseRecord = relations.get(IDataStoreBaseTranslator.class, dataStoreId);

        result.setCode(baseRecord.code);
        result.setDownloadUrl(baseRecord.downloadUrl);
        result.setRemoteUrl(baseRecord.remoteUrl);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);
    }

}
