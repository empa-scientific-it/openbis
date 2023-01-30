package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import java.util.Map;

public class VersionUtils
{
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

    private static String getKey(String prefix, String creationCode)
    {
        String key = String.format("%s-%s", prefix, creationCode);
        return key.toUpperCase();
    }

    public static boolean isNewVersion(String newVersion, Integer storedVersion)
    {
        return "FORCE".equals(newVersion) || ((int) Double.parseDouble(newVersion)) > storedVersion;
    }
}
