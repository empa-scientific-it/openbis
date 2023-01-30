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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.ENTITY_KIND;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.IS_AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.PLUGIN_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.SCRIPT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.SCRIPT_TYPE;

import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.WebClientConfigUtils;

/**
 * Provider of {@link Script} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class ScriptProvider extends AbstractCommonTableModelProvider<Script>
{
    private final ScriptType scriptTypeOrNull;

    private final EntityKind entityKindOrNull;

    private final WebClientConfigurationProvider webClientConfigurationProvider;

    public ScriptProvider(ICommonServer commonServer, String sessionToken,
            ScriptType scriptTypeOrNull, EntityKind entityKindOrNull, 
            WebClientConfigurationProvider webClientConfigurationProvider)
    {
        super(commonServer, sessionToken);
        this.scriptTypeOrNull = scriptTypeOrNull;
        this.entityKindOrNull = entityKindOrNull;
        this.webClientConfigurationProvider = webClientConfigurationProvider;
    }

    @Override
    protected TypedTableModel<Script> createTableModel()
    {
        List<Script> scripts =
                commonServer.listScripts(sessionToken, scriptTypeOrNull, entityKindOrNull);
        TypedTableModelBuilder<Script> builder = new TypedTableModelBuilder<Script>();
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(SCRIPT);
        builder.addColumn(ENTITY_KIND);
        builder.addColumn(SCRIPT_TYPE);
        builder.addColumn(PLUGIN_TYPE);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(IS_AVAILABLE);
        for (Script script : scripts)
        {
            builder.addRow(script);
            builder.column(NAME).addString(script.getName());
            builder.column(DESCRIPTION).addString(script.getDescription());
            builder.column(SCRIPT).addString(script.getScript());
            builder.column(ENTITY_KIND).addString(buildDescription(script.getEntityKind()));
            builder.column(SCRIPT_TYPE).addString(script.getScriptType().getDescription());
            builder.column(PLUGIN_TYPE).addString(script.getPluginType().getDescription());
            builder.column(REGISTRATOR).addPerson(script.getRegistrator());
            builder.column(MODIFICATION_DATE).addDate(script.getModificationDate());
            builder.column(IS_AVAILABLE)
                    .addString(SimpleYesNoRenderer.render(script.isAvailable()));
        }
        return builder.getModel();
    }

    private String buildDescription(EntityKind[] entityKinds)
    {
        if (entityKinds == null)
        {
            return "All";
        } else if (entityKinds.length == 1)
        {
            return WebClientConfigUtils.getTranslatedDescription(webClientConfigurationProvider, entityKinds[0]);
        }

        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (EntityKind entityKind : entityKinds)
        {
            builder.append(WebClientConfigUtils.getTranslatedDescription(webClientConfigurationProvider, entityKind));
        }

        return builder.toString();
    }
}
