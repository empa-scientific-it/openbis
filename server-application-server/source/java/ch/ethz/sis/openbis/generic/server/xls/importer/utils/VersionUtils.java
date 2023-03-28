/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import java.util.Collections;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.handler.VersionInfoHandler;

public class VersionUtils
{

    public static Map<String, Integer> loadAllVersions() {
        final ImportOptions importOptions = new ImportOptions();
        importOptions.setIgnoreVersioning(false);
        return Collections.unmodifiableMap(VersionInfoHandler.loadAllVersions(importOptions));
    }

    public static int getStoredVersion(final Map<String, Integer> versions, final ImportTypes importType, final String vocabularyCodeOrNull,
            final String typeCode)
    {
        String importTypeAsString = importType.getType();
        if (importType == ImportTypes.VOCABULARY_TERM) {
            importTypeAsString += "-" + vocabularyCodeOrNull;
        }
        return versions.getOrDefault(VersionUtils.getKey(importTypeAsString, typeCode), 0);
    }

    public static Integer getStoredVersion(Map<String, Integer> versions, String prefix, String creationCode)
    {
        String key = getKey(prefix, creationCode);
        return versions.getOrDefault(key, 0);
    }

    public static void updateVersion(String version,
                                     Map<String, Integer> versions,
                                     String prefix,
                                     String creationCode)
    {
        if ("FORCE".equals(version))
        {
            return;
        }

        String key = getKey(prefix, creationCode);
        int newVersion = (int) Double.parseDouble(version);

        if (newVersion > versions.getOrDefault(key, 0))
        {
            versions.put(key, newVersion);
        }
    }

    public static String getKey(String prefix, String creationCode)
    {
        String key = String.format("%s-%s", prefix, creationCode);
        return key.toUpperCase();
    }

    public static boolean isNewVersion(String newVersion, Integer storedVersion)
    {
        return "FORCE".equals(newVersion) || ((int) Double.parseDouble(newVersion)) > storedVersion;
    }
}
