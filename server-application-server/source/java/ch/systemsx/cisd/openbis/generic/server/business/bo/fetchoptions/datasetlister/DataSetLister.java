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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStoreURLForDataSets;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSetUrl;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
public class DataSetLister implements IDataSetLister
{

    private IDataSetListingQuery query;

    private IRelationshipTypeDAO relationshipTypeDAO;

    public DataSetLister(IAuthorizationDAOFactory daoFactory, PersonPE person)
    {
        this(QueryTool.getManagedQuery(IDataSetListingQuery.class), daoFactory, person);
    }

    public DataSetLister(IDataSetListingQuery query, IAuthorizationDAOFactory daoFactory, PersonPE person)
    {
        relationshipTypeDAO = daoFactory.getRelationshipTypeDAO();
        if (query == null)
        {
            throw new IllegalArgumentException("Query was null");
        }
        if (person == null)
        {
            throw new IllegalArgumentException("Person was null");
        }
        this.query = query;
    }

    @Override
    public List<DataSet> getDataSetMetaData(List<String> dataSetCodes,
            DataSetFetchOptions dataSetFetchOptions)
    {
        if (dataSetCodes == null)
        {
            throw new IllegalArgumentException("DataSetCodes were null");
        }
        if (dataSetFetchOptions == null)
        {
            throw new IllegalArgumentException("DataSetFetchOptions were null");
        }
        if (false == dataSetFetchOptions.isSubsetOf(DataSetFetchOption.BASIC,
                DataSetFetchOption.PARENTS, DataSetFetchOption.CHILDREN,
                DataSetFetchOption.METAPROJECTS))
        {
            throw new IllegalArgumentException("Currently only " + DataSetFetchOption.BASIC + ","
                    + DataSetFetchOption.PARENTS + ", and " + DataSetFetchOption.CHILDREN + " and "
                    + DataSetFetchOption.METAPROJECTS
                    + " fetch options are supported by this method");
        }

        String[] dataSetCodesArray = dataSetCodes.toArray(new String[dataSetCodes.size()]);
        List<DataSetRecord> dataSetRecords = query.getDataSetMetaData(dataSetCodesArray);

        // create data set initializers
        List<DataSetInitializer> dataSetInitializers =
                createDataSetInitializers(dataSetRecords, dataSetCodesArray);
        injectContainerStubs(dataSetInitializers);

        // fill in parents
        if (dataSetFetchOptions.isSupersetOf(DataSetFetchOption.PARENTS))
        {
            enrichDataSetInitializersWithParents(dataSetInitializers, dataSetCodesArray);
        }

        // fill in children
        if (dataSetFetchOptions.isSupersetOf(DataSetFetchOption.CHILDREN))
        {
            enrichDataSetInitializersWithChildren(dataSetInitializers, dataSetCodesArray);
        }

        // create data sets
        return createDataSets(dataSetInitializers, dataSetFetchOptions);
    }

    private void injectContainerStubs(List<DataSetInitializer> dataSetInitializers)
    {
        LongSet ids = new LongOpenHashSet();
        for (DataSetInitializer dataSetInitializer : dataSetInitializers)
        {
            ids.add(dataSetInitializer.getId());
        }
        Map<Long, List<DataSetRecord>> containersByComponents = new HashMap<Long, List<DataSetRecord>>();
        List<DataSetRecord> dataSetsWithContainer =
                query.getContainers(ids, RelationshipUtils.getContainerComponentRelationshipType(relationshipTypeDAO).getId());
        for (DataSetRecord dataSetRecord : dataSetsWithContainer)
        {
            List<DataSetRecord> containers = containersByComponents.get(dataSetRecord.ds_id);
            if (containers == null)
            {
                containers = new ArrayList<DataSetRecord>();
                containersByComponents.put(dataSetRecord.ds_id, containers);
            }
            containers.add(dataSetRecord);
        }
        for (DataSetInitializer dataSetInitializer : dataSetInitializers)
        {
            List<DataSetRecord> containers = containersByComponents.get(dataSetInitializer.getId());
            if (containers != null)
            {
                List<DataSet> containerDataSets = new ArrayList<DataSet>();
                for (DataSetRecord container : containers)
                {
                    DataSetInitializer containerInitializer = new DataSetInitializer();
                    containerInitializer.setStub(true);
                    containerInitializer.setId(container.ctnr_id);
                    containerInitializer.setCode(container.ctnr_code);
                    containerDataSets.add(new DataSet(containerInitializer));
                }
                dataSetInitializer.setContainerDataSets(containerDataSets);
            }
        }
    }

    private List<DataSet> createDataSets(List<DataSetInitializer> dataSetInitializers,
            DataSetFetchOptions dataSetFetchOptions)
    {
        List<DataSet> dataSets = new ArrayList<DataSet>(dataSetInitializers.size());
        for (DataSetInitializer dataSetInitializer : dataSetInitializers)
        {
            DataSet dataSet = new DataSet(dataSetInitializer);
            dataSet.setFetchOptions(dataSetFetchOptions);
            dataSets.add(dataSet);
        }
        return dataSets;
    }

    private List<DataSetInitializer> createDataSetInitializers(List<DataSetRecord> dataSetRecords,
            String[] dataSetCodes)
    {
        if (dataSetRecords != null)
        {
            // put results in code to initializer map
            Map<String, DataSetInitializer> dataSetMap = new HashMap<String, DataSetInitializer>();
            for (DataSetRecord dataSetRecord : dataSetRecords)
            {
                DataSetInitializer dataSet = createDataSetInitializer(dataSetRecord);
                dataSetMap.put(dataSet.getCode(), dataSet);
            }

            // put results in the same order as the list of data set codes
            List<DataSetInitializer> dataSetList =
                    new ArrayList<DataSetInitializer>(dataSetMap.size());
            for (String dataSetCode : dataSetCodes)
            {
                DataSetInitializer dataSet = dataSetMap.get(dataSetCode);
                if (dataSet == null)
                {
                    throw new UserFailureException("Unknown data set " + dataSetCode);
                } else
                {
                    dataSetList.add(dataSet);
                }
            }
            return dataSetList;

        } else
        {
            return Collections.emptyList();
        }
    }

    private DataSetInitializer createDataSetInitializer(DataSetRecord dataSet)
    {
        ExperimentIdentifier experimentIdentifier = null;
        if (dataSet.ex_code != null)
        {
            experimentIdentifier = new ExperimentIdentifier(dataSet.spe_code, dataSet.pre_code, dataSet.ex_code);
        }

        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setId(dataSet.ds_id);
        initializer.setCode(dataSet.ds_code);
        initializer.setDataSetTypeCode(dataSet.dt_code);
        initializer.setContainerDataSet(DataSetKind.CONTAINER.name().equals(
                dataSet.ds_data_set_kind));
        initializer.setStorageConfirmed(dataSet.ed_sc == null || dataSet.ed_sc); // if there's no
                                                                                 // external data
                                                                                 // than the storage
                                                                                 // is considered
                                                                                 // confirmed
        initializer.setStub(false);
        if (initializer.isContainerDataSet() == false)
        {
            initializer.setPostRegistered(dataSet.ds_is_post_registered);
        }
        initializer.setLinkDataSet(DataSetKind.LINK.name().equals(dataSet.ds_data_set_kind));
        if (initializer.isLinkDataSet())
        {
            initializer.setExternalDataSetCode(dataSet.ld_external_code);
            LinkDataSetUrl linkDataSetUrl =
                    new LinkDataSetUrl(dataSet.ld_external_code, dataSet.edms_address);
            initializer.setExternalDataSetLink(linkDataSetUrl.toString());

            DatabaseInstance db = new DatabaseInstance();
            db.setId(dataSet.die_id);
            db.setCode(dataSet.die_code);
            db.setUuid(dataSet.die_uuid);

            ExternalDataManagementSystem edms = new ExternalDataManagementSystem();
            edms.setId(dataSet.edms_id);
            edms.setCode(dataSet.edms_code);
            edms.setLabel(dataSet.edms_label);
            edms.setUrlTemplate(dataSet.edms_address);
            edms.setAddress(dataSet.edms_address);
            edms.setDatabaseInstance(db);
            edms.setAddressType(ExternalDataManagementSystemType.valueOf(dataSet.edms_address_type));
            edms.setOpenBIS(ExternalDataManagementSystemType.OPENBIS.equals(edms.getAddressType()));
            initializer.setExternalDataManagementSystem(edms);
        }
        initializer.setRegistrationDetails(createDataSetRegistrationDetails(dataSet));
        if (experimentIdentifier != null)
        {
            initializer.setExperimentIdentifier(experimentIdentifier.toString());
        }

        if (dataSet.sa_identifier != null)
        {
            initializer.setSampleIdentifierOrNull(dataSet.sa_identifier);
        }

        return initializer;
    }

    private EntityRegistrationDetails createDataSetRegistrationDetails(DataSetRecord dataSet)
    {
        EntityRegistrationDetailsInitializer initializer =
                new EntityRegistrationDetailsInitializer();
        initializer.setUserId(dataSet.pe_user_id);
        initializer.setFirstName(dataSet.pe_first_name);
        initializer.setLastName(dataSet.pe_last_name);
        initializer.setEmail(dataSet.pe_email);
        initializer.setModifierUserId(dataSet.mod_user_id);
        initializer.setModifierFirstName(dataSet.mod_first_name);
        initializer.setModifierLastName(dataSet.mod_last_name);
        initializer.setModifierEmail(dataSet.mod_email);
        initializer.setRegistrationDate(dataSet.ds_registration_timestamp);
        initializer.setModificationDate(dataSet.ds_modification_timestamp);
        initializer.setAccessTimestamp(dataSet.access_timestamp);
        return new EntityRegistrationDetails(initializer);
    }

    private void enrichDataSetInitializersWithParents(List<DataSetInitializer> dataSetInitializers,
            String[] dataSetCodes)
    {
        Long relationshipTypeId = getParentChildRelationshipType();
        List<DataSetRelationRecord> dataSetRelations = query.getDataSetParentsCodes(dataSetCodes, relationshipTypeId);

        if (dataSetRelations != null && !dataSetRelations.isEmpty())
        {
            Map<String, List<String>> childCodeToParentCodesMap =
                    new HashMap<String, List<String>>();

            for (DataSetRelationRecord dataSetRelation : dataSetRelations)
            {
                List<String> parentCodes = childCodeToParentCodesMap.get(dataSetRelation.dc_code);
                if (parentCodes == null)
                {
                    parentCodes = new LinkedList<String>();
                    childCodeToParentCodesMap.put(dataSetRelation.dc_code, parentCodes);
                }
                parentCodes.add(dataSetRelation.dp_code);
            }

            for (DataSetInitializer dataSetInitializer : dataSetInitializers)
            {
                List<String> parentCodes =
                        childCodeToParentCodesMap.get(dataSetInitializer.getCode());
                dataSetInitializer.setParentCodes(parentCodes);
            }
        }
    }

    private void enrichDataSetInitializersWithChildren(
            List<DataSetInitializer> dataSetInitializers, String[] dataSetCodes)
    {
        Long relationshipTypeId = getParentChildRelationshipType();
        List<DataSetRelationRecord> dataSetRelations = query.getDataSetChildrenCodes(dataSetCodes, relationshipTypeId);

        if (dataSetRelations != null && !dataSetRelations.isEmpty())
        {
            Map<String, List<String>> parentCodeToChildrenCodesMap =
                    new HashMap<String, List<String>>();

            for (DataSetRelationRecord dataSetRelation : dataSetRelations)
            {
                List<String> childrenCodes =
                        parentCodeToChildrenCodesMap.get(dataSetRelation.dp_code);
                if (childrenCodes == null)
                {
                    childrenCodes = new LinkedList<String>();
                    parentCodeToChildrenCodesMap.put(dataSetRelation.dp_code, childrenCodes);
                }
                childrenCodes.add(dataSetRelation.dc_code);
            }

            for (DataSetInitializer dataSetInitializer : dataSetInitializers)
            {
                List<String> childrenCodes =
                        parentCodeToChildrenCodesMap.get(dataSetInitializer.getCode());
                dataSetInitializer.setChildrenCodes(childrenCodes);
            }
        }
    }

    private Long getParentChildRelationshipType()
    {
        return RelationshipUtils.getParentChildRelationshipType(relationshipTypeDAO).getId();
    }

    @Override
    public List<DataStoreURLForDataSets> getDataStoreDownloadURLs(List<String> dataSetCodes)
    {
        final String[] dataSetCodesArray = dataSetCodes.toArray(new String[dataSetCodes.size()]);
        final List<DataSetDownloadRecord> records = query.getDownloadURLs(dataSetCodesArray);
        final List<DataStoreURLForDataSets> result =
                new ArrayList<DataStoreURLForDataSets>(records.size());
        for (DataSetDownloadRecord r : records)
        {
            final String[] dataSetCodeArray =
                    StringUtils.split(r.data_set_codes.substring(1, r.data_set_codes.length() - 1),
                            ',');
            result.add(new DataStoreURLForDataSets(r.url, dataSetCodeArray));
        }
        return result;
    }

    @Override
    public List<DataStoreURLForDataSets> getDataStoreRemoteURLs(List<String> dataSetCodes)
    {
        final String[] dataSetCodesArray = dataSetCodes.toArray(new String[dataSetCodes.size()]);
        final List<DataSetDownloadRecord> records = query.getRemoteURLs(dataSetCodesArray);
        final List<DataStoreURLForDataSets> result =
                new ArrayList<DataStoreURLForDataSets>(records.size());
        for (DataSetDownloadRecord r : records)
        {
            final String[] dataSetCodeArray =
                    StringUtils.split(r.data_set_codes.substring(1, r.data_set_codes.length() - 1),
                            ',');
            result.add(new DataStoreURLForDataSets(r.url, dataSetCodeArray));
        }
        return result;
    }

}
