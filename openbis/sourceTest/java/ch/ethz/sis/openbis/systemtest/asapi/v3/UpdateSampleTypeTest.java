/*
 * Copyright 2017 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test
public class UpdateSampleTypeTest extends UpdateEntityTypeTest<SampleTypeUpdate, SampleType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected SampleTypeUpdate newTypeUpdate()
    {
        return new SampleTypeUpdate();
    }

    @Override
    protected EntityTypePermId getTypeId()
    {
        return new EntityTypePermId("CONTROL_LAYOUT", ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE);
    }
    
    @Override
    protected void createEntity(String sessionToken, IEntityTypeId entityType, String propertyType, String propertyValue)
    {
    }

    @Override
    protected void updateTypes(String sessionToken, List<SampleTypeUpdate> updates)
    {
        v3api.updateSampleTypes(sessionToken, updates);
    }

    @Override
    protected SampleType getType(String sessionToken, EntityTypePermId typeId)
    {
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withPermId().thatEquals(typeId.getPermId());
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withEntityType();
        fetchOptions.withPropertyAssignments().withPropertyType();
        return v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);
    }

    @Override
    protected void updateTypeSpecificFields(SampleTypeUpdate update, int variant)
    {
        switch (variant)
        {
            case 1:
                update.getGeneratedCodePrefix().setValue("M");
                update.isAutoGeneratedCode().setValue(false);
                update.isListable().setValue(false);
                update.isShowContainer().setValue(true);
                update.isShowParents().setValue(false);
                update.isSubcodeUnique().setValue(true);
                update.isShowParentMetadata().setValue(false);
                break;
            default:
                update.isShowParents().setValue(true);
                update.isShowParentMetadata().setValue(true);
        }
    }

    @Override
    protected void assertTypeSpecificFields(SampleType type, SampleTypeUpdate update, int variant)
    {
        assertEquals(type.getGeneratedCodePrefix(), getNewValue(update.getGeneratedCodePrefix(), type.getGeneratedCodePrefix()));
        assertEquals(type.isAutoGeneratedCode(), getNewValue(update.isAutoGeneratedCode(), type.isAutoGeneratedCode()));
        assertEquals(type.isListable(), getNewValue(update.isListable(), type.isListable()));
        assertEquals(type.isShowContainer(), getNewValue(update.isShowContainer(), type.isShowContainer()));
        assertEquals(type.isShowParentMetadata(), getNewValue(update.isShowParentMetadata(), type.isShowParentMetadata()));
        assertEquals(type.isShowParents(), getNewValue(update.isShowParents(), type.isShowParents()));
        assertEquals(type.isSubcodeUnique(), getNewValue(update.isSubcodeUnique(), type.isSubcodeUnique()));
    }

    @Override
    protected String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId)
    {
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType : commonServer.listSampleTypes(sessionToken))
        {
            if (sampleType.getCode().equals(typeId.getPermId()))
            {
                Script validationScript = sampleType.getValidationScript();
                return validationScript == null ? null : validationScript.getName();
            }
        }
        return null;
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId)
    {
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withType().withId().thatEquals(typeId);
        return sampleSearchCriteria;
    }

    @Override
    protected List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        return v3api.searchSamples(sessionToken, (SampleSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

}
