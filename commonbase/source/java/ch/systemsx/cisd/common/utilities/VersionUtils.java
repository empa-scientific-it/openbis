/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.utilities;

/**
 * @author Franz-Josef Elmer
 */
public class VersionUtils
{
    /**
     * Compares the actual version with the required version and return <code>true</code> if they are compatible.
     * Assume the version strings are numbers connected by dots.
     * 
     * @param backward if <code>true</code> backward compatibility will be allowed.
     */
    public static boolean isCompatible(String requiredVersion, String actualVersion, boolean backward)
    {
        if (requiredVersion.equals(actualVersion))
        {
            return true;
        }
        return new Version(requiredVersion).isCompatible(new Version(actualVersion), backward);
    }

    private static final class Version
    {
        private final Integer[] version = new Integer[4];

        Version(String versionAsString)
        {
            String[] versions = versionAsString.split("\\.");
            for (int i = 0; i < versions.length; i++)
            {
                version[i] = Integer.valueOf(versions[i]);
            }
        }

        boolean isCompatible(Version actualVersion, boolean backward)
        {
            for (int i = 0; i < version.length; i++)
            {
                Integer requiredVersionLevel = version[i];
                if (requiredVersionLevel == null)
                {
                    return true;
                }
                Integer actualVersionLevel = actualVersion.version[i];
                if (actualVersionLevel == null)
                {
                    return false;
                }
                if (requiredVersionLevel > actualVersionLevel)
                {
                    return false;
                }
                if (requiredVersionLevel < actualVersionLevel)
                {
                    return backward;
                }
            }
            return true;
        }
    }
}
