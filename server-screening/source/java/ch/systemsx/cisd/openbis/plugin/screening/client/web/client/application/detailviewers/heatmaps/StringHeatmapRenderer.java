package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.ColorConstants.CATEGORY_OTHERS_COLOR;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.ColorConstants.LONG_GRADIENT_DEFAULT_COLORS;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.ColorConstants.SHORT_DEFAULT_COLORS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * Assigns colors to string labels. Colors will be used to create a heatmap.
 * 
 * @author Tomasz Pylak
 */
class StringHeatmapRenderer implements IHeatmapRenderer<String>
{
    private static final String CATEGORY_OTHERS_LABEL = "Others";

    private final List<HeatmapScaleElement> scale;

    private final Map<String, Color> colorsMap;

    private final boolean moreLabelsThanColors;

    /**
     * Assigns colors to string labels from a fixed default set of colors. If there are more values than colors, the "overhead values" are marked as
     * belonging to one "Others" group.
     */
    public StringHeatmapRenderer(List<String> values)
    {
        this(values, null);
    }

    private static List<Color> getDefaultColors(int size)
    {
        List<String> colors;
        if (size <= SHORT_DEFAULT_COLORS.size())
        {
            colors = SHORT_DEFAULT_COLORS;
        } else
        {
            colors = LONG_GRADIENT_DEFAULT_COLORS;
        }
        return ColorConstants.asColors(colors);
    }

    /**
     * Assigns specified colors to string labels using colors in the specified order. If there are more values than colors, the "overhead values" are
     * marked as belonging to one "Others" group.
     */
    public StringHeatmapRenderer(List<String> uniqueValues, List<Color> scaleColorsOrNull)
    {
        List<Color> scaleColors =
                scaleColorsOrNull != null ? scaleColorsOrNull : getDefaultColors(uniqueValues
                        .size());
        this.scale = calculateScale(uniqueValues, scaleColors);
        this.colorsMap = calculateColorMap(scale);
        this.moreLabelsThanColors = (uniqueValues.size() > scaleColors.size());
        if (moreLabelsThanColors)
        {
            scale.add(new HeatmapScaleElement(CATEGORY_OTHERS_LABEL, CATEGORY_OTHERS_COLOR));
        }
    }

    private static Map<String, Color> calculateColorMap(List<HeatmapScaleElement> scale)
    {
        Map<String, Color> colorsMap = new HashMap<String, Color>();
        for (HeatmapScaleElement range : scale)
        {
            colorsMap.put(range.getLabel(), range.getColor());
        }
        return colorsMap;
    }

    private static List<HeatmapScaleElement> calculateScale(List<String> uniqueValues,
            List<Color> scaleColors)
    {
        List<HeatmapScaleElement> scale = new ArrayList<HeatmapScaleElement>();
        Iterator<Color> colorsIter = scaleColors.iterator();
        Iterator<String> valuesIter = uniqueValues.iterator();
        while (colorsIter.hasNext() && valuesIter.hasNext())
        {
            scale.add(new HeatmapScaleElement(valuesIter.next(), colorsIter.next()));
        }
        return scale;
    }

    @Override
    public Color getColor(String value)
    {
        Color color = colorsMap.get(value);

        if (color == null)
        {
            return CATEGORY_OTHERS_COLOR;
        } else
        {
            return color;
        }
    }

    @Override
    public List<HeatmapScaleElement> calculateScale()
    {
        return scale;
    }

    @Override
    public String tryGetFirstLabel()
    {
        return null;
    }
}