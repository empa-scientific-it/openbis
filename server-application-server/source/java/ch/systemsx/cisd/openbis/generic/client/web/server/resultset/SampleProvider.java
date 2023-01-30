/*
 * Copyright 2010 ETH Zuerich, CISD
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
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.CONTAINER_SAMPLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.EXPERIMENT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.EXPERIMENT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.IS_DELETED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.IS_INSTANCE_SAMPLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.METAPROJECTS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PARENTS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PROJECT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PROJECT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PROPERTIES_PREFIX;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SAMPLE_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SAMPLE_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SHOW_DETAILS_LINK_COLUMN_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SPACE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SUBCODE;

import java.util.List;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class SampleProvider extends AbstractCommonTableModelProvider<Sample>
{
    private final ListSampleDisplayCriteria2 criteria;

    public SampleProvider(ICommonServer commonServer, String sessionToken,
            ListSampleDisplayCriteria2 criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    @Override
    protected TypedTableModel<Sample> createTableModel()
    {
        List<Sample> samples = getSamples();
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(CODE);
        builder.addColumn(SUBCODE).hideByDefault();
        builder.addColumn(SPACE).hideByDefault();
        builder.addColumn(SAMPLE_IDENTIFIER).withDefaultWidth(150).hideByDefault();
        builder.addColumn(SAMPLE_TYPE).withDefaultWidth(150).hideByDefault();
        builder.addColumn(IS_INSTANCE_SAMPLE).hideByDefault();
        builder.addColumn(IS_DELETED).hideByDefault();
        builder.addColumn(REGISTRATOR).withDefaultWidth(200);
        builder.addColumn(MODIFIER).withDefaultWidth(200);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(EXPERIMENT);
        builder.addColumn(EXPERIMENT_IDENTIFIER).withDefaultWidth(200).hideByDefault();
        builder.addColumn(PROJECT);
        builder.addColumn(PROJECT_IDENTIFIER);
        builder.addColumn(PERM_ID).hideByDefault();
        builder.addColumn(SHOW_DETAILS_LINK_COLUMN_NAME).hideByDefault();
        builder.addColumn(PARENTS);
        builder.addColumn(CONTAINER_SAMPLE);
        builder.addColumn(METAPROJECTS);
        TableMap<String, SampleType> sampleTypes = getSampleTypes();
        for (Sample sample : samples)
        {
            builder.addRow(sample);

            if (sample.isStub())
            {
                builder.column(PERM_ID).addString(sample.getPermId());
                builder.column(METAPROJECTS).addString(
                        metaProjectsToString(sample.getMetaprojects()));
            } else
            {
                builder.column(CODE).addEntityLink(sample, sample.getCode());
                builder.column(SUBCODE).addEntityLink(sample, sample.getSubCode());
                builder.column(SPACE).addString(
                        sample.getSpace() == null ? "" : sample.getSpace().getCode());
                builder.column(SAMPLE_IDENTIFIER).addEntityLink(sample, sample.getIdentifier());
                builder.column(SAMPLE_TYPE).addString(sample.getSampleType().getCode());
                builder.column(IS_INSTANCE_SAMPLE).addString(
                        SimpleYesNoRenderer.render(sample.getSpace() == null));
                builder.column(IS_DELETED).addString(
                        SimpleYesNoRenderer.render(DeletionUtils.isDeleted(sample)));
                builder.column(REGISTRATOR).addPerson(sample.getRegistrator());
                builder.column(MODIFIER).addPerson(sample.getModifier());
                builder.column(REGISTRATION_DATE).addDate(sample.getRegistrationDate());
                builder.column(MODIFICATION_DATE).addDate(sample.getModificationDate());

                builder.column(METAPROJECTS).addString(
                        metaProjectsToString(sample.getMetaprojects()));

                final Experiment experimentOrNull = sample.getExperiment();
                if (experimentOrNull != null)
                {
                    final Experiment experiment = experimentOrNull;
                    builder.column(EXPERIMENT).addEntityLink(experiment, experiment.getCode());
                    builder.column(EXPERIMENT_IDENTIFIER).addEntityLink(experiment,
                            experiment.getIdentifier());
                }
                addProject(builder, sample);
                builder.column(PERM_ID).addString(sample.getPermId());
                builder.column(SHOW_DETAILS_LINK_COLUMN_NAME).addString(sample.getPermlink());
                builder.column(PARENTS).addEntityLink(sample.getParents());

                final Sample containerOrNull = sample.getContainer();
                if (containerOrNull != null)
                {
                    final Sample container = containerOrNull;
                    builder.column(CONTAINER_SAMPLE).addEntityLink(container,
                            container.getIdentifier());
                }
                SampleType sampleType = sampleTypes.tryGet(sample.getSampleType().getCode());
                IColumnGroup columnGroup = builder.columnGroup(PROPERTIES_PREFIX);
                if (sampleType != null)
                {
                    columnGroup.addColumnsForAssignedProperties(sampleType);
                }
                columnGroup.addProperties(sample.getProperties());
            }
        }
        return builder.getModel();
    }

    protected TableMap<String, SampleType> getSampleTypes()
    {
        List<SampleType> sampleTypes = commonServer.listSampleTypes(sessionToken);
        TableMap<String, SampleType> sampleTypMap =
                new TableMap<String, SampleType>(sampleTypes,
                        new IKeyExtractor<String, SampleType>()
                            {
                                @Override
                                public String getKey(SampleType e)
                                {
                                    return e.getCode();
                                }
                            });
        return sampleTypMap;
    }

    private void addProject(TypedTableModelBuilder<Sample> builder, Sample sample)
    {
        Project project = sample.getProject();
        if (project == null)
        {
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                project = experiment.getProject();
            }
        }
        if (project != null)
        {
            builder.column(PROJECT).addString(project.getCode());
            builder.column(PROJECT_IDENTIFIER).addString(project.getIdentifier());
            
        }
    }

    private final List<Sample> getSamples()
    {
        switch (criteria.getCriteriaKind())
        {
            case BROWSE:
                return commonServer.listSamples(sessionToken, criteria.getBrowseCriteria());
            case SEARCH:
                return commonServer.searchForSamples(sessionToken, criteria.getSearchCriteria());
            case METAPROJECT:
                return commonServer.listMetaprojectSamples(sessionToken, new MetaprojectTechIdId(
                        criteria.getMetaprojectCriteria().getMetaprojectId()));
        }
        return null; // not possible
    }
}
