package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

public class AbstractSampleTest extends AbstractTest
{
    protected static SampleCreation masterPlateCreation(String spaceCode, String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        creation.setSpaceId(new SpacePermId(spaceCode));
        creation.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");
        return creation;
    }

    protected static SampleCreation wellCreation(String spaceCode, String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("WELL"));
        creation.setSpaceId(new SpacePermId(spaceCode));
        return creation;
    }

    protected void createNewPropertyType(String sessionToken, String entityTypeCode, String propertyTypeCode)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        propertyType.setDescription(propertyTypeCode + " description");
        propertyType.setLabel(propertyTypeCode + " label");
        propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode(propertyType.getCode());
        assignment.setEntityTypeCode(entityTypeCode);
        assignment.setEntityKind(EntityKind.SAMPLE);
        assignment.setOrdinal(1000L);

        commonServer.registerAndAssignPropertyType(sessionToken, propertyType, assignment);
    }

    protected void assertNoRelationships(String sessionToken, SamplePermId parentId, SamplePermId... children)
    {
        for (SamplePermId childId : children)
        {
            assertAnnotations(sessionToken, null, null, parentId, childId);
        }
    }

    protected void assertNoRelationships(Sample parent, Sample... children)
    {
        for (Sample child : children)
        {
            assertAnnotations(null, null, parent, child);
        }
    }

    protected void assertAnnotations(String sessionToken, String expectedParentAnnotations, String expectedChildAnnotations,
            SamplePermId parentId, SamplePermId childId)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParents();
        fetchOptions.withChildren();
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, Arrays.asList(parentId, childId), fetchOptions);
        Sample parentSample = samples.get(parentId);
        Sample childSample = samples.get(childId);
        assertAnnotations(expectedParentAnnotations, expectedChildAnnotations, parentSample, childSample);
    }

    protected void assertAnnotations(String expectedParentAnnotations, String expectedChildAnnotations, Sample parent, Sample child)
    {
        assertAnnotations(expectedParentAnnotations, expectedChildAnnotations, parent.getChildRelationship(child.getPermId()));
        assertAnnotations(expectedParentAnnotations, expectedChildAnnotations, child.getParentRelationship(parent.getPermId()));
    }

    private void assertAnnotations(String expectedParentAnnotations, String expectedChildAnnotations, Relationship relationship)
    {
        assertAnnotations(expectedParentAnnotations, relationship != null ? relationship.getParentAnnotations() : null);
        assertAnnotations(expectedChildAnnotations, relationship != null ? relationship.getChildAnnotations() : null);
    }

    private void assertAnnotations(String expectedAnnotations, Map<String, String> annotations)
    {
        if (annotations == null)
        {
            assertEquals(annotations, expectedAnnotations);
        } else
        {
            List<String> keyValuePairs = annotations.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
            Collections.sort(keyValuePairs);
            assertEquals(keyValuePairs.toString(), expectedAnnotations);
        }
    }
}
