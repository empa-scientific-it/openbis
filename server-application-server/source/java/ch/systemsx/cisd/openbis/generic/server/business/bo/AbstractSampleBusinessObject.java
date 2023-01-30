/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IEntityOperationChecker;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * An <i>abstract</i> {@link AbstractSampleIdentifierBusinessObject} extension for <i>Business Object</i> which has to do with {@link SamplePE}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractSampleBusinessObject extends AbstractSampleIdentifierBusinessObject
{
    /**
     * Whether this object works with only new samples (that is: not yet saved into the database) right now.
     */
    protected boolean onlyNewSamples = true;

    protected IEntityOperationChecker entityOperationChecker;

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session,
            IRelationshipService relationshipService,
            IEntityOperationChecker entityOperationChecker,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, EntityKind.SAMPLE, entityInformationProvider, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
        this.entityOperationChecker = entityOperationChecker;
    }

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter,
            IRelationshipService relationshipService,
            IEntityOperationChecker entityOperationChecker,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, entityPropertiesConverter, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
        this.entityOperationChecker = entityOperationChecker;
    }

    private final void defineSampleProperties(final SamplePE sample,
            final IEntityProperty[] sampleProperties)
    {
        final String sampleTypeCode = sample.getSampleType().getCode();
        final List<SamplePropertyPE> properties =
                entityPropertiesConverter.convertProperties(sampleProperties, sampleTypeCode,
                        sample.getRegistrator());
        for (final SamplePropertyPE sampleProperty : properties)
        {
            sample.addProperty(sampleProperty);
        }
    }

    /**
     * Creates an new {@link SamplePE} object out of given <var>newSample</var>.
     * <p>
     * Does not trigger any insert in the database.
     * </p>
     * 
     * @param experimentCacheOrNull
     */
    final SamplePE createSample(final NewSample newSample,
            Map<String, SampleTypePE> sampleTypeCacheOrNull,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCacheOrNull,
            Map<String, ExperimentPE> experimentCacheOrNull, PersonPE registratorOrNull)
            throws UserFailureException
    {
        final SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(newSample);
        SampleOwner sampleOwner = getSampleOwner(sampleOwnerCacheOrNull, sampleIdentifier);
        SampleTypePE sampleTypePE =
                (sampleTypeCacheOrNull != null) ? sampleTypeCacheOrNull.get(newSample
                        .getSampleType().getCode()) : null;
        if (sampleTypePE == null)
        {
            sampleTypePE = getSampleType(newSample.getSampleType().getCode());
            if (sampleTypeCacheOrNull != null)
            {
                sampleTypeCacheOrNull.put(newSample.getSampleType().getCode(), sampleTypePE);
            }
        }
        String experimentIdentifier = newSample.getExperimentIdentifier();
        ExperimentPE experimentPE =
                tryFindExperiment(experimentCacheOrNull, experimentIdentifier,
                        newSample.getDefaultSpaceIdentifier());
        updateModifierAndModificationDate(experimentPE);

        final SamplePE samplePE = new SamplePE();
        samplePE.setPermId(getOrCreatePermID(newSample));
        samplePE.setExperiment(experimentPE);
        samplePE.setCode(sampleIdentifier.getSampleSubCode());
        PersonPE registrator = registratorOrNull != null ? registratorOrNull : findPerson();
        samplePE.setRegistrator(registrator);
        samplePE.setSampleType(sampleTypePE);
        samplePE.setSpace(sampleOwner.tryGetSpace());
        setProject(samplePE, newSample);

        RelationshipUtils.updateModificationDateAndModifier(samplePE, registrator, getTransactionTimeStamp());
        defineSampleProperties(samplePE, newSample.getProperties());
        String containerIdentifier = newSample.getContainerIdentifierForNewSample();
        setContainer(sampleIdentifier, samplePE, containerIdentifier,
                newSample.getDefaultSpaceIdentifier());
        if (newSample.getParentsOrNull() != null)
        {
            final String[] parents = newSample.getParentsOrNull();
            setParents(samplePE, parents, newSample.getDefaultSpaceIdentifier());
        }
        setMetaprojects(samplePE, newSample.getMetaprojectsOrNull());
        return samplePE;
    }

    private void setProject(SamplePE samplePE, NewSample newSample)
    {
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(newSample);
        if (sampleIdentifier.isProjectLevel())
        {
            ProjectPE project = findProject(sampleIdentifier.getProjectLevel());
            setProjectAndSpace(samplePE, project);
        }
        String projectIdentifier = newSample.getProjectIdentifier();
        if (projectIdentifier != null)
        {
            ProjectPE project = findProject(new ProjectIdentifierFactory(projectIdentifier).createIdentifier());
            setProjectAndSpace(samplePE, project);
        }
    }

    private void setProjectAndSpace(SamplePE samplePE, ProjectPE project)
    {
        SampleUtils.assertProjectSamplesEnabled(samplePE, project);
        samplePE.setProject(project);
        samplePE.setSpace(project.getSpace());
    }

    private void updateModifierAndModificationDate(ExperimentPE experimentOrNull)
    {
        if (experimentOrNull != null)
        {
            Date timeStamp = getTransactionTimeStamp();
            RelationshipUtils.updateModificationDateAndModifierOfExperimentAndProject(experimentOrNull, null, session, timeStamp);
        }
    }

    protected SampleOwner getSampleOwner(
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCacheOrNull,
            final SampleIdentifier sampleIdentifier)
    {
        final SampleOwnerIdentifier sampleOwnerIdentifier =
                sampleIdentifier.createSampleOwnerIdentifier();
        SampleOwner sampleOwner =
                (sampleOwnerCacheOrNull != null) ? sampleOwnerCacheOrNull
                        .get(sampleOwnerIdentifier) : null;
        if (sampleOwner == null)
        {
            sampleOwner = getSampleOwnerFinder().figureSampleOwner(sampleIdentifier);
            if (sampleOwnerCacheOrNull != null)
            {
                sampleOwnerCacheOrNull.put(sampleOwnerIdentifier, sampleOwner);
            }
        }
        return sampleOwner;
    }

    private ExperimentPE tryFindExperiment(Map<String, ExperimentPE> experimentCacheOrNull,
            String experimentIdentifier, String defaultSpace)
    {
        ExperimentPE experimentPE = null;
        if (experimentIdentifier != null)
        {
            ExperimentIdentifier expIdent =
                    new ExperimentIdentifierFactory(experimentIdentifier)
                            .createIdentifier(defaultSpace);
            fillSpaceIdentifier(expIdent);

            experimentPE =
                    (experimentCacheOrNull != null) ? experimentCacheOrNull
                            .get(expIdent.toString()) : null;
            if (experimentPE == null)
            {
                experimentPE = findExperiment(expIdent);
                if (experimentCacheOrNull != null)
                {
                    experimentCacheOrNull.put(expIdent.toString(), experimentPE);
                }
            }
        }
        return experimentPE;
    }

    private ProjectPE tryFindProject(Map<String, ProjectPE> projectCache, ProjectIdentifier projectIdentifier)
    {
        if (projectIdentifier == null)
        {
            return null;
        }
        return tryFindProject(projectCache, projectIdentifier.toString(), null);
    }

    private ProjectPE tryFindProject(Map<String, ProjectPE> projectCache, String projectIdentifier,
            String defaultSpace)
    {
        if (projectIdentifier == null)
        {
            return null;
        }
        ProjectIdentifier identifier = new ProjectIdentifierFactory(projectIdentifier).createIdentifier(defaultSpace);
        fillSpaceIdentifier(identifier);
        String key = identifier.toString();
        ProjectPE project = projectCache == null ? null : projectCache.get(key);
        if (project == null)
        {
            project = findProject(identifier);
            if (projectCache != null)
            {
                projectCache.put(key, project);
            }
        }
        return project;
    }

    protected void setContainer(final SampleIdentifier sampleIdentifier, final SamplePE samplePE,
            String containerIdentifier, final String defaultSpace)
    {
        final SamplePE containerPE =
                tryGetValidNotContainedSample(containerIdentifier, sampleIdentifier, defaultSpace);

        if (samplePE.getContainer() == null && containerPE == null)
        {
            return;
        }

        if (samplePE.getContainer() != null && samplePE.getContainer().equals(containerPE))
        {
            return;
        }

        checkIfCanBeContainer(samplePE, containerPE);

        if (containerPE == null)
        {
            relationshipService.removeSampleFromContainer(session, samplePE);
        } else
        {
            relationshipService.assignSampleToContainer(session, samplePE, containerPE);
        }
    }

    protected void setParents(final SamplePE childPE, final String[] parents,
            final String defaultSpace)
    {
        final List<SampleIdentifier> parentIdentifiers =
                IdentifierHelper.extractSampleIdentifiers(parents, defaultSpace);
        final SampleIdentifier childIdentifier = childPE.getSampleIdentifier();
        if (childIdentifier.isProjectLevel())
        {
            final String spaceCode = childIdentifier.getProjectLevel().getSpaceCode();
            for (SampleIdentifier si : parentIdentifiers)
            {
                IdentifierHelper.fillSpaceIfNotSpecified(si, spaceCode);
            }
        } else if (childIdentifier.isSpaceLevel())
        {
            final String spaceCode = childIdentifier.getSpaceLevel().getSpaceCode();
            for (SampleIdentifier si : parentIdentifiers)
            {
                IdentifierHelper.fillSpaceIfNotSpecified(si, spaceCode);
            }
        }
        final Set<SamplePE> parentPEs = new HashSet<SamplePE>();
        for (SampleIdentifier si : parentIdentifiers)
        {
            SamplePE parent = getSampleByIdentifier(si);
            parentPEs.add(parent);
            checkIfCanBeParent(childPE, parent);
        }
        replaceParents(childPE, parentPEs);
    }

    /**
     * check if the given candidate for the container is not already contained in the sample.
     */
    private void checkIfCanBeContainer(final SamplePE sample, final SamplePE container)
    {
        SamplePE containerCandidate = container;

        while (containerCandidate != null)
        {
            if (sample.equals(containerCandidate))
            {
                throw UserFailureException.fromTemplate("'%s' cannot be it's own container.",
                        sample.getIdentifier());
            }
            containerCandidate = containerCandidate.getContainer();
        }
    }

    /**
     * depth-first search through all parents of parent candidate in search of a childPE. If it's found - the exception is being thrown
     */

    private void checkIfCanBeParent(final SamplePE childPE, final SamplePE parentCandidate)
    {
        HashSet<SamplePE> visitedCandidates = new HashSet<SamplePE>();
        Stack<SamplePE> candidates = new Stack<SamplePE>();

        candidates.add(parentCandidate);
        visitedCandidates.add(parentCandidate);

        while (false == candidates.empty())
        {
            SamplePE candidate = candidates.pop();
            if (candidate == childPE)
            {
                throw UserFailureException.fromTemplate("'%s' cannot be its own parent.",
                        childPE.getIdentifier());
            }
            for (SamplePE parent : candidate.getParents())
            {
                if (false == visitedCandidates.contains(parent))
                {
                    candidates.add(parent);
                    visitedCandidates.add(parent);
                }
            }
        }
    }

    private void replaceParents(SamplePE child, Set<SamplePE> newParents)
    {
        for (SamplePE parent : newParents)
        {
            checkParentDeletion(parent, child.getSampleIdentifier());
        }
        List<SampleRelationshipPE> oldParents = new ArrayList<SampleRelationshipPE>();
        for (SampleRelationshipPE r : child.getParentRelationships())
        {
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                oldParents.add(r);
            }
        }
        for (SampleRelationshipPE r : oldParents)
        {
            if (newParents.contains(r.getParentSample()))
            {
                newParents.remove(r.getParentSample());
            } else
            {
                relationshipService.removeParentFromSample(session, child, r.getParentSample());
            }
        }

        for (SamplePE newParent : newParents)
        {
            relationshipService.addParentToSample(session, child, newParent);
        }

    }

    private SamplePE tryGetValidParentSample(final String parentIdentifierOrNull,
            final SampleIdentifier childIdentifier, final String defaultSpace)
    {
        if (parentIdentifierOrNull == null)
        {
            return null;
        }
        final SamplePE parentPE =
                getSampleByIdentifier(SampleIdentifierFactory.parse(parentIdentifierOrNull,
                        defaultSpace));
        checkParentDeletion(parentPE, childIdentifier);
        return parentPE;
    }

    private void checkParentDeletion(final SamplePE parentPE, final SampleIdentifier child)
    {
        if (parentPE.getDeletion() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Sample '%s' has been deleted and can't become a parent of sample '%s'.",
                    parentPE.getIdentifier(), child);
        }
    }

    private SamplePE tryGetValidNotContainedSample(final String parentIdentifierOrNull,
            final SampleIdentifier sampleIdentifier, final String defaultSpace)
    {
        SamplePE sample =
                tryGetValidParentSample(parentIdentifierOrNull, sampleIdentifier, defaultSpace);
        if (sample != null && sample.getContainer() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register sample '%s': parent '%s' is part of another sample.",
                    sampleIdentifier, parentIdentifierOrNull);
        }
        return sample;
    }

    final SampleTypePE getSampleType(final String code) throws UserFailureException
    {
        final SampleTypePE sampleType = getSampleTypeDAO().tryFindSampleTypeByCode(code);
        if (sampleType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample type with code '%s' could be found in the database.", code);
        }
        return sampleType;
    }

    protected ExperimentPE findExperiment(ExperimentIdentifier identifier)
    {
        ProjectPE project = findProject(identifier);
        String experimentCode = identifier.getExperimentCode();
        ExperimentPE experiment =
                getExperimentDAO().tryFindByCodeAndProject(project, experimentCode);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment '%s' could be found in the '%s/%s' project!", experimentCode,
                    identifier.getSpaceCode(), identifier.getProjectCode());
        }
        return experiment;
    }

    protected ProjectPE findProject(ProjectIdentifier projectIdentifier)
    {
        String spaceCode = projectIdentifier.getSpaceCode();
        String projectCode = projectIdentifier.getProjectCode();
        ProjectPE project =
                getProjectDAO().tryFindProject(spaceCode, projectCode);
        if (project == null)
        {
            throw UserFailureException.fromTemplate(
                    "No project '%s' could be found in the '%s' space!", projectCode, spaceCode);
        }
        return project;
    }

    protected void checkAllBusinessRules(SamplePE sample, IDataDAO dataDAO,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cacheOrNull, boolean spaceUpdated)
    {
        checkPropertiesBusinessRules(sample, cacheOrNull);
        checkExperimentBusinessRules(sample);
        checkParentBusinessRules(sample);
        checkContainerBusinessRules(sample);
    }

    protected void checkPropertiesBusinessRules(SamplePE sample,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cacheOrNull)
    {
        if (cacheOrNull != null)
        {
            entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(),
                    sample.getSampleType(), cacheOrNull);
        } else
        {
            entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(),
                    sample.getSampleType());
        }
    }

    protected void checkExperimentBusinessRules(SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment == null)
        {
            String sampleIdentifier = sample.getIdentifier();
            ArrayList<DataPE> dataSets = new ArrayList<DataPE>(sample.getDatasets());
            checkDataSetsDoNotNeedAnExperiment(sampleIdentifier, dataSets);
        }
        if (hasDatasets(sample) && sample.getSpace() == null)
        {
            throw UserFailureException.fromTemplate("Cannot detach the sample '%s' from the space "
                    + "because there are already datasets attached to the sample.",
                    sample.getIdentifier());
        }
        if (experiment != null
                && (sample.getSpace() == null || experiment.getProject().getSpace()
                        .equals(sample.getSpace()) == false))
        {
            throw new UserFailureException("Sample space must be the same as experiment space. "
                    + "Shared samples cannot be attached to experiments. Sample: "
                    + sample.getIdentifier() + ", Experiment: " + experiment.getIdentifier());
        }
    }

    protected void checkParentBusinessRules(SamplePE sample)
    {
        SampleGenericBusinessRules.assertValidParents(sample);
        SampleGenericBusinessRules.assertValidChildren(sample);
    }

    protected void checkContainerBusinessRules(SamplePE sample)
    {
        SampleGenericBusinessRules.assertValidContainer(sample);
        SampleGenericBusinessRules.assertValidComponents(sample);
    }

    private boolean hasDatasets(SamplePE sample)
    {
        // If we just added new data sets in this BO, they won't have data sets, so no need to
        // check.
        return (onlyNewSamples == false) && hasDatasets2(getDataDAO(), sample);
    }

    protected boolean updateSpace(SamplePE sample, SampleIdentifier sampleOwnerIdentifier,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCacheOrNull)
    {
        if (sampleOwnerIdentifier != null)
        {
            final SampleOwner sampleOwner =
                    getSampleOwner(sampleOwnerCacheOrNull, sampleOwnerIdentifier);
            SpacePE space = sampleOwner.tryGetSpace();
            if (space == sample.getSpace() || (space != null && space.equals(sample.getSpace())))
            {
                // not a real update
                return false;
            }

            if (space == null)
            {
                relationshipService.shareSample(session, sample);
            } else if (sample.getSpace() == null)
            {
                relationshipService.unshareSample(session, sample, space);
            } else
            {
                relationshipService.assignSampleToSpace(session, sample, space);
            }
            return true;
        }

        return false;
    }

    protected void updateExperiment(SamplePE sample, ExperimentIdentifier expIdentifierOrNull,
            Map<String, ExperimentPE> experimentCacheOrNull)
    {
        if (sample.getExperiment() == null && expIdentifierOrNull == null)
        {
            return;
        }
        ExperimentPE newExperiment =
                expIdentifierOrNull == null ? null : tryFindExperiment(experimentCacheOrNull, expIdentifierOrNull.toString(), null);
        if (EntityHelper.equalEntities(newExperiment, sample.getExperiment()))
        {
            return;
        }
        ensureExperimentIsValid(expIdentifierOrNull, newExperiment, sample);
        ensureSampleAttachableToExperiment(sample, newExperiment);

        assignSampleAndRelatedDataSetsToExperiment(sample, newExperiment);
    }

    protected void updateProject(SamplePE sample, ProjectIdentifier projectIdentifierOrNull,
            Map<String, ProjectPE> projectCache)
    {
        if (SamplePE.projectSamplesEnabled == false || (sample.getProject() == null && projectIdentifierOrNull == null))
        {
            return;
        }
        ProjectPE newProject = tryFindProject(projectCache, projectIdentifierOrNull);
        if (EntityHelper.equalEntities(newProject, sample.getProject()) == false)
        {
            ensureSampleAttachableToProject(sample, newProject);
            assignSampleToProject(sample, newProject);
        }
    }

    private void ensureSampleAttachableToProject(SamplePE sample, ProjectPE project)
    {
        if (sample.getSpace() == null && project != null)
        {
            throw UserFailureException.fromTemplate(
                    "It is not allowed to connect a shared sample '%s' to a project.",
                    sample.getIdentifier());
        }
    }

    private void ensureSampleAttachableToExperiment(SamplePE sample, ExperimentPE newExperiment)
    {
        if (sample.getSpace() == null && newExperiment != null)
        {
            throw UserFailureException.fromTemplate(
                    "It is not allowed to connect a shared sample '%s' to the experiment.",
                    sample.getIdentifier());
        }
    }

    private void ensureExperimentIsValid(ExperimentIdentifier identOrNull,
            ExperimentPE experimentOrNull, SamplePE sample)
    {
        if (experimentOrNull != null && experimentOrNull.getDeletion() != null)
        {
            throw UserFailureException.fromTemplate(
                    "The sample '%s' cannot be assigned to the experiment '%s' "
                            + "because the experiment has been deleted.",
                    sample.getSampleIdentifier(), identOrNull);
        }
    }

    /**
     * Throws {@link UserFailureException} if adding specified parents to this data set will create a cycle in data set relationships.
     */
    protected void validateRelationshipGraph(Collection<SamplePE> parents, TechId relationship,
            SamplePE sample)
    {
        // DFS from new parents that are to be added to this business object going in direction
        // of parent relationship until:
        // - all related ancestors are visited == graph has no cycles
        // - we get to this business object == cycle is found
        // NOTE: The assumption is that there were no cycles in the graph of relationship before.
        // This algorithm will not find cycles that don't include this business object,
        // although such cycles shouldn't cause it to loop forever.

        // Algorithm operates only on data set ids to make it perform better
        // - there is no need to join DB tables.
        // To be able to inform user about the exact data set that cannot be connected as a parent
        // we need start seeking cycles starting from each parent to be added separately. Otherwise
        // we would need to get invoke more queries to DB (not going layer by layer of graph depth
        // per query) or use BFS instead (which would also be slower in a general case).

        for (SamplePE parentToAdd : parents)
        {
            validateRelationshipGraph(parentToAdd, relationship, sample);
        }
    }

    private void validateRelationshipGraph(SamplePE parentToAdd, TechId relationship,
            SamplePE sample)
    {
        final TechId sampleId = TechId.create(sample);
        final Set<TechId> visited = new HashSet<TechId>();
        Set<TechId> toVisit = new HashSet<TechId>();
        toVisit.add(TechId.create(parentToAdd));
        while (toVisit.isEmpty() == false)
        {
            if (toVisit.contains(sampleId))
            {
                throw UserFailureException.fromTemplate(
                        "Sample '%s' is an ancestor of Sample '%s' "
                                + "and cannot be at the same time set as its child.",
                        sample.getIdentifier(), parentToAdd.getIdentifier());
            } else
            {
                final Set<TechId> nextToVisit =
                        getSampleDAO().listSampleIdsByChildrenIds(toVisit, relationship);
                visited.addAll(toVisit);
                nextToVisit.removeAll(visited);
                toVisit = nextToVisit;
            }
        }
    }

    protected List<SamplePE> listSamplesByIdentifiers(
            final List<SampleIdentifier> sampleIdentifiers,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache)
    {
        assert sampleIdentifiers != null : "Sample identifiers unspecified.";

        Map<SampleOwnerWithContainer, List<String>> samplesByOwner =
                new HashMap<SampleOwnerWithContainer, List<String>>();
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers)
        {
            final SampleOwner sampleOwner = getSampleOwner(sampleOwnerCache, sampleIdentifier);
            final String containerCodeOrNull = sampleIdentifier.tryGetContainerCode();
            final SampleOwnerWithContainer owner =
                    new SampleOwnerWithContainer(sampleOwner, containerCodeOrNull);

            List<String> ownerSamples = samplesByOwner.get(owner);
            if (ownerSamples == null)
            {
                ownerSamples = new ArrayList<String>();
                samplesByOwner.put(owner, ownerSamples);
            }
            ownerSamples.add(sampleIdentifier.getSampleSubCode());
        }

        final ISampleDAO sampleDAO = getSampleDAO();
        final List<SamplePE> results = new ArrayList<SamplePE>();
        for (Entry<SampleOwnerWithContainer, List<String>> entry : samplesByOwner.entrySet())
        {
            final SampleOwnerWithContainer owner = entry.getKey();
            final List<String> sampleCodes = entry.getValue();

            final SampleOwner sampleOwner = owner.getSampleOwner();
            final String containerCodeOrNull = owner.getContainerCodeOrNull();

            List<SamplePE> samples = null;
            if (sampleOwner.isDatabaseInstanceLevel())
            {
                samples =
                        sampleDAO.listByCodesAndDatabaseInstance(sampleCodes, containerCodeOrNull);
            } else if (sampleOwner.isProjectLevel())
            {
                ProjectPE project = sampleOwner.tryGetProject();
                samples = sampleDAO.listByCodesAndProject(sampleCodes, containerCodeOrNull, project);
            } else
            {
                assert sampleOwner.isSpaceLevel() : "Must be of space level.";
                samples =
                        sampleDAO.listByCodesAndSpace(sampleCodes, containerCodeOrNull,
                                sampleOwner.tryGetSpace());
            }
            results.addAll(samples);
        }
        return results;
    }

    protected List<SamplePE> listSamplesByTechIds(final List<TechId> sampleTechIds)
    {
        assert sampleTechIds != null : "Sample identifiers unspecified.";

        final ISampleDAO sampleDAO = getSampleDAO();
        List<Long> ids = new ArrayList<Long>();
        for (TechId sampleTechId : sampleTechIds)
        {
            ids.add(sampleTechId.getId());
        }
        final List<SamplePE> results = new ArrayList<SamplePE>();
        results.addAll(sampleDAO.listByIDs(ids));
        return results;
    }

    /** Helper class encapsulating {@link SampleOwner} and code of container of a sample. */
    private static class SampleOwnerWithContainer
    {
        private final SampleOwner sampleOwner;

        private final String containerCodeOrNull;

        public SampleOwnerWithContainer(SampleOwner sampleOwner, String containerCodeOrNull)
        {
            assert sampleOwner != null;
            this.sampleOwner = sampleOwner;
            this.containerCodeOrNull = containerCodeOrNull;
        }

        public SampleOwner getSampleOwner()
        {
            return sampleOwner;
        }

        public String getContainerCodeOrNull()
        {
            return containerCodeOrNull;
        }

        //
        // Object
        //

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result =
                    prime * result
                            + ((containerCodeOrNull == null) ? 0 : containerCodeOrNull.hashCode());
            result = prime * result + sampleOwner.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof SampleOwnerWithContainer))
            {
                return false;
            }
            SampleOwnerWithContainer other = (SampleOwnerWithContainer) obj;
            if (containerCodeOrNull == null)
            {
                if (other.containerCodeOrNull != null)
                {
                    return false;
                }
            } else if (!containerCodeOrNull.equals(other.containerCodeOrNull))
            {
                return false;
            }
            if (!sampleOwner.equals(other.sampleOwner))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "SampleOwnerWithContainer [sampleOwner=" + sampleOwner
                    + ", containerCodeOrNull=" + containerCodeOrNull + "]";
        }

    }

    protected void assertInstanceSampleCreationAllowed(List<? extends NewSample> samples)
    {
        List<NewSample> instanceSamples = new ArrayList<NewSample>();

        for (NewSample sample : samples)
        {
            SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sample);

            if (sampleIdentifier.isDatabaseInstanceLevel())
            {
                instanceSamples.add(sample);
            }
        }

        if (instanceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertInstanceSampleCreationAllowed(session, instanceSamples);
        }
    }

    protected void assertInstanceSampleUpdateAllowed(List<? extends SamplePE> samples)
    {
        List<SampleIdentifier> instanceSamples = new ArrayList<SampleIdentifier>();

        for (SamplePE sample : samples)
        {
            SampleIdentifier sampleIdentifier = sample.getSampleIdentifier();

            if (sampleIdentifier.isDatabaseInstanceLevel())
            {
                instanceSamples.add(sampleIdentifier);
            }
        }

        if (instanceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertInstanceSampleUpdateAllowed(session, instanceSamples);
        }
    }
}
