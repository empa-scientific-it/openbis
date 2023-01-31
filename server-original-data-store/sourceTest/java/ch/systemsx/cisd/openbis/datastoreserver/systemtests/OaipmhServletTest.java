/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import ch.systemsx.cisd.common.http.GetResponse;
import ch.systemsx.cisd.common.http.HttpTest;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
@Test(groups = { "slow" })
public class OaipmhServletTest extends SystemTestCase
{

    private static final String GENERAL_INFORMATION_SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + IGeneralInformationService.SERVICE_URL;

    private static final String OAIPMH_SERVLET_URL = TestInstanceHostUtils.getDSSUrl() + "/oaipmh/";

    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "password";

    private IGeneralInformationService generalInformationService;

    @BeforeClass
    public void beforeClass()
    {
        generalInformationService = HttpInvokerUtils.createServiceStub(IGeneralInformationService.class, GENERAL_INFORMATION_SERVICE_URL, 5000);
    }

    @Test
    public void testWithoutAuthorizationHeader()
    {
        GetResponse method = HttpTest.sendRequest(null, OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(401, method.getStatus());
    }

    @Test
    public void testWithIncorrectAuthorizationHeader()
    {
        GetResponse method = HttpTest.sendRequest("This is an invalid header", OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(500, method.getStatus());
    }

    @Test
    public void testWithIncorrectCredentials()
    {
        GetResponse method = HttpTest.sendRequest("incorrect", USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(500, method.getStatus());
        String content = method.getContent();
        Assert.assertTrue(content, content.contains("has no role assignments"));
    }

    @Test
    public void testIdentify() throws InterruptedException
    {
        GetResponse method = HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(200, method.getStatus());
        Document document = HttpTest.parseResponse(method);
        Assert.assertEquals("TEST", HttpTest.evaluateToString(document, "/OAI-PMH/Identify/repositoryName"));
    }

    @Test
    public void testListMetadataformats()
    {
        GetResponse method = HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListMetadataFormats");
        Assert.assertEquals(200, method.getStatus());
        Document document = HttpTest.parseResponse(method);
        Assert.assertEquals("testPrefix", HttpTest.evaluateToString(document, "/OAI-PMH/ListMetadataFormats/metadataFormat/metadataPrefix"));
    }

    @Test
    public void testListSets()
    {
        GetResponse method = HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListSets");
        Assert.assertEquals(200, method.getStatus());
        Document document = HttpTest.parseResponse(method);
        Assert.assertEquals("This repository does not support sets", HttpTest.evaluateToString(document, "/OAI-PMH/error"));
    }

    @Test
    public void testListIdentifiers()
    {
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices(USER_ID, USER_PASSWORD);
        List<DataSet> dataSets = generalInformationService.searchForDataSets(sessionToken, new SearchCriteria());

        String resumptionToken = null;
        int dataSetCount = 0;

        do
        {
            GetResponse method = null;
            if (resumptionToken == null)
            {
                method = HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListIdentifiers&metadataPrefix=testPrefix");
            } else
            {
                method =
                        HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListIdentifiers&resumptionToken=" + resumptionToken);
            }
            Assert.assertEquals(200, method.getStatus());

            Document document = HttpTest.parseResponse(method);
            dataSetCount += HttpTest.evaluateToNodeList(document, "/OAI-PMH/ListIdentifiers/header").getLength();
            resumptionToken = HttpTest.evaluateToString(document, "/OAI-PMH/ListIdentifiers/resumptionToken");

        } while (resumptionToken != null && !resumptionToken.isEmpty());

        Assert.assertEquals(dataSets.size(), dataSetCount);
    }

    @Test
    public void testListRecords()
    {
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices(USER_ID, USER_PASSWORD);
        List<DataSet> dataSets = generalInformationService.searchForDataSets(sessionToken, new SearchCriteria());

        String resumptionToken = null;
        int dataSetCount = 0;

        do
        {
            GetResponse method = null;
            if (resumptionToken == null)
            {
                method = HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListRecords&metadataPrefix=testPrefix");
            } else
            {
                method = HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListRecords&resumptionToken=" + resumptionToken);
            }
            Assert.assertEquals(200, method.getStatus());

            Document document = HttpTest.parseResponse(method);
            dataSetCount += HttpTest.evaluateToNodeList(document, "/OAI-PMH/ListRecords/record").getLength();
            resumptionToken = HttpTest.evaluateToString(document, "/OAI-PMH/ListRecords/resumptionToken");

        } while (resumptionToken != null && !resumptionToken.isEmpty());

        Assert.assertEquals(dataSets.size(), dataSetCount);
    }

    @Test
    public void testGetRecord()
    {
        GetResponse method =
                HttpTest.sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL
                        + "?verb=GetRecord&metadataPrefix=testPrefix&identifier=20081105092159111-1");
        Assert.assertEquals(200, method.getStatus());

        Document document = HttpTest.parseResponse(method);
        Assert.assertEquals("20081105092159111-1", HttpTest.evaluateToString(document, "/OAI-PMH/GetRecord/record/header/identifier"));
        Assert.assertEquals("FEMALE", HttpTest.evaluateToString(document, "/OAI-PMH/GetRecord/record/metadata/properties/property[@code='GENDER']"));
    }

}
