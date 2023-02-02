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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;

/**
 * @author Tomasz Pylak
 */
class ColorConstants
{
    public static final List<String> LONG_GRADIENT_DEFAULT_COLORS = Arrays.asList("#67001F",
            "#B2182B", "#D6604D", "#F4A582", "#FDDBC7", "#F7F7F7", "#D1E5F0", "#92C5DE", "#4393C3",
            "#2166AC", "#053061");

    public static final List<String> SHORT_DEFAULT_COLORS = Arrays.asList("#DAFFB3", "#DDCCFF",
            "#4393C3", "#FDB863", "#E66101");

    public static final Color EMPTY_VALUE_COLOR = new Color("#000000");

    public static final Color CATEGORY_OTHERS_COLOR = EMPTY_VALUE_COLOR;

    public static List<Color> asColors(List<String> colorHexs)
    {
        List<Color> colors = new ArrayList<Color>();
        for (String color : colorHexs)
        {
            colors.add(new Color(color));
        }
        return colors;
    }

}
