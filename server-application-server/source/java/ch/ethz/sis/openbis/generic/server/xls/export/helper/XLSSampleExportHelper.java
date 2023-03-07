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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
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
    protected String[] getAttributeNames(final Sample entity)
    {
        return new String[] { "$", "Identifier", "Code", "Space", "Project", "Experiment",
                "Auto generate code", "Parents", "Children" };
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
        return api.getSamples(sessionToken, samplePermIds, fetchOptions).values();
    }

    protected String getAttributeValue(final Sample sample, final String attributeId)
    {
        switch (attributeId)
        {
            case "$":
            {
                return "";
            }
            case "Identifier":
            {
                return sample.getIdentifier().getIdentifier();
            }
            case "Code":
            {
                return sample.getCode();
            }
            case "Space":
            {
                return sample.getSpace() != null ? sample.getSpace().getPermId().getPermId() : "";
            }
            case "Project":
            {
                return sample.getProject() != null ? sample.getProject().getIdentifier().getIdentifier() : "";
            }
            case "Experiment":
            {
                return sample.getExperiment() != null ? sample.getExperiment().getIdentifier().getIdentifier() : "";
            }
            case "Auto generate code":
            {
                return "FALSE";
            }
            case "Parents":
            {
                return sample.getParents() == null ? "" : sample.getParents().stream()
                        .map(parent -> parent.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n"));
            }
            case "Children":
            {
                return sample.getChildren() == null ? "" : sample.getChildren().stream()
                        .map(child -> child.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n"));
            }
            default:
            {
                return null;
            }
        }
    }

    protected Stream<String> getAllAttributeValuesStream(final Sample sample)
    {
        return Stream.of("", sample.getIdentifier().getIdentifier(), sample.getCode(),
                sample.getSpace() != null ? sample.getSpace().getPermId().getPermId() : "",
                sample.getProject() != null ? sample.getProject().getIdentifier().getIdentifier() : "",
                sample.getExperiment() != null ? sample.getExperiment().getIdentifier().getIdentifier() : "",
                "FALSE",
                sample.getParents() == null ? "" : sample.getParents().stream()
                        .map(parent -> parent.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n")),
                sample.getChildren() == null ? "" : sample.getChildren().stream()
                        .map(child -> child.getIdentifier().getIdentifier())
                        .collect(Collectors.joining("\n")));
    }

}
