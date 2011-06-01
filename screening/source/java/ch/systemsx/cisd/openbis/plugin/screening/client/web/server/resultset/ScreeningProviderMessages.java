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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset;

/**
 * UI Messages for grid data providers. Should be moved to the client in the end.
 * 
 * @author Tomasz Pylak
 */
class ScreeningProviderMessages
{
    public static final String SHOW_DETAILS_MSG = "Show";

    public static final String RANK_COLUMN_MSG = " Rank";

    public static String getRankColumnHeader(int numberOfMaterialsInExperiment)
    {
        return "Rank (" + numberOfMaterialsInExperiment + ")";
    }

    public static String getReplicaColumnTitle(String group, int replicaNumber)
    {

        return group + " repl. " + replicaNumber;
    }

}
