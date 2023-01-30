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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class ExternalDmsTranslator extends AbstractCachingTranslator<Long, ExternalDms, ExternalDmsFetchOptions> implements
        IExternalDmsTranslator
{

    @Autowired
    private IExternalDmsBaseTranslator baseTranslator;

    @Override
    protected ExternalDms createObject(TranslationContext context, Long externalDmsId, ExternalDmsFetchOptions fetchOptions)
    {
        ExternalDms externalDms = new ExternalDms();
        externalDms.setFetchOptions(new ExternalDmsFetchOptions());
        return externalDms;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> externalDmsIds,
            ExternalDmsFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IExternalDmsBaseTranslator.class, baseTranslator.translate(context, externalDmsIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long externalDmsId, ExternalDms result, Object objectRelations,
            ExternalDmsFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ExternalDmsBaseRecord baseRecord = relations.get(IExternalDmsBaseTranslator.class, externalDmsId);

        result.setPermId(new ExternalDmsPermId(baseRecord.code));
        result.setCode(baseRecord.code);
        result.setLabel(baseRecord.label);
        result.setUrlTemplate(baseRecord.address);
        result.setAddress(baseRecord.address);
        result.setAddressType(ExternalDmsAddressType.valueOf(baseRecord.addressType));
        result.setOpenbis(ExternalDmsAddressType.OPENBIS.equals(result.getAddressType()));
    }

}
