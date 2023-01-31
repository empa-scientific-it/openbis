package ch.systemsx.cisd.etlserver;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

public class ETLServerExposablePropertyPlaceholderConfigurer extends ExposablePropertyPlaceholderConfigurer
{
    @Override
    public Map<String, String> getDefaultValuesForMissingProperties()
    {
        Map<String, String> defaultValues = new HashMap<String, String>();
        defaultValues.put("download-url", "");
        return defaultValues;
    }
}
