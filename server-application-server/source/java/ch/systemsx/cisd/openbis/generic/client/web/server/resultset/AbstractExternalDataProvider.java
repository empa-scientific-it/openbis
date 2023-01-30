/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.ARCHIVING_STATUS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.CONTAINER_DATASETS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_PRODUCER_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_SET_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_SET_KIND;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_STORE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXPERIMENT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXPERIMENT_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DATA_EXPERIMENT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DATA_SAMPLE_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DMS_ADDRESS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DMS_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DMS_LABEL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.IS_COMPLETE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.IS_DELETED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.LINK_HASH;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.LINK_REPOSITORY;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.LINK_PATH;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.LOCATION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.METAPROJECTS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.ORDER_IN_CONTAINERS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PARENT_DATASETS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PRESENT_IN_ARCHIVE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PRODUCTION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PROJECT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PROJECT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PROPERTIES_PREFIX;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SAMPLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SAMPLE_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SHOW_DETAILS_LINK;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SIZE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SOURCE_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.STORAGE_CONFIRMATION;

import java.util.List;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataProvider extends
        AbstractCommonTableModelProvider<AbstractExternalData>
{
    public AbstractExternalDataProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<AbstractExternalData> createTableModel()
    {
        List<AbstractExternalData> dataSets = getDataSets();
        TypedTableModelBuilder<AbstractExternalData> builder = new TypedTableModelBuilder<AbstractExternalData>();
        builder.addColumn(CODE).withDefaultWidth(150);
        builder.addColumn(EXTERNAL_CODE).withDefaultWidth(150).hideByDefault();
        builder.addColumn(DATA_SET_TYPE).withDefaultWidth(200);
        builder.addColumn(DATA_SET_KIND).withDefaultWidth(150).hideByDefault();
        builder.addColumn(CONTAINER_DATASETS).withDefaultWidth(150).hideByDefault();
        builder.addColumn(ORDER_IN_CONTAINERS).withDefaultWidth(100).hideByDefault();
        builder.addColumn(PARENT_DATASETS).withDefaultWidth(150).hideByDefault();
        builder.addColumn(SAMPLE).withDefaultWidth(100).hideByDefault();
        builder.addColumn(EXTERNAL_DATA_SAMPLE_IDENTIFIER).withDefaultWidth(200);
        builder.addColumn(SAMPLE_TYPE);
        builder.addColumn(EXPERIMENT).withDefaultWidth(100).hideByDefault();
        builder.addColumn(EXTERNAL_DATA_EXPERIMENT_IDENTIFIER).withDefaultWidth(100)
                .hideByDefault();
        builder.addColumn(EXPERIMENT_TYPE).withDefaultWidth(120).hideByDefault();
        builder.addColumn(PROJECT);
        builder.addColumn(PROJECT_IDENTIFIER);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(MODIFIER);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(200);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(200).hideByDefault();
        builder.addColumn(IS_DELETED).hideByDefault();
        builder.addColumn(SOURCE_TYPE).hideByDefault();
        builder.addColumn(IS_COMPLETE).hideByDefault();
        builder.addColumn(LOCATION).hideByDefault();
        builder.addColumn(SIZE).hideByDefault();
        builder.addColumn(ARCHIVING_STATUS).withDefaultWidth(200).hideByDefault();
        builder.addColumn(FILE_FORMAT_TYPE).hideByDefault();
        builder.addColumn(PRODUCTION_DATE).withDefaultWidth(200).hideByDefault();
        builder.addColumn(DATA_PRODUCER_CODE).hideByDefault();
        builder.addColumn(DATA_STORE_CODE).hideByDefault();
        builder.addColumn(EXTERNAL_DMS_CODE).withDefaultWidth(150).hideByDefault();
        builder.addColumn(EXTERNAL_DMS_LABEL).withDefaultWidth(150).hideByDefault();
        builder.addColumn(EXTERNAL_DMS_ADDRESS).withDefaultWidth(150).hideByDefault();
        builder.addColumn(LINK_PATH).withDefaultWidth(150).hideByDefault();
        builder.addColumn(LINK_HASH).withDefaultWidth(150).hideByDefault();
        builder.addColumn(LINK_REPOSITORY).withDefaultWidth(150).hideByDefault();

        builder.addColumn(PERM_ID).hideByDefault();
        builder.addColumn(SHOW_DETAILS_LINK).hideByDefault();
        builder.addColumn(METAPROJECTS);
        TableMap<String, DataSetType> dataSetTypes = getDataSetTypes();
        for (AbstractExternalData dataSet : dataSets)
        {
            builder.addRow(dataSet);

            if (dataSet.isStub())
            {
                builder.column(PERM_ID).addString(dataSet.getPermId());
            } else
            {
                builder.column(CODE).addEntityLink(dataSet, dataSet.getCode());

                addLinkDataSet(builder, dataSet.tryGetAsLinkDataSet());

                builder.column(METAPROJECTS).addString(
                        metaProjectsToString(dataSet.getMetaprojects()));

                builder.column(DATA_SET_TYPE).addString(dataSet.getDataSetType().getCode());
                builder.column(DATA_SET_KIND).addString(dataSet.getDataSetKind().name());
                List<ContainerDataSet> containerDataSets = dataSet.getContainerDataSets();
                builder.column(CONTAINER_DATASETS).addEntityLink(containerDataSets);
                CommaSeparatedListBuilder listBuilder = new CommaSeparatedListBuilder();
                for (ContainerDataSet containerDataSet : containerDataSets)
                {
                    listBuilder.append(dataSet.getOrderInContainer(containerDataSet.getCode()));
                }
                builder.column(ORDER_IN_CONTAINERS).addString(listBuilder.toString());
                builder.column(PARENT_DATASETS).addEntityLink(dataSet.getParents());

                Sample sample = dataSet.getSample();
                if (sample != null)
                {
                    builder.column(SAMPLE).addEntityLink(sample, sample.getCode());
                    builder.column(EXTERNAL_DATA_SAMPLE_IDENTIFIER).addEntityLink(sample,
                            sample.getIdentifier());
                    SampleType sampleType = dataSet.getSampleType();
                    builder.column(SAMPLE_TYPE).addString(sampleType.getCode());
                    addProject(builder, sample.getProject());
                }
                Experiment experiment = dataSet.getExperiment();
                if (experiment != null)
                {
                    builder.column(EXPERIMENT).addEntityLink(experiment, experiment.getCode());
                    builder.column(EXTERNAL_DATA_EXPERIMENT_IDENTIFIER).addEntityLink(experiment,
                            experiment.getIdentifier());
                    builder.column(EXPERIMENT_TYPE).addString(experiment.getEntityType().getCode());
                    if (sample == null)
                    {
                        addProject(builder, experiment.getProject());
                    }
                }
                builder.column(REGISTRATOR).addPerson(dataSet.getRegistrator());
                builder.column(MODIFIER).addPerson(dataSet.getModifier());
                builder.column(REGISTRATION_DATE).addDate(dataSet.getRegistrationDate());
                builder.column(MODIFICATION_DATE).addDate(dataSet.getModificationDate());
                builder.column(IS_DELETED).addString(
                        SimpleYesNoRenderer.render(DeletionUtils.isDeleted(dataSet)));
                builder.column(SOURCE_TYPE).addString(dataSet.getSourceType());
                if (dataSet instanceof PhysicalDataSet)
                {
                    PhysicalDataSet realDataSet = (PhysicalDataSet) dataSet;
                    Boolean complete = realDataSet.getComplete();
                    builder.column(IS_COMPLETE).addString(
                            complete == null ? "?" : SimpleYesNoRenderer.render(complete));
                    builder.column(LOCATION).addString(realDataSet.getFullLocation());
                    builder.column(SIZE).addInteger(realDataSet.getSize());
                    builder.column(ARCHIVING_STATUS).addString(
                            realDataSet.getStatus().getDescription());
                    builder.column(PRESENT_IN_ARCHIVE).addString(
                            SimpleYesNoRenderer.render(realDataSet.isPresentInArchive()));
                    FileFormatType fileFormatType = realDataSet.getFileFormatType();
                    builder.column(FILE_FORMAT_TYPE).addString(
                            fileFormatType == null ? "" : fileFormatType.getCode());
                    builder.column(STORAGE_CONFIRMATION).addString(
                            SimpleYesNoRenderer.render(dataSet.isStorageConfirmation()));
                }
                builder.column(PRODUCTION_DATE).addDate(dataSet.getProductionDate());
                builder.column(DATA_PRODUCER_CODE).addString(dataSet.getDataProducerCode());
                builder.column(DATA_STORE_CODE).addString(dataSet.getDataStore().getCode());
                builder.column(PERM_ID).addString(dataSet.getPermId());
                builder.column(SHOW_DETAILS_LINK).addString(dataSet.getPermlink());

                IColumnGroup columnGroup = builder.columnGroup(PROPERTIES_PREFIX);
                DataSetType dataSetType = dataSet.getDataSetType();
                if (dataSetType != null)
                {
                    DataSetType fullDataSetType = dataSetTypes.tryGet(dataSetType.getCode());
                    columnGroup.addColumnsForAssignedProperties(fullDataSetType);
                }
                columnGroup.addProperties(dataSet.getProperties());
            }

        }
        return builder.getModel();
    }

    private void addLinkDataSet(TypedTableModelBuilder<AbstractExternalData> builder, LinkDataSet linkDataSet)
    {
        if (linkDataSet != null && linkDataSet.getCopies() != null)
        {
            if (linkDataSet.getCopies().size() > 0)
            {
                CommaSeparatedListBuilder dmsCodes = new CommaSeparatedListBuilder();
                CommaSeparatedListBuilder dmsLabels = new CommaSeparatedListBuilder();
                CommaSeparatedListBuilder dmsAddresses = new CommaSeparatedListBuilder();
                CommaSeparatedListBuilder externalCodes = new CommaSeparatedListBuilder();
                CommaSeparatedListBuilder paths = new CommaSeparatedListBuilder();
                CommaSeparatedListBuilder hashes = new CommaSeparatedListBuilder();

                // repository ids of content copies are the same for one data set,
                // so we have only one string
                String repositoryId = "";

                for (IContentCopy copy : linkDataSet.getCopies())
                {
                    dmsCodes.append(copy.getExternalDMSCode());
                    dmsLabels.append(emptyOnNull(copy.getExternalDMSLabel()));
                    dmsAddresses.append(copy.getExternalDMSAddress());
                    externalCodes.append(emptyOnNull(copy.getExternalCode()));
                    paths.append(emptyOnNull(copy.getPath()));
                    hashes.append(emptyOnNull(copy.getCommitHash()));
                    repositoryId = emptyOnNull(copy.getRespitoryId());
                }

                builder.column(EXTERNAL_DMS_CODE).addString(dmsCodes.toString());
                builder.column(EXTERNAL_DMS_LABEL).addString(dmsLabels.toString());
                builder.column(EXTERNAL_DMS_ADDRESS).addString(dmsAddresses.toString());

                builder.column(EXTERNAL_CODE).addString(externalCodes.toString());
                builder.column(LINK_PATH).addString(paths.toString());
                builder.column(LINK_HASH).addString(hashes.toString());
                builder.column(LINK_REPOSITORY).addString(repositoryId);
            }
        }
    }

    private String emptyOnNull(String value)
    {
        return value == null ? "" : value;
    }

    private void addProject(TypedTableModelBuilder<AbstractExternalData> builder, Project project)
    {
        if (project != null)
        {
            builder.column(PROJECT).addString(project.getCode());
            builder.column(PROJECT_IDENTIFIER).addString(project.getIdentifier());
        }
    }

    private TableMap<String, DataSetType> getDataSetTypes()
    {
        List<DataSetType> dataSetTypes = commonServer.listDataSetTypes(sessionToken);
        return new TableMap<>(dataSetTypes, new IKeyExtractor<String, DataSetType>()
            {
                @Override
                public String getKey(DataSetType type)
                {
                    return type.getCode();
                }
            });
    }

    protected abstract List<AbstractExternalData> getDataSets();

}
