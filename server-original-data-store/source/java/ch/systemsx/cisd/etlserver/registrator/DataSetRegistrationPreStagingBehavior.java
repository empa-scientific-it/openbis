/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.registrator;

/**
 * Configures the behaviour of the original input dataset in a dropbox registration process.
 * 
 * @author jakubs
 */
public enum DataSetRegistrationPreStagingBehavior
{
    /**
     * The default behavior without pre-staging. The registration uses original input file through the hole process.
     */
    USE_ORIGINAL,
    /**
     * Use the pre-staging dir and delete original file on success.
     */
    USE_PRESTAGING;

    /**
     * Parses the string in a format acceptable as a property parameter.
     */
    public static DataSetRegistrationPreStagingBehavior fromString(String text)
    {
        if (text.equalsIgnoreCase("use_original"))
        {
            return USE_ORIGINAL;
        }
        if (text.equalsIgnoreCase("default") || text.equalsIgnoreCase("use_prestaging"))
        {
            return USE_PRESTAGING;
        }
        return null;
    }
}
