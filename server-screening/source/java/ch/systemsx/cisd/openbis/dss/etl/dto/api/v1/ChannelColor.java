package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor} instead
 * 
 * @author Jakub Straszewski
 * @deprecated
 */
@Deprecated
public class ChannelColor
{
    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor BLUE =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.BLUE;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor GREEN =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.GREEN;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor RED =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.RED;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor RED_GREEN =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.RED_GREEN;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor RED_BLUE =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.RED_BLUE;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor GREEN_BLUE =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.GREEN_BLUE;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor createFromIndex(
            int colorIndex)
    {
        return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.createFromIndex(colorIndex);
    }

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor valueOf(String item)
    {
        return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.valueOf(item);
    }

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor[] values()
    {
        return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.values();
    }
}