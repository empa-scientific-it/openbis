package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;

/**
 * Allows to get and set the channels chosen by default when images are shown in a specific context.
 * 
 * @author Piotr Buczek
 */
public interface IDefaultChannelState
{
    public List<String> tryGetDefaultChannels();

    public void setDefaultChannels(List<String> channels);

    public void setDefaultTransformation(String channel, String code);

    public String tryGetDefaultTransformation(String channel);

    public ImageResolution tryGetDefaultResolution(String windowId);

    public void setDefaultResolution(ImageResolution resolution, String windowId);

    public void setIntensityRange(String channel, IntensityRange intensityRange);

    public IntensityRange tryGetIntensityRange(String channel);
}