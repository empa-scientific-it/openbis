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

import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.IDENTIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PERM_ID;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;

public class XLSExperimentExportHelper extends AbstractXLSEntityExportHelper<Experiment, ExperimentType>
{

    public XLSExperimentExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    protected Attribute[] getAttributes(final Experiment entity)
    {
        return new Attribute[] { PERM_ID, IDENTIFIER, CODE, PROJECT, REGISTRATOR, REGISTRATION_DATE, MODIFIER, MODIFICATION_DATE };
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
    protected String getAttributeValue(final Experiment experiment, final Attribute attribute)
    {
        switch (attribute)
        {
            case PERM_ID:
            {
                return experiment.getPermId().getPermId();
            }
            case IDENTIFIER:
            {
                return experiment.getIdentifier().getIdentifier();
            }
            case CODE:
            {
                return experiment.getCode();
            }
            case PROJECT:
            {
                return experiment.getProject().getIdentifier().getIdentifier();
            }
            case REGISTRATOR:
            {
                return experiment.getRegistrator().getUserId();
            }
            case REGISTRATION_DATE:
            {
                return DATE_FORMAT.format(experiment.getRegistrationDate());
            }
            case MODIFIER:
            {
                return experiment.getModifier().getUserId();
            }
            case MODIFICATION_DATE:
            {
                return DATE_FORMAT.format(experiment.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
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
