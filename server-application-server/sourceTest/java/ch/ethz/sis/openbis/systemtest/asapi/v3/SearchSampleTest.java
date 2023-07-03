/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsAtLeast;
import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionDoesntContain;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyFieldSearchConditionTranslator;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchSampleTest extends AbstractSampleTest
{

    private static final String[] ALL_SAMPLE_IDS_WITH_CONTAINER = new String[] {
            "/CISD/CL1:A01", "/CISD/CL1:A03", "/CISD/MP002-1:A03", "/CISD/MP002-1:A04", "/CISD/MP002-1:A05",
            "/CISD/MP002-1:A06", "/CISD/MP002-1:A07", "/CISD/MP002-1:A08", "/CISD/MP002-1:A09",
            "/CISD/MP002-1:A10", "/CISD/MP002-1:A11", "/CISD/MP002-1:A12", "/CISD/MP002-1:A13",
            "/CISD/MP002-1:A14", "/CISD/MP002-1:A15", "/CISD/MP002-1:A16", "/CISD/MP002-1:A17",
            "/CISD/MP002-1:A18", "/CISD/MP002-1:A19", "/CISD/MP002-1:A20", "/CISD/MP002-1:A21",
            "/CISD/MP002-1:A22", "/CISD/MP002-1:B03", "/CISD/MP002-1:B04", "/CISD/MP002-1:B05",
            "/CISD/MP002-1:B06", "/CISD/MP002-1:B07", "/CISD/MP002-1:B08", "/CISD/MP002-1:B09",
            "/CISD/MP002-1:B10", "/CISD/MP002-1:B11", "/CISD/MP002-1:B12", "/CISD/MP002-1:B13",
            "/CISD/MP002-1:B14", "/CISD/MP002-1:B15", "/CISD/MP002-1:B16", "/CISD/MP002-1:B17",
            "/CISD/MP002-1:B18", "/CISD/MP002-1:B19", "/CISD/MP002-1:B20", "/CISD/MP002-1:B21",
            "/CISD/MP002-1:B22", "/CISD/MP002-1:C03", "/CISD/MP002-1:C04", "/CISD/MP002-1:C05",
            "/CISD/MP002-1:C06", "/CISD/MP002-1:C07", "/CISD/MP002-1:C08", "/CISD/MP002-1:C09",
            "/CISD/MP002-1:C10", "/CISD/MP002-1:C11", "/CISD/MP002-1:C12", "/CISD/MP002-1:C13",
            "/CISD/MP002-1:C14", "/CISD/MP002-1:C15", "/CISD/MP002-1:C16", "/CISD/MP002-1:C17",
            "/CISD/MP002-1:C18", "/CISD/MP002-1:C19", "/CISD/MP002-1:C20", "/CISD/MP002-1:C21",
            "/CISD/MP002-1:C22", "/CISD/MP002-1:D03", "/CISD/MP002-1:D04", "/CISD/MP002-1:D05",
            "/CISD/MP002-1:D06", "/CISD/MP002-1:D07", "/CISD/MP002-1:D08", "/CISD/MP002-1:D09",
            "/CISD/MP002-1:D10", "/CISD/MP002-1:D11", "/CISD/MP002-1:D12", "/CISD/MP002-1:D13",
            "/CISD/MP002-1:D14", "/CISD/MP002-1:D15", "/CISD/MP002-1:D16", "/CISD/MP002-1:D17",
            "/CISD/MP002-1:D18", "/CISD/MP002-1:D19", "/CISD/MP002-1:D20", "/CISD/MP002-1:D21",
            "/CISD/MP002-1:D22", "/CISD/MP002-1:E03", "/CISD/MP002-1:E04", "/CISD/MP002-1:E05",
            "/CISD/MP002-1:E06", "/CISD/MP002-1:E07", "/CISD/MP002-1:E08", "/CISD/MP002-1:E09",
            "/CISD/MP002-1:E10", "/CISD/MP002-1:E11", "/CISD/MP002-1:E12", "/CISD/MP002-1:E13",
            "/CISD/MP002-1:E14", "/CISD/MP002-1:E15", "/CISD/MP002-1:E16", "/CISD/MP002-1:E17",
            "/CISD/MP002-1:E18", "/CISD/MP002-1:E19", "/CISD/MP002-1:E20", "/CISD/MP002-1:E21",
            "/CISD/MP002-1:E22", "/CISD/MP002-1:F03", "/CISD/MP002-1:F04", "/CISD/MP002-1:F05",
            "/CISD/MP002-1:F06", "/CISD/MP002-1:F07", "/CISD/MP002-1:F08", "/CISD/MP002-1:F09",
            "/CISD/MP002-1:F10", "/CISD/MP002-1:F11", "/CISD/MP002-1:F12", "/CISD/MP002-1:F13",
            "/CISD/MP002-1:F14", "/CISD/MP002-1:F15", "/CISD/MP002-1:F16", "/CISD/MP002-1:F17",
            "/CISD/MP002-1:F18", "/CISD/MP002-1:F19", "/CISD/MP002-1:F20", "/CISD/MP002-1:F21",
            "/CISD/MP002-1:F22", "/CISD/MP002-1:G03", "/CISD/MP002-1:G04", "/CISD/MP002-1:G05",
            "/CISD/MP002-1:G06", "/CISD/MP002-1:G07", "/CISD/MP002-1:G08", "/CISD/MP002-1:G09",
            "/CISD/MP002-1:G10", "/CISD/MP002-1:G11", "/CISD/MP002-1:G12", "/CISD/MP002-1:G13",
            "/CISD/MP002-1:G14", "/CISD/MP002-1:G15", "/CISD/MP002-1:G16", "/CISD/MP002-1:G17",
            "/CISD/MP002-1:G18", "/CISD/MP002-1:G19", "/CISD/MP002-1:G20", "/CISD/MP002-1:G21",
            "/CISD/MP002-1:G22", "/CISD/MP002-1:H03", "/CISD/MP002-1:H04", "/CISD/MP002-1:H05",
            "/CISD/MP002-1:H06", "/CISD/MP002-1:H07", "/CISD/MP002-1:H08", "/CISD/MP002-1:H09",
            "/CISD/MP002-1:H10", "/CISD/MP002-1:H11", "/CISD/MP002-1:H12", "/CISD/MP002-1:H13",
            "/CISD/MP002-1:H14", "/CISD/MP002-1:H15", "/CISD/MP002-1:H16", "/CISD/MP002-1:H17",
            "/CISD/MP002-1:H18", "/CISD/MP002-1:H19", "/CISD/MP002-1:H20", "/CISD/MP002-1:H21",
            "/CISD/MP002-1:H22", "/CISD/MP002-1:I03", "/CISD/MP002-1:I04", "/CISD/MP002-1:I05",
            "/CISD/MP002-1:I06", "/CISD/MP002-1:I07", "/CISD/MP002-1:I08", "/CISD/MP002-1:I09",
            "/CISD/MP002-1:I10", "/CISD/MP002-1:I11", "/CISD/MP002-1:I12", "/CISD/MP002-1:I13",
            "/CISD/MP002-1:I14", "/CISD/MP002-1:I15", "/CISD/MP002-1:I16", "/CISD/MP002-1:I17",
            "/CISD/MP002-1:I18", "/CISD/MP002-1:I19", "/CISD/MP002-1:I20", "/CISD/MP002-1:I21",
            "/CISD/MP002-1:I22", "/CISD/MP002-1:J03", "/CISD/MP002-1:J04", "/CISD/MP002-1:J05",
            "/CISD/MP002-1:J06", "/CISD/MP002-1:J07", "/CISD/MP002-1:J08", "/CISD/MP002-1:J09",
            "/CISD/MP002-1:J10", "/CISD/MP002-1:J11", "/CISD/MP002-1:J12", "/CISD/MP002-1:J13",
            "/CISD/MP002-1:J14", "/CISD/MP002-1:J15", "/CISD/MP002-1:J16", "/CISD/MP002-1:J17",
            "/CISD/MP002-1:J18", "/CISD/MP002-1:J19", "/CISD/MP002-1:J20", "/CISD/MP002-1:J21",
            "/CISD/MP002-1:J22", "/CISD/MP002-1:K03", "/CISD/MP002-1:K04", "/CISD/MP002-1:K05",
            "/CISD/MP002-1:K06", "/CISD/MP002-1:K07", "/CISD/MP002-1:K08", "/CISD/MP002-1:K09",
            "/CISD/MP002-1:K10", "/CISD/MP002-1:K11", "/CISD/MP002-1:K12", "/CISD/MP002-1:K13",
            "/CISD/MP002-1:K14", "/CISD/MP002-1:K15", "/CISD/MP002-1:K16", "/CISD/MP002-1:K17",
            "/CISD/MP002-1:K18", "/CISD/MP002-1:K19", "/CISD/MP002-1:K20", "/CISD/MP002-1:K21",
            "/CISD/MP002-1:K22", "/CISD/MP002-1:L03", "/CISD/MP002-1:L04", "/CISD/MP002-1:L05",
            "/CISD/MP002-1:L06", "/CISD/MP002-1:L07", "/CISD/MP002-1:L08", "/CISD/MP002-1:L09",
            "/CISD/MP002-1:L10", "/CISD/MP002-1:L11", "/CISD/MP002-1:L12", "/CISD/MP002-1:L13",
            "/CISD/MP002-1:L14", "/CISD/MP002-1:L15", "/CISD/MP002-1:L16", "/CISD/MP002-1:L17",
            "/CISD/MP002-1:L18", "/CISD/MP002-1:L19", "/CISD/MP002-1:L20", "/CISD/MP002-1:L21",
            "/CISD/MP002-1:L22", "/CISD/MP002-1:M03", "/CISD/MP002-1:M04", "/CISD/MP002-1:M05",
            "/CISD/MP002-1:M06", "/CISD/MP002-1:M07", "/CISD/MP002-1:M08", "/CISD/MP002-1:M09",
            "/CISD/MP002-1:M10", "/CISD/MP002-1:M11", "/CISD/MP002-1:M12", "/CISD/MP002-1:M13",
            "/CISD/MP002-1:M14", "/CISD/MP002-1:M15", "/CISD/MP002-1:M16", "/CISD/MP002-1:M17",
            "/CISD/MP002-1:M18", "/CISD/MP002-1:M19", "/CISD/MP002-1:M20", "/CISD/MP002-1:M21",
            "/CISD/MP002-1:M22", "/CISD/MP002-1:N03", "/CISD/MP002-1:N04", "/CISD/MP002-1:N05",
            "/CISD/MP002-1:N06", "/CISD/MP002-1:N07", "/CISD/MP002-1:N08", "/CISD/MP002-1:N09",
            "/CISD/MP002-1:N10", "/CISD/MP002-1:N11", "/CISD/MP002-1:N12", "/CISD/MP002-1:N13",
            "/CISD/MP002-1:N14", "/CISD/MP002-1:N15", "/CISD/MP002-1:N16", "/CISD/MP002-1:N17",
            "/CISD/MP002-1:N18", "/CISD/MP002-1:N19", "/CISD/MP002-1:N20", "/CISD/MP002-1:N21",
            "/CISD/MP002-1:N22", "/CISD/MP002-1:O03", "/CISD/MP002-1:O04", "/CISD/MP002-1:O05",
            "/CISD/MP002-1:O06", "/CISD/MP002-1:O07", "/CISD/MP002-1:O08", "/CISD/MP002-1:O09",
            "/CISD/MP002-1:O10", "/CISD/MP002-1:O11", "/CISD/MP002-1:O12", "/CISD/MP002-1:O13",
            "/CISD/MP002-1:O14", "/CISD/MP002-1:O15", "/CISD/MP002-1:O16", "/CISD/MP002-1:O17",
            "/CISD/MP002-1:O18", "/CISD/MP002-1:O19", "/CISD/MP002-1:O20", "/CISD/MP002-1:O21",
            "/CISD/MP002-1:O22", "/CISD/MP002-1:P03", "/CISD/MP002-1:P04", "/CISD/MP002-1:P05",
            "/CISD/MP002-1:P06", "/CISD/MP002-1:P07", "/CISD/MP002-1:P08", "/CISD/MP002-1:P09",
            "/CISD/MP002-1:P10", "/CISD/MP002-1:P11", "/CISD/MP002-1:P12", "/CISD/MP002-1:P13",
            "/CISD/MP002-1:P14", "/CISD/MP002-1:P15", "/CISD/MP002-1:P16", "/CISD/MP002-1:P17",
            "/CISD/MP002-1:P18", "/CISD/MP002-1:P19", "/CISD/MP002-1:P20", "/CISD/MP002-1:P21",
            "/CISD/MP002-1:P22", "/MP:A03", "/MP:A04", "/MP:A05", "/MP:A06", "/MP:A07", "/MP:A08", "/MP:A09",
            "/MP:A10", "/MP:A11", "/MP:A12", "/MP:A13", "/MP:A14", "/MP:A15", "/MP:A16", "/MP:A17", "/MP:A18",
            "/MP:A19", "/MP:A20", "/MP:A21", "/MP:A22", "/MP:B03", "/MP:B04", "/MP:B05", "/MP:B06", "/MP:B07",
            "/MP:B08", "/MP:B09", "/MP:B10", "/MP:B11", "/MP:B12", "/MP:B13", "/MP:B14", "/MP:B15", "/MP:B16",
            "/MP:B17", "/MP:B18", "/MP:B19", "/MP:B20", "/MP:B21", "/MP:B22", "/MP:C03", "/MP:C04", "/MP:C05",
            "/MP:C06", "/MP:C07", "/MP:C08", "/MP:C09", "/MP:C10", "/MP:C11", "/MP:C12", "/MP:C13", "/MP:C14",
            "/MP:C15", "/MP:C16", "/MP:C17", "/MP:C18", "/MP:C19", "/MP:C20", "/MP:C21", "/MP:C22", "/MP:D03",
            "/MP:D04", "/MP:D05", "/MP:D06", "/MP:D07", "/MP:D08", "/MP:D09", "/MP:D10", "/MP:D11", "/MP:D12",
            "/MP:D13", "/MP:D14", "/MP:D15", "/MP:D16", "/MP:D17", "/MP:D18", "/MP:D19", "/MP:D20", "/MP:D21",
            "/MP:D22", "/MP:E03", "/MP:E04", "/MP:E05", "/MP:E06", "/MP:E07", "/MP:E08", "/MP:E09", "/MP:E10",
            "/MP:E11", "/MP:E12", "/MP:E13", "/MP:E14", "/MP:E15", "/MP:E16", "/MP:E17", "/MP:E18", "/MP:E19",
            "/MP:E20", "/MP:E21", "/MP:E22", "/MP:F03", "/MP:F04", "/MP:F05", "/MP:F06", "/MP:F07", "/MP:F08",
            "/MP:F09", "/MP:F10", "/MP:F11", "/MP:F12", "/MP:F13", "/MP:F14", "/MP:F15", "/MP:F16", "/MP:F17",
            "/MP:F18", "/MP:F19", "/MP:F20", "/MP:F21", "/MP:F22", "/MP:G03", "/MP:G04", "/MP:G05", "/MP:G06",
            "/MP:G07", "/MP:G08", "/MP:G09", "/MP:G10", "/MP:G11", "/MP:G12", "/MP:G13", "/MP:G14", "/MP:G15",
            "/MP:G16", "/MP:G17", "/MP:G18", "/MP:G19", "/MP:G20", "/MP:G21", "/MP:G22", "/MP:H03", "/MP:H04",
            "/MP:H05", "/MP:H06", "/MP:H07", "/MP:H08", "/MP:H09", "/MP:H10", "/MP:H11", "/MP:H12", "/MP:H13",
            "/MP:H14", "/MP:H15", "/MP:H16", "/MP:H17", "/MP:H18", "/MP:H19", "/MP:H20", "/MP:H21", "/MP:H22",
            "/MP:I03", "/MP:I04", "/MP:I05", "/MP:I06", "/MP:I07", "/MP:I08", "/MP:I09", "/MP:I10", "/MP:I11",
            "/MP:I12", "/MP:I13", "/MP:I14", "/MP:I15", "/MP:I16", "/MP:I17", "/MP:I18", "/MP:I19", "/MP:I20",
            "/MP:I21", "/MP:I22", "/MP:J03", "/MP:J04", "/MP:J05", "/MP:J06", "/MP:J07", "/MP:J08", "/MP:J09",
            "/MP:J10", "/MP:J11", "/MP:J12", "/MP:J13", "/MP:J14", "/MP:J15", "/MP:J16", "/MP:J17", "/MP:J18",
            "/MP:J19", "/MP:J20", "/MP:J21", "/MP:J22", "/MP:K03", "/MP:K04", "/MP:K05", "/MP:K06", "/MP:K07",
            "/MP:K08", "/MP:K09", "/MP:K10", "/MP:K11", "/MP:K12", "/MP:K13", "/MP:K14", "/MP:K15", "/MP:K16",
            "/MP:K17", "/MP:K18", "/MP:K19", "/MP:K20", "/MP:K21", "/MP:K22", "/MP:L03", "/MP:L04", "/MP:L05",
            "/MP:L06", "/MP:L07", "/MP:L08", "/MP:L09", "/MP:L10", "/MP:L11", "/MP:L12", "/MP:L13", "/MP:L14",
            "/MP:L15", "/MP:L16", "/MP:L17", "/MP:L18", "/MP:L19", "/MP:L20", "/MP:L21", "/MP:L22", "/MP:M03",
            "/MP:M04", "/MP:M05", "/MP:M06", "/MP:M07", "/MP:M08", "/MP:M09", "/MP:M10", "/MP:M11", "/MP:M12",
            "/MP:M13", "/MP:M14", "/MP:M15", "/MP:M16", "/MP:M17", "/MP:M18", "/MP:M19", "/MP:M20", "/MP:M21",
            "/MP:M22", "/MP:N03", "/MP:N04", "/MP:N05", "/MP:N06", "/MP:N07", "/MP:N08", "/MP:N09", "/MP:N10",
            "/MP:N11", "/MP:N12", "/MP:N13", "/MP:N14", "/MP:N15", "/MP:N16", "/MP:N17", "/MP:N18", "/MP:N19",
            "/MP:N20", "/MP:N21", "/MP:N22", "/MP:O03", "/MP:O04", "/MP:O05", "/MP:O06", "/MP:O07", "/MP:O08",
            "/MP:O09", "/MP:O10", "/MP:O11", "/MP:O12", "/MP:O13", "/MP:O14", "/MP:O15", "/MP:O16", "/MP:O17",
            "/MP:O18", "/MP:O19", "/MP:O20", "/MP:O21", "/MP:O22", "/MP:P03", "/MP:P04", "/MP:P05", "/MP:P06",
            "/MP:P07", "/MP:P08", "/MP:P09", "/MP:P10", "/MP:P11", "/MP:P12", "/MP:P13", "/MP:P14", "/MP:P15",
            "/MP:P16", "/MP:P17", "/MP:P18", "/MP:P19", "/MP:P20", "/MP:P21", "/MP:P22", "/CISD/CL-3V:A01",
            "/CISD/CL-3V:A02", "/CISD/B1B3:B01", "/CISD/B1B3:B03", "/CISD/C1:C01", "/CISD/C2:C02",
            "/CISD/C3:C03", "/CISD/MP1-MIXED:A01", "/CISD/MP1-MIXED:A02", "/CISD/MP1-MIXED:A03",
            "/CISD/MP1-MIXED:B02", "/CISD/MP2-NO-CL:A01", "/CISD/MP2-NO-CL:A02", "/CISD/MP2-NO-CL:A03",
            "/CISD/MP2-NO-CL:B02", "/CISD/MP2-NO-CL:C03", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01",
            "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02"
    };

    @Autowired
    protected ISQLExecutor sqlExecutor;

    @Test
    public void testSearchWhichReturnsSharedSamplesForSpaceUser()
    {
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withCode().thatEndsWith("P");
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final List<Sample> samples1 = searchSamples(sessionToken, sampleSearchCriteria, new SampleFetchOptions());
        assertSampleIdentifiers(samples1, "/DP", "/MP");

        final List<Sample> samples2 = searchSamples(sessionToken, sampleSearchCriteria, new SampleFetchOptions());
        assertSampleIdentifiers(samples2, "/DP", "/MP");

        final List<Sample> samples3 = searchSamples(sessionToken, sampleSearchCriteria, new SampleFetchOptions());
        assertSampleIdentifiers(samples3, "/DP", "/MP");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SamplePermId("200902091219327-1025"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithMultipleIds()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));
        criteria.withId().thatEquals(new SamplePermId("200902091250077-1026"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");
    }

    @Test
    public void testSearchWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatEquals()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withIdentifier().thatEquals("/CISD/NEMO/CP-TEST-1");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/CP-TEST-1");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatEquals("/CISD/N*/CP-TEST-*");
        testSearch(TEST_USER, criteria2, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatEquals("/CISD/CP-TEST-*");
        testSearch(TEST_USER, criteria3);

        final SampleSearchCriteria criteria4 = new SampleSearchCriteria();
        criteria4.withIdentifier().withWildcards().thatEquals("/CISD/NEMO/CP-*-1");
        testSearch(TEST_USER, criteria4, "/CISD/NEMO/CP-TEST-1");

        final SampleSearchCriteria criteria5 = new SampleSearchCriteria();
        criteria5.withIdentifier().withoutWildcards().thatEquals("/CISD/CP-*-1");
        testSearch(TEST_USER, criteria5);

        final SampleSearchCriteria criteria6 = new SampleSearchCriteria();
        criteria6.withIdentifier().withWildcards().thatEquals("/*/CP-TEST-*");
        testSearch(TEST_USER, criteria6, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3",
                "/TEST-SPACE/NOE/CP-TEST-4");

        final SampleSearchCriteria criteria7 = new SampleSearchCriteria();
        criteria7.withIdentifier().withoutWildcards().thatEquals("/*/CP-TEST-*");
        testSearch(TEST_USER, criteria7);
    }

    @Test
    public void testSearchWithIdentifierThatStartsWith()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withIdentifier().thatStartsWith("/CISD/NEMO/CP-TEST");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/CP-TEST-1", "/CISD/NEMO/CP-TEST-3");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatStartsWith("/CISD/*-test");
        testSearch(TEST_USER, criteria2, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3", "/CISD/NEMO/DYNA-TEST-1");

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatStartsWith("/CISD/*-test");
        testSearch(TEST_USER, criteria3);
    }

    @Test
    public void testSearchWithIdentifierThatEndsWith()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withIdentifier().thatEndsWith("-TEST-1");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/CP-TEST-1", "/CISD/NEMO/DYNA-TEST-1");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatEndsWith("-TEST-*");
        testSearch(TEST_USER, criteria2, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3",
                "/TEST-SPACE/NOE/CP-TEST-4", "/CISD/NEMO/DYNA-TEST-1");

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatEndsWith("-TEST-*");
        testSearch(TEST_USER, criteria3);
    }

    @Test
    public void testSearchWithIdentifierThatContains()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withIdentifier().thatContains("CP-TEST");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3",
                "/TEST-SPACE/NOE/CP-TEST-4");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatContains("CISD*-TEST");
        testSearch(TEST_USER, criteria2, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3", "/CISD/NEMO/DYNA-TEST-1");

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatContains("CISD*-TEST");
        testSearch(TEST_USER, criteria3);
    }

    @Test
    public void testSearchWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("RP1-A2X");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X");
    }

    @Test
    public void testSearchWithIncorrectCode()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().thatEquals("/CISD/DEFAULT/RP1-A2X/ABCD");
        testSearch(TEST_USER, criteria1);

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().thatEquals("/A/B/C/D/E");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithCodeThatIsLessOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsLessThanOrEqualTo("3VCP5");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126", "/CISD/NEMO/3VCP5");
    }

    @Test
    public void testSearchWithCodeThatIsLessThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsLessThan("3VCP5");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithCodeThatIsGreaterThanOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsGreaterThanOrEqualTo("PLATE_WELLSEARCH:WELL-A01");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE", "/CISD/DEFAULT/RP1-B1X",
                "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02", "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP2-A1X",
                "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithCodeThatIsGreaterThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatIsGreaterThan("PLATE_WELLSEARCH:WELL-A01");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X",
                "/CISD/DEFAULT/RP2-A1X", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02",
                "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE");
    }

    @Test
    public void testSearchWithPropertyThatIsLessThanOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsLessThanOrEqualTo("test comment");
        testSearch(TEST_USER, criteria, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3", "/TEST-SPACE/TEST-PROJECT/EV-TEST");
    }

    @Test
    public void testSearchWithPropertyThatIsLessThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsLessThan("test comment");
        testSearch(TEST_USER, criteria, "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");
    }

    @Test
    public void testSearchWithPropertyThatIsGreaterThanOrEqualTo()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsGreaterThanOrEqualTo("test comment");
        testSearch(TEST_USER, criteria, "/CISD/3VCP7", "/CISD/NEMO/CP-TEST-1", "/TEST-SPACE/TEST-PROJECT/EV-TEST");
    }

    @Test
    public void testSearchWithPropertyThatIsGreaterThan()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("COMMENT").thatIsGreaterThan("test comment");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withProperty(propertyType.getPermId()).thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.VARCHAR);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "Test Space ");
        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withProperty(propertyType.getPermId()).thatEquals("Test Space ");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodes()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("RP1-A2X", "RP1-B1X", "PLATE_WELLSEARCH:WELL-A01",
                "PLATE_WELLSEARCH:WELL-A02"));
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X",
                "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithIncorrectCodes()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("/CISD/DEFAULT/RP1-A2X/ABC", "/A/B/C/D"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithCodesEmpty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList());
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodesWithNull()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList(new String[] { null }));
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodesWithObjectArray()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        CodesSearchCriteria codesSearchCriteria = criteria.withCodes();
        List codes = new ArrayList();
        codes.add("RP1-A2X");
        codesSearchCriteria.setFieldValue(codes);
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X");
    }

    @Test
    public void testSearchWithCodesEmptyAndOther()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList());
        criteria.withCode().thatEquals("RP1-A2X");
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X");
    }

    @Test
    public void testSearchWithCodeThatEqualsWithStarWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().withWildcards().thatEquals("RP1-*X");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEquals("RP1-*X");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithCodeThatEqualsWithQuestionMarkWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().withWildcards().thatEquals("CP???1");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/CP1-A1", "/CISD/DEFAULT/CP1-B1", "/CISD/DEFAULT/CP2-A1");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEquals("CP???1");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithCodeThatStartsWithStarWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().withWildcards().thatStartsWith("PLATE_WELLSEARCH:W*L-");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().withoutWildcards().thatStartsWith("PLATE_WELLSEARCH:W*L-");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithCodeThatStartsWithQuestionMarkWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().withWildcards().thatStartsWith("CP?-");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/CP1-A1", "/CISD/DEFAULT/CP1-A2", "/CISD/DEFAULT/CP1-B1", "/CISD/DEFAULT/CP2-A1");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().withoutWildcards().thatStartsWith("CP?-");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithCodeThatEndsWithStarWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().withWildcards().thatEndsWith("NOR*L");
        testSearch(TEST_USER, criteria1, "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEndsWith("NOR*L");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithCodeThatEndsWithQuestionMarkWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withCode().withWildcards().thatEndsWith("-??2");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEndsWith("-??2");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchForAll()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        testSearch(TEST_USER, criteria, 701);
    }

    @Test
    public void testSearchForAllSpaceSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode();
        testSearch(TEST_USER, criteria, 379);
    }

    @Test
    public void testSearchForAllSharedSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria().withoutSpace();
        testSearch(TEST_USER, criteria, 322);
    }

    @Test
    public void testSearchWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criteria, 1);

        criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodeInContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("PLATE_WELLSEARCH:WELL-A01");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithTypeIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("REINFECT_PLATE"));
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCodeWithWildcard()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withType().withCode().withWildcards().thatEquals("REINFECT_PLAT*");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X", "/CISD/DEFAULT/RP2-A1X");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withType().withCode().withoutWildcards().thatEquals("REINFECT_PLAT*");
        testSearch(TEST_USER, criteria2);
    }

    @Test
    public void testSearchWithTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withSemanticAnnotations().withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = searchSamples(sessionToken, criteria, fo);

        assertEquals(samples.size(), 6, samples.toString());
        for (Sample sample : samples)
        {
            assertEquals(sample.getType().getCode(), "DILUTION_PLATE");
        }
    }

    @Test
    public void testSearchWithTypeWithSemanticAnnotationsWithIdOrIdAndWithSampleCodeThatContains()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("P2");

        SemanticAnnotationSearchCriteria annotationCriteria = criteria.withType().withSemanticAnnotations();
        annotationCriteria.withOrOperator();
        annotationCriteria.withId().thatEquals(new SemanticAnnotationPermId("ST_MASTER_PLATE"));
        annotationCriteria.withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = searchSamples(sessionToken, criteria, fo);

        assertEquals(samples.size(), 2, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertContains("P2", sample.getCode());
            AssertionUtil.assertCollectionContains(Arrays.asList("MASTER_PLATE", "DILUTION_PLATE"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationIsDefinedAtPropertyAssignment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("ST_CELL_PLATE_PT_ORGANISM"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = searchSamples(sessionToken, criteria, fo);

        assertEquals(samples.size(), 14, samples.toString());
        for (Sample sample : samples)
        {
            assertEquals(sample.getType().getCode(), "CELL_PLATE");
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationIsDefinedAtPropertyTypeLevelOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_ORGANISM"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = searchSamples(sessionToken, criteria, fo);

        assertEquals(samples.size(), 2, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContains(Arrays.asList("DELETION_TEST", "NORMAL"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithPropertyTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withPropertyType().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_DESCRIPTION"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = searchSamples(sessionToken, criteria, fo);

        assertEquals(samples.size(), 11, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContains(Arrays.asList("MASTER_PLATE", "CONTROL_LAYOUT", "DELETION_TEST"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPermId().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithExperiment()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A01");
        criteria.withExperiment();
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A01");
        criteria.withoutExperiment();
        testSearch(TEST_USER, criteria, "/CISD/CL1:A01", "/CISD/MP2-NO-CL:A01", "/CISD/CL-3V:A01", "/CISD/MP1-MIXED:A01");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP10"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentPermId("200811050952663-1029"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withPermId().thatEquals("200811050952663-1029");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withCode().thatEquals("EXP-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NEMO/DYNA-TEST-1");
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Sorting and paging failed for object.*")
    public void testSearchWithExperimentWithCodeSortingByExperiment()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withOrOperator();
        criteria.withExperiment().withCode().thatEquals("EXP-TEST-1");
        criteria.withExperiment().withCode().thatEquals("EXP-TEST-2");

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment().sortBy().code().asc();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        searchSamples(sessionToken, criteria, fetchOptions);
    }

    @Test
    public void testSearchWithExperimentWithTypeIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criteria, "/CISD/NOE/CP-TEST-2", "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithParent()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, new SampleFetchOptions());
        final Set<String> actualSet = samples.stream().map(sample -> sample.getIdentifier().getIdentifier()).collect(Collectors.toSet());

        assertCollectionContainsAtLeast(actualSet, "/CISD/3V-125", "/CISD/3V-126", "/CISD/NEMO/3VCP5", "/CISD/NEMO/3VCP6",
                "/CISD/3VCP7", "/CISD/3VCP8", "/CISD/CL-3V:A02", "/CISD/NEMO/CP-TEST-1", "/CISD/DEFAULT/CP1-A1",
                "/CISD/DEFAULT/CP1-A2", "/CISD/DEFAULT/CP1-B1", "/CISD/DEFAULT/CP2-A1", "/CISD/DP1-A", "/CISD/DP1-B", "/CISD/DP2-A",
                "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X", "/CISD/DEFAULT/RP2-A1X", "/DP", "/TEST-SPACE/TEST-PROJECT/EV-INVALID");
        assertCollectionDoesntContain(actualSet, "/CISD/MP1-MIXED");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithChildren()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, new SampleFetchOptions());
        final Set<String> actualSet = samples.stream().map(sample -> sample.getIdentifier().getIdentifier()).collect(Collectors.toSet());

        assertCollectionContainsAtLeast(actualSet, "/CISD/3V-125", "/CISD/CL-3V:A02", "/CISD/CL1:A03",
                "/CISD/NOE/CP-TEST-2", "/CISD/DEFAULT/CP1-A2", "/CISD/DEFAULT/CP1-B1", "/CISD/DEFAULT/CP2-A1", "/CISD/DP1-A", "/CISD/DP1-B",
                "/CISD/DP2-A", "/CISD/MP002-1", "/CISD/MP1-MIXED", "/CISD/MP2-NO-CL", "/MP", "/TEST-SPACE/TEST-PROJECT/EV-PARENT");
        assertCollectionDoesntContain(actualSet, "/CISD/B1B3", "/CISD/C1", "/CISD/C2", "/CISD/C3", "/CISD/CL-3V");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithParentWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SampleIdentifier("/CISD/MP002-1"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SamplePermId("200811050917877-331"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withPermId().thatEquals("200811050917877-331");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withCode().thatEquals("MP002-1");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withId().thatEquals(new SampleIdentifier("/CISD/NEMO/3VCP6"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withId().thatEquals(new SamplePermId("200811050946559-980"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withPermId().thatEquals("200811050946559-980");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withCode().thatEquals("3VCP6");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithCodeAndWithContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("WELL");
        criteria.withContainer();
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithContainer()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer();
        testSearch(TEST_USER, criteria, ALL_SAMPLE_IDS_WITH_CONTAINER);
    }

    @Test
    public void testSearchWithoutContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A1");
        criteria.withoutContainer();
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/CP1-A1", "/CISD/DEFAULT/CP2-A1", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithContainerWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SamplePermId("200811050924274-994"));
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SampleIdentifier("/CISD/B1B3"));
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithPermId()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withContainer().withPermId().thatEquals("200811050924274-994");
        testSearch(TEST_USER, criteria1, "/CISD/B1B3:B01", "/CISD/B1B3:B03");

        // Not existing sample permID as a container.
        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withContainer().withPermId().thatEquals("200811050924274-000");
        testSearch(TEST_USER, criteria2);

        // Existing sample permID which is not a container.
        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withContainer().withPermId().thatEquals("200811050925507-1000");
        testSearch(TEST_USER, criteria3);
    }

    @Test
    public void testSearchWithContainerWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withCode().thatEquals("B1B3");
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithCodeWithWildcardsMatchingComponents()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().withWildcards().thatEquals("B1B3*");
        testSearch(TEST_USER, criteria, "/CISD/B1B3", "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithAnyFieldMatchingCodeOfComponents()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("B1B3:B01");
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01");
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withCode().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP2-A1X");
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT/RP1-B1X");
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, 15);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        final Long count = (Long) sqlExecutor.execute("SELECT count(*) FROM samples WHERE modification_timestamp::DATE = ?::DATE",
                Arrays.asList("2009-08-18")).get(0).get("count");

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModificationDate().thatEquals("2009-08-18");
        testSearch(TEST_USER, criteria, Math.toIntExact(count));
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("\"very advanced stuff\"");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistrationDateWithVariousFormats()
    {
        final SampleSearchCriteria shortFormatCriteria = new SampleSearchCriteria();
        shortFormatCriteria.withAnyField().thatEquals("2009-02-09");
        testSearch(TEST_USER, shortFormatCriteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NEMO/DYNA-TEST-1", "/TEST-SPACE/TEST-PROJECT/FV-TEST",
                "/TEST-SPACE/TEST-PROJECT/EV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-INVALID", "/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID", "/TEST-SPACE/TEST-PROJECT/EV-PARENT",
                "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL", "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE", "/CISD/NOE/CP-TEST-2",
                "/CISD/DEFAULT/PLATE_WELLSEARCH", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02",
                "/TEST-SPACE/NOE/CP-TEST-4", "/CISD/NEMO/CP-TEST-3");

        final SampleSearchCriteria mediumFormatCriteria = new SampleSearchCriteria();
        mediumFormatCriteria.withAnyField().thatEquals("2009-02-09 12:09");
        testSearch(TEST_USER, mediumFormatCriteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NEMO/DYNA-TEST-1", "/TEST-SPACE/TEST-PROJECT/FV-TEST",
                "/TEST-SPACE/TEST-PROJECT/EV-TEST", "/TEST-SPACE/TEST-PROJECT/EV-INVALID", "/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID", "/TEST-SPACE/TEST-PROJECT/EV-PARENT",
                "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL", "/TEST-SPACE/TEST-PROJECT/SAMPLE-TO-DELETE", "/CISD/NOE/CP-TEST-2",
                "/CISD/DEFAULT/PLATE_WELLSEARCH", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02",
                "/TEST-SPACE/NOE/CP-TEST-4");

        final SampleSearchCriteria longFormatCriteria = new SampleSearchCriteria();
        longFormatCriteria.withAnyField().thatEquals("2009-02-09 12:09:50");
        testSearch(TEST_USER, longFormatCriteria, "/CISD/NOE/CP-TEST-2", "/CISD/DEFAULT/PLATE_WELLSEARCH",
                "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01", "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02", "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistrationDateWithVariousCriteriaKinds()
    {
        final SampleSearchCriteria equalsCriteria = new SampleSearchCriteria();
        equalsCriteria.withAnyField().thatEquals("2009-02-09");
        testSearch(TEST_USER, equalsCriteria, 15);

        final SampleSearchCriteria startsWithCriteria = new SampleSearchCriteria();
        startsWithCriteria.withAnyField().thatStartsWith("2009-02-09");
        testSearch(TEST_USER, startsWithCriteria, 15);

        final SampleSearchCriteria endsWithCriteria = new SampleSearchCriteria();
        endsWithCriteria.withAnyField().thatEndsWith("2009-02-09");
        testSearch(TEST_USER, endsWithCriteria, 15);

        final SampleSearchCriteria containsCriteria = new SampleSearchCriteria();
        containsCriteria.withAnyField().thatContains("2009-02-09");
        testSearch(TEST_USER, containsCriteria, 15);
    }

    @Test
    public void testSearchWithAnyFieldMatchingModifiersUserId()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("test_role");
        testSearch(TEST_USER, criteria, "/CISD/CL1", "/CISD/NEMO/CP-TEST-1", "/TEST-SPACE/TEST-PROJECT/FV-TEST");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("\"CP-TEST-2\"");
        testSearch(TEST_USER, criteria, "/CISD/NOE/CP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingIdentifier()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withAnyField().withWildcards().thatEquals("/CISD/N*/CP-TEST-*");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withAnyField().withoutWildcards().thatEquals("/CISD/CP-TEST-*");
        testSearch(TEST_USER, criteria2);

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withAnyField().thatStartsWith("/CISD/NEMO/DYNA");
        testSearch(TEST_USER, criteria3, "/CISD/NEMO/DYNA-TEST-1");

        final SampleSearchCriteria criteria4 = new SampleSearchCriteria();
        criteria4.withAnyField().thatEndsWith("-1");
        testSearch(TEST_USER, criteria4, "/CISD/NEMO/CP-TEST-1", "/CISD/NEMO/DYNA-TEST-1", "/CISD/MP002-1");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistratorOrModifier()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withAnyField().thatEquals("etlserver");
        testSearch(TEST_USER, criteria1, "/CISD/DEFAULT/RP2-A1X", "/CISD/DEFAULT/RP1-A2X", "/CISD/DEFAULT/RP1-B1X");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withAnyField().thatEquals("test_role");
        testSearch(TEST_USER, criteria2, "/CISD/CL1", "/CISD/NEMO/CP-TEST-1", "/TEST-SPACE/TEST-PROJECT/FV-TEST");
    }

    @Test
    public void testSearchWithAnyFieldMatchingSampleType()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("DYNAMIC_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("HUM");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatStartsWith("CP");
        criteria.withCode().thatEndsWith("-1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");
    }

    @Test
    public void testSearchWithCachingNoCache()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.cacheMode(CacheMode.NO_CACHE);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        v3api.logout(sessionToken);

        assertEquals(samples1.get(0).getPermId(), samples2.get(0).getPermId());
        assertEquals(samples1.get(1).getPermId(), samples2.get(1).getPermId());

        assertNotSame(samples1.get(0), samples2.get(0));
        assertNotSame(samples1.get(1), samples2.get(1));
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NOE/CP-TEST-2"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NEMO/CP-TEST-3"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        fo.sortBy().code().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/NEMO/CP-TEST-3", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/3V-125"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().identifier().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/NEMO/CP-TEST-1", "/TEST-SPACE/NOE/CP-TEST-4");

        fo.sortBy().identifier().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/TEST-SPACE/NOE/CP-TEST-4", "/CISD/NEMO/CP-TEST-1", "/CISD/3V-125");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/3V-125"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/NEMO/CP-TEST-1", "/TEST-SPACE/NOE/CP-TEST-4", "/CISD/3V-125");

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/TEST-SPACE/NOE/CP-TEST-4", "/CISD/NEMO/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingBySampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final String propertyName = propertyType.getPermId();
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation1 = new SampleCreation();
        sampleCreation1.setAutoGeneratedCode(true);
        sampleCreation1.setTypeId(sampleType);
        sampleCreation1.setProperty(propertyName, "/CISD/C2");

        final SampleCreation sampleCreation2 = new SampleCreation();
        sampleCreation2.setAutoGeneratedCode(true);
        sampleCreation2.setTypeId(sampleType);
        sampleCreation2.setProperty(propertyName, "/CISD/C1");

        final SampleCreation sampleCreation3 = new SampleCreation();
        sampleCreation3.setAutoGeneratedCode(true);
        sampleCreation3.setTypeId(sampleType);
        sampleCreation3.setProperty(propertyName, "/CISD/C3");

        final List<SamplePermId> samplePermIds = v3api.createSamples(sessionToken,
                Arrays.asList(sampleCreation1, sampleCreation2, sampleCreation3));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        samplePermIds.forEach(samplePermId -> criteria.withPermId().thatEquals(samplePermId.getPermId()));

        final SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().property(propertyName).asc();
        final List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSamplePermIdsInOrder(samples1, samplePermIds.get(1).getPermId(), samplePermIds.get(0).getPermId(),
                samplePermIds.get(2).getPermId());

        fo.sortBy().property(propertyName).desc();
        final List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSamplePermIdsInOrder(samples2, samplePermIds.get(2).getPermId(), samplePermIds.get(0).getPermId(),
                samplePermIds.get(1).getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByMaterialProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createAMaterialPropertyType(sessionToken,
                new EntityTypePermId("VIRUS", EntityKind.MATERIAL));
        final String propertyName = propertyType.getPermId();
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation1 = new SampleCreation();
        sampleCreation1.setAutoGeneratedCode(true);
        sampleCreation1.setTypeId(sampleType);
        sampleCreation1.setProperty(propertyName, "MHV");

        final SampleCreation sampleCreation2 = new SampleCreation();
        sampleCreation2.setAutoGeneratedCode(true);
        sampleCreation2.setTypeId(sampleType);
        sampleCreation2.setProperty(propertyName, "HRV2");

        final SampleCreation sampleCreation3 = new SampleCreation();
        sampleCreation3.setAutoGeneratedCode(true);
        sampleCreation3.setTypeId(sampleType);
        sampleCreation3.setProperty(propertyName, "HSV1");

        final List<SamplePermId> samplePermIds = v3api.createSamples(sessionToken,
                Arrays.asList(sampleCreation1, sampleCreation2, sampleCreation3));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        samplePermIds.forEach(samplePermId -> criteria.withPermId().thatEquals(samplePermId.getPermId()));

        final SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().property(propertyName).asc();
        final List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSamplePermIdsInOrder(samples1, samplePermIds.get(1).getPermId(), samplePermIds.get(2).getPermId(),
                samplePermIds.get(0).getPermId());

        fo.sortBy().property(propertyName).desc();
        final List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSamplePermIdsInOrder(samples2, samplePermIds.get(0).getPermId(), samplePermIds.get(2).getPermId(),
                samplePermIds.get(1).getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByPropertyWithTextValues()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertEquals(samples1.get(0).getProperty("COMMENT"), "extremely simple stuff");
        assertEquals(samples1.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples1.get(2).getProperty("COMMENT"), "very advanced stuff");

        fo.sortBy().property("COMMENT").desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertEquals(samples2.get(0).getProperty("COMMENT"), "very advanced stuff");
        assertEquals(samples2.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples2.get(2).getProperty("COMMENT"), "extremely simple stuff");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByMissingProperty()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");
        criteria.withPermId().thatEquals("200902091250077-1051");
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050945092-977");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        final List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertEquals(samples1.size(), 6);
        assertEquals(samples1.get(0).getProperty("COMMENT"), "extremely simple stuff");
        assertEquals(samples1.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples1.get(2).getProperty("COMMENT"), "very advanced stuff");

        fo.sortBy().property("COMMENT").desc();
        final List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertEquals(samples2.size(), 6);
        assertEquals(samples2.get(0).getProperty("COMMENT"), "very advanced stuff");
        assertEquals(samples2.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples2.get(2).getProperty("COMMENT"), "extremely simple stuff");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByMultipleProperties()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        fo.sortBy().property("SIZE").desc();

        try
        {
            // No exception should be thrown because both properties exist in the result set.
            final List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
            assertEquals(samples1.get(0).getProperty("COMMENT"), "extremely simple stuff");
            assertEquals(samples1.get(1).getProperty("COMMENT"), "stuff like others");
            assertEquals(samples1.get(2).getProperty("COMMENT"), "very advanced stuff");
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    @Test
    public void testSearchWithSortingByMultiplePropertiesWithMissingProperties()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050919915-8");
        criteria.withPermId().thatEquals("201206191219327-1054");
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");
        criteria.withPermId().thatEquals("200902091250077-1051");
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050945092-977");
        criteria.withPermId().thatEquals("200811050946559-981");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("BACTERIUM").asc();
        fo.sortBy().property("OFFSET").desc();
        fo.sortBy().property("COMMENT").asc();
        fo.sortBy().property("ORGANISM").asc();
        fo.sortBy().property("SIZE").asc();

        try
        {
            final List<Sample> samples = searchSamples(sessionToken, criteria, fo);
            assertEquals(samples.size(), 9);
            assertEquals(samples.get(0).getPermId().toString(), "200902091219327-1025");
            assertEquals(samples.get(1).getPermId().toString(), "201206191219327-1054");
            assertEquals(samples.get(2).getPermId().toString(), "200902091250077-1026");
            assertEquals(samples.get(3).getPermId().toString(), "200902091250077-1051");
            assertEquals(samples.get(4).getPermId().toString(), "200811050945092-976");
            assertEquals(samples.get(5).getPermId().toString(), "200811050945092-977");
            assertEquals(samples.get(6).getPermId().toString(), "200902091225616-1027");
            assertEquals(samples.get(7).getPermId().toString(), "200811050946559-981");
            assertEquals(samples.get(8).getPermId().toString(), "200811050919915-8");
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    @Test
    public void testSearchWithSortingByPropertyWithIntegerValues()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");
        criteria.withPermId().thatEquals("200811050946559-981");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("SIZE").asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertEquals(samples1.get(0).getProperty("SIZE"), "123");
        assertEquals(samples1.get(1).getProperty("SIZE"), "321");
        assertEquals(samples1.get(2).getProperty("SIZE"), "666");
        assertEquals(samples1.get(3).getProperty("SIZE"), "4711");

        fo.sortBy().property("SIZE").desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertEquals(samples2.get(0).getProperty("SIZE"), "4711");
        assertEquals(samples2.get(1).getProperty("SIZE"), "666");
        assertEquals(samples2.get(2).getProperty("SIZE"), "321");
        assertEquals(samples2.get(3).getProperty("SIZE"), "123");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByRegistrationDate()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050924274-995");
        criteria.withPermId().thatEquals("200811050927630-1004");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().registrationDate().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/B1B3:B01", "/CISD/MP1-MIXED:A01");

        fo.sortBy().registrationDate().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/MP1-MIXED:A01", "/CISD/B1B3:B01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByModificationDate()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1053");
        criteria.withPermId().thatEquals("200811050928301-1012");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().modificationDate().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/MP2-NO-CL:B02", "/CISD/NEMO/DYNA-TEST-1");

        fo.sortBy().modificationDate().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/NEMO/DYNA-TEST-1", "/CISD/MP2-NO-CL:B02");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByMultipleFields()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050919915-9");
        criteria.withPermId().thatEquals("200811050944030-974");
        criteria.withPermId().thatEquals("200811050924274-995");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        fo.sortBy().registrationDate().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CL1:A01", "/CISD/CL-3V:A01", "/CISD/B1B3:B01");

        fo.sortBy().code().asc();
        fo.sortBy().registrationDate().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/CL-3V:A01", "/CISD/CL1:A01", "/CISD/B1B3:B01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingTopLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        fo.sortBy().code().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/NEMO/CP-TEST-3", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/NEMO/3VCP5", "/CISD/NEMO/3VCP6", "/CISD/3VCP7", "/CISD/3VCP8");
        assertSampleIdentifiersInOrder(samples1.get(1).getChildren(), "/CISD/DP1-A", "/CISD/DP1-B");

        fo.withChildren().sortBy().code().desc();
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/3VCP8", "/CISD/3VCP7", "/CISD/NEMO/3VCP6", "/CISD/NEMO/3VCP5");
        assertSampleIdentifiersInOrder(samples2.get(1).getChildren(), "/CISD/DP1-B", "/CISD/DP1-A");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();

        fo.from(0).count(1);
        final SearchResult<Sample> searchResult1 = v3api.searchSamples(sessionToken, criteria, fo);
        assertEquals(searchResult1.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult1.getObjects(), "/CISD/NEMO/CP-TEST-1");

        fo.from(1).count(1);
        final SearchResult<Sample> searchResult2 = v3api.searchSamples(sessionToken, criteria, fo);
        assertEquals(searchResult2.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult2.getObjects(), "/CISD/NOE/CP-TEST-2");

        fo.from(2).count(1);
        final SearchResult<Sample> searchResult3 = v3api.searchSamples(sessionToken, criteria, fo);
        assertEquals(searchResult3.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult3.getObjects(), "/CISD/NEMO/CP-TEST-3");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopLevelSpecialCases()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleFetchOptions fo1 = new SampleFetchOptions();
        fo1.sortBy().code().asc();

        fo1.count(2);
        final SearchResult<Sample> searchResult1 = v3api.searchSamples(sessionToken, criteria, fo1);
        assertEquals(searchResult1.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult1.getObjects(), "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");

        final SampleFetchOptions fo2 = new SampleFetchOptions();
        fo2.sortBy().code().asc();

        fo2.from(1);
        final SearchResult<Sample> searchResult2 = v3api.searchSamples(sessionToken, criteria, fo2);
        assertEquals(searchResult2.getTotalCount(), 3);
        assertSampleIdentifiersInOrder(searchResult2.getObjects(), "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");
    }

    @Test
    public void testSearchWithPagingSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        fo.withChildren().from(0).count(1);
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/NEMO/3VCP5");
        assertSampleIdentifiersInOrder(samples1.get(1).getChildren(), "/CISD/DP1-A");

        fo.withChildren().from(1).count(1);
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/NEMO/3VCP6");
        assertSampleIdentifiersInOrder(samples2.get(1).getChildren(), "/CISD/DP1-B");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopAndSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        fo.from(0).count(1);
        fo.withChildren().from(0).count(1);
        List<Sample> samples1 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/NEMO/3VCP5");

        fo.from(1).count(1);
        fo.withChildren().from(1).count(1);
        List<Sample> samples2 = searchSamples(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/DP1-B");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchNumeric()
    {
        // SIZE: 4711 CODE: 3VCP7
        // SIZE: 123 CODE: CP-TEST-1
        // SIZE: 321 CODE: CP-TEST-2
        // SIZE: 666 CODE: CP-TEST-3

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions sortByCodeFO = new SampleFetchOptions();
        sortByCodeFO.sortBy().code().asc();
        sortByCodeFO.withProperties();

        // Greater or Equals - Giving integer as real
        SampleSearchCriteria criteriaGOE = new SampleSearchCriteria();
        criteriaGOE.withNumberProperty("SIZE").thatIsGreaterThanOrEqualTo(321.0);
        List<Sample> samplesGOE1 = searchSamples(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE1, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        criteriaGOE = new SampleSearchCriteria();
        criteriaGOE.withNumberProperty("SIZE").thatIsGreaterThanOrEqualTo(320.9);
        List<Sample> samplesGOE = searchSamples(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        criteriaGOE = new SampleSearchCriteria();
        criteriaGOE.withProperty("SIZE").thatIsGreaterThanOrEqualTo("321.0");
        samplesGOE = searchSamples(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        // Greater - Giving integer as real
        SampleSearchCriteria criteriaG = new SampleSearchCriteria();
        criteriaG.withNumberProperty("SIZE").thatIsGreaterThan(321.0);
        List<Sample> samplesG = searchSamples(sessionToken, criteriaG, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesG, "/CISD/3VCP7", "/CISD/NEMO/CP-TEST-3");

        criteriaG = new SampleSearchCriteria();
        criteriaG.withProperty("SIZE").thatIsGreaterThan("321.0");
        samplesG = searchSamples(sessionToken, criteriaG, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesG, "/CISD/3VCP7", "/CISD/NEMO/CP-TEST-3");

        // Equals As Text - Real
        SampleSearchCriteria criteriaETxt2 = new SampleSearchCriteria();
        criteriaETxt2.withProperty("SIZE").thatEquals("666.0");
        List<Sample> samplesETxt2 = searchSamples(sessionToken, criteriaETxt2, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesETxt2, "/CISD/NEMO/CP-TEST-3");

        // Equals As Text - Integer
        SampleSearchCriteria criteriaETxt = new SampleSearchCriteria();
        criteriaETxt.withProperty("SIZE").thatEquals("666");
        List<Sample> samplesETxt = searchSamples(sessionToken, criteriaETxt, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesETxt, "/CISD/NEMO/CP-TEST-3");

        // Equals
        SampleSearchCriteria criteriaE = new SampleSearchCriteria();
        criteriaE.withNumberProperty("SIZE").thatEquals(666);
        List<Sample> samplesE = searchSamples(sessionToken, criteriaE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesE, "/CISD/NEMO/CP-TEST-3");

        // Less
        SampleSearchCriteria criteriaL = new SampleSearchCriteria();
        criteriaL.withNumberProperty("SIZE").thatIsLessThan(666);
        List<Sample> samplesL = searchSamples(sessionToken, criteriaL, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesL, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");

        // Less or Equals
        SampleSearchCriteria criteriaLOE = new SampleSearchCriteria();
        criteriaLOE.withNumberProperty("SIZE").thatIsLessThanOrEqualTo(321);
        List<Sample> samplesLOE = searchSamples(sessionToken, criteriaLOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesLOE, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchListableOnlyShouldNotFindUnlistable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("EV");
        List<Sample> samples = searchSamples(sessionToken, criteria, fo);
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID, "
                + "/TEST-SPACE/TEST-PROJECT/EV-PARENT, /TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST]");
        criteria.withType().withListable().thatEquals(true);

        samples = searchSamples(sessionToken, criteria, fo);

        identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-PARENT, "
                + "/TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST]");
    }

    @Test
    public void testSearchUnlistableOnlyShouldFindListable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("EV");
        List<Sample> samples = searchSamples(sessionToken, criteria, fo);
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/TEST-PROJECT/EV-INVALID, /TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID, "
                + "/TEST-SPACE/TEST-PROJECT/EV-PARENT, /TEST-SPACE/TEST-PROJECT/EV-PARENT-NORMAL, /TEST-SPACE/TEST-PROJECT/EV-TEST]");
        criteria.withType().withListable().thatEquals(false);

        samples = searchSamples(sessionToken, criteria, fo);

        identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/TEST-PROJECT/EV-NOT_INVALID]");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/TEST-PROJECT/EV-TEST"));

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            testSearch(user.getUserId(), criteria, "/TEST-SPACE/TEST-PROJECT/EV-TEST");
        } else
        {
            testSearch(user.getUserId(), criteria);
        }
    }

    @Test
    public void testSearchWithAnyFieldMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyField().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAnyPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyProperty().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithDateDatePropertyThatEquals()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withDateProperty(propertyType.getPermId()).thatEquals("20-2-17");

        // When
        final List<Sample> samples1 = v3api.searchSamples(sessionToken, criteria1, new SampleFetchOptions())
                .getObjects();

        // Then
        assertSampleIdentifiers(samples1, "/CISD/SAMPLE_WITH_DATE_PROPERTY");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withProperty(propertyType.getPermId()).thatEquals("20-2-17");

        // When
        final List<Sample> samples2 = v3api.searchSamples(sessionToken, criteria2, new SampleFetchOptions())
                .getObjects();

        // Then
        assertSampleIdentifiers(samples2, "/CISD/SAMPLE_WITH_DATE_PROPERTY");
    }

    @Test
    public void testSearchWithDateDatePropertyWithInvalidCriteria()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleSearchCriteria criteria = new SampleSearchCriteria();
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
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        final EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        final SampleCreation creation1 = new SampleCreation();
        creation1.setCode("SAMPLE_WITH_DATE_PROPERTY1");
        creation1.setTypeId(sampleType);
        creation1.setSpaceId(new SpacePermId("CISD"));
        creation1.setProperty(propertyType.getPermId(), "2/17/20");
        final SampleCreation creation2 = new SampleCreation();
        creation2.setCode("SAMPLE_WITH_DATE_PROPERTY2");
        creation2.setTypeId(sampleType);
        creation2.setSpaceId(new SpacePermId("CISD"));
        creation2.setProperty(propertyType.getPermId(), "2020-02-16");
        v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withDateProperty(propertyType.getPermId()).thatIsLaterThan("2020-02-16");

        // When
        final List<Sample> samples1 = v3api.searchSamples(sessionToken, criteria1, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples1.get(0).getIdentifier().getIdentifier(), "/CISD/SAMPLE_WITH_DATE_PROPERTY1");
        assertEquals(samples1.size(), 1);

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withDateProperty(propertyType.getPermId()).thatIsLaterThan("2020-02-16");

        // When
        final List<Sample> samples2 = v3api.searchSamples(sessionToken, criteria2, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples2.get(0).getIdentifier().getIdentifier(), "/CISD/SAMPLE_WITH_DATE_PROPERTY1");
        assertEquals(samples2.size(), 1);

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withDateProperty(propertyType.getPermId()).thatIsLaterThan("2020-02-17");

        // When
        final List<Sample> samples3 = v3api.searchSamples(sessionToken, criteria3, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples3.size(), 0);

        final SampleSearchCriteria criteria4 = new SampleSearchCriteria();
        criteria4.withDateProperty(propertyType.getPermId()).thatIsLaterThan("2020-02-17");

        // When
        final List<Sample> samples4 = v3api.searchSamples(sessionToken, criteria4, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples4.size(), 0);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsLaterOrEqual()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation1 = new SampleCreation();
        creation1.setCode("SAMPLE_WITH_DATE_PROPERTY1");
        creation1.setTypeId(sampleType);
        creation1.setSpaceId(new SpacePermId("CISD"));
        creation1.setProperty(propertyType.getPermId(), "2/17/20");
        SampleCreation creation2 = new SampleCreation();
        creation2.setCode("SAMPLE_WITH_DATE_PROPERTY2");
        creation2.setTypeId(sampleType);
        creation2.setSpaceId(new SpacePermId("CISD"));
        creation2.setProperty(propertyType.getPermId(), "2020-02-16");
        v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("2020-02-16");

        // When
        final List<Sample> samples1 = v3api.searchSamples(sessionToken, criteria1, new SampleFetchOptions())
                .getObjects();

        // Then
        assertSampleIdentifiers(samples1, "/CISD/SAMPLE_WITH_DATE_PROPERTY1", "/CISD/SAMPLE_WITH_DATE_PROPERTY2");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withProperty(propertyType.getPermId()).thatIsGreaterThanOrEqualTo("2020-02-16");

        // When
        final List<Sample> samples2 = v3api.searchSamples(sessionToken, criteria2, new SampleFetchOptions())
                .getObjects();

        // Then
        assertSampleIdentifiers(samples2, "/CISD/SAMPLE_WITH_DATE_PROPERTY1", "/CISD/SAMPLE_WITH_DATE_PROPERTY2");
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlier()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        final EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createSamples(sessionToken, Collections.singletonList(creation));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withDateProperty(propertyType.getPermId()).thatIsEarlierThan("1990-11-10");

        // When
        final List<Sample> samples1 = v3api.searchSamples(sessionToken, criteria1, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples1.get(0).getIdentifier().getIdentifier(), "/CISD/SAMPLE_WITH_DATE_PROPERTY");
        assertEquals(samples1.size(), 1);

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withDateProperty(propertyType.getPermId()).thatIsEarlierThan("1990-11-10");

        // When
        final List<Sample> samples2 = v3api.searchSamples(sessionToken, criteria2, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples2.get(0).getIdentifier().getIdentifier(), "/CISD/SAMPLE_WITH_DATE_PROPERTY");
        assertEquals(samples2.size(), 1);

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withDateProperty(propertyType.getPermId()).thatIsEarlierThan("1990-11-09");

        // When
        final List<Sample> samples3 = v3api.searchSamples(sessionToken, criteria3, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples3.size(), 0);

        final SampleSearchCriteria criteria4 = new SampleSearchCriteria();
        criteria4.withDateProperty(propertyType.getPermId()).thatIsEarlierThan("1990-11-09");

        // When
        final List<Sample> samples4 = v3api.searchSamples(sessionToken, criteria4, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples4.size(), 0);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlierOrEqual()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withDateProperty(propertyType.getPermId()).thatIsEarlierThanOrEqualTo("1990-11-10");

        // When
        final List<Sample> samples1 = v3api.searchSamples(sessionToken, criteria1, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples1.get(0).getIdentifier().getIdentifier(), "/CISD/SAMPLE_WITH_DATE_PROPERTY");
        assertEquals(samples1.size(), 1);

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withProperty(propertyType.getPermId()).thatIsLessThanOrEqualTo("1990-11-10");

        // When
        final List<Sample> samples2 = v3api.searchSamples(sessionToken, criteria2, new SampleFetchOptions())
                .getObjects();

        // Then
        assertEquals(samples2.get(0).getIdentifier().getIdentifier(), "/CISD/SAMPLE_WITH_DATE_PROPERTY");
        assertEquals(samples2.size(), 1);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlierWithTimezone()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        DatePropertySearchCriteria datePropertySearchCriteria = criteria.withDateProperty(propertyType.getPermId());
        datePropertySearchCriteria.withTimeZone(6);
        datePropertySearchCriteria.thatIsEarlierThanOrEqualTo("1990-11-09");

        // When
        assertUserFailureException(Void -> v3api.searchSamples(sessionToken, criteria, new SampleFetchOptions()),
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
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyProperty().thatIsLessThanOrEqualTo("2009-02-10");

        // When
        List<Sample> samples = v3api.searchSamples(sessionToken, criteria, new SampleFetchOptions()).getObjects();

        // Then
        assertSampleIdentifiers(samples, "/CISD/NEMO/CP-TEST-1", "/CISD/SAMPLE_WITH_DATE_PROPERTY");
        assertEquals(samples.size(), 2);
    }

    @Test
    public void testSearchWithAnyPropertyThatIsLater()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "4009-02-10");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAndOperator();
        criteria.withAnyProperty().thatIsGreaterThanOrEqualTo("4009-02-10");
        criteria.withSpace().withCode().thatEquals("CISD");

        // When
        List<Sample> samples = v3api.searchSamples(sessionToken, criteria, new SampleFetchOptions()).getObjects();

        // Then
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/CISD/3V-125, /CISD/3V-126, /CISD/3VCP7, /CISD/CL1, "
                + "/CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /CISD/NEMO/CP-TEST-1, "
                + "/CISD/NEMO/CP-TEST-3, /CISD/NOE/CP-TEST-2, /CISD/SAMPLE_WITH_DATE_PROPERTY]");
    }

    @Test
    public void testSearchWithDateDatePropertyWithTimezone()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        DatePropertySearchCriteria datePropertySearchCriteria = criteria.withDateProperty(propertyType.getPermId());
        datePropertySearchCriteria.withTimeZone(-4);
        datePropertySearchCriteria.thatEquals("2020-02-17");

        // When
        assertUserFailureException(Void -> v3api.searchSamples(sessionToken, criteria, new SampleFetchOptions()),
                // Then
                "Search criteria with time zone doesn't make sense for property " + propertyType.getPermId()
                        + " of data type " + DataType.DATE);
    }

    @Test
    public void testSearchForSampleWithIntegerPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAnIntegerPropertyType(sessionToken, "INT_NUMBER");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("INTEGER_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("INT_NUMBER", "123");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaStartsWithMatch = new SampleSearchCriteria();
        criteriaStartsWithMatch.withProperty("INT_NUMBER").thatStartsWith("12");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaStartsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "INTEGER"));

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("INT_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "INTEGER"));

        final SampleSearchCriteria criteriaContainsMatch = new SampleSearchCriteria();
        criteriaContainsMatch.withProperty("INT_NUMBER").thatContains("23");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaContainsMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "INTEGER"));
    }

    @Test
    public void testSearchForSampleWithRealPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createARealPropertyType(sessionToken, "REAL_NUMBER");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("REAL_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("REAL_NUMBER", "1.23");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaStartsWithMatch = new SampleSearchCriteria();
        criteriaStartsWithMatch.withProperty("REAL_NUMBER").thatStartsWith("1.2");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaStartsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "REAL"));

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("REAL_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "REAL"));

        final SampleSearchCriteria criteriaContainsMatch = new SampleSearchCriteria();
        criteriaContainsMatch.withProperty("REAL_NUMBER").thatContains(".2");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaContainsMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "REAL"));
    }

    @Test
    public void testSearchForSampleWithStringPropertyWithAsterisk()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAVarcharPropertyType(sessionToken, "SHORT_TEXT");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation1 = new SampleCreation();
        sampleCreation1.setCode("TEXT_PROPERTY_TEST_1");
        sampleCreation1.setTypeId(sampleType);
        sampleCreation1.setSpaceId(new SpacePermId("CISD"));
        sampleCreation1.setProperty("SHORT_TEXT", "test");

        final SampleCreation sampleCreation2 = new SampleCreation();
        sampleCreation2.setCode("TEXT_PROPERTY_TEST_2");
        sampleCreation2.setTypeId(sampleType);
        sampleCreation2.setSpaceId(new SpacePermId("CISD"));
        sampleCreation2.setProperty("SHORT_TEXT", "t*t");

        v3api.createSamples(sessionToken, Arrays.asList(sampleCreation1, sampleCreation2));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withStringProperty("SHORT_TEXT").withoutWildcards().thatEquals("t*t");
        testSearch(TEST_USER, criteria1, "/CISD/TEXT_PROPERTY_TEST_2");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withStringProperty("SHORT_TEXT").withWildcards().thatEquals("t*t");
        testSearch(TEST_USER, criteria2, "/CISD/TEXT_PROPERTY_TEST_1", "/CISD/TEXT_PROPERTY_TEST_2");

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withStringProperty("SHORT_TEXT").withWildcards().thatEquals("t\\*t");
        testSearch(TEST_USER, criteria3, "/CISD/TEXT_PROPERTY_TEST_2");
    }

    @Test
    public void testSearchForSampleWithStringPropertyWithQuestionMark()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAVarcharPropertyType(sessionToken, "SHORT_TEXT");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation1 = new SampleCreation();
        sampleCreation1.setCode("TEXT_PROPERTY_TEST_1");
        sampleCreation1.setTypeId(sampleType);
        sampleCreation1.setSpaceId(new SpacePermId("CISD"));
        sampleCreation1.setProperty("SHORT_TEXT", "test");

        final SampleCreation sampleCreation2 = new SampleCreation();
        sampleCreation2.setCode("TEXT_PROPERTY_TEST_2");
        sampleCreation2.setTypeId(sampleType);
        sampleCreation2.setSpaceId(new SpacePermId("CISD"));
        sampleCreation2.setProperty("SHORT_TEXT", "te?t");

        v3api.createSamples(sessionToken, Arrays.asList(sampleCreation1, sampleCreation2));

        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withStringProperty("SHORT_TEXT").withoutWildcards().thatEquals("te?t");
        testSearch(TEST_USER, criteria1, "/CISD/TEXT_PROPERTY_TEST_2");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withStringProperty("SHORT_TEXT").withWildcards().thatEquals("te?t");
        testSearch(TEST_USER, criteria2, "/CISD/TEXT_PROPERTY_TEST_1", "/CISD/TEXT_PROPERTY_TEST_2");

        final SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withStringProperty("SHORT_TEXT").withWildcards().thatEquals("te\\?t");
        testSearch(TEST_USER, criteria3, "/CISD/TEXT_PROPERTY_TEST_2");

        // Testing negation as well
        final SampleSearchCriteria criteria4 = new SampleSearchCriteria().withAndOperator();
        criteria4.withStringProperty("SHORT_TEXT").withWildcards().thatEquals("te?t");
        criteria4.withSubcriteria().negate().withStringProperty("SHORT_TEXT").withoutWildcards().thatEquals("te?t");
        testSearch(TEST_USER, criteria4, "/CISD/TEXT_PROPERTY_TEST_1");
    }

    @Test
    public void testSearchForSampleWithStringPropertyQueriedAsIntegerOrDate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAVarcharPropertyType(sessionToken, "SHORT_TEXT");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SHORT_TEXT_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("SHORT_TEXT", "123");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaWithNumberProperty = new SampleSearchCriteria();
        criteriaWithNumberProperty.withNumberProperty("SHORT_TEXT").thatEquals(123);
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaWithNumberProperty, new SampleFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "NumberPropertySearchCriteria", "VARCHAR"));

        final SampleSearchCriteria criteriaWithDateProperty = new SampleSearchCriteria();
        criteriaWithDateProperty.withDateProperty("SHORT_TEXT").thatEquals("1990-11-09");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaWithDateProperty, new SampleFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "DatePropertySearchCriteria", "VARCHAR"));
    }

    @Test
    public void testSearchForSampleWithBooleanProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("BOOLEAN_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("BOOLEAN", "false");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaEqualsFalse = new SampleSearchCriteria();
        criteriaEqualsFalse.withProperty("BOOLEAN").thatEquals("false");
        assertEquals(searchSamples(sessionToken, criteriaEqualsFalse, new SampleFetchOptions()).size(), 1);

        final SampleSearchCriteria criteriaEqualsTrue = new SampleSearchCriteria();
        criteriaEqualsTrue.withProperty("BOOLEAN").thatEquals("true");
        assertEquals(searchSamples(sessionToken, criteriaEqualsTrue, new SampleFetchOptions()).size(), 0);

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("BOOLEAN").thatEquals("bla");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                "String does not represent a boolean: [bla]");
    }

    @Test
    public void testSearchForSampleWithBooleanPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("BOOLEAN_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("BOOLEAN", "false");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaStartsWithMatch = new SampleSearchCriteria();
        criteriaStartsWithMatch.withProperty("BOOLEAN").thatStartsWith("fa");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaStartsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "BOOLEAN"));

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("BOOLEAN").thatEndsWith("lse");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "BOOLEAN"));

        final SampleSearchCriteria criteriaContainsMatch = new SampleSearchCriteria();
        criteriaContainsMatch.withProperty("BOOLEAN").thatContains("als");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaContainsMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "BOOLEAN"));

        final SampleSearchCriteria criteriaLTMatch = new SampleSearchCriteria();
        criteriaLTMatch.withProperty("BOOLEAN").thatIsLessThan("true");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaLTMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThan", "BOOLEAN"));

        final SampleSearchCriteria criteriaLEMatch = new SampleSearchCriteria();
        criteriaLEMatch.withProperty("BOOLEAN").thatIsLessThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaLEMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThanOrEqualTo", "BOOLEAN"));

        final SampleSearchCriteria criteriaGTMatch = new SampleSearchCriteria();
        criteriaGTMatch.withProperty("BOOLEAN").thatIsGreaterThan("true");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaGTMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThan", "BOOLEAN"));

        final SampleSearchCriteria criteriaGEMatch = new SampleSearchCriteria();
        criteriaGEMatch.withProperty("BOOLEAN").thatIsGreaterThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaGEMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThanOrEqualTo", "BOOLEAN"));
    }

    @Test
    public void testSearchForSampleWithDateProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createADatePropertyType(sessionToken, "DATE");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("DATE_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("DATE", "2020-02-09");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleFetchOptions emptyFetchOptions = new SampleFetchOptions();

        final SampleSearchCriteria criteriaLTMatch = new SampleSearchCriteria();
        criteriaLTMatch.withProperty("DATE").thatIsLessThan("2020-02-10");
        final List<Sample> samplesLT = searchSamples(sessionToken, criteriaLTMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesLT, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLTDMatch = new SampleSearchCriteria();
        criteriaLTDMatch.withDateProperty("DATE").thatIsEarlierThan("2020-02-10");
        final List<Sample> samplesLTD = searchSamples(sessionToken, criteriaLTDMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesLTD, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLTEmptyMatch = new SampleSearchCriteria();
        criteriaLTEmptyMatch.withProperty("DATE").thatIsLessThan("2020-02-09");
        final List<Sample> samplesLTEmpty = searchSamples(sessionToken, criteriaLTEmptyMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesLTEmpty);

        final SampleSearchCriteria criteriaLTDEmptyMatch = new SampleSearchCriteria();
        criteriaLTDEmptyMatch.withDateProperty("DATE").thatIsEarlierThan("2020-02-09");
        final List<Sample> samplesLTDEmpty = searchSamples(sessionToken, criteriaLTDEmptyMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesLTDEmpty);

        final SampleSearchCriteria criteriaLEMatch1 = new SampleSearchCriteria();
        criteriaLEMatch1.withProperty("DATE").thatIsLessThanOrEqualTo("2020-02-09");
        final List<Sample> samplesLE1 = searchSamples(sessionToken, criteriaLEMatch1, emptyFetchOptions);
        assertSampleIdentifiers(samplesLE1, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLEDMatch1 = new SampleSearchCriteria();
        criteriaLEDMatch1.withDateProperty("DATE").thatIsEarlierThanOrEqualTo("2020-02-09");
        final List<Sample> samplesLED1 = searchSamples(sessionToken, criteriaLEDMatch1, emptyFetchOptions);
        assertSampleIdentifiers(samplesLED1, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLEMatch2 = new SampleSearchCriteria();
        criteriaLEMatch2.withProperty("DATE").thatIsLessThanOrEqualTo("2020-02-10");
        final List<Sample> samplesLE2 = searchSamples(sessionToken, criteriaLEMatch2, emptyFetchOptions);
        assertSampleIdentifiers(samplesLE2, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLEDMatch2 = new SampleSearchCriteria();
        criteriaLEDMatch2.withDateProperty("DATE").thatIsEarlierThanOrEqualTo("2020-02-10");
        final List<Sample> samplesLED2 = searchSamples(sessionToken, criteriaLEDMatch2, emptyFetchOptions);
        assertSampleIdentifiers(samplesLED2, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGTMatch = new SampleSearchCriteria();
        criteriaGTMatch.withProperty("DATE").thatIsGreaterThan("2020-02-08");
        final List<Sample> samplesGT = searchSamples(sessionToken, criteriaGTMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesGT, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGTDMatch = new SampleSearchCriteria();
        criteriaGTDMatch.withDateProperty("DATE").thatIsLaterThan("2020-02-08");
        final List<Sample> samplesGTD = searchSamples(sessionToken, criteriaGTDMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesGTD, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGTEmptyMatch = new SampleSearchCriteria();
        criteriaGTEmptyMatch.withProperty("DATE").thatIsGreaterThan("2020-02-09");
        final List<Sample> samplesGTEmpty = searchSamples(sessionToken, criteriaGTEmptyMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesGTEmpty);

        final SampleSearchCriteria criteriaGTDEmptyMatch = new SampleSearchCriteria();
        criteriaGTDEmptyMatch.withDateProperty("DATE").thatIsLaterThan("2020-02-09");
        final List<Sample> samplesGTDEmpty = searchSamples(sessionToken, criteriaGTDEmptyMatch, emptyFetchOptions);
        assertSampleIdentifiers(samplesGTDEmpty);

        final SampleSearchCriteria criteriaGEMatch1 = new SampleSearchCriteria();
        criteriaGEMatch1.withProperty("DATE").thatIsGreaterThanOrEqualTo("2020-02-09");
        final List<Sample> samplesGE1 = searchSamples(sessionToken, criteriaGEMatch1, emptyFetchOptions);
        assertSampleIdentifiers(samplesGE1, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGEDMatch1 = new SampleSearchCriteria();
        criteriaGEDMatch1.withDateProperty("DATE").thatIsLaterThanOrEqualTo("2020-02-09");
        final List<Sample> samplesGED1 = searchSamples(sessionToken, criteriaGEDMatch1, emptyFetchOptions);
        assertSampleIdentifiers(samplesGED1, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGEMatch2 = new SampleSearchCriteria();
        criteriaGEMatch2.withProperty("DATE").thatIsGreaterThanOrEqualTo("2020-02-08");
        final List<Sample> samplesGE2 = searchSamples(sessionToken, criteriaGEMatch2, emptyFetchOptions);
        assertSampleIdentifiers(samplesGE2, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGEDMatch2 = new SampleSearchCriteria();
        criteriaGEDMatch2.withDateProperty("DATE").thatIsLaterThanOrEqualTo("2020-02-08");
        final List<Sample> samplesGED2 = searchSamples(sessionToken, criteriaGEDMatch2, emptyFetchOptions);
        assertSampleIdentifiers(samplesGED2, "/CISD/DATE_PROPERTY_TEST");

        final SampleSearchCriteria criteriaMatchNotDate = new SampleSearchCriteria();
        criteriaMatchNotDate.withProperty("DATE").thatEquals("blabla");
        assertUserFailureException(
                aVoid -> searchSamples(sessionToken, criteriaMatchNotDate, new SampleFetchOptions()),
                "String does not represent a date: [blabla]");
    }

    @Test
    public void testSearchForSampleWithDatePropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createADatePropertyType(sessionToken, "DATE");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("DATE_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("DATE", "2020-02-09");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaContainsMatch = new SampleSearchCriteria();
        criteriaContainsMatch.withProperty("DATE").thatContains("02");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaContainsMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "DATE"));

        final SampleSearchCriteria criteriaStartsWithMatch = new SampleSearchCriteria();
        criteriaStartsWithMatch.withProperty("DATE").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaStartsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "DATE"));

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("DATE").thatEndsWith("09");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "DATE"));
    }

    @Test
    public void testSearchForSampleWithTimestampProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createATimestampPropertyType(sessionToken, "TIMESTAMP");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TIMESTAMP_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("TIMESTAMP", "2020-02-09 10:00:00 +0100");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaEMatch = new SampleSearchCriteria();
        criteriaEMatch.withProperty("TIMESTAMP").thatEquals("2020-02-09 10:00:00 +0100");
        final List<Sample> samplesE = searchSamples(sessionToken, criteriaEMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesE, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaNEMatch = new SampleSearchCriteria();
        criteriaNEMatch.withProperty("TIMESTAMP").thatEquals("2020-02-09 10:00:01 +0100");
        final List<Sample> samplesNE = searchSamples(sessionToken, criteriaNEMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesNE);

        final SampleSearchCriteria criteriaLTMatch = new SampleSearchCriteria();
        criteriaLTMatch.withProperty("TIMESTAMP").thatIsLessThan("2020-02-09 11:00:00 +0100");
        final List<Sample> samplesLT = searchSamples(sessionToken, criteriaLTMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesLT, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLTDMatch = new SampleSearchCriteria();
        criteriaLTDMatch.withDateProperty("TIMESTAMP").thatIsEarlierThan("2020-02-09 11:00:00 +0100");
        final List<Sample> samplesLTD = searchSamples(sessionToken, criteriaLTDMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesLTD, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLEMatch = new SampleSearchCriteria();
        criteriaLEMatch.withProperty("TIMESTAMP").thatIsLessThanOrEqualTo("2020-02-09 11:00:00 +0100");
        final List<Sample> samplesLE = searchSamples(sessionToken, criteriaLEMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesLE, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaLEDMatch = new SampleSearchCriteria();
        criteriaLEDMatch.withDateProperty("TIMESTAMP").thatIsEarlierThanOrEqualTo("2020-02-09 11:00:00 +0100");
        final List<Sample> samplesLED = searchSamples(sessionToken, criteriaLEDMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesLED, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGTMatch = new SampleSearchCriteria();
        criteriaGTMatch.withProperty("TIMESTAMP").thatIsGreaterThan("2020-02-09 10:00:00 +0100");
        final List<Sample> samplesGT = searchSamples(sessionToken, criteriaGTMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesGT);

        final SampleSearchCriteria criteriaGTDMatch = new SampleSearchCriteria();
        criteriaGTDMatch.withDateProperty("TIMESTAMP").thatIsLaterThan("2020-02-09 10:00:00 +0100");
        final List<Sample> samplesGTD = searchSamples(sessionToken, criteriaGTDMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesGTD);

        final SampleSearchCriteria criteriaGEMatch = new SampleSearchCriteria();
        criteriaGEMatch.withProperty("TIMESTAMP").thatIsGreaterThanOrEqualTo("2020-02-09 10:00:00 +0100");
        final List<Sample> samplesGE = searchSamples(sessionToken, criteriaGEMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesGE, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaGEDMatch = new SampleSearchCriteria();
        criteriaGEDMatch.withDateProperty("TIMESTAMP").thatIsLaterThanOrEqualTo("2020-02-09 10:00:00 +0100");
        final List<Sample> samplesGED = searchSamples(sessionToken, criteriaGEDMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesGED, "/CISD/TIMESTAMP_PROPERTY_TEST");

        final SampleSearchCriteria criteriaDateMatch = new SampleSearchCriteria();
        criteriaDateMatch.withDateProperty("TIMESTAMP").thatIsLaterThanOrEqualTo("2020-02-09");
        final List<Sample> samplesGEDD = searchSamples(sessionToken, criteriaDateMatch, new SampleFetchOptions());
        assertSampleIdentifiers(samplesGEDD, "/CISD/TIMESTAMP_PROPERTY_TEST");
    }

    @Test
    public void testSearchForSampleWithTimestampPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createATimestampPropertyType(sessionToken, "TIMESTAMP");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TIMESTAMP_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("TIMESTAMP", "2020-02-09 10:00:00 +0100");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaContainsMatch = new SampleSearchCriteria();
        criteriaContainsMatch.withProperty("TIMESTAMP").thatContains("20");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaContainsMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "TIMESTAMP"));

        final SampleSearchCriteria criteriaStartsWithMatch = new SampleSearchCriteria();
        criteriaStartsWithMatch.withProperty("TIMESTAMP").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaStartsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "TIMESTAMP"));

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("TIMESTAMP").thatEndsWith("0100");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "TIMESTAMP"));

        final SampleSearchCriteria criteriaMatchNotDate = new SampleSearchCriteria();
        criteriaMatchNotDate.withProperty("TIMESTAMP").thatEquals("blabla");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaMatchNotDate, new SampleFetchOptions()),
                "String does not represent a date: [blabla]");
    }

    @Test
    public void testSearchForSampleWithBooleanPropertyUnsupportedMatching()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        final SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("BOOLEAN_PROPERTY_TEST");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty("BOOLEAN", "false");

        v3api.createSamples(sessionToken, Collections.singletonList(sampleCreation));

        final SampleSearchCriteria criteriaStartsWithMatch = new SampleSearchCriteria();
        criteriaStartsWithMatch.withProperty("BOOLEAN").thatStartsWith("fa");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaStartsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "BOOLEAN"));

        final SampleSearchCriteria criteriaEndsWithMatch = new SampleSearchCriteria();
        criteriaEndsWithMatch.withProperty("BOOLEAN").thatEndsWith("se");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaEndsWithMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "BOOLEAN"));

        final SampleSearchCriteria criteriaContainsMatch = new SampleSearchCriteria();
        criteriaContainsMatch.withProperty("BOOLEAN").thatContains("ls");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaContainsMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "BOOLEAN"));

        final SampleSearchCriteria criteriaGTMatch = new SampleSearchCriteria();
        criteriaGTMatch.withProperty("BOOLEAN").thatIsGreaterThan("false");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaGTMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThan", "BOOLEAN"));

        final SampleSearchCriteria criteriaGEMatch = new SampleSearchCriteria();
        criteriaGEMatch.withProperty("BOOLEAN").thatIsGreaterThanOrEqualTo("false");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaGEMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThanOrEqualTo", "BOOLEAN"));

        final SampleSearchCriteria criteriaLTMatch = new SampleSearchCriteria();
        criteriaLTMatch.withProperty("BOOLEAN").thatIsLessThan("true");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaLTMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThan", "BOOLEAN"));

        final SampleSearchCriteria criteriaLEMatch = new SampleSearchCriteria();
        criteriaLEMatch.withProperty("BOOLEAN").thatIsLessThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchSamples(sessionToken, criteriaLEMatch, new SampleFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThanOrEqualTo", "BOOLEAN"));
    }

    @Test
    public void testSearchWithPropertyAndAttribute()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleSearchCriteria criteria = new SampleSearchCriteria().withOrOperator();
        criteria.withProperty("$NAME").thatContains("P1-A2");
        criteria.withCode().thatContains("P1-A2");

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment().withProperties();
        fetchOptions.withProperties();

        // When
        final List<Sample> samples = v3api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();

        // Then
        assertSampleIdentifiers(samples, "/CISD/DEFAULT/CP1-A2", "/CISD/DEFAULT/RP1-A2X");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria c = new SampleSearchCriteria();
        c.withCode().thatEquals("RP1-A2X");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();

        v3api.searchSamples(sessionToken, c, fo);

        assertAccessLog("search-samples  SEARCH_CRITERIA:\n'SAMPLE\n    with attribute 'code' equal to 'RP1-A2X'\n'\n"
                + "FETCH_OPTIONS:\n'Sample\n    with Project\n    with Space\n'");
    }

    @Test
    public void testNestedLogicalOperators()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        final SampleSearchCriteria subCriteria1 = criteria.withSubcriteria().withOrOperator();
        subCriteria1.withIdentifier().thatStartsWith("/MP:");
        subCriteria1.withIdentifier().thatStartsWith("/CISD/B");

        final SampleSearchCriteria subCriteria2 = criteria.withSubcriteria().withOrOperator();
        subCriteria2.withIdentifier().thatEndsWith(":B03");
        subCriteria2.withIdentifier().thatEndsWith(":B01");

        testSearch(TEST_USER, criteria, "/MP:B03", "/CISD/B1B3:B03", "/CISD/B1B3:B01");
    }

    @Test
    public void testSearchWithAttributeNegation()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withCode().thatContains("P1-A2");
        criteria.withSubcriteria().negate().withCode().thatEquals("CP1-A2");

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment().withProperties();
        fetchOptions.withProperties();

        // When
        final List<Sample> samples = v3api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();

        // Then
        assertSampleIdentifiers(samples, "/CISD/DEFAULT/RP1-A2X");
    }

    @Test
    public void testSearchWithNumericPropertyNegation()
    {
        // SIZE: 4711 CODE: 3VCP7
        // SIZE: 123 CODE: CP-TEST-1
        // SIZE: 321 CODE: CP-TEST-2
        // SIZE: 666 CODE: CP-TEST-3

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions sortByCodeFO = new SampleFetchOptions();
        sortByCodeFO.sortBy().code().asc();
        sortByCodeFO.withProperties();

        // Greater or Equals - Giving integer as real
        SampleSearchCriteria criteriaGOE = new SampleSearchCriteria().withAndOperator();
        criteriaGOE.withNumberProperty("SIZE");
        criteriaGOE.withSubcriteria().negate().withNumberProperty("SIZE").thatIsLessThan(321.0);
        List<Sample> samplesGOE1 = searchSamples(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE1, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        criteriaGOE = new SampleSearchCriteria().withAndOperator();
        criteriaGOE.withNumberProperty("SIZE");
        criteriaGOE.withSubcriteria().negate().withNumberProperty("SIZE").thatIsLessThan(320.9);
        List<Sample> samplesGOE = searchSamples(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        criteriaGOE = new SampleSearchCriteria().withAndOperator();
        criteriaGOE.withProperty("SIZE");
        criteriaGOE.withSubcriteria().negate().withProperty("SIZE").thatIsLessThan("321.0");
        samplesGOE = searchSamples(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE, "/CISD/3VCP7", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        // Greater - Giving integer as real
        SampleSearchCriteria criteriaG = new SampleSearchCriteria().withAndOperator();
        criteriaG.withNumberProperty("SIZE");
        criteriaG.withSubcriteria().negate().withNumberProperty("SIZE").thatIsLessThanOrEqualTo(321.0);
        List<Sample> samplesG = searchSamples(sessionToken, criteriaG, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesG, "/CISD/3VCP7", "/CISD/NEMO/CP-TEST-3");

        criteriaG = new SampleSearchCriteria().withAndOperator();
        criteriaG.withProperty("SIZE");
        criteriaG.withSubcriteria().negate().withProperty("SIZE").thatIsLessThanOrEqualTo("321.0");
        samplesG = searchSamples(sessionToken, criteriaG, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesG, "/CISD/3VCP7", "/CISD/NEMO/CP-TEST-3");

        // Equals
        final SampleSearchCriteria criteriaE = new SampleSearchCriteria().withAndOperator();
        criteriaE.withNumberProperty("SIZE");
        criteriaE.withSubcriteria().negate().withNumberProperty("SIZE").thatEquals(666);
        final List<Sample> samplesE = searchSamples(sessionToken, criteriaE, sortByCodeFO);
        samplesE.forEach(sample -> assertNotEquals(sample.getIdentifier().getIdentifier(), "/CISD/NEMO/CP-TEST-3"));

        // Less
        final SampleSearchCriteria criteriaL = new SampleSearchCriteria().withAndOperator();
        criteriaL.withNumberProperty("SIZE");
        criteriaL.withSubcriteria().negate().withNumberProperty("SIZE").thatIsGreaterThanOrEqualTo(666);
        final List<Sample> samplesL = searchSamples(sessionToken, criteriaL, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesL, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");

        // Less or Equals
        final SampleSearchCriteria criteriaLOE = new SampleSearchCriteria().withAndOperator();
        criteriaLOE.withNumberProperty("SIZE");
        criteriaLOE.withSubcriteria().negate().withNumberProperty("SIZE").thatIsGreaterThan(321);
        final List<Sample> samplesLOE = searchSamples(sessionToken, criteriaLOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesLOE, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2");

        v3api.logout(sessionToken);
    }

    @Test
    public void testNestedLogicalOperatorsMultipleNesting()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        final SampleSearchCriteria subCriteria1 = criteria.withSubcriteria().withOrOperator();
        subCriteria1.withSubcriteria().withIdentifier().thatStartsWith("/MP:");
        subCriteria1.withSubcriteria().withSubcriteria().withIdentifier().thatStartsWith("/CISD/B");

        final SampleSearchCriteria subCriteria2 = criteria.withSubcriteria().withOrOperator();
        subCriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withIdentifier().thatEndsWith(":B03");
        subCriteria2.withIdentifier().thatEndsWith(":B01");

        testSearch(TEST_USER, criteria, "/MP:B03", "/CISD/B1B3:B03", "/CISD/B1B3:B01");
    }

    @Test
    public void testNestedLogicalOperatorsWithExperiment()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        final SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withExperiment().withType().withCode().thatEquals("COMPOUND_HCS");
        subcriteria1.withExperiment().withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));

        final SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
        subcriteria2.withExperiment().withProject().withCode().thatEquals("NOE");
        subcriteria2.withCode().thatEquals("RP1-A2X");

        testSearch(TEST_USER, criteria, "/CISD/NOE/CP-TEST-2", "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testNestedLogicalOperatorsWithChildren()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        final SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withCode().thatEquals("3V-125");
        subcriteria1.withCode().thatEquals("DP1-A");
        subcriteria1.withCode().thatEquals("MP002-1");

        final SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
        subcriteria2.withChildren().withCode().thatEndsWith("5");
        subcriteria2.withChildren().withPermId().thatEndsWith("7");
        testSearch(TEST_USER, criteria, "/CISD/MP002-1", "/CISD/MP002-1", "/CISD/3V-125", "/CISD/DP1-A");
    }

    @Test
    public void testNestedLogicalOperatorsWithChildrenMultipleNesting()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        final SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withSubcriteria().withCode().thatEquals("3V-125");
        subcriteria1.withCode().thatEquals("DP1-A");
        subcriteria1.withCode().thatEquals("MP002-1");

        final SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
        subcriteria2.withSubcriteria().withSubcriteria().withChildren().withCode().thatEndsWith("5");
        subcriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withChildren().withPermId()
                .thatEndsWith("7");
        testSearch(TEST_USER, criteria, "/CISD/MP002-1", "/CISD/MP002-1", "/CISD/3V-125", "/CISD/DP1-A");
    }

    @Test
    public void testNestedLogicalOperatorsWithParentsAndChildren()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        final SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withParents().withId().thatEquals(new SampleIdentifier("/CISD/MP002-1"));
        subcriteria1.withChildren().withId().thatEquals(new SamplePermId("200811050946559-980"));

        final SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withAndOperator();
        subcriteria2.withParents();
        subcriteria2.withChildren();

        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testNestedLogicalOperatorsWithParentsAndChildrenMultipleNesting()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        final SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withSubcriteria().withSubcriteria().withParents().withId()
                .thatEquals(new SampleIdentifier("/CISD/MP002-1"));
        subcriteria1.withSubcriteria().withChildren().withId().thatEquals(new SamplePermId("200811050946559-980"));

        final SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withAndOperator();
        subcriteria2.withSubcriteria().withParents();
        subcriteria2.withSubcriteria().withSubcriteria().withSubcriteria().withChildren();

        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testNestedLogicalOperatorsWithContainerSamplesAndType()
    {
        // Given
        EntityTypePermId typeId = new EntityTypePermId("NORMAL");
        ExperimentIdentifier experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        SpacePermId spacePermId = new SpacePermId("CISD");
        CreationId creationId1 = new CreationId("S1");
        SampleCreation s1 = new SampleCreation();
        s1.setTypeId(typeId);
        s1.setAutoGeneratedCode(true);
        s1.setExperimentId(experimentId);
        s1.setSpaceId(spacePermId);
        s1.setCreationId(creationId1);
        SampleCreation s2 = new SampleCreation();
        s2.setTypeId(typeId);
        s2.setAutoGeneratedCode(true);
        s2.setExperimentId(experimentId);
        s2.setSpaceId(spacePermId);
        s2.setContainerId(creationId1);
        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));

        SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withContainer().withPermId().thatEquals(sampleIds.get(0).getPermId());
        criteria.withType().withCode().thatEquals(typeId.getPermId());
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainer();

        // When
        List<Sample> samples = v3api.searchSamples(systemSessionToken, criteria, fetchOptions).getObjects();

        // Then
        assertEquals(samples.get(0).getPermId().getPermId(), sampleIds.get(1).getPermId());
        assertEquals(samples.get(0).getContainer().getPermId().getPermId(), sampleIds.get(0).getPermId());
        assertEquals(samples.size(), 1);
    }

    @Test
    public void testNestedLogicalOperatorsWithParentsExperimentAndPermID()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        final SampleParentsSearchCriteria subcriteria1 = criteria.withParents().withOrOperator();
        subcriteria1.withCode().thatContains("-");
        subcriteria1.withCode().thatStartsWith("C");

        final SampleParentsSearchCriteria subcriteria2 = criteria.withParents().withOrOperator();
        subcriteria2.withExperiment();
        subcriteria2.withType().withCode().thatStartsWith("MASTER");

        criteria.withPermId().thatEndsWith("6");

        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EV-INVALID", "/CISD/3V-125", "/CISD/DP2-A");
    }

    @Test
    public void testNestedLogicalOperatorsMultipleNestingWithParents()
    {
        final SampleSearchCriteria criteria3 = new SampleSearchCriteria().withOrOperator();
        final SampleSearchCriteria subcriteria3 = criteria3.withSubcriteria();
        subcriteria3.withParents().withOrOperator().withExperiment();
        subcriteria3.withParents().withOrOperator().withType().withCode().thatStartsWith("MASTER");
        criteria3.withPermId().thatEndsWith("6");
        testSearch(TEST_USER, criteria3, 74);

        final SampleSearchCriteria criteria4 = new SampleSearchCriteria().withOrOperator();
        final SampleSearchCriteria subcriteria4 = criteria4.withSubcriteria().withSubcriteria().withSubcriteria();
        subcriteria4.withParents().withOrOperator().withExperiment();
        subcriteria4.withParents().withOrOperator().withType().withCode().thatStartsWith("MASTER");
        criteria4.withPermId().thatEndsWith("6");
        testSearch(TEST_USER, criteria4, 74);
    }

    @Test
    public void testMultipleNestingWithChildren()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria();
        criteria1.withParents().withCode().thatEquals("MP002-1");
        testSearch(TEST_USER, criteria1, "/CISD/3V-125", "/CISD/3V-126");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withChildren().withId().thatEquals(new SampleIdentifier("/CISD/NEMO/3VCP6"));
        testSearch(TEST_USER, criteria2, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testNestedLogicalOperatorsWithContainers()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        final SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
        subcriteria1.withContainer().withPermId().thatEquals("200811050927630-1003");
        subcriteria1.withContainer().withCode().thatEquals("MP2-NO-CL");

        final SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
        subcriteria2.withCode().thatStartsWith("C");
        subcriteria2.withPermId().thatContains("-100");

        testSearch(TEST_USER, criteria, "/CISD/MP1-MIXED:A01", "/CISD/MP1-MIXED:A02", "/CISD/MP1-MIXED:A03",
                "/CISD/MP1-MIXED:B02", "/CISD/MP2-NO-CL:A01");
    }

    @Test
    public void testSearchWithPermIdWithAttributeFullTextSearch()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        criteria.withTextAttribute().thatMatches("cp-test-1 cp-test-2 cp-test-3 cp-test-4");
        criteria.withPermId().thatContains("7-");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/TEST-SPACE/NOE/CP-TEST-4");
    }

    @Test
    public void testSearchWithPermIdWithPropertyFullTextSearch()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria().withAndOperator();

        criteria1.withProperty("COMMENT").thatMatches("test");
        criteria1.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria1, "/CISD/3VCP7");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria().withAndOperator();

        criteria2.withProperty("DESCRIPTION").thatMatches("test");
        criteria2.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria2, "/CISD/CL1");
    }

    @Test
    public void testSearchWithPermIdWithStringPropertyFullTextSearch()
    {
        final SampleSearchCriteria criteria1 = new SampleSearchCriteria().withAndOperator();

        criteria1.withStringProperty("COMMENT").thatMatches("test");
        criteria1.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria1, "/CISD/3VCP7");

        final SampleSearchCriteria criteria2 = new SampleSearchCriteria().withAndOperator();

        criteria2.withStringProperty("DESCRIPTION").thatMatches("test");
        criteria2.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria2, "/CISD/CL1");
    }

    @Test
    public void testSearchWithPermIdWithAnyPropertyFullTextSearch()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        criteria.withAnyProperty().thatMatches("test");
        criteria.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria, "/CISD/CL1", "/CISD/3VCP7");
    }

    @Test
    public void testSearchWithPermIdWithAnyStringPropertyFullTextSearch()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        criteria.withAnyStringProperty().thatMatches("test");
        criteria.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria, "/CISD/CL1", "/CISD/3VCP7");
    }

    @Test
    public void testSearchWithPermIdWithAnyFieldFullTextSearch()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        criteria.withAnyField().thatMatches("test cp-test-1 cp-test-2");
        criteria.withPermId().thatContains("19");

        testSearch(TEST_USER, criteria, "/CISD/CL1", "/TEST-SPACE/TEST-PROJECT/EV-TEST", "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testSearchFullTextSearchStringPropertyMatchSortingByScore()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        criteria.withStringProperty("COMMENT").thatMatches("simple stuff");

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.sortBy().stringMatchPropertyScore("COMMENT", "simple stuff").desc();
        fetchOptions.withProperties();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);
        assertSampleIdentifiers(samples, "/CISD/NEMO/CP-TEST-1", "/CISD/NOE/CP-TEST-2", "/CISD/NEMO/CP-TEST-3");

        // "/CISD/NEMO/CP-TEST-1" -> "very advanced stuff"
        // "/CISD/NOE/CP-TEST-2" -> "extremely simple stuff"
        // "/CISD/NEMO/CP-TEST-3" -> "stuff like others"

        assertEquals(samples.get(0).getIdentifier().toString(), "/CISD/NOE/CP-TEST-2");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchFullTextSearchAnyStringPropertyMatchSortingByScore()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();

        criteria.withAnyStringProperty().thatMatches("test control");

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.sortBy().stringMatchAnyPropertyScore("test control").desc();
        fetchOptions.withProperties();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);
        assertSampleIdentifiers(samples, "/CISD/CL1", "/CISD/3VCP7", "/TEST-SPACE/TEST-PROJECT/EV-TEST");

        // "/CISD/CL1" -> "test control layout"
        // "/CISD/3VCP7" -> "test comment"
        // "/TEST-SPACE/TEST-PROJECT/EV-TEST" -> "test comment"

        assertEquals(samples.get(0).getIdentifier().toString(), "/CISD/CL1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchSamplesWithAnyProperty()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withAnyProperty();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);

        try {
            samples.forEach(sample -> assertFalse(sample.getProperties().isEmpty()));
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    @Test
    public void testSearchSamplesWithAnyStringProperty()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withAnyStringProperty();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);

        assertEquals(samples.size(), 6);
        try {
            samples.forEach(sample -> assertFalse(sample.getProperties().isEmpty()));
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    @Test
    public void testSearchSamplesWithAnyStringPropertyThatMatchesEmptyString()
    {
        // Given
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyStringProperty().thatMatches("");
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // When
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);

        // Then
        assertEquals(samples.size(), 6);
        System.err.println(samples);
        try {
            samples.forEach(sample -> assertFalse(sample.getProperties().isEmpty(), sample + " has unexpected properties"));
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    @Test
    public void testSearchSamplesWithAnyNumberProperty()
    {
        final List<Sample> allSamples = getAllSamplesWithProperties();
        final Set<String> expectedSampleIds = allSamples.stream().filter(sample ->
                !sample.getProperties().isEmpty() &&
                        sample.getProperties().values().stream().anyMatch(SearchSampleTest::isNumber)).
                        map(sample -> sample.getPermId().toString()).
                collect(Collectors.toSet());

        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withAnyNumberProperty();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);
        final Set<String> samplePermIds = samples.stream().map(sample -> sample.getPermId().toString()).
                collect(Collectors.toSet());

        assertEquals(samplePermIds, expectedSampleIds);
    }

    @Test
    public void testSearchSamplesWithAnyDateProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType1 = createAPropertyType(sessionToken, DataType.DATE);
        final EntityTypePermId sampleType1 = createASampleType(sessionToken, true, propertyType1);
        final SampleCreation creation1 = new SampleCreation();
        creation1.setCode("SAMPLE_WITH_DATE_PROPERTY");
        creation1.setTypeId(sampleType1);
        creation1.setSpaceId(new SpacePermId("CISD"));
        creation1.setProperty(propertyType1.getPermId(), "2020-02-17");
        v3api.createSamples(sessionToken, Collections.singletonList(creation1));

        final PropertyTypePermId propertyType2 = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        final EntityTypePermId sampleType2 = createASampleType(sessionToken, true, propertyType2);
        final SampleCreation creation2 = new SampleCreation();
        creation2.setCode("SAMPLE_WITH_TIMESTAMP_PROPERTY");
        creation2.setTypeId(sampleType2);
        creation2.setSpaceId(new SpacePermId("CISD"));
        creation2.setProperty(propertyType2.getPermId(), "2020-02-17 10:00:00");
        v3api.createSamples(sessionToken, Collections.singletonList(creation2));

        final List<Sample> allSamples = getAllSamplesWithProperties();
        final Set<String> expectedSampleIds = allSamples.stream().filter(sample ->
                !sample.getProperties().isEmpty() &&
                        sample.getProperties().values().stream().anyMatch(SearchSampleTest::isDate)).
                        map(sample -> sample.getPermId().toString()).
                collect(Collectors.toSet());

        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withAnyDateProperty();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);
        final Set<String> samplePermIds = samples.stream().map(sample -> sample.getPermId().toString()).
                collect(Collectors.toSet());

        assertEquals(samplePermIds, expectedSampleIds);
    }

    @Test
    public void testSearchSamplesWithAnyBooleanProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType1 = createAPropertyType(sessionToken, DataType.BOOLEAN);
        final EntityTypePermId sampleType1 = createASampleType(sessionToken, true, propertyType1);
        final SampleCreation creation1 = new SampleCreation();
        creation1.setCode("SAMPLE_WITH_BOOLEAN_PROPERTY_1");
        creation1.setTypeId(sampleType1);
        creation1.setSpaceId(new SpacePermId("CISD"));
        creation1.setProperty(propertyType1.getPermId(), Boolean.TRUE.toString());
        v3api.createSamples(sessionToken, Collections.singletonList(creation1));

        final PropertyTypePermId propertyType2 = createAPropertyType(sessionToken, DataType.BOOLEAN);
        final EntityTypePermId sampleType2 = createASampleType(sessionToken, true, propertyType2);
        final SampleCreation creation2 = new SampleCreation();
        creation2.setCode("SAMPLE_WITH_BOOLEAN_PROPERTY_2");
        creation2.setTypeId(sampleType2);
        creation2.setSpaceId(new SpacePermId("CISD"));
        creation2.setProperty(propertyType2.getPermId(), Boolean.FALSE.toString());
        v3api.createSamples(sessionToken, Collections.singletonList(creation2));

        final List<Sample> allSamples = getAllSamplesWithProperties();
        final Set<String> expectedSampleIds = allSamples.stream().filter(sample ->
                !sample.getProperties().isEmpty() &&
                        sample.getProperties().values().stream().anyMatch(SearchSampleTest::isBoolean)).
                        map(sample -> sample.getPermId().toString()).
                collect(Collectors.toSet());

        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withAnyBooleanProperty();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);
        final Set<String> samplePermIds = samples.stream().map(sample -> sample.getPermId().toString()).
                collect(Collectors.toSet());

        assertEquals(samplePermIds, expectedSampleIds);
    }

    private List<Sample> getAllSamplesWithProperties()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final List<Sample> samples = searchSamples(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);

        return samples;
    }

    private static boolean isNumber(final Serializable property)
    {
        try
        {
            Double.parseDouble((String) property);
        } catch (final NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    private static boolean isDate(final Serializable property)
    {
        return DateFieldSearchCriteria.DATE_FORMATS.stream().map(dateFormat ->
        {
            final Date formattedValue = DateFieldSearchCriteria.formatValue((String) property, dateFormat);
            return (formattedValue == null) ? null
                    : new Object[] { AnyFieldSearchConditionTranslator.TRUNCATION_INTERVAL_BY_DATE_FORMAT
                    .get(dateFormat.getClass()), formattedValue};
        }).anyMatch(Objects::nonNull);
    }

    private static boolean isBoolean(final Serializable propertyValue)
    {
        return ((String) propertyValue).equalsIgnoreCase(Boolean.TRUE.toString()) ||
                ((String) propertyValue).equalsIgnoreCase(Boolean.FALSE.toString());
    }

    private void testSearch(String user, SampleSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Sample> samples = searchSamples(sessionToken, criteria, new SampleFetchOptions());
        assertSampleIdentifiers(samples, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, SampleSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Sample> samples = searchSamples(sessionToken, criteria, new SampleFetchOptions());
        assertEquals(samples.size(), expectedCount);
        v3api.logout(sessionToken);
    }

    private List<Sample> searchSamples(String sessionToken, SampleSearchCriteria criteria, SampleFetchOptions fetchOptions)
    {
        SearchResult<Sample> searchResult =
                v3api.searchSamples(sessionToken, criteria, fetchOptions);
        return searchResult.getObjects();
    }

    private List<String> extractIndentifiers(List<Sample> samples)
    {
        List<String> identifiers = new ArrayList<>();
        for (Sample sample : samples)
        {
            identifiers.add(sample.getIdentifier().getIdentifier());
        }
        return identifiers;
    }

}
