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
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.AUTO_GENERATE_CODES;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CHILDREN;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.IDENTIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PARENTS;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.SPACE;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
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
    protected Attribute[] getAttributes(final Sample entity)
    {
        return new Attribute[] { IDENTIFIER, CODE, SPACE, PROJECT, EXPERIMENT, PARENTS, CHILDREN,
                REGISTRATOR, REGISTRATION_DATE, MODIFIER, MODIFICATION_DATE };
    }

    @Override
    protected Attribute[] getImportAttributes()
    {
        return new Attribute[] { $, AUTO_GENERATE_CODES };
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
                return sample.getSpace() != null ? sample.getSpace().getPermId().getPermId() : "";
            }
            case PROJECT:
            {
                return sample.getProject() != null ? sample.getProject().getIdentifier().getIdentifier() : "";
            }
            case EXPERIMENT:
            {
                return sample.getExperiment() != null ? sample.getExperiment().getIdentifier().getIdentifier() : "";
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
                return sample.getRegistrator().getUserId();
            }
            case REGISTRATION_DATE:
            {
                return DATE_FORMAT.format(sample.getRegistrationDate());
            }
            case MODIFIER:
            {
                return sample.getModifier().getUserId();
            }
            case MODIFICATION_DATE:
            {
                return DATE_FORMAT.format(sample.getModificationDate());
            }
            case $:
            {
                return "";
            }
            case AUTO_GENERATE_CODES:
            {
                return "FALSE";
            }
            default:
            {
                return null;
            }
        }
    }

    protected Stream<String> getAttributeValuesStream(final Sample sample)
    {
        return Stream.of(sample.getIdentifier().getIdentifier(), sample.getCode(),
                sample.getSpace() != null ? sample.getSpace().getPermId().getPermId() : "",
                sample.getProject() != null ? sample.getProject().getIdentifier().getIdentifier() : "",
                sample.getExperiment() != null ? sample.getExperiment().getIdentifier().getIdentifier() : "",
                sample.getParents() == null ? "" : sample.getParents().stream()
                        .map(parent -> parent.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n")),
                sample.getChildren() == null ? "" : sample.getChildren().stream()
                        .map(child -> child.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n")), sample.getRegistrator().getUserId(),
                DATE_FORMAT.format(sample.getRegistrationDate()), sample.getModifier().getUserId(),
                DATE_FORMAT.format(sample.getModificationDate()));
    }

    @Override
    protected Stream<String> getImportAttributeValuesStream()
    {
        return Stream.of("", "FALSE");
    }

    @Override
    protected String typePermIdToString(final SampleType sampleType)
    {
        return sampleType.getPermId().getPermId();
    }

}
