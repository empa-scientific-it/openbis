package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

@Test(groups = "ANewMetadataUITest")
public class ANewMetadataUITest extends SystemTestCase
{

    @Test
    public void testRegisterAndAssignPropertyType()
    {
        // Entity Type
        final String entityTypeCode = "TEST_ENTITY_TYPE_CODE_" + System.currentTimeMillis();
        final EntityKind entityKind = EntityKind.EXPERIMENT;

        ExperimentType entityType = new ExperimentType();
        entityType.setCode(entityTypeCode);

        // Property Type
        final String propertyTypeCode = "TEST_PROPERTY_TYPE_CODE_" + System.currentTimeMillis();
        final String propertyTypeLabel = "TEST_PROPERTY_TYPE_LABEL_" + System.currentTimeMillis();
        final String propertyTypeDescription = "TEST_PROPERTY_TYPE_DESCRIPTION_" + System.currentTimeMillis();
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.INTEGER);

        PropertyType newPropertyType = new PropertyType();
        newPropertyType.setCode(propertyTypeCode);
        newPropertyType.setLabel(propertyTypeLabel);
        newPropertyType.setDescription(propertyTypeDescription);
        newPropertyType.setDataType(dataType);

        // Assigment
        NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
        newETPTAssigment.setEntityKind(entityKind);
        newETPTAssigment.setPropertyTypeCode(propertyTypeCode);
        newETPTAssigment.setEntityTypeCode(entityTypeCode);
        newETPTAssigment.setOrdinal(0L);

        // Call
        commonServer.registerExperimentType(systemSessionToken, entityType);
        commonServer.registerAndAssignPropertyType(systemSessionToken, newPropertyType, newETPTAssigment);

        // Validation
        List<EntityTypePropertyType<?>> listAssigments = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);

        assertEquals(newPropertyType, listAssigments.get(0).getPropertyType());
    }

    @Test
    public void testRegisterEntitytypeAndAssignPropertyTypes() throws Exception
    {
        registerEntitytypeAndAssignPropertyTypesWithId(0);
    }

    @Test
    public void testUpdateEntitytypeAndPropertyTypes() throws Exception
    {
        int testId = 1;
        registerEntitytypeAndAssignPropertyTypesWithId(testId);
        for (EntityKind entityKind : EntityKind.values())
        {
            // Existing Entity Type
            final String entityTypeCode = "TEST_ENTITY_TYPE_CODE_" + entityKind.name() + "_" + testId;
            List<? extends EntityType> types = null;
            EntityType entityType = null;

            switch (entityKind)
            {
                case EXPERIMENT:
                    types = commonServer.listExperimentTypes(systemSessionToken);
                    break;
                case DATA_SET:
                    types = commonServer.listDataSetTypes(systemSessionToken);
                    break;
                case MATERIAL:
                    types = commonServer.listMaterialTypes(systemSessionToken);
                    break;
                case SAMPLE:
                    types = commonServer.listSampleTypes(systemSessionToken);
                    break;
            }

            for (EntityType type : types)
            {
                if (type.getCode().equals(entityTypeCode))
                {
                    entityType = type;
                }
            }

            // Existing Properties
            List<EntityTypePropertyType<?>> listAssigmentsOld = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);
            List listAssigmentsOldHack = listAssigmentsOld;
            switch (entityKind)
            {
                case EXPERIMENT:
                    ((ExperimentType) entityType).setExperimentTypePropertyTypes(listAssigmentsOldHack);
                    break;
                case DATA_SET:
                    ((DataSetType) entityType).setDataSetTypePropertyTypes(listAssigmentsOldHack);
                    break;
                case MATERIAL:
                    ((MaterialType) entityType).setMaterialTypePropertyTypes(listAssigmentsOldHack);
                    break;
                case SAMPLE:
                    ((SampleType) entityType).setSampleTypePropertyTypes(listAssigmentsOldHack);
                    break;
            }

            // Complete Assignments Object
            NewETNewPTAssigments assigments = new NewETNewPTAssigments();
            assigments.setEntity(entityType);
            assigments.setAssigments(new ArrayList<NewPTNewAssigment>());

            // Existing Assignments
            for (int i = 0; i < entityType.getAssignedPropertyTypes().size(); i++)
            {
                NewETPTAssignment oldETPTAssigment = new NewETPTAssignment();
                oldETPTAssigment.setEntityKind(entityType.getAssignedPropertyTypes().get(i).getEntityKind());
                oldETPTAssigment.setPropertyTypeCode(entityType.getAssignedPropertyTypes().get(i).getPropertyType().getCode());
                oldETPTAssigment.setEntityTypeCode(entityType.getAssignedPropertyTypes().get(i).getEntityType().getCode());
                oldETPTAssigment.setOrdinal((long) entityType.getAssignedPropertyTypes().get(i).getOrdinal());
                oldETPTAssigment.setModificationDate(entityType.getAssignedPropertyTypes().get(i).getModificationDate());

                NewPTNewAssigment oldPTNewAssigment = new NewPTNewAssigment();
                oldPTNewAssigment.setExistingPropertyType(true);
                oldPTNewAssigment.setPropertyType(entityType.getAssignedPropertyTypes().get(i).getPropertyType());
                oldPTNewAssigment.setAssignment(oldETPTAssigment);

                assigments.getAssigments().add(oldPTNewAssigment);
            }

            // New Assignments
            Random random = new Random();
            final int numberOfProperties = random.nextInt(30) + 1;
            for (int i = 0; i < numberOfProperties; i++)
            {
                switch (random.nextInt(3))
                {
                    case 0: // Insert
                        final String propertyTypeCode = "TEST_PROPERTY_TYPE_CODE_" + random.nextInt();
                        final String propertyTypeLabel = "TEST_PROPERTY_TYPE_LABEL_" + random.nextInt();
                        final String propertyTypeDescription = "TEST_PROPERTY_TYPE_DESCRIPTION_" + random.nextInt();
                        final DataType dataType = new DataType();
                        dataType.setCode(DataTypeCode.INTEGER);

                        PropertyType newPropertyType = new PropertyType();
                        newPropertyType.setCode(propertyTypeCode);
                        newPropertyType.setLabel(propertyTypeLabel);
                        newPropertyType.setDescription(propertyTypeDescription);
                        newPropertyType.setDataType(dataType);

                        NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
                        newETPTAssigment.setEntityKind(entityKind);
                        newETPTAssigment.setPropertyTypeCode(propertyTypeCode);
                        newETPTAssigment.setEntityTypeCode(entityTypeCode);
                        if (assigments.getAssigments().size() > 0)
                        {
                            newETPTAssigment.setOrdinal((long) random.nextInt(assigments.getAssigments().size()));
                        } else
                        {
                            newETPTAssigment.setOrdinal(0L);
                        }
                        newETPTAssigment.setModificationDate(new Date());

                        NewPTNewAssigment newPTNewassignment = new NewPTNewAssigment();
                        newPTNewassignment.setExistingPropertyType(false);
                        newPTNewassignment.setPropertyType(newPropertyType);
                        newPTNewassignment.setAssignment(newETPTAssigment);

                        assigments.refreshOrderAdd(newPTNewassignment);
                        break;
                    case 1: // Modification
                        if (assigments.getAssigments().size() > 0)
                        {
                            int posToModify = random.nextInt(assigments.getAssigments().size()); // Random Position
                            NewETPTAssignment toModify = assigments.getAssigments().get(posToModify).getAssignment();
                            int toNewPos = random.nextInt(assigments.getAssigments().size()); // Random Position
                            toModify.setOrdinal((long) toNewPos);
                            assigments.refreshOrderUpdate(toModify);
                        }
                        break;
                    case 2: // Delete
                        if (assigments.getAssigments().size() > 0)
                        {
                            int posToDelete = random.nextInt(assigments.getAssigments().size()); // Random Position
                            assigments.refreshOrderDelete(assigments.getAssigments().get(posToDelete).getPropertyType().getCode());
                        }
                        break;
                }
            }

            // Call
            commonServer.updateEntitytypeAndPropertyTypes(systemSessionToken, assigments);

            // Validation
            List<EntityTypePropertyType<?>> listAssigments = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);

            assertEquals(assigments.getAssigments().size(), listAssigments.size());
            for (int i = 0; i < assigments.getAssigments().size(); i++)
            {
                if (assigments.getAssigments().get(i).getPropertyType().getCode().equals(listAssigments.get(i).getPropertyType().getCode())
                        || isContained(assigments.getAssigments().get(i).getPropertyType(), listAssigments))
                {
                    // Cool, is ok
                } else
                {
                    throw new AssertionError("Not contained in list");
                }
            }
        }
    }

    public static boolean isContained(PropertyType propertyType, List<EntityTypePropertyType<?>> inList)
    {
        for (EntityTypePropertyType etpt : inList)
        {
            if (etpt.getPropertyType().getCode().equals(propertyType.getCode()))
            {
                return true;
            }
        }
        return false;
    }

    private void registerEntitytypeAndAssignPropertyTypesWithId(int testId) throws Exception
    {
        for (EntityKind entityKind : EntityKind.values())
        {
            // Entity Type
            final String entityTypeCode = "TEST_ENTITY_TYPE_CODE_" + entityKind.name() + "_" + testId;
            EntityType entityType = null;

            switch (entityKind)
            {
                case EXPERIMENT:
                    entityType = new ExperimentType();
                    ((ExperimentType) entityType).setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
                    break;
                case DATA_SET:
                    entityType = new DataSetType();
                    ((DataSetType) entityType).setDataSetTypePropertyTypes(new ArrayList<DataSetTypePropertyType>());
                    break;
                case MATERIAL:
                    entityType = new MaterialType();
                    ((MaterialType) entityType).setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
                    break;
                case SAMPLE:
                    entityType = new SampleType();
                    ((SampleType) entityType).setGeneratedCodePrefix("TEST");
                    ((SampleType) entityType).setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
                    break;
            }
            entityType.setCode(entityTypeCode);

            // Complete Assignments Object
            NewETNewPTAssigments assigments = new NewETNewPTAssigments();
            assigments.setEntity(entityType);
            assigments.setAssigments(new ArrayList<NewPTNewAssigment>());

            Random random = new Random();
            final int numberOfProperties = random.nextInt(20) + 1;
            for (int i = 0; i < numberOfProperties; i++)
            {
                // Property Type
                final String propertyTypeCode = "TEST_PROPERTY_TYPE_CODE_" + random.nextInt();
                final String propertyTypeLabel = "TEST_PROPERTY_TYPE_LABEL_" + random.nextInt();
                final String propertyTypeDescription = "TEST_PROPERTY_TYPE_DESCRIPTION_" + random.nextInt();
                final DataType dataType = new DataType();
                dataType.setCode(DataTypeCode.INTEGER);

                PropertyType newPropertyType = new PropertyType();
                newPropertyType.setCode(propertyTypeCode);
                newPropertyType.setLabel(propertyTypeLabel);
                newPropertyType.setDescription(propertyTypeDescription);
                newPropertyType.setDataType(dataType);

                // New Assignments
                NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
                newETPTAssigment.setEntityKind(entityKind);
                newETPTAssigment.setPropertyTypeCode(propertyTypeCode);
                newETPTAssigment.setEntityTypeCode(entityTypeCode);
                newETPTAssigment.setOrdinal((long) assigments.getAssigments().size());

                NewPTNewAssigment newPTNewAssigment = new NewPTNewAssigment();
                newPTNewAssigment.setExistingPropertyType(false);
                newPTNewAssigment.setPropertyType(newPropertyType);
                newPTNewAssigment.setAssignment(newETPTAssigment);

                assigments.refreshOrderAdd(newPTNewAssigment);
            }

            // Call
            commonServer.registerEntitytypeAndAssignPropertyTypes(systemSessionToken, assigments);

            // Validation
            List<EntityTypePropertyType<?>> listAssigments = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);
            for (int i = 0; i < assigments.getAssigments().size(); i++)
            {
                assertEquals(assigments.getAssigments().get(i).getPropertyType(), listAssigments.get(i).getPropertyType());
            }
        }
    }
}
