package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.MinMaxAndRange;

/**
 * Allows to convert any object into a float and present the float on the heatmap using {@link NumberHeatmapRenderer}.
 */
abstract class DelegatingFloatHeatmapRenderer<T> implements IHeatmapRenderer<T>
{
    /** Convert T into a float which will be represented on the heatmap. */
    protected abstract Float convert(T value);

    protected final CodeAndLabel feature;

    private final IHeatmapRenderer<Float> delegator;

    public DelegatingFloatHeatmapRenderer(MinMaxAndRange minMaxRange, CodeAndLabel feature,
            IRealNumberRenderer realNumberRenderer)
    {
        this.feature = feature;
        this.delegator = new NumberHeatmapRenderer(minMaxRange, realNumberRenderer);
    }

    @Override
    public Color getColor(T value)
    {
        return delegator.getColor(convert(value));
    }

    @Override
    public List<HeatmapScaleElement> calculateScale()
    {
        return delegator.calculateScale();
    }

    @Override
    public String tryGetFirstLabel()
    {
        return delegator.tryGetFirstLabel();
    }

}