package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel} instead
 * 
 * @author Jakub Straszewski
 * @deprecated
 */
@Deprecated
public final class Channel extends ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel
{
    private static final long serialVersionUID = 1L;

    public Channel(String code, String label,
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor channelColorOrNull)
    {
        super(code, label, channelColorOrNull);
    }

    public Channel(String code, String label, ChannelColorRGB channelColorOrNull)
    {
        super(code, label, channelColorOrNull);
    }

    public Channel(String code, String label)
    {
        super(code, label);
    }
}