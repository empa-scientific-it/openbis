/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.dbmigration.migration;

/**
 * @author pkupczyk
 */
public class SqlMigrationVersion
{

    private int versionInt;

    public SqlMigrationVersion(String versionStr)
    {
        setVersionString(versionStr);
    }

    public SqlMigrationVersion(int versionInt)
    {
        setVersionInt(versionInt);
    }

    public String getVersionString()
    {
        return String.format("%03d", versionInt);
    }

    public int getVersionInt()
    {
        return versionInt;
    }

    private void setVersionString(String versionStr)
    {
        if (versionStr == null)
        {
            throw new IllegalArgumentException("Version was null");
        }
        try
        {
            setVersionInt(Integer.valueOf(versionStr));
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Version had incorrect format", e);
        }

    }

    private void setVersionInt(int versionInt)
    {
        if (versionInt < 1)
        {
            throw new IllegalArgumentException("Version was < 1");
        }
        this.versionInt = versionInt;
    }

}
