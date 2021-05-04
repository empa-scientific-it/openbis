/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_SPACE_USER, new DataSetSearchCriteria(), "20120619092259000-22", "20120628092259000-24",
                "20120628092259000-25", "20120628092259000-41", "VALIDATIONS_CNTNR-26", "VALIDATIONS_IMPOS-27",
                "VALIDATIONS_PARENT-28", "DATASET-TO-DELETE", "COMPONENT_3AX");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withId().thatEquals(new DataSetPermId("20081105092259000-18"));
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithCodeForNonAdminUserWhereDataSetIsOwnedBySpaceSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatEquals("20120628092259000-23");
        testSearch(TEST_ROLE_V3, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithCodes()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("20081105092259000-18", "20081105092259000-19"));
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-19");
    }

    @Test
    public void testSearchTwoDataSetsWithCodeAndId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatEquals("20081105092259000-18");
        criteria.withCode().thatEquals("20081105092259000-19");
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-19");
    }

    @Test
    public void testSearchWithProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withProperty("COMMENT").thatContains("non-virt");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);

        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        dataSetCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withProperty(propertyType.getPermId()).thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithRegistrationDateIsEarlierThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05 09:22:00");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithModificationDateIsLaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModificationDate().thatIsLaterThanOrEqualTo("2011-05-01");
        criteria.withContainer().withCode().thatContains("2");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "COMPONENT_2A", "20110509092359990-12");
    }

    @Test
    public void testSearchWithContainer()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer();
        testSearch(TEST_USER, criteria, "CONTAINER_1", "CONTAINER_2", "20110509092359990-11", "20110509092359990-12",
                "VALIDATIONS_IMPOS-27", "COMPONENT_1A", "COMPONENT_2A", "COMPONENT_1B", "COMPONENT_3A", "COMPONENT_3AB",
                "COMPONENT_3AX");
    }

    @Test
    public void testSearchWithContainerWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer().withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithChildren()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withChildren();
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3",
                "20081105092259000-19", "20081105092259000-9", "20081105092259900-0", "20081105092259900-1",
                "20110805092359990-17", "VALIDATIONS_PARENT-28");
    }

    @Test
    public void testSearchWithChildrenWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3");
    }

    @Test
    public void testSearchWithChildrenWithPropertyEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withProperty("GENDER").thatEquals("FEMALE");
        criteria.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithParent()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents();
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-20", "20081105092259000-21",
                "20081105092259000-8", "20081105092259000-9", "20081105092259900-0", "20081105092259900-1",
                "20081105092359990-2", "VALIDATIONS_IMPOS-27");
    }

    @Test
    public void testSearchWithParentWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents().withCode().thatEquals("20081105092159111-1");
        testSearch(TEST_USER, criteria, "20081105092259000-9");
    }

    @Test
    public void testSearchWithExperiment()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withExperiment();
        testSearch(TEST_USER, criteria, "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withoutExperiment();
        testSearch(TEST_USER, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithExperimentWithPermIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withPermId().thatEquals("200902091255058-1035");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithExperimentWithProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withProperty("GENDER");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17", "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentWithPropertyThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withProperty("GENDER").thatEquals("MALE");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentYoungerThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09 12:11:00");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withSample();
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-41");
    }

    @Test
    public void testSearchWithoutSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withoutSample();
        testSearch(TEST_USER, criteria, "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithSampleWithPropertiesThatContains()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        SampleSearchCriteria sampleSearchCriteria = criteria.withSample().withOrOperator();
        sampleSearchCriteria.withProperty("BACTERIUM").thatContains("x");
        sampleSearchCriteria.withProperty("ORGANISM").thatContains("LY");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159333-3", "20110805092359990-17", "20120628092259000-41");
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("LINK_TYPE"));
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withCode().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withPermId().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("non-virtual");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithAnyField()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyField().thatEquals("20110509092359990-11");
        testSearch(TEST_USER, criteria, "20110509092359990-11");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistratorOrModifier()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyField().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2", "20110509092359990-11",
                "20110509092359990-12");
    }

    @Test
    public void testSearchWithAnyFieldMatchingDataSetType()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyField().thatEquals("UNKNOWN");
        testSearch(TEST_USER, criteria, "DATASET-TO-DELETE");
    }

    @Test
    public void testSearchWithAnyFieldMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);

        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        dataSetCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));

        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyField().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAnyPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);

        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        dataSetCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));

        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyProperty().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithTag()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "20120619092259000-22");
    }

    @Test
    public void testSearchWithPhysicalData()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData();
        testSearch(TEST_USER, criteria, 25);
    }

    @Test
    public void testSearchWithPhysicalDataWithShareIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEquals("42");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEquals("4");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEquals("2");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithShareIdThatStartsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatStartsWith("42");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatStartsWith("4");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatStartsWith("2");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithShareIdThatEndsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEndsWith("42");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEndsWith("4");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEndsWith("2");
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithPhysicalDataWithLocationThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEquals("analysis/result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEquals("analys");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEquals("result");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithLocationThatStartsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatStartsWith("analysis/result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatStartsWith("analys");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatStartsWith("result");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithLocationThatEndsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEndsWith("analysis/result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEndsWith("analys");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEndsWith("result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithPhysicalDataWithSizeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatEquals(4710);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatEquals(4711);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatEquals(4712);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSizeThatIsGreaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsGreaterThan(4710);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsGreaterThan(4711);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsGreaterThan(4712);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSizeThatIsLessThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsLessThan(4710);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsLessThan(4711);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsLessThan(4712);
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithPhysicalDataWithStorageFormatWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageFormat().withCode().thatEquals("PROPRIETARY");
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageFormat().withCode().thatEquals("PRO");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageFormat().withCode().thatEquals("TARY");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithFileFormatTypeWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withFileFormatType().withCode().thatEquals("TIFF");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withFileFormatType().withCode().thatEquals("TIF");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withFileFormatType().withCode().thatEquals("IFF");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithLocatorTypeWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocatorType().withCode().thatEquals("RELATIVE_LOCATION");
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocatorType().withCode().thatEquals("REL");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocatorType().withCode().thatEquals("TION");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithCompleteThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withComplete().thatEquals(Complete.UNKNOWN);
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withComplete().thatEquals(Complete.YES);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withComplete().thatEquals(Complete.NO);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithStatusThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStatus().thatEquals(ArchivingStatus.AVAILABLE);
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStatus().thatEquals(ArchivingStatus.ARCHIVED);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithPresentInArchiveThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withPresentInArchive().thatEquals(false);
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withPresentInArchive().thatEquals(true);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithArchivingRequestedThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withArchivingRequested().thatEquals(true);
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withArchivingRequested().thatEquals(false);
        testSearch(TEST_USER, criteria, 24);
    }

    @Test
    public void testSearchWithPhysicalDataWithStorageConfirmationThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageConfirmation().thatEquals(false);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageConfirmation().thatEquals(true);
        testSearch(TEST_USER, criteria, 24);
    }

    @Test
    public void testSearchWithPhysicalDataWithSpeedHintThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatEquals(-50);
        testSearch(TEST_USER, criteria, 24);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatEquals(42);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatEquals(50);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSpeedHintThatIsGreaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsGreaterThan(-50);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsGreaterThan(42);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsGreaterThan(50);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSpeedHintThatIsLessThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsLessThan(-50);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsLessThan(42);
        testSearch(TEST_USER, criteria, 24);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsLessThan(50);
        testSearch(TEST_USER, criteria, 25);
    }

    @Test
    public void testSearchWithLinkedData()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData();
        testSearch(TEST_USER, criteria, 4);
    }

    @Test
    public void testSearchWithLinkedDataWithExternalCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEquals("CODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEquals("CODE");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEquals("ODE1");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithLinkedDataWithExternalCodeThatStartsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatStartsWith("CODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatStartsWith("CODE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatStartsWith("ODE1");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithLinkedDataWithExternalCodeThatEndsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEndsWith("CODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEndsWith("CODE");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEndsWith("ODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithLinkedDataWithExternalDmsWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalDms().withCode().thatEquals("DMS_1");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalDms().withCode().thatEquals("DM");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalDms().withCode().thatEquals("MS");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3",
                "20110805092359990-17");
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModificationDate().thatEquals("2011-05-09");
        testSearch(TEST_USER, criteria, 14);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("VALIDATIONS");
        criteria.withCode().thatContains("PARENT");
        testSearch(TEST_USER, criteria, "VALIDATIONS_PARENT-28");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20081105092159111-1");
        criteria.withPermId().thatEquals("20081105092159222-2");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criteria, 1);

        criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithSortingByProperty()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20110509092359990-12");
        criteria.withPermId().thatEquals("20081105092259000-20");
        criteria.withPermId().thatEquals("20081105092159111-1");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        final List<DataSet> dataSets1 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodesInOrder(dataSets1, "20081105092259000-20", "20081105092159111-1", "20110509092359990-12");

        fo.sortBy().property("COMMENT").desc();
        fo.from(0).count(3);
        final List<DataSet> dataSets2 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodesInOrder(dataSets2, "20110509092359990-12", "20081105092159111-1", "20081105092259000-20");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAndSortingByPropertyAndPaging()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20110509092359990-12");
        criteria.withPermId().thatEquals("20081105092259000-20");
        criteria.withPermId().thatEquals("20081105092159111-1");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        fo.from(1).count(2);
        final SearchResult<DataSet> dataSetSearchResult1 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult1.getTotalCount(), 3);
        List<DataSet> dataSets1 = dataSetSearchResult1.getObjects();
        assertDataSetCodesInOrder(dataSets1, "20081105092159111-1", "20110509092359990-12");

        fo.sortBy().property("COMMENT").asc();
        fo.from(1).count(1);
        final SearchResult<DataSet> dataSetSearchResult2 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult2.getTotalCount(), 3);
        List<DataSet> dataSets2 = dataSetSearchResult2.getObjects();
        assertDataSetCodesInOrder(dataSets2, "20081105092159111-1");

        fo.sortBy().property("COMMENT").desc();
        fo.from(1).count(3);
        final SearchResult<DataSet> dataSetSearchResult3 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult3.getTotalCount(), 3);
        List<DataSet> dataSets3 = dataSetSearchResult3.getObjects();
        assertDataSetCodesInOrder(dataSets3, "20081105092159111-1", "20081105092259000-20");

        fo.sortBy().property("COMMENT").desc();
        fo.from(2).count(1);
        final SearchResult<DataSet> dataSetSearchResult4 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult4.getTotalCount(), 3);
        List<DataSet> dataSets4 = dataSetSearchResult4.getObjects();
        assertDataSetCodesInOrder(dataSets4, "20081105092259000-20");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1B"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1A"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_2A"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();

        fo.sortBy().code().asc();
        List<DataSet> dataSets1 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets1, "COMPONENT_1A", "COMPONENT_1B", "COMPONENT_2A");

        fo.sortBy().code().desc();
        List<DataSet> dataSets2 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets2, "COMPONENT_2A", "COMPONENT_1B", "COMPONENT_1A");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("ROOT_CONTAINER"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1A"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_2A"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<DataSet> dataSets1 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets1, "ROOT_CONTAINER", "COMPONENT_1A", "COMPONENT_2A");

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<DataSet> dataSets2 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets2, "COMPONENT_2A", "COMPONENT_1A", "ROOT_CONTAINER");

        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("20120619092259000-22"));
        criteria.withId().thatEquals(new DataSetPermId("20081105092159188-3"));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SearchResult<DataSet> result = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions());

        if (user.isInstanceUser())
        {
            assertEquals(result.getObjects().size(), 2);
        } else if ((user.isTestSpaceUser() || user.isTestProjectUser()) && !user.isDisabledProjectUser())
        {
            assertEquals(result.getObjects().size(), 1);
            assertEquals(result.getObjects().get(0).getCode(), "20120619092259000-22");
        } else
        {
            assertEquals(result.getObjects().size(), 0);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testFetchDataSetKind()
    {
        // given
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("20120628092259000-23")); // link
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1A")); // physical
        criteria.withId().thatEquals(new DataSetPermId("ROOT_CONTAINER")); // container

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.sortBy().code().asc();

        // when
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();

        // then
        assertEquals(dataSets.get(0).getKind(), DataSetKind.LINK);
        assertEquals(dataSets.get(1).getKind(), DataSetKind.PHYSICAL);
        assertEquals(dataSets.get(2).getKind(), DataSetKind.CONTAINER);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchForDataSetWithIntegerPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAnIntegerPropertyType(sessionToken, "INT_NUMBER");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("INTEGER_PROPERTY_TEST");
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("INT_NUMBER", "123");

        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteriaStartsWithMatch = new DataSetSearchCriteria();
        criteriaStartsWithMatch.withProperty("INT_NUMBER").thatStartsWith("12");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaStartsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "INTEGER"));

        final DataSetSearchCriteria criteriaEndsWithMatch = new DataSetSearchCriteria();
        criteriaEndsWithMatch.withProperty("INT_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaEndsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "INTEGER"));

        final DataSetSearchCriteria criteriaContainsMatch = new DataSetSearchCriteria();
        criteriaContainsMatch.withProperty("INT_NUMBER").thatContains("23");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaContainsMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "INTEGER"));
    }

    @Test
    public void testSearchForDataSetWithBooleanPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("BOOLEAN_PROPERTY_TEST");
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("BOOLEAN", "false");

        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteriaStartsWithMatch = new DataSetSearchCriteria();
        criteriaStartsWithMatch.withProperty("BOOLEAN").thatStartsWith("fa");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaStartsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "BOOLEAN"));

        final DataSetSearchCriteria criteriaEndsWithMatch = new DataSetSearchCriteria();
        criteriaEndsWithMatch.withProperty("BOOLEAN").thatEndsWith("lse");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaEndsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "BOOLEAN"));

        final DataSetSearchCriteria criteriaContainsMatch = new DataSetSearchCriteria();
        criteriaContainsMatch.withProperty("BOOLEAN").thatContains("als");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaContainsMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "BOOLEAN"));

        final DataSetSearchCriteria criteriaLTMatch = new DataSetSearchCriteria();
        criteriaLTMatch.withProperty("BOOLEAN").thatIsLessThan("true");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaLTMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThan", "BOOLEAN"));

        final DataSetSearchCriteria criteriaLEMatch = new DataSetSearchCriteria();
        criteriaLEMatch.withProperty("BOOLEAN").thatIsLessThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaLEMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThanOrEqualTo", "BOOLEAN"));

        final DataSetSearchCriteria criteriaGTMatch = new DataSetSearchCriteria();
        criteriaGTMatch.withProperty("BOOLEAN").thatIsGreaterThan("true");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaGTMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThan", "BOOLEAN"));

        final DataSetSearchCriteria criteriaGEMatch = new DataSetSearchCriteria();
        criteriaGEMatch.withProperty("BOOLEAN").thatIsGreaterThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaGEMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThanOrEqualTo", "BOOLEAN"));
    }

    @Test
    public void testSearchForDataSetWithRealPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createARealPropertyType(sessionToken, "REAL_NUMBER");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("REAL_PROPERTY_TEST");
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("REAL_NUMBER", "1.23");

        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteriaStartsWithMatch = new DataSetSearchCriteria();
        criteriaStartsWithMatch.withProperty("REAL_NUMBER").thatStartsWith("1.2");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaStartsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "REAL"));

        final DataSetSearchCriteria criteriaEndsWithMatch = new DataSetSearchCriteria();
        criteriaEndsWithMatch.withProperty("REAL_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaEndsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "REAL"));

        final DataSetSearchCriteria criteriaContainsMatch = new DataSetSearchCriteria();
        criteriaContainsMatch.withProperty("REAL_NUMBER").thatContains(".2");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaContainsMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "REAL"));
    }

    @Test
    public void testSearchWithDateDatePropertyThatEquals()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatEquals("20-2-17");

        // When
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()).getObjects();

        // Then
        assertDataSetCodesInOrder(dataSets, "DATA_SET_WITH_DATE_PROPERTY");
    }

    @Test
    public void testSearchWithDateDatePropertyWithInvalidCriteria()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        try
        {
            // When
            criteria.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("20-2-37");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // Then
            assertEquals(e.getMessage(), "Date value: later than or equal to '20-2-37' "
                    + "does not match any of the supported formats: [y-M-d HH:mm:ss, y-M-d HH:mm, y-M-d]");
        }
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsLater()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("2020-02-16");

        // When
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()).getObjects();

        // Then
        assertEquals(dataSets.get(0).getCode(), "DATA_SET_WITH_DATE_PROPERTY");
        assertEquals(dataSets.size(), 1);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsLaterOrEqual()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation1 = physicalDataSetCreation();
        creation1.setCode("DATA_SET_WITH_DATE_PROPERTY1");
        creation1.setTypeId(dataSetType);
        creation1.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation1.setProperty(propertyType.getPermId(), "2/17/20");
        DataSetCreation creation2 = physicalDataSetCreation();
        creation2.setCode("DATA_SET_WITH_DATE_PROPERTY2");
        creation2.setTypeId(dataSetType);
        creation2.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation2.setProperty(propertyType.getPermId(), "2020-02-16");
        v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("2020-02-16");

        // When
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()).getObjects();

        // Then
        assertDataSetCodesInOrder(dataSets, "DATA_SET_WITH_DATE_PROPERTY1", "DATA_SET_WITH_DATE_PROPERTY2");
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlier()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatIsEarlierThanOrEqualTo("1990-11-10");

        // When
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()).getObjects();

        // Then
        assertEquals(dataSets.get(0).getCode(), "DATA_SET_WITH_DATE_PROPERTY");
        assertEquals(dataSets.size(), 1);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlierWithTimezone()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        DatePropertySearchCriteria datePropertySearchCriteria = criteria.withDateProperty(propertyType.getPermId());
        datePropertySearchCriteria.withTimeZone(6);
        datePropertySearchCriteria.thatIsEarlierThanOrEqualTo("1990-11-09");

        // When
        assertUserFailureException(Void -> v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()),
                // Then
                "Search criteria with time zone doesn't make sense for property " + propertyType.getPermId()
                        + " of data type " + DataType.DATE);
    }

    @Test
    public void testSearchWithAnyPropertyThatIsEarlier()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyProperty().thatIsLessThanOrEqualTo("2009-02-10");

        // When
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()).getObjects();

        // Then
        assertDataSetCodesInOrder(dataSets, "DATA_SET_WITH_DATE_PROPERTY");
    }

    @Test
    public void testSearchWithDateDatePropertyWithTimezone()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("DATA_SET_WITH_DATE_PROPERTY");
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        DatePropertySearchCriteria datePropertySearchCriteria = criteria.withDateProperty(propertyType.getPermId());
        datePropertySearchCriteria.withTimeZone(-4);
        datePropertySearchCriteria.thatEquals("2020-02-17");

        // When
        assertUserFailureException(Void -> v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions()),
                // Then
                "Search criteria with time zone doesn't make sense for property " + propertyType.getPermId()
                        + " of data type " + DataType.DATE);
    }

    @Test
    public void testSearchNumeric()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId integerPropertyType = createAnIntegerPropertyType(sessionToken, "INT_NUMBER");
        final PropertyTypePermId realPropertyType = createARealPropertyType(sessionToken, "REAL_NUMBER");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, integerPropertyType,
                realPropertyType);

        final DataSetCreation dataSetCreation1 = getDataSetCreation(dataSetType, 1, 0.01);
        final DataSetCreation dataSetCreation2 = getDataSetCreation(dataSetType, 2, 0.02);
        final DataSetCreation dataSetCreation3 = getDataSetCreation(dataSetType, 3, 0.03);

        v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation1, dataSetCreation2,
                dataSetCreation3));

        final DataSetFetchOptions emptyFetchOptions = new DataSetFetchOptions();
        emptyFetchOptions.sortBy().code();

        // Greater or Equal - Integer
        final DataSetSearchCriteria criteriaGE = new DataSetSearchCriteria();
        criteriaGE.withNumberProperty("INT_NUMBER").thatIsGreaterThanOrEqualTo(2);
        final List<DataSet> dataSetsGE = search(sessionToken, criteriaGE, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsGE, "TEST_2", "TEST_3");

        // Greater or Equal - Integer as Real
        final DataSetSearchCriteria criteriaGEIR = new DataSetSearchCriteria();
        criteriaGEIR.withNumberProperty("INT_NUMBER").thatIsGreaterThanOrEqualTo(2.0);
        final List<DataSet> dataSetsGEIR = search(sessionToken, criteriaGEIR, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsGEIR, "TEST_2", "TEST_3");

        // Greater or Equal - Real
        final DataSetSearchCriteria criteriaGER = new DataSetSearchCriteria();
        criteriaGER.withNumberProperty("REAL_NUMBER").thatIsGreaterThanOrEqualTo(0.02);
        final List<DataSet> dataSetsGER = search(sessionToken, criteriaGER, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsGER, "TEST_2", "TEST_3");

        // Greater - Integer
        final DataSetSearchCriteria criteriaG = new DataSetSearchCriteria();
        criteriaG.withNumberProperty("INT_NUMBER").thatIsGreaterThan(2);
        final List<DataSet> dataSetsG = search(sessionToken, criteriaG, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsG, "TEST_3");

        // Greater - Integer as Real
        final DataSetSearchCriteria criteriaGIR = new DataSetSearchCriteria();
        criteriaGIR.withNumberProperty("INT_NUMBER").thatIsGreaterThan(2.0);
        final List<DataSet> dataSetsGIR = search(sessionToken, criteriaGIR, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsGIR, "TEST_3");

        // Greater - Real
        final DataSetSearchCriteria criteriaGR = new DataSetSearchCriteria();
        criteriaGR.withNumberProperty("REAL_NUMBER").thatIsGreaterThan(0.02);
        final List<DataSet> dataSetsGR = search(sessionToken, criteriaGR, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsGR, "TEST_3");

        // Equal - Integer
        final DataSetSearchCriteria criteriaE = new DataSetSearchCriteria();
        criteriaE.withNumberProperty("INT_NUMBER").thatEquals(2);
        final List<DataSet> dataSetsE = search(sessionToken, criteriaE, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsE, "TEST_2");

        // Equal - Integer as String
        final DataSetSearchCriteria criteriaES = new DataSetSearchCriteria();
        criteriaES.withProperty("INT_NUMBER").thatEquals("2");
        final List<DataSet> dataSetsES = search(sessionToken, criteriaES, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsES, "TEST_2");

        // Equal - Integer as Real as String
        final DataSetSearchCriteria criteriaERS = new DataSetSearchCriteria();
        criteriaERS.withProperty("INT_NUMBER").thatEquals("2.0");
        final List<DataSet> dataSetsERS = search(sessionToken, criteriaERS, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsERS, "TEST_2");

        // Greater or Equal - Integer
        final DataSetSearchCriteria criteriaLE = new DataSetSearchCriteria();
        criteriaLE.withNumberProperty("INT_NUMBER").thatIsLessThanOrEqualTo(2);
        final List<DataSet> dataSetsLE = search(sessionToken, criteriaLE, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsLE, "TEST_1", "TEST_2");

        // Less or Equal - Real
        final DataSetSearchCriteria criteriaLER = new DataSetSearchCriteria();
        criteriaLER.withNumberProperty("REAL_NUMBER").thatIsLessThanOrEqualTo(0.02);
        final List<DataSet> dataSetsLER = search(sessionToken, criteriaLER, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsLER, "TEST_1", "TEST_2");

        // Less - Integer
        final DataSetSearchCriteria criteriaL = new DataSetSearchCriteria();
        criteriaL.withNumberProperty("INT_NUMBER").thatIsLessThan(2);
        final List<DataSet> dataSetsL = search(sessionToken, criteriaL, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsL, "TEST_1");

        // Less - Integer as Real
        final DataSetSearchCriteria criteriaLIR = new DataSetSearchCriteria();
        criteriaLIR.withNumberProperty("INT_NUMBER").thatIsLessThan(2.0);
        final List<DataSet> dataSetsLIR = search(sessionToken, criteriaLIR, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsLIR, "TEST_1");

        // Less - Real
        final DataSetSearchCriteria criteriaLR = new DataSetSearchCriteria();
        criteriaLR.withNumberProperty("REAL_NUMBER").thatIsLessThan(0.02);
        final List<DataSet> dataSetsLR = search(sessionToken, criteriaLR, emptyFetchOptions);
        assertDataSetCodesInOrder(dataSetsLR, "TEST_1");
    }

    @Test
    public void testSearchForDataSetWithDatePropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createADatePropertyType(sessionToken, "DATE");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("DATE_PROPERTY_TEST");
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("DATE", "2020-02-09");

        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteriaContainsMatch = new DataSetSearchCriteria();
        criteriaContainsMatch.withProperty("DATE").thatContains("02");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaContainsMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "DATE"));

        final DataSetSearchCriteria criteriaStartsWithMatch = new DataSetSearchCriteria();
        criteriaStartsWithMatch.withProperty("DATE").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaStartsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "DATE"));

        final DataSetSearchCriteria criteriaEndsWithMatch = new DataSetSearchCriteria();
        criteriaEndsWithMatch.withProperty("DATE").thatEndsWith("09");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaEndsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "DATE"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchForDataSetWithTimestampPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createATimestampPropertyType(sessionToken, "TIMESTAMP");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("TIMESTAMP_PROPERTY_TEST");
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("TIMESTAMP", "2020-02-09 10:00:00 +0100");

        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteriaContainsMatch = new DataSetSearchCriteria();
        criteriaContainsMatch.withProperty("TIMESTAMP").thatContains("20");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaContainsMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "TIMESTAMP"));

        final DataSetSearchCriteria criteriaStartsWithMatch = new DataSetSearchCriteria();
        criteriaStartsWithMatch.withProperty("TIMESTAMP").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaStartsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "TIMESTAMP"));

        final DataSetSearchCriteria criteriaEndsWithMatch = new DataSetSearchCriteria();
        criteriaEndsWithMatch.withProperty("TIMESTAMP").thatEndsWith("0100");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaEndsWithMatch, new DataSetFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "TIMESTAMP"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchForDataSetWithStringPropertyQueriedAsIntegerOrDate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAVarcharPropertyType(sessionToken, "SHORT_TEXT");
        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("SHORT_TEXT_PROPERTY_TEST");
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("SHORT_TEXT", "123");

        v3api.createDataSets(sessionToken, Collections.singletonList(dataSetCreation));

        final DataSetSearchCriteria criteriaWithNumberProperty = new DataSetSearchCriteria();
        criteriaWithNumberProperty.withNumberProperty("SHORT_TEXT").thatEquals(123);
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaWithNumberProperty, new DataSetFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "NumberPropertySearchCriteria", "VARCHAR"));

        final DataSetSearchCriteria criteriaWithDateProperty = new DataSetSearchCriteria();
        criteriaWithDateProperty.withDateProperty("SHORT_TEXT").thatEquals("1990-11-09");
        assertUserFailureException(
                Void -> searchDataSets(sessionToken, criteriaWithDateProperty, new DataSetFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "DatePropertySearchCriteria", "VARCHAR"));
    }

    @Test
    public void testNestedLogicalOperators()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        final DataSetSearchCriteria subCriteria1 = criteria.withSubcriteria().withOrOperator();
        subCriteria1.withCode().thatStartsWith("2008");
        subCriteria1.withCode().thatStartsWith("CON");

        final DataSetSearchCriteria subCriteria2 = criteria.withSubcriteria().withOrOperator();
        subCriteria2.withCode().thatEndsWith("1");
        subCriteria2.withCode().thatEndsWith("A");

        testSearch(TEST_USER, criteria, "CONTAINER_1", "20081105092259000-21", "20081105092159111-1", "CONTAINER_3A",
                "20081105092259900-1");
    }

    @Test
    public void testNestedLogicalOperatorsMultipleNesting()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        final DataSetSearchCriteria subCriteria1 = criteria.withSubcriteria().withOrOperator();
        subCriteria1.withSubcriteria().withCode().thatStartsWith("2008");
        subCriteria1.withSubcriteria().withSubcriteria().withCode().thatStartsWith("CON");

        final DataSetSearchCriteria subCriteria2 = criteria.withSubcriteria().withOrOperator();
        subCriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withCode().thatEndsWith("1");
        subCriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withSubcriteria().withCode()
                .thatEndsWith("A");

        testSearch(TEST_USER, criteria, "CONTAINER_1", "20081105092259000-21", "20081105092159111-1", "CONTAINER_3A",
                "20081105092259900-1");
    }

    @Test
    public void testNestedLogicalOperatorsWithParentsAndChildren()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        final DataSetSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withParents().withCode().thatStartsWith("20081105092259000-");
        subcriteria1.withParents().withCode().thatStartsWith("20081105092259900-");
        subcriteria1.withParents().withCode().thatStartsWith("20110509092359990-");
        subcriteria1.withParents().withCode().thatStartsWith("20081105092159");

        final DataSetSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
        subcriteria2.withChildren().withCode().thatEndsWith("2");
        subcriteria2.withChildren().withCode().thatEndsWith("AB");

        testSearch(TEST_INSTANCE_ETLSERVER, criteria, "20081105092259900-0", "20081105092259900-1");
    }

    @Test
    public void testNestedLogicalOperatorsWithParentsAndChildrenMultipleNesting()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        final DataSetSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withSubcriteria().withParents().withCode().thatStartsWith("20081105092259000-");
        subcriteria1.withSubcriteria().withSubcriteria().withParents().withCode().thatStartsWith("20081105092259900-");
        subcriteria1.withSubcriteria().withSubcriteria().withSubcriteria().withParents().withCode()
                .thatStartsWith("20110509092359990-");
        subcriteria1.withSubcriteria().withSubcriteria().withSubcriteria().withSubcriteria().withParents().withCode()
                .thatStartsWith("20081105092159");

        final DataSetSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
        subcriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withSubcriteria().withSubcriteria()
                .withChildren().withCode().thatEndsWith("2");
        subcriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withChildren().withCode().thatEndsWith("AB");

        testSearch(TEST_INSTANCE_ETLSERVER, criteria, "20081105092259900-0", "20081105092259900-1");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withCode().thatEquals("20081105092259000-18");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withSample();
        fo.withExperiment();

        v3api.searchDataSets(sessionToken, c, fo);

        assertAccessLog("search-data-sets  SEARCH_CRITERIA:\n'DATASET\n    with attribute 'code' equal to " +
                "'20081105092259000-18'\n'\nFETCH_OPTIONS:\n'DataSet\n    with Experiment\n    with Sample\n'");
    }

    @Test
    public void testSearchWithPermIdWithAttributeFullTextSearch()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        criteria.withTextAttribute().thatMatches(
                "component_1a component_1b component_2a component_2b container_1 container_2");
        criteria.withPermId().thatStartsWith("COMPONENT");

        testSearch(TEST_USER, criteria, "COMPONENT_1A", "COMPONENT_1B", "COMPONENT_2A");
    }

    @Test
    public void testSearchWithPermIdWithPropertyFullTextSearch()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        criteria.withProperty("COMMENT").thatMatches("virtual female bacterium1");
        criteria.withPermId().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "20110509092359990-11");
    }

    @Test
    public void testSearchWithPermIdWithStringPropertyFullTextSearch()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        criteria.withStringProperty("COMMENT").thatMatches("virtual female bacterium1");
        criteria.withPermId().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "20110509092359990-11");
    }

    @Test
    public void testSearchWithPermIdWithAnyPropertyFullTextSearch()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        criteria.withAnyProperty().thatMatches("virtual female bacterium1");
        criteria.withPermId().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "20110509092359990-11", "20081105092159111-1");
    }

    @Test
    public void testSearchWithPermIdWithAnyStringPropertyFullTextSearch()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        criteria.withAnyStringProperty().thatMatches("virtual female bacterium1");
        criteria.withPermId().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "20110509092359990-11");
    }

    @Test
    public void testSearchWithPermIdWithAnyFieldFullTextSearch()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();

        criteria.withAnyField().thatMatches("virtual female bacterium1 20081105092259900-1 20081105092259900-2");
        criteria.withPermId().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "20110509092359990-11", "20081105092159111-1", "20081105092259900-1");
    }

    @Test
    public void testSearchWithAttributeNegation()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();
        criteria.withContainer();

        final DataSetSearchCriteria subcriteria = criteria.withSubcriteria().negate().withOrOperator();
        subcriteria.withCode().thatContains("-");
        subcriteria.withCode().thatStartsWith("COMPONENT_3A");
        subcriteria.withCode().thatEquals("COMPONENT_1B");

        testSearch(TEST_USER, criteria, "CONTAINER_1", "CONTAINER_2", "COMPONENT_1A", "COMPONENT_2A");
    }

    @Test
    public void testSearchWithAnyPropertyWithNegation()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();
        criteria.withAnyProperty().thatEndsWith("comment");
        criteria.withSubcriteria().negate().withAnyProperty().thatStartsWith("co ");
        criteria.withSubcriteria().negate().withAnyProperty().thatStartsWith("no ");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    private List<DataSet> search(final String sessionToken, final DataSetSearchCriteria criteria,
            final DataSetFetchOptions options)
    {
        return v3api.searchDataSets(sessionToken, criteria, options).getObjects();
    }

    public DataSetCreation getDataSetCreation(final EntityTypePermId dataSetType, final int intValue,
            final double realValue)
    {
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setCode("TEST_" + intValue);
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setProperty("INT_NUMBER", String.valueOf(intValue));
        dataSetCreation.setProperty("REAL_NUMBER", String.valueOf(realValue));
        return dataSetCreation;
    }

    private void testSearch(String user, DataSetSearchCriteria criteria, String... expectedIdentifiers)
    {
        final String sessionToken = v3api.login(user, PASSWORD);
        List<DataSet> dataSets = searchDataSets(sessionToken, criteria, new DataSetFetchOptions());
        assertIdentifiers(dataSets, expectedIdentifiers);
    }

    private void testSearch(String user, DataSetSearchCriteria criteria, int expectedCount)
    {
        final String sessionToken = v3api.login(user, PASSWORD);
        List<DataSet> dataSets = searchDataSets(sessionToken, criteria, new DataSetFetchOptions());
        assertEquals(dataSets.size(), expectedCount);
    }

    private List<DataSet> searchDataSets(final String sessionToken, final DataSetSearchCriteria criteria,
            final DataSetFetchOptions fetchOptions)
    {
        final SearchResult<DataSet> searchResult = v3api.searchDataSets(sessionToken, criteria, fetchOptions);
        final List<DataSet> dataSets = searchResult.getObjects();
        v3api.logout(sessionToken);
        return dataSets;
    }

}
