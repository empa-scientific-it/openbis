/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * @author Jakub Straszewski
 */
public class MetaprojectAssignmentsHelper
{
    private final IDAOFactory daoFactory;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public MetaprojectAssignmentsHelper(IDAOFactory daoFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.daoFactory = daoFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    /**
     * @param session is used only for getBaseIndexURL.
     * @param userId is the user for whom the authorization is performed
     */
    public MetaprojectAssignments getMetaprojectAssignments(Session session,
            Metaproject metaproject, String userId,
            EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions)
    {
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null");
        }

        String baseIndexURL = session.getBaseIndexURL();
        AuthorizationServiceUtils authorizationUtils =
                new AuthorizationServiceUtils(daoFactory, userId);

        MetaprojectAssignments metaprojectAssignments = new MetaprojectAssignments();
        metaprojectAssignments.setMetaproject(metaproject);

        List<Experiment> experiments = new ArrayList<Experiment>();
        List<Sample> samples = new ArrayList<Sample>();
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        List<Material> materials = new ArrayList<Material>();

        if (fetchOptions.contains(MetaprojectAssignmentsFetchOption.EXPERIMENTS))
        {
            for (MetaprojectAssignmentPE metaprojectAssignmentPE : getMetaprojectAssignments(
                    metaproject.getId(), EntityKind.EXPERIMENT))
            {
                if (authorizationUtils.canAccessExperiment(metaprojectAssignmentPE.getExperiment()))
                {
                    experiments.add(ExperimentTranslator.translate(
                            metaprojectAssignmentPE.getExperiment(), baseIndexURL, null,
                            managedPropertyEvaluatorFactory,
                            new SamplePropertyAccessValidator(session, daoFactory), LoadableFields.PROPERTIES));
                } else
                {
                    experiments.add(ExperimentTranslator.translateWithoutRevealingData(
                            metaprojectAssignmentPE.getExperiment(), null));
                }
            }
        }

        if (fetchOptions.contains(MetaprojectAssignmentsFetchOption.SAMPLES))
        {
            for (MetaprojectAssignmentPE metaprojectAssignmentPE : getMetaprojectAssignments(
                    metaproject.getId(), EntityKind.SAMPLE))
            {
                if (authorizationUtils.canAccessSample(metaprojectAssignmentPE.getSample()))
                {
                    samples.add(SampleTranslator.translate(metaprojectAssignmentPE.getSample(),
                            baseIndexURL, null, managedPropertyEvaluatorFactory,
                            new SamplePropertyAccessValidator(session, daoFactory)));
                } else
                {
                    samples.add(SampleTranslator
                            .translateWithoutRevealingData(metaprojectAssignmentPE.getSample()));
                }
            }
        }

        if (fetchOptions.contains(MetaprojectAssignmentsFetchOption.DATA_SETS))
        {
            for (MetaprojectAssignmentPE metaprojectAssignmentPE : getMetaprojectAssignments(
                    metaproject.getId(), EntityKind.DATA_SET))
            {
                if (authorizationUtils.canAccessDataSet(metaprojectAssignmentPE.getDataSet()))
                {
                    dataSets.add(DataSetTranslator.translate(metaprojectAssignmentPE.getDataSet(),
                            baseIndexURL, null, managedPropertyEvaluatorFactory,
                            new SamplePropertyAccessValidator(session, daoFactory)));
                } else
                {
                    dataSets.add(DataSetTranslator
                            .translateWithoutRevealingData(metaprojectAssignmentPE.getDataSet()));
                }
            }
        }

        if (fetchOptions.contains(MetaprojectAssignmentsFetchOption.MATERIALS))
        {
            for (MetaprojectAssignmentPE metaprojectAssignmentPE : getMetaprojectAssignments(
                    metaproject.getId(), EntityKind.MATERIAL))
            {
                materials.add(MaterialTranslator.translate(metaprojectAssignmentPE.getMaterial(),
                        null, managedPropertyEvaluatorFactory,
                        new SamplePropertyAccessValidator(session, daoFactory)));
            }
        }

        metaprojectAssignments.setExperiments(experiments);
        metaprojectAssignments.setSamples(samples);
        metaprojectAssignments.setDataSets(dataSets);
        metaprojectAssignments.setMaterials(materials);

        return metaprojectAssignments;
    }

    public Collection<MetaprojectAssignmentPE> getMetaprojectAssignments(Long metaprojectId,
            EntityKind entityKind)
    {
        return daoFactory.getMetaprojectDAO().listMetaprojectAssignments(metaprojectId,
                DtoConverters.convertEntityKind(entityKind));
    }
}
