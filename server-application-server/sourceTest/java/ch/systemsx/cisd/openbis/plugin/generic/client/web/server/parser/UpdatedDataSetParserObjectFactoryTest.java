/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.parser.DefaultPropertyMapper;
import ch.systemsx.cisd.common.parser.MandatoryPropertyMissingException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedDataSet;

/**
 * Test cases for corresponding {@link UpdatedDataSetParserObjectFactory} class.
 * 
 * @author pkupczyk
 */
public final class UpdatedDataSetParserObjectFactoryTest
{

    @Test(expectedExceptions = MandatoryPropertyMissingException.class)
    public void testLineWithNoColumnsSpecified()
    {
        createObject(new Tokens());
    }

    @Test(expectedExceptions = MandatoryPropertyMissingException.class)
    public void testLineWithCodeColumnSpecifiedButEmpty()
    {
        Tokens tokens = new Tokens();
        tokens.setToken(Code.CODE, "");
        createObject(tokens);
    }

    @Test
    public void testLineWithCodeColumnSpecifiedAndNotEmpty()
    {
        Tokens tokens = new Tokens();
        tokens.setToken(Code.CODE, "TEST-CODE");

        UpdatedDataSet dataset = createObject(tokens);

        Assert.assertEquals("TEST-CODE", dataset.getCode());

        Assert.assertNull(dataset.getSampleIdentifierOrNull());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isSampleUpdateRequested());

        Assert.assertNull(dataset.getExperimentIdentifier());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isExperimentUpdateRequested());

        Assert.assertNull(dataset.getContainerIdentifierOrNull());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isContainerUpdateRequested());

        Assert.assertEquals(0, dataset.getParentsIdentifiersOrNull().length);
        Assert.assertFalse(dataset.getBatchUpdateDetails().isParentsUpdateRequested());

        Assert.assertNull(dataset.getFileFormatOrNull());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isFileFormatUpdateRequested());

        Assert.assertEquals(0, dataset.getProperties().length);
    }

    @Test
    public void testLineWithColumnsSpecifiedButEmpty()
    {
        Tokens tokens = new Tokens();
        tokens.setToken(Code.CODE, "TEST-CODE");
        tokens.setToken(NewDataSet.CONTAINER, "");
        tokens.setToken(NewDataSet.EXPERIMENT, "");
        tokens.setToken(NewDataSet.FILE_FORMAT, "");
        tokens.setToken(NewDataSet.PARENTS, "");
        tokens.setToken(NewDataSet.SAMPLE, "");
        tokens.setToken("TEST-PROPERTY-1", "");
        tokens.setToken("TEST-PROPERTY-2", "");

        UpdatedDataSet dataset = createObject(tokens);

        Assert.assertEquals("TEST-CODE", dataset.getCode());

        Assert.assertNull(dataset.getSampleIdentifierOrNull());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isSampleUpdateRequested());

        Assert.assertNull(dataset.getExperimentIdentifier());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isExperimentUpdateRequested());

        Assert.assertNull(dataset.getContainerIdentifierOrNull());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isContainerUpdateRequested());

        Assert.assertEquals(0, dataset.getParentsIdentifiersOrNull().length);
        Assert.assertFalse(dataset.getBatchUpdateDetails().isParentsUpdateRequested());

        Assert.assertNull(dataset.getFileFormatOrNull());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isFileFormatUpdateRequested());

        Assert.assertEquals(0, dataset.getProperties().length);
    }

    @Test
    public void testLineWithColumnsSpecifiedAndNotEmpty()
    {
        Tokens tokens = new Tokens();
        tokens.setToken(Code.CODE, "TEST-CODE");
        tokens.setToken(NewDataSet.CONTAINER, "TEST-CONTAINER");
        tokens.setToken(NewDataSet.EXPERIMENT, "TEST-EXPERIMENT");
        tokens.setToken(NewDataSet.FILE_FORMAT, "TEST-FORMAT");
        tokens.setToken(NewDataSet.PARENTS, "TEST-PARENT-1,TEST-PARENT-2");
        tokens.setToken(NewDataSet.SAMPLE, "TEST-SAMPLE");
        tokens.setToken("TEST-PROPERTY-1", "TEST-VALUE-1");
        tokens.setToken("TEST-PROPERTY-2", "TEST-VALUE-2");

        UpdatedDataSet dataset = createObject(tokens);

        Assert.assertEquals("TEST-CODE", dataset.getCode());

        Assert.assertEquals("TEST-SAMPLE", dataset.getSampleIdentifierOrNull());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isSampleUpdateRequested());

        Assert.assertEquals("TEST-EXPERIMENT", dataset.getExperimentIdentifier());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isExperimentUpdateRequested());

        Assert.assertEquals("TEST-CONTAINER", dataset.getContainerIdentifierOrNull());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isContainerUpdateRequested());

        Assert.assertEquals(2, dataset.getParentsIdentifiersOrNull().length);
        Assert.assertEquals("TEST-PARENT-1", dataset.getParentsIdentifiersOrNull()[0]);
        Assert.assertEquals("TEST-PARENT-2", dataset.getParentsIdentifiersOrNull()[1]);
        Assert.assertTrue(dataset.getBatchUpdateDetails().isParentsUpdateRequested());

        Assert.assertEquals("TEST-FORMAT", dataset.getFileFormatOrNull());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isFileFormatUpdateRequested());

        Assert.assertEquals(2, dataset.getProperties().length);
        Assert.assertEquals("TEST-VALUE-1", dataset.getProperties()[0].getValue());
        Assert.assertEquals("TEST-VALUE-2", dataset.getProperties()[1].getValue());
    }

    @Test
    public void testLineWithColumnsSpecifiedAndMarkedForDeletetion()
    {
        Tokens tokens = new Tokens();
        tokens.setToken(Code.CODE, "TEST-CODE");
        tokens.setToken(NewDataSet.CONTAINER, "--DELETE--");
        // experiment column cannot be marked for deletion
        tokens.setToken(NewDataSet.FILE_FORMAT, "__DELETE__");
        tokens.setToken(NewDataSet.PARENTS, "--DELETE--");
        tokens.setToken(NewDataSet.SAMPLE, "--DELETE--");
        tokens.setToken("TEST-PROPERTY-1", "--DELETE--");
        tokens.setToken("TEST-PROPERTY-2", "__DELETE__");

        UpdatedDataSet dataset = createObject(tokens);

        Assert.assertEquals("TEST-CODE", dataset.getCode());

        Assert.assertNull(dataset.getSampleIdentifierOrNull());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isSampleUpdateRequested());

        Assert.assertNull(dataset.getExperimentIdentifier());
        Assert.assertFalse(dataset.getBatchUpdateDetails().isExperimentUpdateRequested());

        Assert.assertNull(dataset.getContainerIdentifierOrNull());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isContainerUpdateRequested());

        Assert.assertEquals(0, dataset.getParentsIdentifiersOrNull().length);
        Assert.assertTrue(dataset.getBatchUpdateDetails().isParentsUpdateRequested());

        Assert.assertNull(dataset.getFileFormatOrNull());
        Assert.assertTrue(dataset.getBatchUpdateDetails().isFileFormatUpdateRequested());

        Assert.assertEquals(0, dataset.getProperties().length);
    }

    private UpdatedDataSet createObject(Tokens tokens)
    {
        UpdatedDataSetParserObjectFactory parser =
                new UpdatedDataSetParserObjectFactory(new DefaultPropertyMapper(
                        tokens.getTokensNames(), null));

        return (UpdatedDataSet) parser.createObject(tokens.getTokensValues());
    }

    private class Tokens
    {

        private Map<String, String> tokens = new LinkedHashMap<String, String>();

        public void setToken(String name, String value)
        {
            tokens.put(name, value);
        }

        public String[] getTokensNames()
        {
            return tokens.keySet().toArray(new String[tokens.size()]);
        }

        public String[] getTokensValues()
        {
            return tokens.values().toArray(new String[tokens.size()]);
        }

    }

}
