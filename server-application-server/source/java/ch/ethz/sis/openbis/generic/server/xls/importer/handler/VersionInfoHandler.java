package ch.ethz.sis.openbis.generic.server.xls.importer.handler;

import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class VersionInfoHandler
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ExcelParser.class);

    // Only used for development
    private static final String DEVELOPMENT_DEFAULT_PATH = "../openbis/targets/xls-import-version-info-dev.json";

    public static Map<String, Integer> loadVersions(ImportOptions options, String xlsName)
    {
        if (options.getIgnoreVersioning())
        {
            return new HashMap<>();
        }
        return loadVersionFile().getOrDefault("VERSION-" + xlsName, new HashMap<>());
    }

    public static void writeVersions(ImportOptions options, String xlsName, Map<String, Integer> versions)
    {
        if (options.getIgnoreVersioning())
        {
            return;
        }

        String path = getVersionFilePath();
        String newPath = path + ".new";

        Map<String, Map<String, Integer>> allVersions = loadVersionFile();
        allVersions.put("VERSION-" + xlsName, versions);
        JSONHandler.writeVersionDataFile(allVersions, newPath);

        try
        {
            Files.move(Paths.get(newPath), Paths.get(path), REPLACE_EXISTING);
        } catch (Exception e)
        {
            throw new UserFailureException(e.getMessage());
        }
    }

    private static Map<String, Map<String, Integer>> loadVersionFile()
    {
        try
        {
            String versionFilePath = getVersionFilePath();
            if (Files.exists(Paths.get(versionFilePath)))
            {
                String content = new String(Files.readAllBytes(Paths.get(versionFilePath)));
                return JSONHandler.parseVersionDataFile(content);
            } else
            {
                return new HashMap<>();
            }
        } catch (Exception e)
        {
            throw new UserFailureException(e.getMessage());
        }
    }

    private static String getVersionFilePath()
    {
        ApplicationContext context = CommonServiceProvider.getApplicationContext();
        if (context == null)
        {
            return DEVELOPMENT_DEFAULT_PATH;
        } else
        {
            ExposablePropertyPlaceholderConfigurer config =
                    (ExposablePropertyPlaceholderConfigurer) context.getBean(ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME);
            return config.getResolvedProps().getProperty("xls-import.version-data-file", "../../../xls-import-version-info.json");
        }
    }
}
