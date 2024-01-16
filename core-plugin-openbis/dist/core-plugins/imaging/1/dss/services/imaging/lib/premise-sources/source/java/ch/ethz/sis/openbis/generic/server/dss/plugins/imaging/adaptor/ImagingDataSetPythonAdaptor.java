/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor;

import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPreview;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.ImagingServiceContext;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.Util;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class ImagingDataSetPythonAdaptor implements IImagingDataSetAdaptor
{

    protected String pythonPath;

    protected String scriptPath;

    @Override
    public Map<String, Serializable> process(ImagingServiceContext context, File rootFile, String format,
            Map<String, Serializable> imageConfig,
            Map<String, Serializable> imageMetadata,
            Map<String, Serializable> previewConfig,
            Map<String, Serializable> previewMetadata)
    {
        ProcessBuilder processBuilder = new ProcessBuilder(pythonPath,
                scriptPath, rootFile.getAbsolutePath(), format, convertMapToJson(imageConfig),
                convertMapToJson(imageMetadata), convertMapToJson(previewConfig), convertMapToJson(previewMetadata));
        processBuilder.redirectErrorStream(false);

        String fullOutput;
        try
        {
            Process process = processBuilder.start();
            fullOutput =
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                String error =
                        new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new UserFailureException("Script evaluation failed: " + error);
            }
        } catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        if (fullOutput.trim().isEmpty())
        {
            throw new UserFailureException("Script produced no results!");
        }
        String[] result = fullOutput.split("\n");
        return convertPythonOutput(result[result.length-1]);
    }

    @Override
    public void computePreview(ImagingServiceContext context, File rootFile,
            ImagingDataSetImage image, ImagingDataSetPreview preview)
    {
        Map<String, Serializable> map = process(context, rootFile, preview.getFormat(),
                image.getConfig(), image.getMetadata(),
                preview.getConfig(), preview.getMetadata());

        for (Map.Entry<String, Serializable> entry : map.entrySet())
        {
            if (entry.getKey().equalsIgnoreCase("width"))
            {
                Integer value = Integer.valueOf(entry.getValue().toString());
                preview.setWidth(value);
            } else if (entry.getKey().equalsIgnoreCase("height"))
            {
                Integer value = Integer.valueOf(entry.getValue().toString());
                preview.setHeight(value);
            } else if (entry.getKey().equalsIgnoreCase("bytes"))
            {
                preview.setBytes(entry.getValue().toString());
            } else
            {
                preview.getMetadata().put(entry.getKey(), entry.getValue());
            }
        }
    }

    private String convertMapToJson(Map<String, ?> map)
    {
        Map<String, ?> mapToConvert = map;
        if(map == null) {
            mapToConvert = new HashMap<>();
        }
        try
        {
            ObjectMapper objectMapper = new GenericObjectMapper();
            return objectMapper.writeValueAsString(mapToConvert);
        } catch (Exception e)
        {
            throw new UserFailureException("Couldn't convert map to string", e);
        }
    }

    private Map<String, Serializable> convertPythonOutput(String line) {
        try
        {
            return Util.readConfig(line, Map.class);
        } catch (Exception e)
        {
            return Map.of("bytes", line);
        }
    }

}
