/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import loci.formats.CoreMetadata;
import loci.formats.IFormatReader;

/**
 * Get some meta-data from an image.
 *
 * @author Franz-Josef Elmer
 */
public class BioFormatsInfo
{

    private static final int CUTTOFF = 50;

    public static void main(String[] args) throws Exception
    {
        for (String fileName : args)
        {
            IFormatReader reader = BioFormatsImageUtils.tryToCreateReaderForFile(fileName);
            if (reader == null)
            {
                System.out.println("No image reader found for " + fileName);
                continue;
            }
            reader.setId(fileName);
            System.out.println("====== " + fileName);
            System.out.println(" format: " + reader.getFormat());
            System.out.println(" image count: " + reader.getImageCount());
            System.out.println(" bits per pixel: " + reader.getBitsPerPixel());
            System.out.println(" dimension order: " + reader.getDimensionOrder());
            System.out.println(" size T: " + reader.getSizeT());
            System.out.println(" size X: " + reader.getSizeX());
            System.out.println(" size Y: " + reader.getSizeY());
            System.out.println(" size Z: " + reader.getSizeZ());
            System.out.println(" size C: " + reader.getSizeC());
            System.out.println(" effective size C: " + reader.getEffectiveSizeC());
            System.out.println(" pixel type: " + reader.getPixelType());
            System.out.println(" RGB channel count: " + reader.getRGBChannelCount());
            List<CoreMetadata> coreMetadata = reader.getCoreMetadataList();
            System.out.println(" # of core meta-data: " + coreMetadata.size());
            printMap("  global meta-data", reader.getGlobalMetadata());
            int seriesCount = reader.getSeriesCount();
            System.out.println(" series count: " + seriesCount);
            for (int i = 0; i < Math.min(2, seriesCount); i++)
            {
                reader.setSeries(i);
                printMap(" series meta-data " + i, reader.getSeriesMetadata());
            }
        }

    }

    private static void printMap(String title, Map<String, Object> map)
    {
        System.out.println(title + ":");
        List<Entry<String, Object>> entries = new ArrayList<Entry<String, Object>>(map.entrySet());
        Collections.sort(entries, new Comparator<Entry<String, Object>>()
            {
                @Override
                public int compare(Entry<String, Object> o1, Entry<String, Object> o2)
                {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
        for (Entry<String, Object> entry : entries)
        {
            String value = entry.getValue().toString();
            if (value.length() > CUTTOFF)
            {
                value = value.substring(0, CUTTOFF) + "...";
            }
            System.out.println("     " + entry.getKey() + " = " + value);
        }
    }

}
