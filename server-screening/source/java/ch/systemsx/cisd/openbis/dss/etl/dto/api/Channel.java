package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.awt.Color;
import java.io.Serializable;

import ch.systemsx.cisd.common.image.WavelengthColor;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformation;

/**
 * A channel in which the image has been acquired.
 * <p>
 * Each channel has its <code>code</code> which uniquely identifies it in one experiment or dataset.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class Channel extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String code;

    private final String label;

    private String description;

    private Integer wavelength;

    private ChannelColorRGB channelColorOrNull;

    private ImageTransformation[] availableTransformations = new ImageTransformation[0];

    /**
     * Constructs a channel with a specified code and label. The channel will be presented in a default color.
     */
    public Channel(String code, String label)
    {
        this(code, label, (ChannelColorRGB) null);
    }

    /**
     * Constructs a channel with a specified code and label. The channel will be presented in a specified color.
     */
    public Channel(String code, String label, ChannelColor channelColorOrNull)
    {
        this(code, label, convertColor(channelColorOrNull));
    }

    /**
     * Constructs a channel with a specified code and label. The channel will be presented in a specified color.
     */
    public Channel(String code, String label, ChannelColorRGB channelColorOrNull)
    {
        assert code != null : "code is null";
        assert label != null : "label is null";
        this.label = label;
        this.code = code;
        this.channelColorOrNull = channelColorOrNull;
    }

    private static ChannelColorRGB convertColor(ChannelColor plainChannelColorOrNull)
    {
        if (plainChannelColorOrNull == null)
        {
            return null;
        }
        return plainChannelColorOrNull.getRGB();
    }

    public String getCode()
    {
        return code;
    }

    public String tryGetDescription()
    {
        return description;
    }

    public Integer tryGetWavelength()
    {
        return wavelength;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * @return color for the specified channel which will be used to display merged channels images. null only during dataset registration when
     *         default color should be used, afterwards never null.
     */
    public ChannelColorRGB tryGetChannelColor()
    {
        return channelColorOrNull;
    }

    // never null, can be empty
    public ImageTransformation[] getAvailableTransformations()
    {
        return availableTransformations;
    }

    // ------------------- setters -------------------

    /** Sets the description of the channel. Optional. */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /** Sets the wavelength of the light (in nanometers) used to acquire this channel. Optional. */
    public void setWavelength(Integer wavelength)
    {
        this.wavelength = wavelength;
    }

    /**
     * Sets the wavelength of the light (in nanometers) used to acquire this channel.<br>
     * Additionally sets the channel color. The color is calculated for display on a computer monitor on the basis of the given wavelength using
     * Bruton's algorithm. See <a href="http://www.midnightkite.com/color.html">COLOR SCIENCE web page</a> for details.
     * <p>
     * Optional.
     */
    public void setWavelengthAndColor(Integer wavelength)
    {
        this.wavelength = wavelength;
        Color color = WavelengthColor.getColorForWavelength(wavelength);
        setChannelColorRGB(convertColor(color));
    }

    private ChannelColorRGB convertColor(Color color)
    {
        return new ChannelColorRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Sets the plain color in which this channel will be displayed. */
    public void setChannelColor(ChannelColor channelColor)
    {
        this.channelColorOrNull = convertColor(channelColor);
    }

    /** Sets RGB color in which this channel will be displayed. */
    public void setChannelColorRGB(ChannelColorRGB channelColor)
    {
        this.channelColorOrNull = channelColor;
    }

    /** Sets available transformations which can be applied to images of this channel on request. */
    public void setAvailableTransformations(ImageTransformation[] transformations)
    {
        if (transformations == null)
        {
            this.availableTransformations = new ImageTransformation[0];
        } else
        {
            this.availableTransformations = transformations;
        }
    }

}