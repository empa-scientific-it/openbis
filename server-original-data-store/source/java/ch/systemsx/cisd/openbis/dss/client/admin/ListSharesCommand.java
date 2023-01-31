/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.admin;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.server.DirectoryRendererUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.ShareInfo;

/**
 * @author Franz-Josef Elmer
 */
public class ListSharesCommand extends AbstractCommand
{
    static final class ListSharesCommandArguments extends CommonArguments
    {

    }

    private ListSharesCommandArguments arguments = new ListSharesCommandArguments();

    ListSharesCommand()
    {
        super("list-shares");
    }

    @Override
    protected ListSharesCommandArguments getArguments()
    {
        return arguments;
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "";
    }

    @Override
    void execute()
    {
        List<ShareInfo> shares = service.listAllShares(sessionToken);
        if (shares.isEmpty())
        {
            System.out.println("No shares!");
        }
        System.out.println(shares.size() + " shares:");
        for (ShareInfo shareInfo : shares)
        {
            StringBuilder builder = new StringBuilder();
            builder.append(shareInfo.getShareId()).append(": ");
            builder.append(DirectoryRendererUtil.renderFileSize(shareInfo.getFreeSpace()));
            builder.append(" bytes free.");
            if (shareInfo.isIncoming())
            {
                builder.append(" Incoming share.");
            }
            if (shareInfo.isWithdrawShare())
            {
                builder.append(" Data sets should be with drawn.");
            }
            if (shareInfo.isIgnoredForShuffling())
            {
                builder.append(" To be ignored in shuffling.");
            }
            System.out.println(builder.toString());
        }
    }
}
