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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import junit.framework.Assert;

import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class LinkDataSetUrlTest
{

    @Test
    public void testToStringWithCodeThatDoesntRequireEncoding()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();
        system.setUrlTemplate("http://testdomain.com/?q=${code}");

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST_EXTERNAL_CODE");
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals("http://testdomain.com/?q=TEST_EXTERNAL_CODE", new LinkDataSetUrl(
                dataset).toString());
    }

    @Test
    public void testToStringWithCodeThatDoesRequireEncoding()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();
        system.setUrlTemplate("http://testdomain.com/?q=${code}");

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST EXTERNAL CODE");
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals("http://testdomain.com/?q=TEST+EXTERNAL+CODE", new LinkDataSetUrl(
                dataset).toString());
    }

    @Test
    public void testToStringWithTemplateUrlWithoutCodePattern()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();
        system.setUrlTemplate("http://testdomain.com/?q=ABC");

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST_EXTERNAL_CODE");
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals("http://testdomain.com/?q=ABC", new LinkDataSetUrl(dataset).toString());
    }

    @Test
    public void testToStringWithTemplateUrlWithManyCodePatterns()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();
        system.setUrlTemplate("http://testdomain.com/?q1=${code}&q2=${code}");

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST_EXTERNAL_CODE");
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals("http://testdomain.com/?q1=TEST_EXTERNAL_CODE&q2=TEST_EXTERNAL_CODE",
                new LinkDataSetUrl(dataset).toString());
    }

    @Test
    public void testToStringWithTemplateUrlNull()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST_EXTERNAL_CODE");
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals(null, new LinkDataSetUrl(dataset).toString());
    }

    @Test
    public void testToStringWithTemplateUrlEmpty()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();
        system.setUrlTemplate("");

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST_EXTERNAL_CODE");
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals("", new LinkDataSetUrl(dataset).toString());
    }

    @Test
    public void testToStringWithExternalCodeNull()
    {
        ExternalDataManagementSystem system = new ExternalDataManagementSystem();
        system.setUrlTemplate("http://testdomain.com/?q=${code}");

        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalDataManagementSystem(system);

        Assert.assertEquals(null, new LinkDataSetUrl(dataset).toString());
    }

    @Test
    public void testToStringWithExternalSystemNull()
    {
        LinkDataSet dataset = new LinkDataSet();
        dataset.setExternalCode("TEST_EXTERNAL_CODE");

        Assert.assertEquals(null, new LinkDataSetUrl(dataset).toString());
    }

}
