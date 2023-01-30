package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;

public class MaterialDeletionTest extends DeletionTest
{

    @Test
    @Rollback(false)
    public void testAttributes() throws Exception
    {
        Date after = new Date();
        MaterialPermId material =
                createMaterial("MATERIAL_ATT", props("DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X"));

        newTx();
        Date before = new Date();

        delete(material);

        HashMap<String, String> expectations = new HashMap<String, String>();
        expectations.put("CODE", "MATERIAL_ATT");
        expectations.put("ENTITY_TYPE", "DELETION_TEST");
        expectations.put("REGISTRATOR", "test");
        assertAttributes(material.getCode(), expectations);
        assertRegistrationTimestampAttribute(material.getCode(), after, before);
    }

    @Test
    @Rollback(false)
    public void changeProperties() throws Exception
    {
        MaterialPermId material =
                createMaterial("MATERIAL_1", props("DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X"));

        newTx();
        setProperties(material, "DESCRIPTION", "desc2", "ORGANISM", "GORILLA", "BACTERIUM", "BACTERIUM-Y");

        newTx();
        setProperties(material);

        newTx();
        setProperties(material, "DESCRIPTION", "desc3", "ORGANISM", "DOG", "BACTERIUM", "BACTERIUM2");

        delete(material);

        assertPropertiesHistory(material.getCode(), "DESCRIPTION", "desc", "desc2", "desc3");
        assertPropertiesHistory(material.getCode(), "ORGANISM", "FLY [ORGANISM]", "GORILLA [ORGANISM]", "DOG [ORGANISM]");
        assertPropertiesHistory(material.getCode(), "BACTERIUM", "BACTERIUM-X [BACTERIUM]", "BACTERIUM-Y [BACTERIUM]", "BACTERIUM2 [BACTERIUM]");
    }
    
    @Test
    public void deleteGraphOfMaterials() throws Exception
    {
        MaterialCreator creator = new MaterialCreator();
        MaterialPermId m1 = creator.create(null);
        MaterialPermId m2 = creator.create(m1);
        MaterialPermId m3 = creator.create(m1);
        MaterialPermId m4 = creator.create(m3);
        MaterialPermId m5 = creator.create(m3);
        MaterialPermId m6 = creator.create(m1);
        MaterialPermId m7 = creator.create(m6);

        delete(m1, m2, m3, m4, m5, m6, m7);
    }
    
    private class MaterialCreator
    {
        private String baseCode;
        private int counter;

        MaterialCreator()
        {
            baseCode = "MAT-" + System.currentTimeMillis() + "-";
        }
        
        MaterialPermId create(MaterialPermId permId)
        {
            Map<String, String> props;
            String description = "desc " + ++counter;
            if (permId == null)
            {
                props = props("DESCRIPTION", description);
            } else
            {
                props = props("DESCRIPTION", description, "ANY_MATERIAL", permId.toString());
            }
            return createMaterial(baseCode + counter,  "OTHER_REF", props);
        }
    }
}
