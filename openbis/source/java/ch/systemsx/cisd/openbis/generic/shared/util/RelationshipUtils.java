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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.IModifierAndModificationDateBean;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

/**
 * Utility function for relation ship.
 * 
 * @author Franz-Josef Elmer
 */
public class RelationshipUtils
{

    public static void updateModificationDateAndModifierOfRelatedProjectsOfExperiments(List<ExperimentPE> experiments, IAuthSession session)
    {
        for (ExperimentPE experiment : experiments)
        {
            updateModificationDateAndModifier(experiment.getProject(), session);
        }
    }

    public static void updateModificationDateAndModifierOfRelatedEntitiesOfSamples(List<SamplePE> samples, IAuthSession session)
    {
        for (SamplePE sample : samples)
        {
            ExperimentPE experiment = sample.getExperiment();
            if (experiment != null)
            {
                updateModificationDateAndModifier(experiment, session);
            }
            SamplePE container = sample.getContainer();
            if (container != null)
            {
                updateModificationDateAndModifier(container, session);
            }
            List<SamplePE> parents = sample.getParents();
            if (parents != null)
            {
                for (SamplePE parent : parents)
                {
                    updateModificationDateAndModifier(parent, session);
                }
            }
            Set<SampleRelationshipPE> childRelationships = sample.getChildRelationships();
            if (childRelationships != null)
            {
                for (SampleRelationshipPE childRelationship : childRelationships)
                {
                    SamplePE childSample = childRelationship.getChildSample();
                    updateModificationDateAndModifier(childSample, session);
                }
            }
        }
    }

    public static void updateModificationDateAndModifierOfRelatedEntitiesOfDataSets(List<DataPE> dataSets, IAuthSession session)
    {
        for (DataPE dataSet : dataSets)
        {
            ExperimentPE experiment = dataSet.getExperiment();
            updateModificationDateAndModifier(experiment, session);
            SamplePE sample = dataSet.tryGetSample();
            if (sample != null)
            {
                updateModificationDateAndModifier(sample, session);
            }
            RelationshipUtils.updateModificationDateAndModifierOfDataSets(dataSet.getChildren(), session);
            RelationshipUtils.updateModificationDateAndModifierOfDataSets(dataSet.getParents(), session);
            Set<DataSetRelationshipPE> relationships = dataSet.getParentRelationships();
            for (DataSetRelationshipPE relationship : getContainerComponentRelationships(relationships))
            {
                updateModificationDateAndModifier(relationship.getParentDataSet(), session);
            }
        }
    }

    private static void updateModificationDateAndModifierOfDataSets(List<DataPE> dataSets, IAuthSession session)
    {
        if (dataSets != null)
        {
            for (DataPE child : dataSets)
            {
                updateModificationDateAndModifier(child, session);
            }
        }
    }

    public static boolean isParentChildRelationship(DataSetRelationshipPE relationship)
    {
        return isRelationshipOfType(relationship, BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
    }

    public static boolean isContainerComponentRelationship(DataSetRelationshipPE relationship)
    {
        return isRelationshipOfType(relationship, BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
    }

    private static boolean isRelationshipOfType(DataSetRelationshipPE relationship, String code)
    {
        return relationship.getRelationshipType().getCode().equals(code);
    }

    public static List<DataSetRelationshipPE> getParentChildRelationships(Collection<DataSetRelationshipPE> relationships)
    {
        return filterRelationships(relationships, BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
    }

    public static List<DataSetRelationshipPE> getContainerComponentRelationships(Collection<DataSetRelationshipPE> relationships)
    {
        return filterRelationships(relationships, BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
    }

    private static List<DataSetRelationshipPE> filterRelationships(Collection<DataSetRelationshipPE> relationships, String code)
    {
        List<DataSetRelationshipPE> result = new ArrayList<DataSetRelationshipPE>();
        for (DataSetRelationshipPE relationship : relationships)
        {
            if (relationship.getRelationshipType().getCode().equals(code))
            {
                result.add(relationship);
            }
        }
        return result;
    }

    public static void setSampleForDataSet(DataPE dataSet, SamplePE sample, IAuthSession session)
    {
        updateModificationDateAndModifier(dataSet.tryGetSample(), session);
        dataSet.setSample(sample);
        if (sample != null)
        {
            if (dataSet.getExperiment() != sample.getExperiment())
            {
                if (dataSet.getExperiment() != null)
                {
                    updateModificationDateAndModifier(dataSet.getExperiment(), session);
                }
                if (sample.getExperiment() != null)
                {
                    updateModificationDateAndModifier(sample.getExperiment(), session);
                }
            }
            dataSet.setExperiment(sample.getExperiment());
        }
        updateModificationDateAndModifier(sample, session);
        updateModificationDateAndModifier(dataSet, session);
    }

    public static void setContainerForSample(SamplePE sample, SamplePE container,
            IAuthSession session)
    {
        updateModificationDateAndModifier(sample.getContainer(), session);
        sample.setContainer(container);
        updateModificationDateAndModifier(container, session);
        updateModificationDateAndModifier(sample, session);
    }

    public static void setExperimentForDataSet(DataPE dataSet, ExperimentPE experiment,
            IAuthSession session)
    {
        updateModificationDateAndModifier(dataSet.getExperiment(), session);
        dataSet.setExperiment(experiment);
        dataSet.setSample(null);
        updateModificationDateAndModifier(experiment, session);
        updateModificationDateAndModifier(dataSet, session);
    }

    public static void updateModificationDateAndModifier(
            IModifierAndModificationDateBean beanOrNull, IAuthSession session)
    {
        if (beanOrNull == null)
        {
            return;
        }
        log(beanOrNull, session);
        PersonPE person = session.tryGetPerson();
        updateModificationDateAndModifier(beanOrNull, person);
    }

    public static void updateModificationDateAndModifier(
            IModifierAndModificationDateBean beanOrNull, PersonPE personOrNull)
    {
        if (personOrNull != null)
        {
            beanOrNull.setModifier(personOrNull);
        }
        beanOrNull.setModificationDate(new Date());
    }

    private static void log(IModifierAndModificationDateBean beanOrNull, IAuthSession session)
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace)
        {
            if (stackTraceElement.getClassName().contains("OptimisticLockingTest"))
            {
                System.out.println("===== UPDATE modification date and modifier ===== "
                        + stackTraceElement.getMethodName());
                System.out.println("bean: " + beanOrNull);
                System.out.println("user: " + session.tryGetPerson());
                System.out.println("\t" + stackTrace[3]);
                System.out.println("\t" + stackTrace[4]);
                System.out.println("\t" + stackTrace[5]);
                System.out.println("\t...");
                System.out.println("\t" + stackTraceElement);
                break;
            }
        }
    }

}
