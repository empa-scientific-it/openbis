package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.MinMaxAndRange;

/**
 * Assigns colors to numeric ranges. Colors will be used to create a heat map.
 * 
 * @author Izabela Adamczyk
 */
class NumberHeatmapRenderer implements IHeatmapRenderer<Float>
{
    static final String LESS_THAN_EQUAL = "\u2264";

    static final String GREATER_THAN_EQUAL = "\u2265";

    private final float step;

    private final List<Color> colors;

    private final float from;

    private final float until;

    private final IRealNumberRenderer realNumberRenderer;

    private final String scaleTop;

    private final String scaleBottom;

    public NumberHeatmapRenderer(MinMaxAndRange minMaxRange, IRealNumberRenderer realNumberRenderer)
    {
        this(minMaxRange, ColorConstants.LONG_GRADIENT_DEFAULT_COLORS, realNumberRenderer);
    }

    public NumberHeatmapRenderer(MinMaxAndRange minMaxRange, List<String> colors,
            IRealNumberRenderer realNumberRenderer)
    {
        from = minMaxRange.getRange().getFrom();
        until = minMaxRange.getRange().getUntil();
        this.colors = ColorConstants.asColors(colors);
        this.realNumberRenderer = realNumberRenderer;
        if (from == until)
        {
            this.step = 1.0f / this.colors.size();
        } else
        {
            this.step = (until - from) / this.colors.size();
        }
        if (step > 0)
        {
            scaleTop = createScaleEndLabel(until, minMaxRange.getMax(), minMaxRange.getMin());
            scaleBottom = createScaleEndLabel(from, minMaxRange.getMin(), minMaxRange.getMax());
        } else
        {
            scaleTop = createScaleEndLabel(until, minMaxRange.getMin(), minMaxRange.getMax());
            scaleBottom = createScaleEndLabel(from, minMaxRange.getMax(), minMaxRange.getMin());
        }
    }

    private String createScaleEndLabel(float scaleEnd, float dataEnd, float otherDataEnd)
    {
        String scaleNumber = round(scaleEnd);
        if ((dataEnd - scaleEnd) * (scaleEnd - otherDataEnd) > 0)
        {
            scaleNumber = (scaleEnd < dataEnd ? GREATER_THAN_EQUAL : LESS_THAN_EQUAL) + " " + scaleNumber;
        }
        return scaleNumber;
    }

    @Override
    public Color getColor(Float value)
    {
        if (value == null || Float.isNaN(value) || Float.isInfinite(value))
        {
            return ColorConstants.EMPTY_VALUE_COLOR;
        }
        float part = (until - value) / step;
        int colorNumber = Math.max(0, Math.min((int) Math.floor(part), colors.size() - 1));
        return colors.get(colorNumber);
    }

    @Override
    public List<HeatmapScaleElement> calculateScale()
    {
        List<HeatmapScaleElement> scale = new ArrayList<HeatmapScaleElement>();
        for (int i = 0; i < colors.size() - 1; i++)
        {
            String label = round(until - step * (i + 1));
            scale.add(new HeatmapScaleElement(label, colors.get(i)));
        }
        scale.add(new HeatmapScaleElement(scaleBottom, colors.get(colors.size() - 1)));
        return scale;
    }

    @Override
    public String tryGetFirstLabel()
    {
        return scaleTop;
    }

    private String round(float labelValue)
    {
        return realNumberRenderer
                .render(Math.abs(step) > 10 ? Math.round(labelValue) : labelValue);
    }
}