/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataSetBusinessObject extends AbstractSampleIdentifierBusinessObject
{

    private IServiceConversationClientManagerLocal conversationClient;

    public AbstractDataSetBusinessObject(IDAOFactory daoFactory, Session session,
            IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, EntityKind.DATA_SET, entityInformationProvider, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
        this.conversationClient = conversationClient;
    }

    public AbstractDataSetBusinessObject(IDAOFactory daoFactory, Session session,
            IEntityPropertiesConverter entityPropertiesConverter,
            IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, entityPropertiesConverter, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
        this.conversationClient = conversationClient;
    }

    protected void enrichWithParentsAndExperiment(DataPE dataPE)
    {
        HibernateUtils.initialize(dataPE.getParents());
        HibernateUtils.initialize(dataPE.getExperiment());
    }

    protected void enrichWithChildren(DataPE dataPE)
    {
        HibernateUtils.initialize(dataPE.getChildRelationships());
    }

    protected void checkPropertiesBusinessRules(DataPE data)
    {
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
    }

    protected void updateSample(DataPE data, SampleIdentifier sampleIdentifierOrNull)
    {
        assert sampleIdentifierOrNull != null;
        SamplePE newSample = getSampleByIdentifier(sampleIdentifierOrNull);
        assignDataSetToSampleAndExperiment(data, newSample, newSample.getExperiment());
    }

    protected void updateExperiment(DataPE data, ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null;
        assignDataSetToSampleAndExperiment(data, null, getExperimentByIdentifier(experimentIdentifier));
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(
                        identifier.getSpaceCode(), identifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate("Unkown %s because of unkown project: %s",
                    getExperimentText(), identifier);
        }
        return getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
    }

    protected void setContainedDataSets(final DataPE container,
            final List<String> modifiedContainedCodesOrNull)
    {
        if (modifiedContainedCodesOrNull == null)
        {
            return; // contained are not changed
        }
        final Set<DataPE> newComponents = findDataSetsByCodes(asSet(modifiedContainedCodesOrNull));

        List<DataPE> oldComponents = container.getContainedDataSets();
        for (DataPE dataPE : oldComponents)
        {
            if (newComponents.remove(dataPE) == false)
            {
                relationshipService.removeDataSetFromContainer(session, dataPE, container);
            }
        }

        for (DataPE dataPE : newComponents)
        {
            relationshipService.assignDataSetToContainer(session, dataPE, container);
            validateContainerContainedRelationshipGraph(container, dataPE);
        }
        Map<String, DataSetRelationshipPE> relationShipsByCode = new HashMap<String, DataSetRelationshipPE>();
        List<DataSetRelationshipPE> childRelationships = RelationshipUtils.getContainerComponentRelationships(container.getChildRelationships());
        for (DataSetRelationshipPE childRelationship : childRelationships)
        {
            DataPE childDataSet = childRelationship.getChildDataSet();
            relationShipsByCode.put(childDataSet.getCode(), childRelationship);
        }
        for (int i = 0; i < modifiedContainedCodesOrNull.size(); i++)
        {
            String componentCode = modifiedContainedCodesOrNull.get(i);
            DataSetRelationshipPE dataSetRelationship = relationShipsByCode.get(componentCode);
            dataSetRelationship.setOrdinal(i);
        }
    }

    protected void setParents(final DataPE childPE,
            final List<String> modifiedParentDatasetCodesOrNull)
    {
        if (modifiedParentDatasetCodesOrNull == null)
        {
            return; // parents were not changed
        } else
        {
            // quick check for direct cycle
            for (String parentCode : modifiedParentDatasetCodesOrNull)
            {
                if (parentCode.equals(childPE.getCode()))
                {
                    throw new UserFailureException("Data set '" + childPE.getCode()
                            + "' can not be its own parent.");
                }
            }

            final Set<DataPE> parentPEs =
                    findDataSetsByCodes(asSet(modifiedParentDatasetCodesOrNull));
            replaceParents(childPE, parentPEs, true);
        }
    }

    protected void replaceParents(DataPE child, Set<DataPE> newParents, boolean validate)
    {
        // quick check for deletions
        for (DataPE parent : newParents)
        {
            checkParentDeletion(parent, child.getCode());
        }

        // get old parents
        List<DataPE> oldParents = new ArrayList<DataPE>();
        for (DataSetRelationshipPE oldParentRelation : RelationshipUtils.getParentChildRelationships(child.getParentRelationships()))
        {
            oldParents.add(oldParentRelation.getParentDataSet());
        }

        Set<DataPE> parentsToRemove = new HashSet<DataPE>();
        Set<DataPE> parentsToAdd = new HashSet<DataPE>();

        findToBeRemovedOrAdded(newParents, oldParents, parentsToRemove, parentsToAdd);

        // check cycles
        if (validate)
        {
            validateParentsRelationshipGraph(child, parentsToAdd);
        }

        // remove parents
        for (DataPE parentToRemove : parentsToRemove)
        {
            relationshipService.removeParentFromDataSet(session, child, parentToRemove);
        }

        // add parents
        for (DataPE parentToAdd : parentsToAdd)
        {
            relationshipService.addParentToDataSet(session, child, parentToAdd);
        }
    }

    protected void findToBeRemovedOrAdded(Collection<DataPE> newDataSets, Collection<DataPE> oldDataSets, Set<DataPE> toBeRemoved,
            Set<DataPE> toBeAdded)
    {
        // find data sets to be added (exist in newDataSets but do not exist in oldDataSets)
        for (DataPE newDataSet : newDataSets)
        {
            if (oldDataSets.contains(newDataSet) == false)
            {
                toBeAdded.add(newDataSet);
            }
        }
        // find data sets to be removed (exist in oldDataSets but do not exist in newDataSets)
        for (DataPE oldDataSet : oldDataSets)
        {
            if (newDataSets.contains(oldDataSet) == false)
            {
                toBeRemoved.add(oldDataSet);
            }
        }
    }

    private void checkParentDeletion(final DataPE parentPE, final String child)
    {
        if (parentPE.getDeletion() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Data set '%s' has been deleted and can't become a parent of data set '%s'.",
                    parentPE.getIdentifier(), child);
        }
    }

    /**
     * Throws {@link UserFailureException} if adding specified parents to this data set will create a cycle in data set relationships.
     */
    protected void validateParentsRelationshipGraph(DataPE data, Collection<DataPE> parentsToAdd)
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
        for (DataPE parentToAdd : parentsToAdd)
        {
            validateParentsRelationshipGraph(data, parentToAdd);
        }
    }

    private void validateParentsRelationshipGraph(DataPE data, DataPE parentToAdd)
    {
        final TechId updatedDataSetId = TechId.create(data);
        final Set<TechId> visited = new HashSet<TechId>();
        Set<TechId> toVisit = new HashSet<TechId>();
        toVisit.add(TechId.create(parentToAdd));
        while (toVisit.isEmpty() == false)
        {
            if (toVisit.contains(updatedDataSetId))
            {
                throw UserFailureException.fromTemplate(
                        "Data Set '%s' is an ancestor of Data Set '%s' "
                                + "and cannot be at the same time set as its child.",
                        data.getCode(), parentToAdd.getCode());
            } else
            {
                final Set<TechId> nextToVisit = findParentIds(toVisit);
                visited.addAll(toVisit);
                nextToVisit.removeAll(visited);
                toVisit = nextToVisit;
            }
        }
    }

    private Set<TechId> findParentIds(Set<TechId> dataSetIds)
    {
        return getDataDAO().findParentIds(dataSetIds, getParentChildRelationshipType().getId());
    }

    protected Set<DataPE> findDataSetsByCodes(Collection<String> codes)
    {
        final IDataDAO dao = getDataDAO();
        final Set<DataPE> dataSets = new LinkedHashSet<DataPE>();
        final List<String> missingDataSetCodes = new ArrayList<String>();
        for (String code : codes)
        {
            DataPE dataSetOrNull = dao.tryToFindDataSetByCode(code);
            if (dataSetOrNull == null)
            {
                missingDataSetCodes.add(code);
            } else
            {
                dataSets.add(dataSetOrNull);
            }
        }
        if (missingDataSetCodes.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Data Sets with following codes do not exist: '%s'.",
                    CollectionUtils.abbreviate(missingDataSetCodes, 10));
        } else
        {
            return dataSets;
        }
    }

    protected void updateContainers(DataPE data, String containerCodes)
    {
        if (containerCodes == null)
        {
            return;
        }
        data.setModificationDate(getTransactionTimeStamp());
        List<String> codes = Arrays.asList(containerCodes.split(" *, *"));
        Map<String, DataPE> newContainers = getDataSets(codes);
        List<DataPE> oldContainers = data.getContainers();
        for (DataPE oldContainer : oldContainers)
        {
            if (newContainers.remove(oldContainer.getCode()) == null)
            {
                relationshipService.removeDataSetFromContainer(session, data, oldContainer);
            }
        }
        for (DataPE newContainer : newContainers.values())
        {
            if (newContainer.isContainer() == false)
            {
                throw UserFailureException.fromTemplate(
                        "Data Set with a following code is not a container: '%s'.", newContainer.getCode());
            }
            relationshipService.assignDataSetToContainer(session, data, newContainer);
            validateContainerContainedRelationshipGraph(newContainer, data);
        }
    }

    private Map<String, DataPE> getDataSets(List<String> codes)
    {
        Set<DataPE> dataSets = findDataSetsByCodes(codes);
        Map<String, DataPE> dataSetsByCode = new HashMap<String, DataPE>();
        for (DataPE dataSet : dataSets)
        {
            dataSetsByCode.put(dataSet.getCode(), dataSet);
        }
        return dataSetsByCode;
    }

    private void validateContainerContainedRelationshipGraph(DataPE container, DataPE contained)
    {
        if (container.getCode().equals(contained.getCode()))
        {
            throw new UserFailureException(
                    "Data set '"
                            + container.getCode()
                            + "' cannot contain itself as a component neither directly nor via subordinate components.");
        }
        Set<DataSetRelationshipPE> parentRelationships = container.getParentRelationships();
        for (DataSetRelationshipPE relationship : RelationshipUtils.getContainerComponentRelationships(parentRelationships))
        {
            validateContainerContainedRelationshipGraph(relationship.getParentDataSet(), contained);
        }
    }

    protected void updateFileFormatType(DataPE data, String fileFormatTypeCode)
    {
        if (data.isExternalData())
        {
            if (fileFormatTypeCode == null)
            {
                throw new UserFailureException("Data set '" + data.getCode()
                        + "' cannot have empty file format.");
            }
            ExternalDataPE externalData = data.tryAsExternalData();
            FileFormatTypePE fileFormatTypeOrNull =
                    getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(fileFormatTypeCode);
            if (fileFormatTypeOrNull == null)
            {
                throw new UserFailureException(String.format("File type '%s' does not exist.",
                        fileFormatTypeCode));
            } else if (equalFileFormatTypes(externalData.getFileFormatType(), fileFormatTypeOrNull) == false)
            {
                externalData.setFileFormatType(fileFormatTypeOrNull);
                RelationshipUtils.updateModificationDateAndModifier(data, session, getTransactionTimeStamp());
            }
        }
    }

    private boolean equalFileFormatTypes(FileFormatTypePE type1, FileFormatTypePE type2)
    {
        return type1 == null ? type1 == type2 : type1.equals(type2);
    }

    protected static Set<String> extractCodes(Collection<DataPE> parents)
    {
        Set<String> codes = new HashSet<String>(parents.size());
        for (DataPE parent : parents)
        {
            codes.add(parent.getCode());
        }
        return codes;
    }

    protected static Set<String> asSet(List<String> objects)
    {
        return new LinkedHashSet<String>(objects); // keep the ordering
    }

    public IRelationshipService getRelationshipService()
    {
        return relationshipService;
    }

    public IServiceConversationClientManagerLocal getConversationClient()
    {
        return conversationClient;
    }
}
