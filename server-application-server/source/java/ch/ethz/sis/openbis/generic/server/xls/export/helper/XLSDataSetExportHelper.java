/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.ARCHIVING_STATUS;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CHILDREN;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.IDENTIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PARENTS;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PERM_ID;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PRESENT_IN_ARCHIVE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.SIZE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.STORAGE_CONFIRMATION;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSDataSetExportHelper extends AbstractXLSEntityExportHelper<DataSet, DataSetType>
{

    public XLSDataSetExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, final int rowNumber, final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        return compatibleWithImport ? new AdditionResult(0, List.of(), Map.of())
                : super.add(api, sessionToken, wb, permIds, rowNumber, entityTypeExportFieldsMap, textFormatting, false);
    }

    @Override
    protected Collection<DataSet> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<DataSetPermId> dataSetPermIds = permIds.stream().map(DataSetPermId::new)
                .collect(Collectors.toList());
        final DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withSample();
        fetchOptions.withExperiment();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withPhysicalData();
        fetchOptions.withParents();
        fetchOptions.withChildren();
        return api.getDataSets(sessionToken, dataSetPermIds, fetchOptions).values();
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.DATASET;
    }

    @Override
    protected ExportableKind getTypeExportableKind()
    {
        return ExportableKind.DATASET_TYPE;
    }

    @Override
    protected String getEntityTypeName()
    {
        return "Dataset type";
    }

    @Override
    protected String getIdentifier(final DataSet entity)
    {
        return entity.getPermId().getPermId();
    }

    @Override
    protected Function<DataSet, DataSetType> getTypeFunction()
    {
        return DataSet::getType;
    }

    @Override
    protected Attribute[] getAttributes(final Collection<DataSet> dataSets)
    {
        return new Attribute[] { PERM_ID, CODE, IDENTIFIER, ARCHIVING_STATUS, PRESENT_IN_ARCHIVE, STORAGE_CONFIRMATION, SAMPLE, EXPERIMENT, PARENTS,
                CHILDREN, REGISTRATOR, REGISTRATION_DATE, MODIFIER, MODIFICATION_DATE, SIZE };
    }

    @Override
    protected String getAttributeValue(final DataSet dataSet, final Attribute attribute)
    {
        switch (attribute)
        {
            case PERM_ID:
            {
                return dataSet.getPermId().getPermId();
            }
            case ARCHIVING_STATUS:
            {
                final PhysicalData physicalData = dataSet.getPhysicalData();
                return physicalData != null ? physicalData.getStatus().toString() : null;
            }
            case PRESENT_IN_ARCHIVE:
            {
                final PhysicalData physicalData = dataSet.getPhysicalData();
                return physicalData != null ? physicalData.isPresentInArchive().toString().toUpperCase() : null;
            }
            case STORAGE_CONFIRMATION:
            {
                final PhysicalData physicalData = dataSet.getPhysicalData();
                return physicalData != null ? physicalData.isStorageConfirmation().toString().toUpperCase() : null;
            }
            case SIZE:
            {
                final PhysicalData physicalData = dataSet.getPhysicalData();
                return physicalData != null && physicalData.getSize() != null ? physicalData.getSize().toString() : null;
            }
            case IDENTIFIER:
            case CODE:
            {
                return dataSet.getCode();
            }
            case SAMPLE:
            {
                final Sample sample = dataSet.getSample();
                return sample != null ? sample.getIdentifier().getIdentifier() : null;
            }
            case EXPERIMENT:
            {
                final Experiment experiment = dataSet.getExperiment();
                return experiment != null ? experiment.getIdentifier().getIdentifier() : null;
            }
            case REGISTRATOR:
            {
                final Person registrator = dataSet.getRegistrator();
                return registrator != null ? registrator.getUserId() : null;
            }
            case REGISTRATION_DATE:
            {
                final Date registrationDate = dataSet.getRegistrationDate();
                return registrationDate != null ? DATE_FORMAT.format(registrationDate) : null;
            }
            case MODIFIER:
            {
                final Person modifier = dataSet.getModifier();
                return modifier != null ? modifier.getUserId() : null;
            }
            case MODIFICATION_DATE:
            {
                final Date modificationDate = dataSet.getModificationDate();
                return modificationDate != null ? DATE_FORMAT.format(modificationDate) : null;
            }
            case PARENTS:
            {
                return dataSet.getParents() == null ? "" : dataSet.getParents().stream()
                        .map(DataSet::getCode)
                        .collect(Collectors.joining("\n"));
            }
            case CHILDREN:
            {
                return dataSet.getChildren() == null ? "" : dataSet.getChildren().stream()
                        .map(DataSet::getCode)
                        .collect(Collectors.joining("\n"));
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected String typePermIdToString(final DataSetType dataSetType)
    {
        return dataSetType.getPermId().getPermId();
    }

}
