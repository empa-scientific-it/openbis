/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dynamix;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.etl.AbstractImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dynamix.WellLocationMappingUtils.DynamixWellPosition;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Image extractor for DynamiX project - work in progress.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends AbstractImageFileExtractor
{
    private static final String DYNAMIX_TOKEN_SEPARATOR = "_";

    private static final String POSITION_MAPPING_FILE_NAME = "pos2loc.tsv";

    private final Map<File/* mapping file */, Map<DynamixWellPosition, WellLocation>> wellLocationMapCache;

    // date when the first timepoint image has been acquired
    private final Map<File/* well images dir */, Date> firstMeasurementDateCache;

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties, false);
        this.wellLocationMapCache = new HashMap<File, Map<DynamixWellPosition, WellLocation>>();
        this.firstMeasurementDateCache = new HashMap<File, Date>();
    }

    @Override
    protected ImageFileInfo tryExtractImageInfo(File imageFile, File incomingDataSetDirectory,
            SampleIdentifier datasetSample)
    {
        String baseName = FilenameUtils.getBaseName(imageFile.getPath());
        // example name: "left_dia_pos100_t20100227_152439.tif"
        String[] tokens = StringUtils.split(baseName, DYNAMIX_TOKEN_SEPARATOR);
        if (tokens == null || tokens.length != 5)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Not enough underscore-separated tokens in %s, exactly 5 expected",
                        imageFile));
            }
            return null;
        }
        WellLocation wellLocation = getWellLocation(imageFile, tokens);
        String channelCode = tokens[1].toUpperCase();
        Float timepoint = (float) getSecondsFromFirstMeasurement(imageFile, tokens);
        String imageRelativePath = getRelativeImagePath(incomingDataSetDirectory, imageFile);

        ImageFileInfo info = new ImageFileInfo(channelCode, 1, 1, imageRelativePath);
        info.setTimepoint(timepoint);
        info.setWell(wellLocation.getRow(), wellLocation.getColumn());
        return info;
    }

    private long getSecondsFromFirstMeasurement(File imageFile, String[] tokens)
    {
        Date firstMeasurementDate = getFirstMeasurementDate(imageFile);
        return getSecondsFromFirstMeasurement(tokens, firstMeasurementDate);
    }

    private Date getFirstMeasurementDate(File imageFile)
    {
        File wellImagesDir = imageFile.getParentFile();
        Date date = firstMeasurementDateCache.get(wellImagesDir);
        if (date == null)
        {
            date = calculateFirstMeasurementDate(imageFile);
            firstMeasurementDateCache.put(wellImagesDir, date);
        }
        return date;
    }

    private static Date calculateFirstMeasurementDate(File imageFile)
    {
        File[] images = imageFile.getParentFile().listFiles();
        Arrays.sort(images);
        String firstMeasurementFilePath = images[0].getPath();

        String firstMeasurementFileBaseName = FilenameUtils.getBaseName(firstMeasurementFilePath);
        String[] firstMeasurementTokens =
                StringUtils.split(firstMeasurementFileBaseName, DYNAMIX_TOKEN_SEPARATOR);
        return parseDate(firstMeasurementTokens);
    }

    @Private
    static long getSecondsFromFirstMeasurement(String[] tokens, Date firstMeasurementDate)
    {
        Date thisMeasurementDate = parseDate(tokens);
        return (thisMeasurementDate.getTime() - firstMeasurementDate.getTime()) / 1000;
    }

    @Private
    static Date parseDate(String[] tokens)
    {
        // t20100227_152439 -> 20100227152439
        String dateToken = tokens[3].substring(1) + tokens[4];
        try
        {
            return new SimpleDateFormat("yyyymmddhhmmss").parse(dateToken);
        } catch (ParseException ex)
        {
            throw new EnvironmentFailureException("Cannot parse the data in the file name: " + ex);
        }
    }

    private WellLocation getWellLocation(File imageFile, final String[] tokens)
    {
        Map<DynamixWellPosition, WellLocation> map = getWellLocationMapping(imageFile);
        String posToken = tokens[2].substring("pos".length());
        DynamixWellPosition wellPos =
                WellLocationMappingUtils.parseWellPosition(tokens[0], posToken);
        return map.get(wellPos);
    }

    private Map<DynamixWellPosition, WellLocation> getWellLocationMapping(File imageFile)
    {
        File mappingFile = getMappingFile(imageFile);
        Map<DynamixWellPosition, WellLocation> map = wellLocationMapCache.get(mappingFile);
        if (map == null)
        {
            map = WellLocationMappingUtils.parseWellLocationMap(mappingFile);
            wellLocationMapCache.put(mappingFile, map);
        }
        return map;
    }

    private static File getMappingFile(File imageFile)
    {
        // mappingDir/pos/channel/image
        File mappingDir = imageFile.getParentFile().getParentFile().getParentFile();
        return new File(mappingDir, POSITION_MAPPING_FILE_NAME);
    }
}
