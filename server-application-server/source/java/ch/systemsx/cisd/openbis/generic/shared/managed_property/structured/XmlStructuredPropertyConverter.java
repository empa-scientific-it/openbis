/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.EntityLinkElementKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * An implementation of {@link IStructuredPropertyConverter}, that translates elements to XML.
 * 
 * @author Kaloyan Enimanev
 */
public class XmlStructuredPropertyConverter implements IStructuredPropertyConverter
{

    private static final String ROOT_NAME = "root";

    private final IElementFactory factory;

    public XmlStructuredPropertyConverter(IElementFactory factory)
    {
        this.factory = factory;
    }

    public boolean canHandle(IManagedProperty property)
    {
        return canHandle((String)property.getValue());
    }

    public boolean canHandle(String string)
    {
        return string.startsWith("<" + ROOT_NAME) || ManagedProperty.isSpecialValue(string)
                || StringUtils.isBlank(string);
    }

    @Override
    public List<IElement> convertToElements(IManagedProperty property)
    {
        return convertStringToElements((String)property.getValue());
    }

    @Override
    public List<IElement> convertStringToElements(String propertyValue)
    {
        if (ManagedProperty.isSpecialValue(propertyValue) || StringUtils.isBlank(propertyValue))
        {
            return Collections.<IElement> emptyList();
        }

        Document document = XmlUtils.parseXmlDocument(propertyValue);
        IElement root = transformFromDOM(document.getDocumentElement());
        return root.getChildren();
    }

    @Override
    public String convertToString(List<IElement> elements)
    {
        IElement root = createRootElement(elements);
        Document doc = createEmptyDocument();
        Node rootNode = transformToDOM(root, doc);
        doc.appendChild(rootNode);

        return XmlUtils.serializeDocument(doc);
    }

    private IElement createRootElement(List<IElement> elements)
    {
        IElement root = new Element(ROOT_NAME);
        root.setChildren(elements);
        return root;
    }

    private Node transformToDOM(IElement element, Document document)
    {
        Node result = document.createElement(element.getName());

        for (IElement child : element.getChildren())
        {
            Node childNode = transformToDOM(child, document);
            result.appendChild(childNode);
        }

        transformAttributesToDOM(element, document, result);
        transformDataToDOM(element, document, result);

        return result;
    }

    private IElement transformFromDOM(Node node)
    {
        IElement result = createElementForNode(node);

        transformAttributesFromDOM(node, result);

        // children & text data
        NodeList domChildren = node.getChildNodes();
        List<IElement> children = new ArrayList<IElement>();
        if (domChildren != null)
        {
            for (int i = 0; i < domChildren.getLength(); i++)
            {
                Node domChild = domChildren.item(i);
                if (domChild.getNodeType() == Node.ELEMENT_NODE)
                {
                    IElement child = transformFromDOM(domChild);
                    children.add(child);
                } else if (domChild.getNodeType() == Node.TEXT_NODE)
                {

                    String textContent = domChild.getNodeValue().trim();
                    textContent = StringEscapeUtils.unescapeXml(textContent);
                    if (false == StringUtils.isBlank(textContent))
                    {
                        result.setData(textContent);
                    }
                }
            }

        }

        result.setChildren(children);
        return result;
    }

    private void transformAttributesFromDOM(Node node, IElement result)
    {
        NamedNodeMap domAttributes = node.getAttributes();
        if (domAttributes != null)
        {
            Map<String, String> attributes = new HashMap<String, String>();
            for (int i = 0; i < domAttributes.getLength(); i++)
            {
                Attr domAttr = (Attr) domAttributes.item(i);
                String unescapedValue = StringEscapeUtils.unescapeXml(domAttr.getValue());
                attributes.put(domAttr.getName(), unescapedValue);
            }
            result.setAttributes(attributes);
        }
    }

    private IElement createElementForNode(Node node)
    {
        String nodeName = node.getLocalName();

        EntityLinkElementKind linkKind = EntityLinkElementKind.tryGetForElementName(nodeName);
        if (linkKind != null)
        {
            String permId = getAttrValueOrFail(node, EntityLinkElement.PERMID_ATTR_NAME);
            return new EntityLinkElement(linkKind, permId);
        }

        // plain element, no special treatment needed
        return factory.createElement(nodeName);
    }

    private String getAttrValueOrFail(Node node, String attrName)
    {
        Attr attr = (Attr) node.getAttributes().getNamedItem(attrName);
        if (attr == null)
        {
            String error =
                    String.format("Attribute [%s] expected in nodes with name %s", attrName,
                            node.getNodeName());
            throw new IllegalArgumentException(error);
        }
        return attr.getValue();
    }

    private void transformAttributesToDOM(IElement element, Document document, Node node)
    {
        Map<String, String> attributes = element.getAttributes();
        for (String key : element.getAttributes().keySet())
        {
            Attr domAttr = document.createAttribute(key);
            String escapedValue = StringEscapeUtils.escapeXml(attributes.get(key));
            domAttr.setValue(escapedValue);
            node.getAttributes().setNamedItem(domAttr);
        }
    }

    private void transformDataToDOM(IElement element, Document document, Node node)
    {
        // text data
        String data = element.getData();
        if (StringUtils.isBlank(data) == false)
        {
            String escapedXmlContent = StringEscapeUtils.escapeXml(data);
            Node textNode = document.createTextNode(escapedXmlContent);
            node.appendChild(textNode);
        }
    }

    private Document createEmptyDocument()
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document doc = parser.newDocument();
            return doc;
        } catch (ParserConfigurationException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
