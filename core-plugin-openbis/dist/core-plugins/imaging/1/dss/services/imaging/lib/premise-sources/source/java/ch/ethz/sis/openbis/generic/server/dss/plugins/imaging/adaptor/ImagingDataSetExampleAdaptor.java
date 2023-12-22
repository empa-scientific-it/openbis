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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ImagingDataSetExampleAdaptor implements IImagingDataSetAdaptor
{

    private static final int WIDTH = 640;
    private static final int HEIGHT = 640;

    public ImagingDataSetExampleAdaptor(Properties properties) {
    }

    @Override
    public Map<String, Serializable> process(ImagingServiceContext context, File rootFile, String format,
            Map<String, Serializable> imageConfig,
            Map<String, Serializable> imageMetadata,
            Map<String, Serializable> previewConfig,
            Map<String, Serializable> previewMetadata)
    {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<HEIGHT; y++)
        {
            for (int x = 0; x < WIDTH; x++)
            {
                int a = (int)(Math.random()*256);
                int r = (int)(Math.random()*256);
                int g = (int)(Math.random()*256);
                int b = (int)(Math.random()*256);

                //pixel
                int p = (a<<24) | (r<<16) | (g<<8) | b;

                img.setRGB(x, y, p);
            }
        }
        try
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(img, format, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            String bytes = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            HashMap<String, Serializable> map = new HashMap<>();
            map.put("width", WIDTH);
            map.put("height", HEIGHT);
            map.put("bytes", bytes);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
