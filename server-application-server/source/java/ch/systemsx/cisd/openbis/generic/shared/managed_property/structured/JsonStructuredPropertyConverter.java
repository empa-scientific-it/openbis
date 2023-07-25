/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.EntityLinkElementKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;

/**
 * An implementation of {@link IStructuredPropertyConverter}, that translates elements to JSON.
 * 
 * @author Bernd Rinn
 */
public class JsonStructuredPropertyConverter implements IStructuredPropertyConverter
{
    private static final String ROOT_NAME = "root";

    private static final String ID_KEY = "id";

    private static final String DATA_KEY = "data";

    private static final String ATTR_KEY = "attr";

    private static final String CHILDREN_KEY = "child";

    private static final TopLevelPrettyPrinter topLevelPrettyPrinter = new TopLevelPrettyPrinter();

    private final IElementFactory factory;

    private static class EntryIt implements Iterable<Map.Entry<String, JsonNode>>
    {
        private final Iterator<Map.Entry<String, JsonNode>> it;

        EntryIt(Iterator<Map.Entry<String, JsonNode>> it)
        {
            this.it = it;
        }

        @Override
        public Iterator<Map.Entry<String, JsonNode>> iterator()
        {
            return it;
        }
    }

    private static class TopLevelPrettyPrinter extends MinimalPrettyPrinter
    {
        private int level = 0;

        @Override
        public void writeStartArray(JsonGenerator jg) throws IOException, JsonGenerationException
        {
            if (level++ == 0)
            {
                jg.writeRaw("[\n  ");
            } else
            {
                jg.writeRaw('[');
            }
        }

        @Override
        public void writeArrayValueSeparator(JsonGenerator jg) throws IOException,
                JsonGenerationException
        {
            if (level == 1)
            {
                jg.writeRaw(",\n  ");
            } else
            {
                jg.writeRaw(',');
            }
        }

        @Override
        public void writeEndArray(JsonGenerator jg, int nrOfValues) throws IOException,
                JsonGenerationException
        {
            if (--level == 0)
            {
                jg.writeRaw("\n]");
            } else
            {
                jg.writeRaw(']');
            }
        }
    }

    public JsonStructuredPropertyConverter(IElementFactory factory)
    {
        this.factory = factory;
    }

    public boolean canHandle(IManagedProperty property)
    {
        return canHandle((String)property.getValue());
    }

    public boolean canHandle(String string)
    {
        return string.startsWith("[");
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

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode;
        try
        {
            rootNode = mapper.readValue(propertyValue, JsonNode.class);
        } catch (Exception ex)
        {
            throw new UserFailureException("Cannot parse property '" + propertyValue + "'.", ex);
        }
        final IElement root = transformFromDOM(rootNode);
        return root.getChildren();
    }

    private IElement transformFromDOM(JsonNode node)
    {
        if (node.isObject())
        {
            return transformFromDOM((ObjectNode) node);
        } else if (node.isArray())
        {
            final IElement result = factory.createElement(ROOT_NAME);
            for (JsonNode child : node)
            {
                result.addChildren(transformFromDOM(child));
            }
            return result;
        } else
        {
            throw new IllegalArgumentException("Illegal JSNO node: " + node.getClass());
        }
    }

    private IElement transformFromDOM(ObjectNode node)
    {
        final IElement result = createElementForNode(node);
        transformAttributesFromDOM(node, result);
        transformDataFromDOM(node, result);
        transformChildrenFromDOM(node, result);
        return result;
    }

    private void transformAttributesFromDOM(JsonNode node, IElement result)
    {
        final Map<String, String> attributes = new HashMap<String, String>();
        for (Map.Entry<String, JsonNode> entry : new EntryIt(node.path(ATTR_KEY).fields()))
        {
            attributes.put(entry.getKey(), entry.getValue().textValue());
        }
        result.setAttributes(attributes);
    }

    private void transformDataFromDOM(ObjectNode node, final IElement result)
    {
        final JsonNode dataNode = node.path(DATA_KEY);
        if (dataNode.isValueNode())
        {
            result.setData(dataNode.asText());
        }
    }

    private void transformChildrenFromDOM(ObjectNode node, final IElement result)
    {
        final JsonNode childrenNode = node.path(CHILDREN_KEY);
        if (childrenNode.isArray())
        {
            for (JsonNode child : childrenNode)
            {
                result.addChildren(transformFromDOM(child));
            }
        }
    }

    private IElement createElementForNode(ObjectNode node)
    {
        final String nodeName = node.get(ID_KEY).textValue();

        EntityLinkElementKind linkKind = EntityLinkElementKind.tryGetForElementName(nodeName);
        if (linkKind != null)
        {
            final String permId =
                    getAttrValueOrFail(nodeName, node, EntityLinkElement.PERMID_ATTR_NAME);
            return new EntityLinkElement(linkKind, permId);
        }

        // plain element, no special treatment needed
        return factory.createElement(nodeName);
    }

    private String getAttrValueOrFail(String nodeName, ObjectNode node, String attrName)
    {
        final String attr = node.path(ATTR_KEY).path(attrName).asText();
        if (StringUtils.isBlank(attr))
        {
            String error =
                    String.format("Attribute [%s] expected in nodes with name %s", attrName,
                            nodeName);
            throw new IllegalArgumentException(error);
        }
        return attr;
    }

    @Override
    public String convertToString(List<IElement> elements)
    {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode = transformToDOM(mapper, elements);

        try
        {
            return mapper.writer(topLevelPrettyPrinter).writeValueAsString(rootNode);
        } catch (Exception ex)
        {
            throw new UserFailureException("Cannot generate DOM.", ex);
        }
    }

    private JsonNode transformToDOM(ObjectMapper mapper, List<IElement> elements)
    {
        final ArrayNode result = mapper.createArrayNode();

        for (IElement child : elements)
        {
            final JsonNode childNode = transformToDOM(mapper, child);
            result.add(childNode);
        }

        return result;
    }

    private JsonNode transformToDOM(ObjectMapper mapper, IElement element)
    {
        final ObjectNode result = mapper.createObjectNode();
        result.put(ID_KEY, element.getName());
        transformAttributesToDOM(mapper, element, result);
        if (element.getData() != null)
        {
            result.put(DATA_KEY, element.getData());
        }
        if (element.getChildren().isEmpty() == false)
        {
            result.put(CHILDREN_KEY, transformToDOM(mapper, element.getChildren()));
        }
        return result;
    }

    private void transformAttributesToDOM(ObjectMapper mapper, IElement element, ObjectNode result)
    {
        if (element.getAttributes().isEmpty())
        {
            return;
        }
        final ObjectNode attributes = mapper.createObjectNode();
        for (Map.Entry<String, String> entry : element.getAttributes().entrySet())
        {
            attributes.put(entry.getKey(), entry.getValue());
        }
        result.put(ATTR_KEY, attributes);
    }
}
