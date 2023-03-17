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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;

public class XLSExperimentExportHelper extends AbstractXLSEntityExportHelper<Experiment, ExperimentType>
{

    public XLSExperimentExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    protected String[] getAttributeNames(final Experiment entity, final boolean compatibleWithImport)
    {
        return new String[] { "Identifier", "Code", "Project", "Registrator", "Registration Date", "Modifier", "Modification Date" };
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.EXPERIMENT;
    }

    @Override
    protected ExportableKind getTypeExportableKind()
    {
        return ExportableKind.EXPERIMENT_TYPE;
    }

    @Override
    protected String getEntityTypeName()
    {
        return "Experiment type";
    }

    @Override
    protected String getIdentifier(final Experiment experiment)
    {
        return experiment.getIdentifier().getIdentifier();
    }

    @Override
    protected Function<Experiment, ExperimentType> getTypeFunction()
    {
        return Experiment::getType;
    }

    @Override
    protected String getAttributeValue(final Experiment experiment, final String attributeId)
    {
        switch (attributeId)
        {
            case "Identifier":
            {
                return experiment.getIdentifier().getIdentifier();
            }
            case "Code":
            {
                return experiment.getCode();
            }
            case "Project":
            {
                return experiment.getProject().getIdentifier().getIdentifier();
            }
            case "Registrator":
            {
                return experiment.getRegistrator().getUserId();
            }
            case "Registration Date":
            {
                return DATE_FORMAT.format(experiment.getRegistrationDate());
            }
            case "Modifier":
            {
                return experiment.getModifier().getUserId();
            }
            case "Modification Date":
            {
                return DATE_FORMAT.format(experiment.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    protected Stream<String> getAllAttributeValuesStream(final Experiment experiment, final boolean compatibleWithImport)
    {
        return Stream.of(experiment.getIdentifier().getIdentifier(), experiment.getCode(),
                experiment.getProject().getIdentifier().getIdentifier(), experiment.getRegistrator().getUserId(),
                DATE_FORMAT.format(experiment.getRegistrationDate()), experiment.getModifier().getUserId(),
                DATE_FORMAT.format(experiment.getModificationDate()));
    }

    protected Collection<Experiment> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<ExperimentPermId> experimentPermIds = permIds.stream().map(ExperimentPermId::new)
                .collect(Collectors.toList());
        final ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getExperiments(sessionToken, experimentPermIds, fetchOptions).values();
    }

    @Override
    protected String typePermIdToString(final ExperimentType experimentType)
    {
        return experimentType.getPermId().getPermId();
    }

}
