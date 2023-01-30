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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs.CREATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MetaprojectGridColumnIDs.NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMetaprojectsCriteria;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author pkupczyk
 */
public class MetaprojectProvider extends AbstractCommonTableModelProvider<Metaproject>
{

    private ListMetaprojectsCriteria criteria;

    public MetaprojectProvider(ICommonServer commonServer, String sessionToken,
            ListMetaprojectsCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    @Override
    protected TypedTableModel<Metaproject> createTableModel()
    {
        List<Metaproject> metaprojects = commonServer.listMetaprojects(sessionToken);
        TypedTableModelBuilder<Metaproject> builder = new TypedTableModelBuilder<Metaproject>();
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(CREATION_DATE).withDefaultWidth(300);

        Set<String> whitelistLowerCase = new HashSet<String>();

        if (criteria.getWhitelist() != null)
        {
            for (String whitelistItem : criteria.getWhitelist())
            {
                whitelistLowerCase.add(whitelistItem.toLowerCase());
            }

            for (Metaproject metaproject : metaprojects)
            {
                if (whitelistLowerCase.contains(metaproject.getName().toLowerCase()))
                {
                    builder.addRow(metaproject);
                    builder.column(NAME).addString(metaproject.getName());
                    builder.column(DESCRIPTION).addString(metaproject.getDescription());
                    builder.column(CREATION_DATE).addDate(metaproject.getCreationDate());
                }
            }
            return builder.getModel();
        }

        Set<String> blacklistLowerCase = new HashSet<String>();

        if (criteria.getBlacklist() != null)
        {
            for (String blacklistItem : criteria.getBlacklist())
            {
                blacklistLowerCase.add(blacklistItem.toLowerCase());
            }
        }

        for (Metaproject metaproject : metaprojects)
        {
            if (blacklistLowerCase.contains(metaproject.getName().toLowerCase()) == false)
            {
                builder.addRow(metaproject);
                builder.column(NAME).addString(metaproject.getName());
                builder.column(DESCRIPTION).addString(metaproject.getDescription());
                builder.column(CREATION_DATE).addDate(metaproject.getCreationDate());
            }
        }

        return builder.getModel();
    }

}
