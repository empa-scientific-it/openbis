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

package ch.systemsx.cisd.openbis.generic.shared.util;

/**
 * @author Pawel Glyzewski
 */
public class TestInstanceHostUtils
{
    private static final String OPENBIS_URL = "http://localhost";

    public static int getOpenBISPort()
    {
        return 8800 + getProjectNumber() + 8;
    }

    public static String getOpenBISUrl()
    {
        return OPENBIS_URL + ":" + getOpenBISPort();
    }

    public static int getDSSPort()
    {
        return 8800 + getProjectNumber() + 9;
    }

    public static String getDSSUrl()
    {
        return OPENBIS_URL + ":" + getDSSPort();
    }

    private static int getProjectNumber()
    {
        String projectName = System.getProperty("ant.project.name", "");

        if (projectName.equals("openbis"))
        {
            return 0;
        } else if (projectName.equals("datastore_server"))
        {
            return 10;
        } else if (projectName.equals("screening"))
        {
            return 20;
        } else if (projectName.equals("deep_sequencing_unit"))
        {
            return 30;
        } else if (projectName.equals("openbis_oai_pmh"))
        {
            return 40;
        }

        return 80;
    }
}
