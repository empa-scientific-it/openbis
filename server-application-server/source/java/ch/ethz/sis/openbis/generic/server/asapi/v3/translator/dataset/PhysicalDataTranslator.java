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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.PhysicalDataFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class PhysicalDataTranslator extends AbstractCachingTranslator<Long, PhysicalData, PhysicalDataFetchOptions> implements
        IPhysicalDataTranslator
{

    @Autowired
    private IPhysicalDataBaseTranslator baseTranslator;

    @Autowired
    private IPhysicalDataFileFormatTypeTranslator fileFormatTypeTranslator;

    @Autowired
    private IPhysicalDataLocatorTypeTranslator locatorTypeTranslator;

    @Autowired
    private IPhysicalDataStorageFormatTranslator storageFormatTranslator;

    @Override
    protected PhysicalData createObject(TranslationContext context, Long dataSetId, PhysicalDataFetchOptions fetchOptions)
    {
        PhysicalData physicalData = new PhysicalData();
        physicalData.setFetchOptions(new PhysicalDataFetchOptions());
        return physicalData;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> dataSetIds, PhysicalDataFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IPhysicalDataBaseTranslator.class, baseTranslator.translate(context, dataSetIds, null));

        if (fetchOptions.hasFileFormatType())
        {
            relations.put(IPhysicalDataFileFormatTypeTranslator.class,
                    fileFormatTypeTranslator.translate(context, dataSetIds, fetchOptions.withFileFormatType()));
        }

        if (fetchOptions.hasLocatorType())
        {
            relations.put(IPhysicalDataLocatorTypeTranslator.class,
                    locatorTypeTranslator.translate(context, dataSetIds, fetchOptions.withLocatorType()));
        }

        if (fetchOptions.hasStorageFormat())
        {
            relations.put(IPhysicalDataStorageFormatTranslator.class,
                    storageFormatTranslator.translate(context, dataSetIds, fetchOptions.withStorageFormat()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataSetId, PhysicalData result, Object objectRelations,
            PhysicalDataFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        PhysicalDataBaseRecord baseRecord = relations.get(IPhysicalDataBaseTranslator.class, dataSetId);

        result.setShareId(baseRecord.shareId);
        result.setLocation(baseRecord.location);
        result.setSize(baseRecord.size);
        result.setComplete(translateComplete(baseRecord.isComplete));
        result.setStatus(translateStatus(baseRecord.status));
        result.setPresentInArchive(baseRecord.isPresentInArchive);
        result.setStorageConfirmation(baseRecord.isStorageConfirmed);
        result.setSpeedHint(baseRecord.speedHint);
        result.setArchivingRequested(baseRecord.isArchivingRequested);

        if (fetchOptions.hasFileFormatType())
        {
            result.setFileFormatType(relations.get(IPhysicalDataFileFormatTypeTranslator.class, dataSetId));
            result.getFetchOptions().withFileFormatTypeUsing(fetchOptions.withFileFormatType());
        }

        if (fetchOptions.hasLocatorType())
        {
            result.setLocatorType(relations.get(IPhysicalDataLocatorTypeTranslator.class, dataSetId));
            result.getFetchOptions().withLocatorTypeUsing(fetchOptions.withLocatorType());
        }

        if (fetchOptions.hasStorageFormat())
        {
            result.setStorageFormat(relations.get(IPhysicalDataStorageFormatTranslator.class, dataSetId));
            result.getFetchOptions().withStorageFormatUsing(fetchOptions.withStorageFormat());
        }

    }

    private Complete translateComplete(String value)
    {
        if (value != null)
        {
            switch (value)
            {
                case "T":
                    return Complete.YES;
                case "F":
                    return Complete.NO;
                case "U":
                    return Complete.UNKNOWN;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }

        return null;
    }

    private ArchivingStatus translateStatus(String value)
    {
        if (value != null)
        {
            switch (value)
            {
                case "ARCHIVE_PENDING":
                    return ArchivingStatus.ARCHIVE_PENDING;
                case "ARCHIVED":
                    return ArchivingStatus.ARCHIVED;
                case "AVAILABLE":
                    return ArchivingStatus.AVAILABLE;
                case "BACKUP_PENDING":
                    return ArchivingStatus.BACKUP_PENDING;
                case "LOCKED":
                    return ArchivingStatus.LOCKED;
                case "UNARCHIVE_PENDING":
                    return ArchivingStatus.UNARCHIVE_PENDING;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }

        return null;
    }

}
