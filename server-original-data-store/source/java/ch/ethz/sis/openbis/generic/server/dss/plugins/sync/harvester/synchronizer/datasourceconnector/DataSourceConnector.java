/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.datasourceconnector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.BasicAuthCredentials;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;

/**
 * @author Ganime Betul Akin
 */
public class DataSourceConnector implements IDataSourceConnector
{

    private final String dataSourceUrl;

    private final BasicAuthCredentials authCredentials;

    private Logger operationLog;

    public DataSourceConnector(String url, BasicAuthCredentials authCredentials, Logger operationLog)
    {
        this.dataSourceUrl = url;
        this.authCredentials = authCredentials;
        this.operationLog = operationLog;
    }

    @Override
    public Document getResourceListAsXMLDoc(List<String> spaceBlackList, List<String> spaceWhiteList) throws Exception
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        Request requestEntity = createResourceListRequest(client, spaceBlackList, spaceWhiteList);
        operationLog.info("Start loading a resource list from " + requestEntity.getURI());
        
        InputStreamResponseListener listener = new InputStreamResponseListener();
        requestEntity.send(listener);
        Response response = listener.get(50, TimeUnit.SECONDS);
        if (response.getStatus() == HttpStatus.OK_200)
        {
            try (InputStream responseContent = listener.getInputStream())
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                builder.setEntityResolver(new EntityResolver()
                    {
                        
                        @Override
                        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
                        {
                            return new InputSource(new StringReader(""));
                        }
                    });
                Document document = builder.parse(responseContent);
                
                if (isResourceListIndex(document))
                {
                    operationLog.info("Received a resource list index (the resource list was too big and was split into parts).");
                    List<String> locations = getResourceListPartLocations(document);
                    List<Document> parts = loadResourceListParts(client, locations);
                    return mergeResourceListParts(parts);
                } else
                {
                    operationLog.info("Received the resource list.");
                    return document;
                }
            }
        }
        throw new IOException("Resource List could not be retrieved: " + response.getStatus());
    }

    private boolean isResourceListIndex(Document document)
    {
        return document != null && document.hasChildNodes() && document.getFirstChild().getNodeName().equals("sitemapindex");
    }

    private List<String> getResourceListPartLocations(Document document)
    {
        Node sitemapindex = document.getFirstChild();
        List<String> locations = new ArrayList<String>();

        for (int i = 0; i < sitemapindex.getChildNodes().getLength(); i++)
        {
            Node child = sitemapindex.getChildNodes().item(i);
            if (child.getNodeName().equals("sitemap"))
            {
                for (int j = 0; j < child.getChildNodes().getLength(); j++)
                {
                    Node grandChild = child.getChildNodes().item(j);
                    if (grandChild.getNodeName().equals("loc"))
                    {
                        String location = grandChild.getTextContent().trim();
                        operationLog.info("Resource list part location: " + location);
                        locations.add(location);
                    }
                }
            }
        }

        if (locations.isEmpty())
        {
            operationLog.info("No locations of the resource list parts were found in the index.");
        }

        return locations;
    }

    private List<Document> loadResourceListParts(HttpClient client, List<String> locations) throws Exception
    {
        List<Document> parts = new ArrayList<Document>();

        for (String location : locations)
        {
            Request request = createRequest(client, location);
            operationLog.info("Start loading a resource list part from " + location);
            ContentResponse response = getResponse(request);
            operationLog.info("Received the resource list part.");
            parts.add(parse(response.getContent()));
        }

        return parts;
    }

    private Document mergeResourceListParts(List<Document> parts) throws Exception
    {
        Document mergedDocument = parts.get(0);
        Node mergedUrlset = mergedDocument.getFirstChild();

        for (int i = 1; i < parts.size(); i++)
        {
            Document part = parts.get(i);
            Node urlset = part.getFirstChild();

            for (int j = 0; j < urlset.getChildNodes().getLength(); j++)
            {
                Node child = urlset.getChildNodes().item(j);
                if (child.getNodeName().equals("url"))
                {
                    mergedUrlset.appendChild(mergedDocument.importNode(child, true));
                }
            }
        }

        operationLog.info("Merged the resource list parts.");

        return mergedDocument;
    }

    private Document parse(byte[] content) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(bis);
        return doc;
    }

    private ContentResponse getResponse(Request requestEntity) throws InterruptedException, TimeoutException, ExecutionException, IOException
    {
        ContentResponse contentResponse = requestEntity.send();
        int statusCode = contentResponse.getStatus();

        if (statusCode != HttpStatus.Code.OK.getCode())
        {
            throw new IOException("Resource List could not be retrieved: " + contentResponse.getContentAsString());
        }
        return contentResponse;
    }

    private Request createResourceListRequest(HttpClient client, List<String> spaceBlackList, List<String> spaceWhiteList)
    {
        String url = createRequestUrl(spaceBlackList, spaceWhiteList);
        return createRequest(client, url);
    }

    private String createRequestUrl(List<String> spaceBlackList, List<String> spaceWhiteList)
    {
        String url = dataSourceUrl + "?verb=resourcelist.xml";
        for (String space : spaceBlackList)
        {
            url += "&black_list=" + space;
        }
        for (String space : spaceWhiteList)
        {
            url += "&white_list=" + space;
        }
        return url;
    }

    private Request createRequest(HttpClient client, String url)
    {
        Request requestEntity = client.newRequest(url).method("GET");
        requestEntity.header(HttpHeader.AUTHORIZATION, "Basic " + B64Code.encode(authCredentials.getUser() + ":" + authCredentials.getPassword()));
        return requestEntity;
    }

}
