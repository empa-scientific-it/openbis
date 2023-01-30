/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Map.Entry;
import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;

/**
 * The default implementation of {@link IElement}.
 * 
 * @author Kaloyan Enimanev
 */
public class Element implements IElement
{

    private final String name;

    private final Map<String, String> attributes = new HashMap<String, String>();

    private final List<IElement> children = new ArrayList<IElement>();

    private String data;

    public Element(String name)
    {
        assert Pattern.matches("[a-zA-Z][\\w:]*", name) : "Element names must be non-emtpy strings "
                + "containing characters from the English alphabet or digits.";
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getAttribute(String key)
    {
        String value = attributes.get(key);
        if (value == null)
        {
            String error = String.format("Attribute '%s' does not exist.", key);
            throw new IllegalArgumentException(error);
        }
        return value;
    }

    @Override
    public String getAttribute(String key, String defaultValue)
    {
        String value = attributes.get(key);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public String getData()
    {
        return data;
    }

    @Override
    public List<IElement> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    @Override
    public Map<String, String> getAttributes()
    {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public IElement setAttributes(Map<String, String> newAttributes)
    {
        assert newAttributes != null : "Setting null attributes is not allowed.";
        for (Entry<String, String> entry : newAttributes.entrySet())
        {
            validateAttribute(entry.getKey(), entry.getValue());
        }
        this.attributes.clear();
        this.attributes.putAll(newAttributes);
        return this;
    }

    @Override
    public IElement setChildren(List<IElement> newChildren)
    {
        assert newChildren != null : "Setting null children is not allowed.";
        this.children.clear();
        this.children.addAll(newChildren);
        return this;
    }

    @Override
    public IElement setData(String data)
    {
        this.data = data;
        return this;
    }

    @Override
    public IElement addChildren(IElement... newChildren)
    {
        for (IElement child : newChildren)
        {
            children.add(child);
        }
        return this;
    }

    @Override
    public IElement addAttribute(String key, String value)
    {
        validateAttribute(key, value);
        attributes.put(key, value);
        return this;
    }

    private void validateAttribute(String key, String value)
    {
        if (Pattern.matches("[a-zA-Z][\\w:]*", key) == false)
        {
            String error =
                    String.format(
                            "Invalid attribute name '%s'. Attribute names must "
                                    + "be non-emtpy strings containing characters from the English alphabet.",
                            key);
            throw new IllegalArgumentException(error);
        }
        if (value == null)
        {
            String error = String.format("Attribute with key '%s' has NULL value.", key);
            throw new IllegalArgumentException(error);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Element other = (Element) obj;
        if (attributes == null)
        {
            if (other.attributes != null)
            {
                return false;
            }
        } else if (!attributes.equals(other.attributes))
        {
            return false;
        }
        if (children == null)
        {
            if (other.children != null)
            {
                return false;
            }
        } else if (!children.equals(other.children))
        {
            return false;
        }
        if (data == null)
        {
            if (other.data != null)
            {
                return false;
            }
        } else if (!data.equals(other.data))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        } else if (!name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Element [name=" + name + ", attributes=" + attributes + ", children=" + children
                + ", data=" + data + "]";
    }
}
