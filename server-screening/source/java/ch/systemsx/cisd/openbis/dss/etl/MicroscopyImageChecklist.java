/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageSeriesPoint;

/**
 * This is a copy of the HCSImageCheckList and should be refactored together.
 * 
 * @author Franz-Josef Elmer
 */
public final class MicroscopyImageChecklist extends AbstractImageChecklist
{
    private final Map<FullLocation, Check> imageMap;

    private final HashMap<FullLocationImageSeriesPoint, DuplicateLocationImages> duplicateLocationImages;

    public MicroscopyImageChecklist(final List<String> channelCodes, final Geometry wellGeometry)
    {
        if (channelCodes.size() < 1)
        {
            throw new IllegalArgumentException("No channels defined!");
        }

        if (wellGeometry == null)
        {
            throw new IllegalArgumentException("Unspecified well geometry.");
        }
        imageMap = new HashMap<FullLocation, Check>();

        duplicateLocationImages =
                new HashMap<FullLocationImageSeriesPoint, DuplicateLocationImages>();

        for (String channelCode : channelCodes)
        {

            for (int tileCol = 1; tileCol <= wellGeometry.getColumns(); tileCol++)
            {
                for (int tileRow = 1; tileRow <= wellGeometry.getRows(); tileRow++)
                {
                    imageMap.put(new FullLocation(tileRow, tileCol, channelCode), new Check());
                }
            }
        }
        assert imageMap.size() == channelCodes.size() * wellGeometry.getColumns()
                * wellGeometry.getRows() : "Wrong map size";
    }

    @Override
    public final void checkOff(AcquiredSingleImage image)
    {
        assert image != null : "Unspecified image.";
        FullLocation location = createLocation(image);
        final Check check = imageMap.get(location);
        if (check == null)
        {
            throw new IllegalArgumentException("Invalid channel/well/tile: " + image);
        }
        Float timepointOrNull = image.tryGetTimePoint();
        Float depthOrNull = image.tryGetDepth();
        Integer seriesNumberOrNull = image.tryGetSeriesNumber();
        if (check.isCheckedOff(timepointOrNull, depthOrNull, seriesNumberOrNull))
        {
            FullLocationImageSeriesPoint fullLocationImageSeriesPoint =
                    new FullLocationImageSeriesPoint(location, new ImageSeriesPoint(
                            timepointOrNull, depthOrNull, seriesNumberOrNull));
            DuplicateLocationImages duplicates =
                    duplicateLocationImages.get(fullLocationImageSeriesPoint);
            if (null == duplicates)
            {
                AcquiredSingleImage primaryImage =
                        check.getImageForLocation(timepointOrNull, depthOrNull, seriesNumberOrNull);
                duplicates =
                        new DuplicateLocationImages(fullLocationImageSeriesPoint, primaryImage);
                duplicateLocationImages.put(fullLocationImageSeriesPoint, duplicates);
            }
            duplicates.addDuplicateLocationImage(image);
        } else
        {
            check.checkOff(timepointOrNull, depthOrNull, seriesNumberOrNull, image);
        }
    }

    /**
     * Throw an exception if there are duplicate images.
     * <p>
     * This method should be called after checkOff is called on all the images to register. If any duplicate images were found in the checkOff calls,
     * an exception will be thrown containing information about all the duplicate images.
     * 
     * @throws IllegalArgumentException Thrown if duplicate images are detected.
     */
    @Override
    public void checkForDuplicates() throws IllegalArgumentException
    {
        if (duplicateLocationImages.isEmpty())
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Only one image may represent a location for a depth/time/series point. The following ambiguities were found:\n");

        FullLocationImageSeriesPoint[] locations =
                duplicateLocationImages.keySet().toArray(new FullLocationImageSeriesPoint[0]);
        Arrays.sort(locations);
        for (FullLocationImageSeriesPoint location : locations)
        {
            DuplicateLocationImages duplicateImage = duplicateLocationImages.get(location);
            sb.append(duplicateImage.errorString());
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        throw new IllegalArgumentException(sb.toString());
    }

    private static FullLocation createLocation(AcquiredSingleImage image)
    {
        return new FullLocation(image.getTileRow(), image.getTileColumn(), image.getChannelCode());
    }

    public final List<FullLocation> getCheckedOnFullLocations()
    {
        final List<FullLocation> fullLocations = new ArrayList<FullLocation>();
        for (final Map.Entry<FullLocation, Check> entry : imageMap.entrySet())
        {
            if (entry.getValue().isCheckedOff(null, null, null) == false)
            {
                fullLocations.add(entry.getKey());
            }
        }
        return fullLocations;
    }

    //
    // Helper classes
    //

    private static final class Check
    {
        private boolean checkedOff = false;

        private final Set<ImageSeriesPoint> dimensions = new HashSet<ImageSeriesPoint>();

        private final HashMap<ImageSeriesPoint, AcquiredSingleImage> seriesImageMap =
                new HashMap<ImageSeriesPoint, AcquiredSingleImage>();

        final void checkOff(Float timepointOrNull, Float depthOrNull, Integer seriesNumberOrNull,
                AcquiredSingleImage image)
        {
            ImageSeriesPoint seriesPoint =
                    new ImageSeriesPoint(timepointOrNull, depthOrNull, seriesNumberOrNull);
            dimensions.add(seriesPoint);
            seriesImageMap.put(seriesPoint, image);
            checkedOff = true;
        }

        final boolean isCheckedOff(Float timepointOrNull, Float depthOrNull,
                Integer seriesNumberOrNull)
        {
            ImageSeriesPoint dim = null;
            if (timepointOrNull != null || depthOrNull != null || seriesNumberOrNull != null)
            {
                dim = new ImageSeriesPoint(timepointOrNull, depthOrNull, seriesNumberOrNull);
            }
            return checkedOff && (dim == null || dimensions.contains(dim));
        }

        /**
         * Return the image associated with a location. This will be non-null if isCheckedOff is true.
         * 
         * @return The image for the given location.
         */
        final AcquiredSingleImage getImageForLocation(Float timepointOrNull, Float depthOrNull,
                Integer seriesNumberOrNull)
        {
            ImageSeriesPoint seriesPoint =
                    new ImageSeriesPoint(timepointOrNull, depthOrNull, seriesNumberOrNull);
            return seriesImageMap.get(seriesPoint);
        }

    }

    public final static class FullLocation extends AbstractHashable implements
            Comparable<FullLocation>
    {
        final int tileRow, tileCol;

        final String channelCode;

        public FullLocation(int tileRow, int tileCol, String channelCode)
        {
            this.tileRow = tileRow;
            this.tileCol = tileCol;
            this.channelCode = channelCode.toUpperCase();
        }

        private final static String toString(final int row, final int col, final String type)
        {
            return type + "=(" + row + "," + col + ")";
        }

        //
        // AbstractHashable
        //

        @Override
        public final String toString()
        {
            return "[channel=" + channelCode + ", " + toString(tileRow, tileCol, "tile") + "]";
        }

        /**
         * Orders locations by wellRow, wellCol, tileRow, tileCol, channelCode
         */
        @Override
        public int compareTo(FullLocation o)
        {
            int tileRowCompare = Integer.valueOf(tileRow).compareTo(Integer.valueOf(o.tileRow));
            if (tileRowCompare != 0)
            {
                return tileRowCompare;
            }
            return Integer.valueOf(tileCol).compareTo(Integer.valueOf(o.tileCol));
        }
    }

    /**
     * Represents the location of an image, both on a plate and in the series.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class FullLocationImageSeriesPoint extends AbstractHashable implements
            Comparable<FullLocationImageSeriesPoint>
    {
        private final FullLocation location;

        private final ImageSeriesPoint imageSeriesPoint;

        /**
         * @param location
         * @param imageSeriesPoint
         */
        public FullLocationImageSeriesPoint(FullLocation location, ImageSeriesPoint imageSeriesPoint)
        {
            super();
            this.location = location;
            this.imageSeriesPoint = imageSeriesPoint;
        }

        /**
         * Order by full location, then time, depth, series.
         */
        @Override
        public int compareTo(FullLocationImageSeriesPoint o)
        {
            int locationCompare = location.compareTo(o.location);
            if (locationCompare != 0)
            {
                return locationCompare;
            }

            return imageSeriesPoint.compareTo(o.imageSeriesPoint);
        }
    }

    /**
     * An internal class for intelligently keeping track of the image associated with a location and its duplicates.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class DuplicateLocationImages
    {
        private final FullLocationImageSeriesPoint location;

        private final AcquiredSingleImage primaryImage;

        private final ArrayList<AcquiredSingleImage> duplicateImages;

        public DuplicateLocationImages(FullLocationImageSeriesPoint location,
                AcquiredSingleImage primaryImage)
        {
            this.location = location;
            this.primaryImage = primaryImage;
            this.duplicateImages = new ArrayList<AcquiredSingleImage>();
        }

        public void addDuplicateLocationImage(AcquiredSingleImage image)
        {
            duplicateImages.add(image);
        }

        public String errorString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Location: ");
            sb.append("Tile[");
            sb.append(location.location.tileRow);
            sb.append(",");
            sb.append(location.location.tileCol);
            sb.append("]");
            sb.append(" Channel[");
            sb.append(location.location.channelCode);
            sb.append("]");
            if (location.imageSeriesPoint.getDepth() != null)
            {
                sb.append(" Depth[");
                sb.append(location.imageSeriesPoint.getDepth());
                sb.append("]");
            }

            if (location.imageSeriesPoint.getTime() != null)
            {
                sb.append(" Time[");
                sb.append(location.imageSeriesPoint.getTime());
                sb.append("]");
            }

            if (location.imageSeriesPoint.getSeriesNumber() != null)
            {
                sb.append(" Series[");
                sb.append(location.imageSeriesPoint.getSeriesNumber());
                sb.append("]");
            }

            sb.append(" mapped to the following images:");
            sb.append("\n\t");
            sb.append(primaryImage.getImageReference().getImageRelativePath());
            for (AcquiredSingleImage duplicateImage : duplicateImages)
            {
                sb.append("\n\t");
                sb.append(duplicateImage.getImageReference().getImageRelativePath());
            }
            return sb.toString();
        }
    }

    @Override
    public int getCheckedOnFullLocationsSize()
    {
        return getCheckedOnFullLocations().size();
    }

    @Override
    public String getIncompleteDataSetErrorMessage(String dataSetFileName)
    {
        List<FullLocation> fullLocations = getCheckedOnFullLocations();
        return String.format("Incomplete data set '%s': %d image file(s) "
                + "are missing (locations: %s)", dataSetFileName, fullLocations.size(),
                CollectionUtils.abbreviate(fullLocations, 10));
    }
}
