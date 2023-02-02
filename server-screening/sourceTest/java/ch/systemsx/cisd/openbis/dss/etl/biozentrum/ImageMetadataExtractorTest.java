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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.Location;

/**
 * @author Kaloyan Enimanev
 */
public class ImageMetadataExtractorTest extends AssertJUnit
{
    private final String TEST_FILE =
            "./sourceTest/java/ch/systemsx/cisd/openbis/dss/etl/biozentrum/metadata.csv";

    /**
     * the data from TEST_FILE works for epsilon values from 0.7 up to approx. 890
     */
    private final double EPSILON = 1.0;

    private class ImageFile
    {
        Integer tileNumber;

        String positionX;

        String positionY;

        /** A01, A02 .. etc */
        String positionID;

        @Override
        public String toString()
        {
            ToStringBuilder builder = new ToStringBuilder(this);
            builder.append("tileNumber", tileNumber);
            builder.append("positionX", positionX);
            builder.append("positionY", positionY);
            builder.append("positionID", positionID);
            return builder.toString();
        }
    }

    @Test
    public void testTileMapping() throws Exception
    {

        List<ImageFile> imageFiles = readMetaDataFromFile(TEST_FILE);

        Map<Integer, Location> locations =
                ImageMetadataExtractor.tryGetTileMapping(createMetaData(imageFiles), EPSILON);

        Map<String, List<ImageFile>> groupByPosition = groupByPositions(imageFiles);
        for (String position : groupByPosition.keySet())
        {
            List<ImageFile> imagesForPosition = groupByPosition.get(position);
            ImageFile firstImage = imagesForPosition.get(0);
            for (ImageFile otherImage : imagesForPosition)
            {
                assertEquals(locations, firstImage, otherImage);
            }
        }

        assertLocationDimensions(49, 49, locations.values());
    }

    private void assertEquals(Map<Integer, Location> locations, ImageFile expected, ImageFile actual)
    {
        Location expectedLocation = locations.get(expected.tileNumber);
        Location actualLocation = locations.get(actual.tileNumber);

        String error =
                String.format("%s has been assigned a different location than %s", expected, actual);
        assertEquals(error, expectedLocation, actualLocation);
    }

    private void assertLocationDimensions(int maxRow, int maxColumn, Collection<Location> locations)
    {
        for (Location location : locations)
        {
            assertTrue("Location should not have row > " + maxRow, maxRow > location.getRow());
            assertTrue("Location should not have column > " + maxColumn,
                    maxColumn > location.getColumn());

        }

    }

    private List<ImageFile> readMetaDataFromFile(String fileName) throws Exception
    {
        List<ImageFile> result = new ArrayList<ImageFile>();
        CsvReader reader = new CsvReader(fileName);

        reader.readHeaders();

        int tileNumber = 1;
        while (reader.readRecord())
        {
            ImageFile img = new ImageFile();
            img.tileNumber = tileNumber++;
            img.positionX = reader.get("PositionX");
            img.positionY = reader.get("PositionY");
            img.positionID = reader.get("stage-label");

            result.add(img);
        }

        reader.close();

        return result;
    }

    private Map<Integer, Map<String, Object>> createMetaData(List<ImageFile> imageFiles)
    {
        Map<Integer, Map<String, Object>> result = new HashMap<Integer, Map<String, Object>>();
        for (ImageFile imgFile : imageFiles)
        {
            result.put(imgFile.tileNumber, createTileMetadata(imgFile));
        }
        return result;
    }

    private Map<String, List<ImageFile>> groupByPositions(List<ImageFile> imageFiles)
    {
        Map<String, List<ImageFile>> result = new HashMap<String, List<ImageFile>>();
        for (ImageFile imgFile : imageFiles)
        {
            List<ImageFile> list = result.get(imgFile.positionID);
            if (list == null)
            {
                list = new ArrayList<ImageFile>();
                result.put(imgFile.positionID, list);
            }
            list.add(imgFile);
        }
        return result;
    }

    private Map<String, Object> createTileMetadata(ImageFile imgFile)
    {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(ImageMetadataExtractor.POSITION_X_PROP, imgFile.positionX);
        metadata.put(ImageMetadataExtractor.POSITION_Y_PROP, imgFile.positionY);
        return metadata;
    }

}
