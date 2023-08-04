/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel.NewDataSetInfo.Status;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.UploadObserver;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * DataSetUploadOperation represents a request to upload a data set to openBIS/dss. The upload operation runs in its own thread, which is managed by
 * this class, and notifies the GUI and client model of changes in status.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
final class DataSetUploadOperation implements Runnable
{
    private final DataSetUploadTableModel tableModel;

    private final DataSetUploadClientModel clientModel;

    private final NewDataSetInfo newDataSetInfo;

    private final IUserNotifier userNotifier;

    DataSetUploadOperation(DataSetUploadTableModel model, DataSetUploadClientModel clientModel,
            NewDataSetInfo newDataSetInfo, IUserNotifier userNotifier)
    {
        this.tableModel = model;
        this.clientModel = clientModel;
        this.newDataSetInfo = newDataSetInfo;
        this.userNotifier = userNotifier;
    }

    @Override
    public void run()
    {
        try
        {
            if (newDataSetInfo.getStatus() == NewDataSetInfo.Status.QUEUED_FOR_UPLOAD)
            {
                newDataSetInfo.setStatus(Status.UPLOADING);
                tableModel.fireChanged(newDataSetInfo, Status.UPLOADING);
                createAdHocVocabularyTermsIfNeeded(newDataSetInfo);
                NewDataSetDTO cleanDto =
                        clientModel.cleanNewDataSetDTO(newDataSetInfo.getNewDataSetBuilder()
                                .asNewDataSetDTO());
                cleanDto.addUploadObserver(new UploadObserver()
                    {
                        @Override
                        public void updateTotalBytesRead(long totalBytesRead)
                        {
                            double totalFileSize = newDataSetInfo.getTotalFileSize();
                            int percent = (int) ((totalBytesRead / totalFileSize) * 100);
                            newDataSetInfo.updateProgress(percent, totalBytesRead);
                            tableModel.fireChanged(newDataSetInfo, Status.UPLOADING);
                        }
                    });
                clientModel.getOpenBISService().putDataSet(cleanDto,
                        newDataSetInfo.getNewDataSetBuilder().getFile());
            }
            newDataSetInfo.setStatus(Status.COMPLETED_UPLOAD);
            tableModel.fireChanged(newDataSetInfo, Status.COMPLETED_UPLOAD);
        } catch (Throwable th)
        {
            newDataSetInfo.setStatus(Status.FAILED);
            tableModel.fireChanged(newDataSetInfo, Status.FAILED);
            userNotifier.notifyUserOfThrowable(newDataSetInfo.getNewDataSetBuilder().getFile()
                    .getAbsolutePath(), "Uploading", CheckedExceptionTunnel.unwrapIfNecessary(th),
                    null);
        }
    }

    /**
     * When metadata extraction has produced non-existing vocabulary terms, we create them on the fly (as ad-hoc terms) to make the data set
     * registration possible.
     */
    private void createAdHocVocabularyTermsIfNeeded(NewDataSetInfo dataSetInfo)
    {
        NewDataSetMetadataDTO metadata = dataSetInfo.getNewDataSetBuilder().getDataSetMetadata();
        String dataSetTypeCode = metadata.tryDataSetType();
        if (dataSetTypeCode == null)
        {
            return;
        }

        DataSetType dataSetType = clientModel.getDataSetType(dataSetTypeCode);
        Map<String, String> vocabularyProperties =
                getVocabularyPropertyNamesToVocabularyMap(dataSetType);
        if (vocabularyProperties.isEmpty())
        {
            return;
        }

        Map<String, Serializable> dataSetProperties = metadata.getProperties();
        Collection<String> unmodifiableProperties =
                new ArrayList<String>(metadata.getUnmodifiableProperties());

        for (String property : unmodifiableProperties)
        {
            if (vocabularyProperties.keySet().contains(property))
            {
                Serializable term = dataSetProperties.get(property);
                if (null == term)
                {
                    continue;
                }
                Vocabulary vocabulary =
                        clientModel.getVocabulary(vocabularyProperties.get(property));
                if (null == vocabulary)
                {
                    continue;
                }
                if (false == hasTerm(vocabulary, term))
                {
                    String stringTerm = (String) term;
                    clientModel.addUnofficialVocabularyTerm(vocabulary, stringTerm, stringTerm.trim(),
                            stringTerm, getMaxOrdinal(vocabulary));
                }
            }
        }

    }

    private boolean hasTerm(Vocabulary vocabulary, Serializable termCode)
    {
        for (VocabularyTerm term : vocabulary.getTerms())
        {
            if (term.getCode().equalsIgnoreCase((String)termCode))
            {
                return true;
            }
        }
        return false;
    }

    private long getMaxOrdinal(Vocabulary vocabulary)
    {
        long maxOrdinal = -1L;
        for (VocabularyTerm term : vocabulary.getTerms())
        {
            if (term.getOrdinal() > maxOrdinal)
            {
                maxOrdinal = term.getOrdinal();
            }
        }
        return maxOrdinal;
    }

    private Map<String, String> getVocabularyPropertyNamesToVocabularyMap(DataSetType dataSetType)
    {
        HashMap<String, String> vocabularyProperties = new HashMap<String, String>();
        for (PropertyTypeGroup typeGroup : dataSetType.getPropertyTypeGroups())
        {
            for (PropertyType propertyType : typeGroup.getPropertyTypes())
            {
                if (propertyType.getDataType() == DataTypeCode.CONTROLLEDVOCABULARY)
                {
                    ControlledVocabularyPropertyType vocabPropertyType =
                            (ControlledVocabularyPropertyType) propertyType;
                    vocabularyProperties.put(propertyType.getCode(), vocabPropertyType
                            .getVocabulary().getCode());
                }
            }
        }
        return vocabularyProperties;
    }
}
