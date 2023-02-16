/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Tests for {@link IScreeningQuery}.
 *
 * @author Tomasz Pylak
 */
@Test(groups =
        { "db", "screening" })
public class ScreeningDAOTest extends AbstractScreeningDAOTest
{

    static
    {
        System.setProperty("script-folder", "../server-application-server/sourceTest");
        System.setProperty("mass-upload-folder",
                "../server-application-server/sourceTest/sql/postgresql");

    }

    private IScreeningQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DataSource dataSource = DatabaseContextUtils.getDatabaseContext(daoFactory).getDataSource();
        // Create query with a connection and not with the data source because otherwise the test
        // would
        // fail if run after the tests of the subclasses of
        // ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractDBTest
        query = QueryTool.getQuery(dataSource.getConnection(), IScreeningQuery.class);
    }

    @Test
    public void testGetPlateLocationsForOneMaterial()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialId(1, 1));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateLocationsForOneMaterialAllExperiments()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialId(1));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateLocationsForOneMaterialAllExperimentsFromProject()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialId(1, "space",
                        "project"));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateMappingAllTypes()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateMapping("xxx"));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateMappingAllTypesAugmentedCode()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateMapping("xxx", "yyy"));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateGeometryAugmentedCode()
    {
        final PlateGeometryContainer container = query.tryGetPlateGeometry("CISD", "MP002-1");
        AssertJUnit.assertEquals("384_WELLS_16X24", container.plate_geometry);
        AssertJUnit.assertEquals("200811050917877-331", container.perm_id);
    }

    @Test
    public void testGetPlateGeometry()
    {
        final PlateGeometryContainer container = query.tryGetPlateGeometry("200811050917877-331");
        AssertJUnit.assertEquals("384_WELLS_16X24", container.plate_geometry);
        AssertJUnit.assertEquals("MP002-1", container.plate_code);
        AssertJUnit.assertEquals("CISD", container.space_code);
    }

    @Test
    public void testGetPlateMappingGeneType()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateMappingForMaterialType("xxx", "Gene"));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateMappingGeneTypeAugmentedCode()
    {
        // it just tests if the sql runs
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateMappingForMaterialType("xxx", "yyy",
                        "Gene"));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateLocationsForManyMaterials()
    {
        String[] materialTypeCodes = new String[]
                { ScreeningConstants.GENE_PLUGIN_TYPE_CODE };
        // it just tests if the sql is correct

        long[] ids =
                { 1, 2 };

        // one experiment
        List<WellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialCodes(ids,
                        materialTypeCodes, 1));
        AssertJUnit.assertEquals(0, locations.size());

        // one project
        locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialCodesInProject(ids,
                        materialTypeCodes, "space", "project"));
        AssertJUnit.assertEquals(0, locations.size());

        // all experiments
        locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialCodes(ids,
                        materialTypeCodes));
        AssertJUnit.assertEquals(0, locations.size());

    }

    @Test
    public void testGetGlobalBasicPlateLocations()
    {
        List<BasicWellContentQueryResult> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForExperiment(new long[]
                        { 1, 2 }, "GENE"));
        AssertJUnit.assertEquals(0, locations.size());

        List<ExperimentReferenceQueryResult> experiments = query.getExperimentsWithMaterial(1);
        AssertJUnit.assertEquals(0, experiments.size());

        experiments = query.getExperimentsWithMaterial(1, 1);
        AssertJUnit.assertEquals(0, experiments.size());

    }

}
