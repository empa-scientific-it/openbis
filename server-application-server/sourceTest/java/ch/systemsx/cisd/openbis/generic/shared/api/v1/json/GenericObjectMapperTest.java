/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.json;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonMappingException;

import ch.systemsx.cisd.openbis.generic.shared.api.common.json.AbstractGenericObjectMapperTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;

/**
 * @author pkupczyk
 */
public class GenericObjectMapperTest extends AbstractGenericObjectMapperTest
{

    public GenericObjectMapperTest()
    {
        super(new GenericObjectMapper());
    }

    @Test
    public void testObjectWithNameThatExistsOnlyInV1IsDeserializedAsV1() throws Exception
    {
        SamplePermIdId permIdId = deserialize("objectWithNameThatExistsOnlyInV1.json");
        Assert.assertEquals("ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId", permIdId.getClass().getName());
        Assert.assertEquals("TEST_PERM_ID", permIdId.getPermId());
    }

    @Test(expectedExceptions = { JsonMappingException.class })
    public void testObjectWithNameThatExistsOnlyInV3IsNotDeserialized() throws Exception
    {
        deserialize("objectWithNameThatExistsOnlyInV3.json");
    }

    @Test
    public void testObjectWithLegacyClassAttributeThatIsSupportedOnlyInV1IsDeserialized() throws Exception
    {
        MaterialIdentifier materialIdentifier = deserialize("objectWithLegacyClassAttributeThatIsSupportedOnlyInV1.json");
        Assert.assertEquals("ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier", materialIdentifier.getClass().getName());
        Assert.assertEquals("TEST_MATERIAL_IDENTIFIER", materialIdentifier.getMaterialCode());
    }

}
