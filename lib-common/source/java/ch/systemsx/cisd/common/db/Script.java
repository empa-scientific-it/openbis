/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Bean class for a script. Holds script name and code.
 * 
 * @author Franz-Josef Elmer
 */
public class Script
{
    private final String name;

    private final String content;

    private final String version;

    /**
     * Creates an instance for the specified script name, content and version.
     */
    public Script(final String name, final String content)
    {
        this(name, content, "-");
    }

    /**
     * Creates an instance for the specified script name, code and version.
     */
    public Script(final String name, final String content, final String version)
    {
        assert name != null;
        assert content != null;
        assert version != null;
        this.name = name;
        this.content = content;
        this.version = version;
    }

    /**
     * Returns script code.
     */
    public String getContent()
    {
        return content;
    }

    /**
     * Returns script name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the version of the script.
     */
    public final String getVersion()
    {
        return version;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return "Script [name=" + name + ", version=" + version + "]";
    }

}
