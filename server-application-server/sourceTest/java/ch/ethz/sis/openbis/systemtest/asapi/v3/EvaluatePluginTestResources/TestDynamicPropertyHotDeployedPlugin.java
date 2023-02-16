/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.asapi.v3.EvaluatePluginTestResources;

import java.util.EnumSet;

import ch.ethz.cisd.hotdeploy.PluginInfo;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculatorHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

@PluginInfo(name = TestDynamicPropertyHotDeployedPlugin.PLUGIN_NAME, pluginType = TestDynamicPropertyHotDeployedPlugin.class)
public class TestDynamicPropertyHotDeployedPlugin implements IDynamicPropertyCalculatorHotDeployPlugin
{

    public static final String PLUGIN_NAME = "test dynamic property hot deployed name";

    @Override
    public String eval(IEntityAdaptor entity)
    {
        return entity.properties().isEmpty() ? null : entity.properties().iterator().next().valueAsString();
    }

    @Override
    public String getDescription()
    {
        return "test dynamic property hot deployed description";
    }

    @Override
    public EnumSet<EntityKind> getSupportedEntityKinds()
    {
        return EnumSet.of(EntityKind.SAMPLE);
    }

}
