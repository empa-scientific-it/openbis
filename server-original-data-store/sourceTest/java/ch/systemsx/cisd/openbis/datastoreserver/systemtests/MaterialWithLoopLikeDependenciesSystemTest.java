/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * @author Jakub Straszewski
 */
public class MaterialWithLoopLikeDependenciesSystemTest extends SystemTestCase
{

    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/link-data-test/link-data-set-test-handler.py

    File emailDirectory = new File(new File(new File(workingDirectory, "SystemTests"), "dss-root"),
            "email");

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-material-dependencies-test");
    }

    @Test
    public void checkmaterialWithLoopImported() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        createExampleDataSet(exampleDataSet);
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        Material sfrom = openBISService.tryGetMaterial(new MaterialIdentifier("FROM", "SELF_REF"));
        Material sto = openBISService.tryGetMaterial(new MaterialIdentifier("TO", "SELF_REF"));
        Material ofrom = openBISService.tryGetMaterial(new MaterialIdentifier("FROM", "OTHER_REF"));
        Material oto = openBISService.tryGetMaterial(new MaterialIdentifier("TO", "OTHER_REF"));

        assertNotNull(sfrom);
        assertNotNull(sto);
        assertNotNull(ofrom);
        assertNotNull(oto);

        assertNull(getMaterialProperty(oto));
        assertNull(getMaterialProperty(sto));

        assertEquals(oto.getIdentifier(), getMaterialProperty(sfrom));
        assertEquals(sto.getIdentifier(), getMaterialProperty(ofrom));
    }

    private String getMaterialProperty(Material m)
    {
        List<IEntityProperty> properties = m.getProperties();
        for (IEntityProperty iEntityProperty : properties)
        {
            if (iEntityProperty.getPropertyType().getCode().equals("ANY_MATERIAL"))
            {
                return iEntityProperty.getMaterial().getIdentifier();
            }
        }
        return null;
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
    }

}
