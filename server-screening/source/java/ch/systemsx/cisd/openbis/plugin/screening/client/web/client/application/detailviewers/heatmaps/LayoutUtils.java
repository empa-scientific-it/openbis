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

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.RowData;

/**
 * Generic layout utilities.
 * 
 * @author Tomasz Pylak
 */
public class LayoutUtils
{
    public static final int ONE_HEADER_LINE_HEIGHT_PX = 15;

    private static final int MARGIN_SIZE_PX = 10;

    /** @return layout data with big margin on all sides */
    public static RowData createRowLayoutSurroundingData()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(MARGIN_SIZE_PX));
        return layoutData;
    }

    /** @return layout data with big margin on top and bottom */
    public static RowData createRowLayoutHorizontalMargin()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(MARGIN_SIZE_PX, 0, MARGIN_SIZE_PX, 0));
        return layoutData;
    }

    /** @return layout data with big margin on left and right */
    public static RowData createRowLayoutVerticalMargin()
    {
        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(0, MARGIN_SIZE_PX, 0, MARGIN_SIZE_PX));
        return layoutData;
    }

}
