/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageIdentifier} instead
 * 
 * @author Jakub Straszewski
 * @deprecated
 */
@Deprecated
public class ImageIdentifier extends ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageIdentifier
{

    private static final long serialVersionUID = 1L;

    public ImageIdentifier(int seriesIndex, int timeSeriesIndex, int focalPlaneIndex,
            int colorChannelIndex)
    {
        super(seriesIndex, timeSeriesIndex, focalPlaneIndex, colorChannelIndex);
    }
}