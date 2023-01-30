/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.EntityMetaprojectRelationRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaprojectRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.PropertyRecord;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
public class SampleListerTest extends AssertJUnit
{
    private static final Long RELATION_SHIP_TYPE = 137L;

    private Mockery context;

    private ISampleListingQuery query;

    private SampleLister lister;

    private PersonPE user;

    private IValidator<IIdentifierHolder> filter;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        query = context.mock(ISampleListingQuery.class);
        filter = context.mock(IValidator.class);
        user = ManagerTestTool.createPerson();
        lister = new SampleLister(query, user);
        context.checking(new Expectations()
            {
                {
                    atMost(1).of(query).getRelationshipTypeId("PARENT_CHILD", true);
                    will(returnValue(RELATION_SHIP_TYPE));
                }
            });
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
    public void testGetEmptyCollection()
    {
        List<Sample> samples =
                lister.getSamples(Collections.<Long> emptyList(),
                        EnumSet.of(SampleFetchOption.BASIC), filter);

        assertEquals(0, samples.size());
    }

    @Test
    public void testGetSamplesWithPropertiesAndMetaProjects()
    {
        final List<Long> sampleIDs = Arrays.asList(101L, 102L);
        context.checking(new Expectations()
            {
                {
                    SampleRecord r101 = record(101);
                    SampleRecord r102 = record(102);
                    LongOpenHashSet sampleIdSet = new LongOpenHashSet(new long[] { r101.s_id, r102.s_id });
                    one(query).listSamplesByIds(sampleIdSet);
                    will(returnValue(Arrays.asList(r101, r102)));

                    one(query).getProperties(sampleIdSet);
                    will(returnValue(Arrays.asList(property(101, "A", "alpha"))));

                    one(query).getMetaprojectAssignments(sampleIdSet, user.getId());
                    will(returnValue(Arrays.asList(sampleMetaproject(102, 42))));

                    one(query).getMetaprojects(new LongOpenHashSet(new long[] { 42 }));
                    will(returnValue(Arrays.asList(metaproject(42, "answer"))));

                    atLeast(1).of(filter).isValid(user, sample(r101));
                    will(returnValue(true));

                    atLeast(1).of(filter).isValid(user, sample(r102));
                    will(returnValue(true));
                }
            });

        List<Sample> samples =
                lister.getSamples(sampleIDs,
                        EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.METAPROJECTS),
                        filter);

        assertEquals("[Sample[/SPACE/S-101,MY-TYPE,{A=alpha},parents=?,children=?], "
                + "Sample[/SPACE/S-102,MY-TYPE,{},parents=?,children=?]]", samples.toString());
        assertEquals("/john_doe/answer", samples.get(1).getMetaprojects().get(0).getIdentifier());
        assertEquals(1, samples.get(1).getMetaprojects().size());
    }

    @Test
    public void testGetSamplesAndParents()
    {
        final List<Long> sampleIDs = Arrays.asList(101L, 102L);
        context.checking(new Expectations()
            {
                {
                    one(query).getParents(RELATION_SHIP_TYPE, new LongOpenHashSet(sampleIDs));
                    will(returnValue(Arrays.asList(parentChild(1, 101), parentChild(2, 101),
                            parentChild(3, 102))));

                    SampleRecord r1 = record(1);
                    SampleRecord r2 = record(2);
                    SampleRecord r3 = record(3);
                    SampleRecord r101 = record(101);
                    SampleRecord r102 = record(102);
                    one(query).listSamplesByIds(new LongOpenHashSet(new long[] { r1.s_id, r2.s_id, r3.s_id, r101.s_id, r102.s_id }));
                    will(returnValue(Arrays.asList(r1, r2, r3, r101, r102)));

                    atLeast(1).of(filter).isValid(user, sample(r1));
                    will(returnValue(true));

                    atLeast(1).of(filter).isValid(user, sample(r2));
                    will(returnValue(false));

                    atLeast(1).of(filter).isValid(user, sample(r3));
                    will(returnValue(true));

                    atLeast(1).of(filter).isValid(user, sample(r101));
                    will(returnValue(true));

                    atLeast(1).of(filter).isValid(user, sample(r102));
                    will(returnValue(false));
                }
            });

        List<Sample> samples =
                lister.getSamples(sampleIDs, EnumSet.of(SampleFetchOption.PARENTS), filter);

        assertEquals(
                "[Sample[/SPACE/S-101,MY-TYPE,properties=?,parents={Sample[/SPACE/S-1,"
                        + "MY-TYPE,properties=?,parents=?,children=?]},children=?]]",
                samples.toString());
        assertEquals("[BASIC, PARENTS]", samples.get(0).getRetrievedFetchOptions().toString());
        assertEquals("[BASIC]", samples.get(0).getParents().get(0).getRetrievedFetchOptions()
                .toString());
    }

    @Test
    public void testGetSamplesAndDescendantsAndAncestors()
    {
        final List<Long> sampleIDs = Arrays.asList(201L, 202L);
        context.checking(new Expectations()
            {
                {
                    one(query).getDescendants(RELATION_SHIP_TYPE, new LongOpenHashSet(sampleIDs));
                    will(returnValue(Arrays.asList(parentChild(201, 301), parentChild(201, 302),
                            parentChild(302, 401))));
                    one(query).getAncestors(RELATION_SHIP_TYPE, new LongOpenHashSet(sampleIDs));
                    will(returnValue(Arrays.asList(parentChild(1, 101), parentChild(2, 101),
                            parentChild(3, 102), parentChild(101, 201), parentChild(102, 202))));

                    SampleRecord r1 = record(1);
                    SampleRecord r2 = record(2);
                    SampleRecord r3 = record(3);
                    SampleRecord r101 = record(101);
                    SampleRecord r102 = record(102);
                    SampleRecord r201 = record(201);
                    SampleRecord r202 = record(202);
                    SampleRecord r301 = record(301);
                    SampleRecord r302 = record(302);
                    SampleRecord r401 = record(401);
                    one(query).listSamplesByIds(
                            new LongOpenHashSet(new long[] { r1.s_id, r2.s_id, r3.s_id, r101.s_id, r102.s_id, r201.s_id,
                                    r202.s_id, r301.s_id, r302.s_id, r401.s_id }));
                    will(returnValue(Arrays.asList(r1, r2, r3, r101, r102, r201, r202, r301, r302,
                            r401)));

                    atLeast(1).of(filter).isValid(user, sample(r1));
                    will(returnValue(true));
                    atLeast(1).of(filter).isValid(user, sample(r2));
                    will(returnValue(false));
                    atLeast(1).of(filter).isValid(user, sample(r3));
                    will(returnValue(true));
                    atLeast(1).of(filter).isValid(user, sample(r101));
                    will(returnValue(true));
                    atLeast(1).of(filter).isValid(user, sample(r102));
                    will(returnValue(false));
                    atLeast(1).of(filter).isValid(user, sample(r201));
                    will(returnValue(true));
                    atLeast(1).of(filter).isValid(user, sample(r202));
                    will(returnValue(false));
                    atLeast(1).of(filter).isValid(user, sample(r301));
                    will(returnValue(false));
                    atLeast(1).of(filter).isValid(user, sample(r302));
                    will(returnValue(true));
                    atLeast(1).of(filter).isValid(user, sample(r401));
                    will(returnValue(true));
                }
            });

        List<Sample> samples =
                lister.getSamples(sampleIDs,
                        EnumSet.of(SampleFetchOption.DESCENDANTS, SampleFetchOption.ANCESTORS),
                        filter);

        assertEquals(
                "[Sample[/SPACE/S-201,MY-TYPE,properties=?,parents={Sample[/SPACE/S-101,MY-TYPE,"
                        + "properties=?,parents={Sample[/SPACE/S-1,MY-TYPE,properties=?,parents=[],"
                        + "children=?]},children=?]},children={Sample[/SPACE/S-302,MY-TYPE,"
                        + "properties=?,parents=?,children={Sample[/SPACE/S-401,MY-TYPE,properties=?,"
                        + "parents=?,children=[]]}]}]]",
                samples.toString());
        assertEquals("[BASIC, PARENTS, CHILDREN]", samples.get(0).getRetrievedFetchOptions()
                .toString());
        assertEquals("[BASIC, PARENTS]", samples.get(0).getParents().get(0)
                .getRetrievedFetchOptions().toString());
        assertEquals("[BASIC, PARENTS]", samples.get(0).getParents().get(0).getParents().get(0)
                .getRetrievedFetchOptions().toString());
        assertEquals("[BASIC, CHILDREN]", samples.get(0).getChildren().get(0)
                .getRetrievedFetchOptions().toString());
        assertEquals("[BASIC, CHILDREN]", samples.get(0).getChildren().get(0).getChildren().get(0)
                .getRetrievedFetchOptions().toString());
    }

    private SampleRelationshipRecord parentChild(long parentID, long childID)
    {
        SampleRelationshipRecord sampleRelationshipRecord = new SampleRelationshipRecord();
        sampleRelationshipRecord.sample_id_parent = parentID;
        sampleRelationshipRecord.sample_id_child = childID;
        return sampleRelationshipRecord;
    }

    private SampleRecord record(long id)
    {
        SampleRecord record = new SampleRecord();
        record.s_id = id;
        record.s_code = "S-" + id;
        record.s_perm_id = "P-" + id;
        record.st_id = 1L;
        record.st_code = "MY-TYPE";
        record.sp_code = "SPACE";
        return record;
    }

    private PropertyRecord property(long sampleId, String code, String value)
    {
        PropertyRecord propertyRecord = new PropertyRecord();
        propertyRecord.entity_id = sampleId;
        propertyRecord.code = code;
        propertyRecord.value = value;
        return propertyRecord;
    }

    private EntityMetaprojectRelationRecord sampleMetaproject(long sampleId, long metaProjectId)
    {
        EntityMetaprojectRelationRecord record = new EntityMetaprojectRelationRecord();
        record.entity_id = sampleId;
        record.metaproject_id = metaProjectId;
        return record;
    }

    private MetaprojectRecord metaproject(long id, String name)
    {
        MetaprojectRecord record = new MetaprojectRecord();
        record.id = id;
        record.name = name;
        record.is_private = false;
        return record;
    }

    private Sample sample(SampleRecord record)
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(record.s_id);
        initializer.setCode(record.s_code);
        initializer.setPermId(record.s_perm_id);
        initializer.setSampleTypeId(record.st_id);
        initializer.setSampleTypeCode(record.st_code);
        initializer.setSpaceCode(record.sp_code);
        initializer.setIdentifier("/" + record.sp_code + "/" + record.s_code);
        initializer.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        return new Sample(initializer);
    }

}
