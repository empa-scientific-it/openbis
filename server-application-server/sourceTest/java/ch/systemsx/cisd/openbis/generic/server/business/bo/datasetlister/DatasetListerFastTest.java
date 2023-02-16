/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.WrappingDataIterator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
{ IDatasetListingQuery.class })
public class DatasetListerFastTest extends AssertJUnit
{
    private static final Comparator<IDatasetLocationNode> DATA_SET_LOCATION_NODE_COMPARATOR = new Comparator<IDatasetLocationNode>()
        {
            @Override
            public int compare(IDatasetLocationNode n1, IDatasetLocationNode n2)
            {
                return n1.getLocation().getDataSetCode().compareTo(n2.getLocation().getDataSetCode());
            }
        };

    private static final String DSS_URL = "dss-url";

    private static final String DSS_CODE = "DSS";

    private Mockery context;

    private IDatasetListingQuery query;

    private DatasetLister datasetLister;

    private IRelationshipTypeDAO relationshipTypeDAO;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        query = context.mock(IDatasetListingQuery.class);
        IEntityPropertiesEnricher propertiesEnricher =
                context.mock(IEntityPropertiesEnricher.class);
        relationshipTypeDAO = context.mock(IRelationshipTypeDAO.class);
        datasetLister =
                new DatasetLister(42L, new DatabaseInstance(), query, propertiesEnricher, null, null,
                        relationshipTypeDAO, null, null);
    }

    @AfterMethod
    public void afterMethod(Method m)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(m.getName() + "() : ", t);
        }
    }

    @Test
    public void testListLocationsByDatasetCode()
    {
        context.checking(new Expectations()
            {
                {
                    one(relationshipTypeDAO).tryFindRelationshipTypeByCode(
                            BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
                    RelationshipTypePE type = new RelationshipTypePE();
                    type.setId(137L);
                    will(returnValue(type));
                    one(query).listLocationsByDatasetCode("ds-1", 137L);
                    will(returnValue(new WrappingDataIterator<DatasetLocationNodeRecord>(Arrays
                            .<DatasetLocationNodeRecord> asList(
                                    location(2L, "ds-c1", "a/b/c/1", 1L, 0),
                                    location(3L, "ds-c2", "a/b/c/2", 1L, 1),
                                    location(1L, "ds-1", null, null, null)))));
                }
            });

        IDatasetLocationNode location = datasetLister.listLocationsByDatasetCode("ds-1");

        assertEquals("ds-1", location.getLocation().getDataSetCode());
        assertEquals(null, location.getLocation().getDataSetLocation());
        assertEquals(DSS_CODE, location.getLocation().getDataStoreCode());
        assertEquals(DSS_URL, location.getLocation().getDataStoreUrl());
        assertEquals(true, location.isContainer());
        List<IDatasetLocationNode> components =
                new ArrayList<IDatasetLocationNode>(location.getComponents());
        Collections.sort(components, DATA_SET_LOCATION_NODE_COMPARATOR);
        assertEquals("ds-c1", components.get(0).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(0), components.get(0).getLocation().getOrderInContainer("ds-1"));
        assertEquals("a/b/c/1", components.get(0).getLocation().getDataSetLocation());
        assertEquals("ds-c2", components.get(1).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(1), components.get(1).getLocation().getOrderInContainer("ds-1"));
        assertEquals("a/b/c/2", components.get(1).getLocation().getDataSetLocation());
    }

    @Test
    public void testListLocationsOfContainerComponentDAG()
    {
        context.checking(new Expectations()
            {
                {
                    one(relationshipTypeDAO).tryFindRelationshipTypeByCode(
                            BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
                    RelationshipTypePE type = new RelationshipTypePE();
                    type.setId(137L);
                    will(returnValue(type));
                    one(query).listLocationsByDatasetCode("-2306", 137L);
                    will(returnValue(new WrappingDataIterator<DatasetLocationNodeRecord>(Arrays
                            .<DatasetLocationNodeRecord> asList(
                                    location(999L, "-2306", null, null, null),
                                    location(998L, "-2304", null, 999L, 0),
                                    location(997L, "-2303", "a/b/c/2303", 999L, 1),
                                    location(995L, "-2305", "a/b/c/2305", 999L, 2),
                                    location(996L, "-2302", "a/b/c/2302", 998L, 1),
                                    location(997L, "-2303", "a/b/c/2303", 998L, 2)))));
                }
            });

        IDatasetLocationNode location = datasetLister.listLocationsByDatasetCode("-2306");

        List<IDatasetLocationNode> components =
                new ArrayList<IDatasetLocationNode>(location.getComponents());
        Collections.sort(components, DATA_SET_LOCATION_NODE_COMPARATOR);
        assertEquals("-2303", components.get(0).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(1), components.get(0).getLocation().getOrderInContainer("-2306"));
        assertEquals("a/b/c/2303", components.get(0).getLocation().getDataSetLocation());
        assertEquals("[]", components.get(0).getComponents().toString());
        assertEquals("-2304", components.get(1).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(0), components.get(1).getLocation().getOrderInContainer("-2306"));
        assertEquals(null, components.get(1).getLocation().getDataSetLocation());
        List<IDatasetLocationNode> subComponents =
                new ArrayList<IDatasetLocationNode>(components.get(1).getComponents());
        Collections.sort(subComponents, DATA_SET_LOCATION_NODE_COMPARATOR);
        assertEquals("-2302", subComponents.get(0).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(1), subComponents.get(0).getLocation().getOrderInContainer("-2304"));
        assertEquals("a/b/c/2302", subComponents.get(0).getLocation().getDataSetLocation());
        assertEquals("-2303", subComponents.get(1).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(2), subComponents.get(1).getLocation().getOrderInContainer("-2304"));
        assertEquals("a/b/c/2303", subComponents.get(1).getLocation().getDataSetLocation());
        assertEquals("-2305", components.get(2).getLocation().getDataSetCode());
        assertEquals(Integer.valueOf(2), components.get(2).getLocation().getOrderInContainer("-2306"));
        assertEquals("a/b/c/2305", components.get(2).getLocation().getDataSetLocation());
        assertEquals("[]", components.get(2).getComponents().toString());

    }

    private DatasetLocationNodeRecord location(long id, String code, String location,
            Long containerID, Integer ordinal)
    {
        DatasetLocationNodeRecord record = new DatasetLocationNodeRecord();
        record.id = id;
        record.code = code;
        record.data_store_code = DSS_CODE;
        record.data_store_url = DSS_URL;
        record.location = location;
        record.container_id = containerID;
        record.ordinal = ordinal;
        return record;
    }

}
