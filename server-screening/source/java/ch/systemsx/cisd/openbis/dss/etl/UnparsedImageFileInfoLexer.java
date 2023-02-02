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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.etl.dto.UnparsedImageFileInfo;

/**
 * Utility to parse information about the image from its file name. Assumes that tokens are separated by '_'. The first letter of the token tells
 * which type of information the token contains. <br>
 * The convention is compatible with the one adopted in Biozentrum by iBrain2.
 * 
 * <pre>
 * example: bDZ01-1A_wD17_s3_z0_t0_cGFP.tif
 *       marker value   meaning
 *          b   DZ01-1A plate
 *          w   D17     well
 *          s   3       tile
 *          z   0       depth
 *          t   0       timepoint
 *          c   GFP     channel
 * </pre>
 * 
 * @author Tomasz Pylak
 */
public class UnparsedImageFileInfoLexer
{
    private static final String TOKENS_SEPARATOR = "_";

    private static final char WELL_MARKER = 'w';

    private static final char CHANNEL_MARKER = 'c';

    private static final char TILE_MARKER = 's'; // a.k.a. side

    private static final char DEPTH_MARKER = 'z';

    private static final char TIME_MARKER = 't';

    private static final char SERIES_NUMBER_MARKER = 'n';

    public static UnparsedImageFileInfo tryExtractHCSImageFileInfo(File imageFile,
            File incomingDataSetPath)
    {
        UnparsedImageFileInfo info = tryExtractImageFileInfo(imageFile, incomingDataSetPath);
        if (info.getWellLocationToken() == null || info.getTileLocationToken() == null
                || info.getChannelToken() == null)
        {
            return null;
        }
        return info;
    }

    public static UnparsedImageFileInfo tryExtractMicroscopyImageFileInfo(File imageFile,
            File incomingDataSetPath)
    {
        UnparsedImageFileInfo info = tryExtractImageFileInfo(imageFile, incomingDataSetPath);
        if (info.getTileLocationToken() == null || info.getChannelToken() == null)
        {
            return null;
        }
        return info;
    }

    private static UnparsedImageFileInfo tryExtractImageFileInfo(File imageFile,
            File incomingDataSetPath)
    {
        UnparsedImageFileInfo info = extractImageFileInfo(getFileBaseName(imageFile));

        return info;
    }

    private static String getFileBaseName(File imageFile)
    {
        return FilenameUtils.getBaseName(imageFile.getPath());
    }

    /**
     * Extracts useful information from dataset image file name specific to iBrain2.
     */
    @Private
    static UnparsedImageFileInfo extractImageFileInfo(String text)
    {
        Map<Character, String> tokensMap = extractTokensMap(text);
        final String wellLocationToken = tokensMap.get(WELL_MARKER);
        final String tileLocationToken = tokensMap.get(TILE_MARKER);
        final String channelToken = tokensMap.get(CHANNEL_MARKER);
        final String timepointToken = tokensMap.get(TIME_MARKER);
        final String depthToken = tokensMap.get(DEPTH_MARKER);
        final String seriesNumberToken = tokensMap.get(SERIES_NUMBER_MARKER);

        UnparsedImageFileInfo info = new UnparsedImageFileInfo();
        info.setWellLocationToken(wellLocationToken);
        info.setTileLocationToken(tileLocationToken);
        info.setChannelToken(channelToken);
        info.setTimepointToken(timepointToken);
        info.setDepthToken(depthToken);
        info.setSeriesNumberToken(seriesNumberToken);
        return info;
    }

    public static Map<Character, String> extractTokensMap(String text)
    {
        Map<Character, String> tokensMap = new HashMap<Character, String>();
        String[] tokens = StringUtils.split(text, TOKENS_SEPARATOR);
        for (String token : tokens)
        {
            if (StringUtils.isBlank(token) == false)
            {
                char marker = token.charAt(0);
                String value = token.substring(1);
                tokensMap.put(marker, value);
            }
        }
        return tokensMap;
    }
}
