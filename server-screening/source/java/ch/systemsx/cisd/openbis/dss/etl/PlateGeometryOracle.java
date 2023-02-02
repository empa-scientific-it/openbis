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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Based on the list of available plate locations and geometries tries to figure the correct plate geometry.
 * 
 * @author Izabela Adamczyk
 */
public class PlateGeometryOracle
{
    public static String figureGeometry(
            DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails,
            IEncapsulatedBasicOpenBISService openBisService)
    {
        List<ImageFileInfo> images =
                registrationDetails.getDataSetInformation().getImageDataSetStructure().getImages();
        List<WellLocation> locations = extractLocations(images);
        List<String> plateGeometries =
                loadPlateGeometries(openBisService);
        return PlateGeometryOracle.figureGeometry(locations, plateGeometries);
    }

    private static List<String> loadPlateGeometries(IEncapsulatedBasicOpenBISService openbisService)
    {
        Collection<VocabularyTerm> terms =
                openbisService.listVocabularyTerms(ScreeningConstants.PLATE_GEOMETRY);
        List<String> plateGeometries = new ArrayList<String>();
        for (VocabularyTerm v : terms)
        {
            plateGeometries.add(v.getCode());
        }
        return plateGeometries;
    }

    private static List<WellLocation> extractLocations(List<ImageFileInfo> images)
    {
        List<WellLocation> locations = new ArrayList<WellLocation>();
        for (ImageFileInfo image : images)
        {
            locations.add(image.tryGetWellLocation());
        }
        return locations;
    }

    public static String figureGeometry(List<WellLocation> plateLocations,
            List<String> plateGeometries)
    {
        return getMatchingGeometry(getMaxLocation(plateLocations), plateGeometries);
    }

    private static Location getMaxLocation(List<WellLocation> locations)
    {
        int maxX = -1;
        int maxY = -1;
        for (WellLocation l : locations)
        {
            if (maxX < l.getColumn())
            {
                maxX = l.getColumn();
            }
            if (maxY < l.getRow())
            {
                maxY = l.getRow();
            }
        }
        return new Location(maxX, maxY);
    }

    private static String getMatchingGeometry(Location location, List<String> plateGeometries)
    {
        Map<Geometry, String> map = new HashMap<Geometry, String>();
        List<Geometry> geometries = new ArrayList<Geometry>();
        for (String geometryString : plateGeometries)
        {
            Geometry geometry = Geometry.createFromPlateGeometryString(geometryString);
            geometries.add(geometry);
            map.put(geometry, geometryString);
        }
        Collections.sort(geometries, new Comparator<Geometry>()
            {

                @Override
                public int compare(Geometry a, Geometry b)
                {
                    return a.getHeight() * a.getWidth() - b.getHeight() * b.getWidth();
                }
            });
        for (Geometry g : geometries)
        {
            if (isEnough(g, location))
            {
                return map.get(g);
            }
        }
        throw new UserFailureException(String.format(
                "Matching geometry not found (max location: %s, geometries: %s)", location,
                StringUtils.joinList(plateGeometries)));
    }

    static private boolean isEnough(Geometry geometry, Location location)
    {
        int dimX = geometry.getDimX();
        int x = location.getX();
        int dimY = geometry.getDimY();
        int y = location.getY();
        return dimX >= x && dimY >= y;
    }
}
