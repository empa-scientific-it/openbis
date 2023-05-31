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

import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.$;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.AUTO_GENERATE_CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CHILDREN;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.IDENTIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PARENTS;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PERM_ID;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.SPACE;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;

public class XLSSampleExportHelper extends AbstractXLSEntityExportHelper<Sample, SampleType>
{

    public XLSSampleExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.SAMPLE;
    }

    @Override
    protected ExportableKind getTypeExportableKind()
    {
        return ExportableKind.SAMPLE_TYPE;
    }

    @Override
    protected String getEntityTypeName()
    {
        return "Sample type";
    }

    @Override
    protected String getIdentifier(final Sample sample)
    {
        return sample.getIdentifier().getIdentifier();
    }

    @Override
    protected Function<Sample, SampleType> getTypeFunction()
    {
        return Sample::getType;
    }

    @Override
    protected Attribute[] getAttributes(final Collection<Sample> entities)
    {
        return new Attribute[] { $, AUTO_GENERATE_CODE, PERM_ID, IDENTIFIER, CODE, SPACE, PROJECT, EXPERIMENT, PARENTS, CHILDREN,
                REGISTRATOR, REGISTRATION_DATE, MODIFIER, MODIFICATION_DATE };
    }

    @Override
    protected Collection<Sample> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SamplePermId> samplePermIds = permIds.stream().map(SamplePermId::new)
                .collect(Collectors.toList());
        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        fetchOptions.withExperiment();
        fetchOptions.withParents();
        fetchOptions.withChildren();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getSamples(sessionToken, samplePermIds, fetchOptions).values();
    }

    protected String getAttributeValue(final Sample sample, final Attribute attribute)
    {
        switch (attribute)
        {
            case PERM_ID:
            {
                return sample.getPermId().getPermId();
            }
            case IDENTIFIER:
            {
                return sample.getIdentifier().getIdentifier();
            }
            case CODE:
            {
                return sample.getCode();
            }
            case SPACE:
            {
                final Space space = sample.getSpace();
                return space != null ? space.getPermId().getPermId() : null;
            }
            case PROJECT:
            {
                final Project project = sample.getProject();
                return project != null ? project.getIdentifier().getIdentifier() : null;
            }
            case EXPERIMENT:
            {
                final Experiment experiment = sample.getExperiment();
                return experiment != null ? experiment.getIdentifier().getIdentifier() : null;
            }
            case PARENTS:
            {
                return sample.getParents() == null ? "" : sample.getParents().stream()
                        .map(parent -> parent.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n"));
            }
            case CHILDREN:
            {
                return sample.getChildren() == null ? "" : sample.getChildren().stream()
                        .map(child -> child.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n"));
            }
            case REGISTRATOR:
            {
                final Person registrator = sample.getRegistrator();
                return registrator != null ? registrator.getUserId() : null;
            }
            case REGISTRATION_DATE:
            {
                final Date registrationDate = sample.getRegistrationDate();
                return registrationDate != null ?DATE_FORMAT.format(registrationDate) : null;
            }
            case MODIFIER:
            {
                final Person modifier = sample.getModifier();
                return modifier != null ? modifier.getUserId() : null;
            }
            case MODIFICATION_DATE:
            {
                final Date modificationDate = sample.getModificationDate();
                return modificationDate != null ? DATE_FORMAT.format(modificationDate) : null;
            }
            case AUTO_GENERATE_CODE:
            {
                return "FALSE";
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected String typePermIdToString(final SampleType sampleType)
    {
        return sampleType.getPermId().getPermId();
    }

}
