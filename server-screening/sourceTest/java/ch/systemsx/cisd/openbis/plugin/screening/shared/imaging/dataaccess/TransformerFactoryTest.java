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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.SerializationUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * @author Jakub Straszewski
 */
@Test
public class TransformerFactoryTest
{

    @DataProvider(name = "transformers")
    public Object[][] getTransformers()
    {
        return new Object[][] {
                { "aced00057372005e63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e76312e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f69636574006b4c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f76312f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006963682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e76312e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74003b2d636f6e74726173742d737472657463682030202d656467652031202d7468726573686f6c642031202d7472616e73706172656e7420626c61636b" }
        };
    }

    @Test(dataProvider = "transformers")
    public void testDeserializationOfLegacyTransformations(String serializedTransformer)
    {
        byte[] data =
                DatatypeConverter
                        .parseHexBinary(serializedTransformer);
        IImageTransformerFactory transformerFactory = (IImageTransformerFactory) SerializationUtils
                .deserialize(data);

        IImageTransformer transformer = transformerFactory.createTransformer();

        Assert.assertTrue(transformer instanceof ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformer);
    }

}
