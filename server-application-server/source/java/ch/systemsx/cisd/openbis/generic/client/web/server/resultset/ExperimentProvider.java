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
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.EXPERIMENT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.EXPERIMENT_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.IS_DELETED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.METAPROJECTS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.PROJECT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.PROJECT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.SHOW_DETAILS_LINK;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs.SPACE;

import java.util.List;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentProvider extends AbstractCommonTableModelProvider<Experiment>
{

    private final ListExperimentsCriteria criteria;

    public ExperimentProvider(ICommonServer commonServer, String sessionToken,
            ListExperimentsCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    @Override
    protected TypedTableModel<Experiment> createTableModel()
    {
        List<Experiment> experiments = null;

        if (criteria.tryGetMetaprojectId() != null)
        {
            experiments =
                    commonServer.listMetaprojectExperiments(sessionToken, new MetaprojectTechIdId(
                            criteria.tryGetMetaprojectId()));
        } else if (criteria.tryGetProjectCode() != null)
        {
            experiments =
                    commonServer.listExperiments(
                            sessionToken,
                            criteria.getExperimentType(),
                            new ProjectIdentifier(criteria.getSpaceCode(), criteria
                                    .tryGetProjectCode()));
        } else
        {
            experiments =
                    commonServer.listExperiments(sessionToken, criteria.getExperimentType(),
                            new SpaceIdentifier(criteria.getSpaceCode()));
        }
        TypedTableModelBuilder<Experiment> builder = new TypedTableModelBuilder<Experiment>();
        builder.addColumn(CODE);
        builder.addColumn(EXPERIMENT_TYPE).hideByDefault();
        builder.addColumn(EXPERIMENT_IDENTIFIER).hideByDefault().withDefaultWidth(150);
        builder.addColumn(SPACE).hideByDefault();
        builder.addColumn(PROJECT).hideByDefault();
        builder.addColumn(PROJECT_IDENTIFIER).hideByDefault();
        builder.addColumn(REGISTRATOR);
        builder.addColumn(MODIFIER);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(200);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(200).hideByDefault();
        builder.addColumn(IS_DELETED).hideByDefault();
        builder.addColumn(PERM_ID).hideByDefault();
        builder.addColumn(SHOW_DETAILS_LINK).hideByDefault();
        builder.addColumn(METAPROJECTS);
        TableMap<String, ExperimentType> experimentTypes = getExperimentTypes();
        for (Experiment experiment : experiments)
        {
            builder.addRow(experiment);

            if (experiment.isStub())
            {
                builder.column(PERM_ID).addString(experiment.getPermId());
            } else
            {
                builder.column(CODE).addEntityLink(experiment, experiment.getCode());
                builder.column(EXPERIMENT_TYPE).addString(experiment.getExperimentType().getCode());
                builder.column(EXPERIMENT_IDENTIFIER).addEntityLink(experiment,
                        experiment.getIdentifier());
                builder.column(SPACE).addString(experiment.getProject().getSpace().getCode());
                builder.column(PROJECT).addString(experiment.getProject().getCode());
                builder.column(PROJECT_IDENTIFIER).addString(experiment.getProject().getIdentifier());
                builder.column(REGISTRATOR).addPerson(experiment.getRegistrator());
                builder.column(MODIFIER).addPerson(experiment.getModifier());
                builder.column(REGISTRATION_DATE).addDate(experiment.getRegistrationDate());
                builder.column(MODIFICATION_DATE).addDate(experiment.getModificationDate());
                builder.column(IS_DELETED).addString(
                        SimpleYesNoRenderer.render(DeletionUtils.isDeleted(experiment)));
                builder.column(PERM_ID).addString(experiment.getPermId());
                builder.column(SHOW_DETAILS_LINK).addString(experiment.getPermlink());
                builder.column(METAPROJECTS).addString(
                        metaProjectsToString(experiment.getMetaprojects()));
                ExperimentType experimentType =
                        experimentTypes.tryGet(experiment.getExperimentType().getCode());
                IColumnGroup columnGroup =
                        builder.columnGroup(ExperimentBrowserGridColumnIDs.PROPERTIES_PREFIX);
                if (experimentType != null)
                {
                    columnGroup.addColumnsForAssignedProperties(experimentType);
                }
                columnGroup.addProperties(experiment.getProperties());
            }
        }
        return builder.getModel();
    }

    protected TableMap<String, ExperimentType> getExperimentTypes()
    {
        List<ExperimentType> experimentTypes = commonServer.listExperimentTypes(sessionToken);
        TableMap<String, ExperimentType> experimentTypMap =
                new TableMap<String, ExperimentType>(experimentTypes,
                        new IKeyExtractor<String, ExperimentType>()
                            {
                                @Override
                                public String getKey(ExperimentType e)
                                {
                                    return e.getCode();
                                }
                            });
        return experimentTypMap;
    }

}
