/*
 * Copyright 2010 ETH Zuerich, CISD
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

/**
 * @author Izabela Adamczyk
 */
public class DynamicPropertyEvaluationInfo extends BasicEntityDescription
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String sciptName;

    private String script;

    private PluginType pluginType;

    public DynamicPropertyEvaluationInfo()
    {
    }

    public DynamicPropertyEvaluationInfo(EntityKind entityKind, String entityIdentifier,
            PluginType pluginType, String scriptName, String script)
    {
        super(entityKind, entityIdentifier);
        this.pluginType = pluginType;
        this.sciptName = scriptName;
        this.script = script;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public PluginType getPluginType()
    {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType)
    {
        this.pluginType = pluginType;
    }

    public String getSciptName()
    {
        return sciptName;
    }

    public void setSciptName(String sciptName)
    {
        this.sciptName = sciptName;
    }
}
