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
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.ImagingServiceContext;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.helper.IDssServiceScriptRunner;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.helper.ScriptRunnerFactory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

public class ImagingDataSetJythonAdaptor implements IImagingDataSetAdaptor
{
    private final String scriptPath;


    public ImagingDataSetJythonAdaptor(Properties properties) {
        this.scriptPath = properties.getProperty("script-path", "");
        if(scriptPath.trim().isEmpty()) {
            throw new UserFailureException("There is no script defined for this adaptor!");
        }
    }
    @Override
    public Map<String, Serializable> process(ImagingServiceContext context, File rootFile, String format,
            Map<String, Serializable> imageConfig,
            Map<String, Serializable> imageMetadata,
            Map<String, Serializable> previewConfig,
            Map<String, Serializable> previewMetadata)
    {
        CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions();
        options.withParameter("sessionToken", context.getSessionToken());
        options.withParameter("asApi", context.getAsApi());
        options.withParameter("dssApi", context.getDssApi());
        options.withParameter("file", rootFile);
        options.withParameter("format", format);
        options.withParameter("imageConfig", imageConfig);
        options.withParameter("imageMetadata", imageMetadata);
        options.withParameter("previewConfig", previewConfig);
        options.withParameter("previewMetadata", previewMetadata);
            IDssServiceScriptRunner
                    runner = new ScriptRunnerFactory(scriptPath, context.getAsApi(),
                    context.getDssApi())
                    .createServiceRunner(context.getSessionToken());
        return Map.of("BYTES", runner.process(options));
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

}
