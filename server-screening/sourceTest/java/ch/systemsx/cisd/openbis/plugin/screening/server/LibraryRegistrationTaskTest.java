/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Kaloyan Enimanev
 */
@Friend(toClasses = LibraryRegistrationTask.class)
public class LibraryRegistrationTaskTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "session";

    private Mockery context;

    private ICommonServer commonServer;

    private IGenericServer genericServer;

    private IDAOFactory daoFactory;

    private IEntityTypeDAO entityTypeDAO;

    private LibraryRegistrationTask task;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        commonServer = context.mock(ICommonServer.class);
        genericServer = context.mock(IGenericServer.class);
        daoFactory = context.mock(IDAOFactory.class);
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateGeneSymbols()
    {
        NewMaterial g1 = createNewGene("G1", "AB");
        NewMaterial g2 = createNewGene("G2", "AB");
        NewMaterial g3 = createNewGene("G3", "BC AB");
        NewMaterial g4 = createNewGene("G4", "AB AB");
        NewMaterial g5 = createNewGene("G5", "AB DDD");

        final List<NewMaterial> newGenes = Arrays.asList(g1, g2, g3, g4, g5);
        final List<Material> existingGenes =
                Arrays.asList(createExistingGene("G1", "ABC A"), createExistingGene("G2", "AB"),
                        createExistingGene("G3", "AB BC"), createExistingGene("G4", "XY AB"),
                        createExistingGene("G5", "XY AB YZ DDD"));
        final RecordingMatcher<List<NewMaterialsWithTypes>> materialsWithTypesMatcher =
                new RecordingMatcher<List<NewMaterialsWithTypes>>();

        task =
                new LibraryRegistrationTask(SESSION_TOKEN, newGenes, null, null, commonServer,
                        genericServer, daoFactory);

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(
                            ScreeningConstants.GENE_PLUGIN_TYPE_CODE);
                    will(returnValue(new MaterialTypePE()));

                    one(commonServer).listMaterials(with(SESSION_TOKEN),
                            with(any(ListMaterialCriteria.class)), with(true));
                    will(returnValue(existingGenes));

                    one(genericServer).registerOrUpdateMaterials(with(SESSION_TOKEN),
                            with(materialsWithTypesMatcher));
                }
            });

        task.doAction(new StringWriter());

        assertEquals("ABC A AB", extractGeneSymbol(g1));
        assertEquals("AB", extractGeneSymbol(g2));
        assertEquals("AB BC", extractGeneSymbol(g3));
        assertEquals("XY AB", extractGeneSymbol(g4));
        assertEquals("XY AB YZ DDD", extractGeneSymbol(g5));

        List<NewMaterialsWithTypes> materialsWithTypes =
                LibraryRegistrationTask.createMaterialsWithTypes(
                        ScreeningConstants.GENE_PLUGIN_TYPE_CODE, newGenes);
        List<NewMaterialsWithTypes> registeredMaterialsWithTypes =
                materialsWithTypesMatcher.getRecordedObjects().get(0);
        assertEquals(materialsWithTypes.size(), registeredMaterialsWithTypes.size());
        for (int i = 0; i < materialsWithTypes.size(); ++i)
        {
            assertEquals(materialsWithTypes.get(i).getEntityType(), registeredMaterialsWithTypes
                    .get(i).getEntityType());
            assertEquals(materialsWithTypes.get(i).getNewEntities(), registeredMaterialsWithTypes
                    .get(i).getNewEntities());
        }
    }

    private NewMaterial createNewGene(String code, String geneSymbol)
    {
        IEntityProperty geneSymbolProperty = createGeneSymbolProperty(geneSymbol);
        NewMaterial gene = new NewMaterial(code);
        gene.setProperties(new IEntityProperty[]
        { geneSymbolProperty });

        return gene;
    }

    private Material createExistingGene(String code, String geneSymbol)
    {
        IEntityProperty geneSymbolProperty = createGeneSymbolProperty(geneSymbol);
        Material gene = new Material();
        gene.setCode(code);
        gene.setMaterialType(new MaterialType());
        gene.setProperties(Arrays.asList(geneSymbolProperty));
        return gene;
    }

    private IEntityProperty createGeneSymbolProperty(String geneSymbol)
    {
        PropertyType propType = new PropertyType();
        propType.setDataType(new DataType(DataTypeCode.MATERIAL));
        propType.setCode(ScreeningConstants.GENE_SYMBOLS);

        GenericEntityProperty entityProperty = new GenericEntityProperty();
        entityProperty.setPropertyType(propType);
        entityProperty.setValue(geneSymbol);
        return entityProperty;
    }

    private String extractGeneSymbol(NewMaterial newMaterial)
    {
        IEntityProperty property =
                EntityHelper.tryFindProperty(newMaterial.getProperties(),
                        ScreeningConstants.GENE_SYMBOLS);
        return property.getStringValue();
    }
}
