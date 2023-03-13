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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;

public class XLSDataSetExportHelper extends AbstractXLSEntityExportHelper<DataSet, DataSetType>
{

    public XLSDataSetExportHelper(final Workbook wb)
    {
        super(wb);
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
    protected String[] getAttributeNames(final DataSet dataSet)
    {
        return new String[] { "Code", dataSet.getSample() != null ? "Sample" : "Experiment", "Registrator", "Registration Date",
                "Modifier", "Modification Date" };
    }

    @Override
    protected String getAttributeValue(final DataSet dataSet, final String attributeId)
    {
        switch (attributeId)
        {
            case "Code":
            {
                return dataSet.getCode();
            }
            case "Sample":
            {
                return dataSet.getSample().getIdentifier().getIdentifier();
            }
            case "Experiment":
            {
                return dataSet.getExperiment().getIdentifier().getIdentifier();
            }
            case "Registrator":
            {
                return dataSet.getRegistrator().getUserId();
            }
            case "Registration Date":
            {
                return DATE_FORMAT.format(dataSet.getRegistrationDate());
            }
            case "Modifier":
            {
                return dataSet.getModifier().getUserId();
            }
            case "Modification Date":
            {
                return DATE_FORMAT.format(dataSet.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected Stream<String> getAllAttributeValuesStream(final DataSet dataSet)
    {
        return Stream.of(dataSet.getCode(), dataSet.getSample() != null
                ? dataSet.getSample().getIdentifier().getIdentifier()
                : dataSet.getExperiment().getIdentifier().getIdentifier(),
                dataSet.getRegistrator().getUserId(), DATE_FORMAT.format(dataSet.getRegistrationDate()),
                dataSet.getModifier().getUserId(), DATE_FORMAT.format(dataSet.getModificationDate()));
    }

    @Override
    protected String typePermIdToString(final DataSetType dataSetType)
    {
        return dataSetType.getPermId().getPermId();
    }

}
