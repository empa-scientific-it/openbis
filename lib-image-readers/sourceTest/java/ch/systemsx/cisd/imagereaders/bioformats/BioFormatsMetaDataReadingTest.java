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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.imagereaders.ImageReaderTestCase;
import ch.systemsx.cisd.imagereaders.ImageReadersTestHelper;

/**
 * Test for metadata reading.
 * 
 * @author Kaloyan Enimanev
 */
public class BioFormatsMetaDataReadingTest extends ImageReaderTestCase
{

    @Test
    public void testMetaDataExtraction() throws Exception
    {
        File imageFile = getImageFileForLibrary(ImageReaderConstants.BIOFORMATS_LIBRARY, "demo.tif");
        IImageReader reader = getReaderFromFactory(imageFile);

        assertTrue("BioFormat readers shoud be meta-data aware", reader.isMetaDataAware());

        Map<String, Object> metaData = reader.readMetaData(imageFile, ImageID.NULL, null);
        String metaDataAsString = getMetaDataAsString(metaData);

        assertEquals(getExpectedMetaData(), metaDataAsString);
    }

    private String getExpectedMetaData()
    {
        return "ApplicationName: MetaMorph\n"
                + "ApplicationVersion: 3.1.0.79\n"
                + "Binning: 1 x 1\n"
                + "BitsPerSample: 16\n"
                + "Camera Bit Depth: 12\n"
                + "Camera Shutter: Always Open\n"
                + "Clear Count: 2\n"
                + "Clear Mode: CLEAR PRE SEQUENCE\n"
                + "Compression: Uncompressed\n"
                + "DateTime: 20101121 16:52:52.986\n"
                + "Digitizer: 20 MHz\n"
                + "Exposure: 10 ms\n"
                + "Frames to Average: 1\n"
                + "Gain: Gain 1 (1x)\n"
                + "ImageLength: 1040\n"
                + "ImageWidth: 1392\n"
                + "ImageXpress Micro Filter Cube: Texas Red\n"
                + "ImageXpress Micro Objective: 10X S Fluor\n"
                + "ImageXpress Micro Shutter: Closed\n"
                + "Laser focus score: 36.4006\n"
                + "MetaDataPhotometricInterpretation: Monochrome\n"
                + "MetaDataVersion: 1\n"
                + "NewSubfileType: 2\n"
                + "NumberOfChannels: 1\n"
                + "PhotometricInterpretation: BlackIsZero\n"
                + "Region: Region\n"
                + "SamplesPerPixel: 1\n"
                + "Shading: Off\n"
                + "Software: MetaSeries\n"
                + "Subtract: Off\n"
                + "Temperature: Temperature\n"
                + "Trigger Mode: Normal (TIMED)\n"
                + "User Description: Experiment base name:ME20101116METADATA03\n"
                + "Experiment set:ME20101116METADATA03\n"
                + "Plate Screen\n"
                + "X position for position #1: ome.units.quantity.Length: value[73296.5], unit[reference frame] stored as java.lang.Double\n"
                + "Y position for position #1: ome.units.quantity.Length: value[25554.2], unit[reference frame] stored as java.lang.Double\n"
                + "_IllumSetting_: Cy3\n"
                + "_MagNA_: 0.5\n"
                + "_MagRI_: 1\n"
                + "_MagSetting_: 10X S Fluor\n"
                + "acquisition-time-local: 20101121 16:52:52.986\n"
                + "autoscale-max-percent: 0\n"
                + "autoscale-min-percent: 0\n"
                + "autoscale-state: off\n"
                + "bits-per-pixel: 16\n"
                + "camera-binning-x: 1\n"
                + "camera-binning-y: 1\n"
                + "camera-chip-offset-x: 0\n"
                + "camera-chip-offset-y: 0\n"
                + "exposure time (ms) #1: 10.0\n"
                + "gamma: 1\n"
                + "gray-calibration-curve-fit-algorithm: 4\n"
                + "gray-calibration-max: -1\n"
                + "gray-calibration-min: -1\n"
                + "gray-calibration-units: \n"
                + "gray-calibration-values: \n"
                + "image-name: Cy3\n"
                + "look-up-table-name: Set By Wavelength\n"
                + "look-up-table-type: by-wavelength\n"
                + "modification-time-local: 20101121 16:52:53.50\n"
                + "number-of-planes: 1\n"
                + "photonegative-mode: off\n"
                + "pixel-size-x: 1392\n"
                + "pixel-size-y: 1040\n"
                + "plane-guid: {70EAFB69-A568-464E-9EE2-4479767F96EF}\n"
                + "plane-type: plane\n"
                + "scale-max: 1058\n"
                + "scale-min: 96\n"
                + "spatial-calibration-state: on\n"
                + "spatial-calibration-units: um\n"
                + "spatial-calibration-x: 0.645\n"
                + "spatial-calibration-y: 0.645\n"
                + "stage-label: F09 : Site 1\n"
                + "stage-position-x: 73296.5\n"
                + "stage-position-y: 25554.2\n"
                + "threshold-color: 4080ff\n"
                + "threshold-high: 65535\n"
                + "threshold-low: 0\n"
                + "threshold-state: ThresholdOff\n"
                + "timestamp #1: 1290358372986\n"
                + "wavelength: 624\n" + "z-position: 11145.7\n" + "zoom-percent: 50\n";
    }

    private String getMetaDataAsString(Map<String, Object> metaData)
    {
        List<String> sortedKeys = new ArrayList<String>(metaData.keySet());
        Collections.sort(sortedKeys);

        StringBuilder output = new StringBuilder();
        for (String key : sortedKeys)
        {
            Object value = metaData.get(key);
            output.append(key + ": " + value);
            output.append("\n");
        }
        return output.toString();
    }

    private IImageReader getReaderFromFactory(File imageFile) throws Exception
    {
        ImageReadersTestHelper.setUpLibraries(ImageReaderConstants.BIOFORMATS_LIBRARY);

        IImageReader reader =
                ImageReaderFactory.tryGetReaderForFile(ImageReaderConstants.BIOFORMATS_LIBRARY,
                        imageFile.getAbsolutePath());
        return reader;
    }

}
