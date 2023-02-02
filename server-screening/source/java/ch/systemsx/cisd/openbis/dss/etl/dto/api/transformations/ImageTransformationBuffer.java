/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Utility class to construct various kinds of image transformations.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransformationBuffer
{
    private static final String PREDEFINED_TRANSFORMATIONS_CODE_PREFIX = "_";

    private final List<ImageTransformation> imageTransformations;

    public ImageTransformationBuffer()
    {
        this.imageTransformations = new ArrayList<ImageTransformation>();
    }

    /**
     * Appends a single transformation and returns it. Note that code of each added transformation should be unique.
     */
    public ImageTransformation append(ImageTransformation transformation)
    {
        appendAll(transformation);
        return transformation;
    }

    /**
     * Appends any specified transformations. Note that code of each added transformation should be unique.
     */
    public void appendAll(ImageTransformation... transformations)
    {
        for (ImageTransformation transformation : transformations)
        {
            imageTransformations.add(transformation);
        }
        ensureTransformationCodesUnique();
    }

    // returns a set of used transformation codes
    private Set<String> ensureTransformationCodesUnique()
    {
        Set<String> usedTransformationCodes = new HashSet<String>();
        for (ImageTransformation transformation : imageTransformations)
        {
            String code = transformation.getCode();
            if (usedTransformationCodes.contains(code))
            {
                throw new IllegalArgumentException("Two transformations have the same code: "
                        + code);
            }
            usedTransformationCodes.add(code);
        }
        return usedTransformationCodes;
    }

    private void ensureOnlyOneDefault()
    {
        ImageTransformation defaultTansf = null;
        for (ImageTransformation transformation : imageTransformations)
        {
            if (transformation.isDefault())
            {
                if (defaultTansf == null)
                {
                    defaultTansf = transformation;
                } else
                {
                    throw ConfigurationFailureException.fromTemplate(
                            "Only one image transformation can be default, but two were found: '%s' and "
                                    + "'%s'.", defaultTansf.getLabel(), transformation.getLabel());
                }
            }
        }
    }

    /** @returns all appended transformations */
    public ImageTransformation[] getTransformations()
    {
        // codes of transformations could be changed after they have been added
        ensureTransformationCodesUnique();
        ensureOnlyOneDefault();
        return imageTransformations.toArray(new ImageTransformation[imageTransformations.size()]);
    }

    // -------------- bit shifting transformations ---------------------

    /**
     * Appends transformations which extracts a range of grayscale image colors by choosing 8 consecutive bits. All shifts which make sense for 12 bit
     * images will be appended (from 0 to 4).
     * 
     * @return appended transformations. They can be used to e.g. modify their label or description.
     */
    public ImageTransformation[] appendAllBitShiftsFor12BitGrayscale()
    {
        return appendAllAvailableGrayscaleBitShifts(12);
    }

    /**
     * Appends transformations which extracts a range of grayscale image colors by choosing 8 consecutive bits. All shifts which make sense for 16 bit
     * images will be appended (from 0 to 8).
     * 
     * @return appended transformations. They can be used to e.g. modify their label or description.
     */
    public ImageTransformation[] appendAllBitShiftsFor16BitGrayscale()
    {
        return appendAllAvailableGrayscaleBitShifts(16);
    }

    private ImageTransformation[] appendAllAvailableGrayscaleBitShifts(int totalNumberOfBits)
    {
        int transformationsNumber = totalNumberOfBits - 8 + 1;
        ImageTransformation[] transformations = new ImageTransformation[transformationsNumber];
        for (int i = 0; i < transformationsNumber; i++)
        {
            transformations[i] = appendGrayscaleBitShifting(i);
        }
        return transformations;
    }

    /**
     * Appends transformation which extracts a range of grayscale image colors by choosing 8 consecutive bits starting from the specified one.
     * 
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendGrayscaleBitShifting(int shiftBits)
    {
        return append(createGrayscaleBitShifting(shiftBits));
    }

    /**
     * Creates a transformation which extracts a range of grayscale image colors by choosing 8 consecutive bits, staring from the specified one.
     * <p>
     * This method is useful when one wants to modify the default code, label or description afterwards.
     */
    private static ImageTransformation createGrayscaleBitShifting(int shiftBits)
    {
        if (shiftBits < 0)
        {
            throw new IllegalArgumentException(
                    "Cannot create an image transformation which shifts by a negative number of bits");
        }
        String label = createBitShiftingTransformationLabel(shiftBits);
        String code = createBitShiftingTransformationCode(shiftBits);
        String description = createBitShiftingTransformationDescription(shiftBits);

        IImageTransformerFactory factory = new BitShiftingImageTransformerFactory(shiftBits);
        return new ImageTransformation(code, label, description, factory);
    }

    private static String createBitShiftingTransformationCode(int shiftBits)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "BIT_SHIFTING_" + shiftBits;
    }

    private static String createBitShiftingTransformationDescription(int shiftBits)
    {
        return String.format(
                "Shows a range of grayscale image colors by visualising only 8 consecutive bits from %d to %d. "
                        + "Useful for extracting information from 12 and 16 bit images.",
                shiftBits, shiftBits + 7);
    }

    private static String createBitShiftingTransformationLabel(int shiftBits)
    {
        return String.format("Show bits %d-%d", shiftBits, shiftBits + 7);
    }

    // -----------------------------

    /**
     * Appends transformation which converts grayscale image pixel intensities from the range [blackPointIntensity, whitePointIntensity] to 8 bit
     * color depth. Useful to compare images of higher color depth with each other when they do not use the whole range of available intensities.
     * 
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendRescaleGrayscaleIntensity(int blackPointIntensity,
            int whitePointIntensity)
    {
        return appendRescaleGrayscaleIntensity(blackPointIntensity, whitePointIntensity, null);
    }

    /**
     * See {@link #appendRescaleGrayscaleIntensity(int, int)}.
     * <p>
     * Additionally sets the label of the transformation.
     * 
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendRescaleGrayscaleIntensity(int blackPointIntensity,
            int whitePointIntensity, String userFriendlyLabelOrNull)
    {
        return append(createRescaleGrayscaleIntensity(blackPointIntensity, whitePointIntensity,
                userFriendlyLabelOrNull));
    }

    /**
     * Creates a transformation which converts grayscale image pixel intensities from the range [blackPointIntensity, whitePointIntensity] to 8 bit
     * color depth.
     * <p>
     * This method is useful when one wants to modify the default code, label or description afterwards.
     * 
     * @param userFriendlyLabelOrNull label of the transformation. If null a default label is assigned.
     */
    public static ImageTransformation createRescaleGrayscaleIntensity(int blackPointIntensity,
            int whitePointIntensity, String userFriendlyLabelOrNull)
    {
        if (blackPointIntensity > whitePointIntensity || blackPointIntensity < 0
                || whitePointIntensity < 0)
        {
            throw new IllegalArgumentException(String.format(
                    "Cannot create an image transformation because the range "
                            + "of intensities is invalid: [%d, %d]", blackPointIntensity,
                    whitePointIntensity));
        }
        String label = createIntensityRangeTransformationLabel(userFriendlyLabelOrNull);
        String code =
                createIntensityRangeTransformationCode(blackPointIntensity, whitePointIntensity);
        String description =
                createIntensityRangeTransformationDescription(blackPointIntensity,
                        whitePointIntensity);

        IImageTransformerFactory factory =
                new IntensityRangeImageTransformerFactory(blackPointIntensity, whitePointIntensity);
        return new ImageTransformation(code, label, description, factory);
    }

    private static String createIntensityRangeTransformationCode(int blackPointIntensity,
            int whitePointIntensity)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "INTENSITY_LEVEL_" + blackPointIntensity
                + "_" + whitePointIntensity;
    }

    private static String createIntensityRangeTransformationDescription(int blackPointIntensity,
            int whitePointIntensity)
    {
        return String
                .format("Transforms grayscale image by converting intensities of its pixels "
                        + "which are in the range [%d, %d] to 8 bit color depth. "
                        + "The range of intensities is usually calculated by processing a series of 12 or 16 bit images, "
                        + "then the transformation becomes useful to compare these images with each other in 8 bit color depth, "
                        + "especially when they use only a small part of available intensities range.",
                        blackPointIntensity, whitePointIntensity);
    }

    private static String createIntensityRangeTransformationLabel(String labelOrNull)
    {
        return labelOrNull != null ? labelOrNull : "Fixed rescaling";
    }

    // --------------------------

    /**
     * Appends transformation which converts each single grayscale image to 8 bit color depth and rescales pixels intensities so that the darkest
     * pixel will become black and the brightest will become white.
     * <p>
     * Note that by default openBIS applies this transformation with threshold 0 if it deals with grayscale image where color depth is bigger then 8
     * bit. So calling this method with parameter 0 is not necessary.
     * </p>
     * 
     * @param threshold value form 0 to 1, it specifies the percentage of darkest and brightest pixels which will be ignored (they will all become
     *            black or white).
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendAutoRescaleGrayscaleIntensity(float threshold)
    {
        return appendAutoRescaleGrayscaleIntensity(threshold, null);
    }

    /**
     * See {@link #appendAutoRescaleGrayscaleIntensity(float)}.
     * <p>
     * Additionally sets the label of the transformation.
     * 
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendAutoRescaleGrayscaleIntensity(float threshold,
            String userFriendlyLabelOrNull)
    {
        return append(createAutoRescaleGrayscaleIntensity(threshold, userFriendlyLabelOrNull));
    }

    /**
     * Creates a transformation which converts each single grayscale image to 8 bit color depth and rescales pixels intensities so that the darkest
     * pixel will become black and the brightest will become white (with some threshold margin).
     * <p>
     * This method is useful when one wants to modify the default code, label or description afterwards.
     * 
     * @param threshold value form 0 to 1, it specifies the percentage of darkest and brightest pixels which will be ignored (they will all become
     *            black or white).
     * @param userFriendlyLabelOrNull label of the transformation. If null a default label is assigned.
     */
    private static ImageTransformation createAutoRescaleGrayscaleIntensity(float threshold,
            String userFriendlyLabelOrNull)
    {
        if (threshold < 0 || threshold > 1)
        {
            throw new IllegalArgumentException(
                    "Invalid value of the threshold, should be between 0 and 1, but is: "
                            + threshold);
        }
        String label =
                createAutoRescaleIntensityTransformationLabel(userFriendlyLabelOrNull, threshold);
        String code = createAutoRescaleIntensityTransformationCode(threshold);
        String description = createAutoRescaleIntensityTransformationDescription(threshold);

        IImageTransformerFactory factory =
                new AutoRescaleIntensityImageTransformerFactory(threshold);
        return new ImageTransformation(code, label, description, factory);
    }

    private static String createAutoRescaleIntensityTransformationCode(float threshold)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "AUTO_INTENSITY_LEVEL_" + (threshold * 100);
    }

    private static String createAutoRescaleIntensityTransformationDescription(float threshold)
    {
        return String
                .format("Creates a transformation which converts each single grayscale image to 8 bit color depth and "
                        + "rescales pixels intensities so that the darkest pixel will become black and the brightest "
                        + "will become white (threshold margin: %s).",
                        new DecimalFormat("#.###").format(threshold));
    }

    @Private
    static String createAutoRescaleIntensityTransformationLabel(String labelOrNull, float threshold)
    {
        String thresholdPercentage = new DecimalFormat("##.#").format(threshold * 100);
        return labelOrNull != null ? labelOrNull : String.format("Optimal (image, %s%% cut)",
                thresholdPercentage);
    }

    // --------------------------

    /**
     * Allows to transform the images with ImageMagic convert tool (which has to be installed and accessible). Convert will be called with the
     * specified parameters.
     * 
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendImageMagicConvert(String convertCliArguments)
    {
        return appendImageMagicConvert(convertCliArguments, null);
    }

    /**
     * See {@link #appendImageMagicConvert(String)}.
     * <p>
     * Additionally sets the label of the transformation.
     * 
     * @return appended transformation. It can be used to e.g. modify its label or description.
     */
    public ImageTransformation appendImageMagicConvert(String convertCliArguments,
            String userFriendlyLabelOrNull)
    {
        return append(createImageMagicConvert(convertCliArguments,
                generateUniqueConvertTransformationCode(), userFriendlyLabelOrNull));
    }

    /**
     * Creates a transformation which converts the images with ImageMagic convert tool.
     * <p>
     * This method is useful when one wants to modify the default code, label or description afterwards.
     * 
     * @param userFriendlyLabelOrNull label of the transformation. If null a default label is assigned.
     */
    private static ImageTransformation createImageMagicConvert(String convertCliArguments,
            String transformationCode, String userFriendlyLabelOrNull)
    {
        if (StringUtils.isBlank(convertCliArguments))
        {
            throw new IllegalArgumentException(
                    "No argument has been specified for the 'convert' command");
        }
        if (StringUtils.isBlank(transformationCode))
        {
            throw new IllegalArgumentException("Transformation has not been specified");
        }
        String label =
                createImageMagicConvertTransformationLabel(convertCliArguments,
                        userFriendlyLabelOrNull);
        String description = createImageMagicConvertTransformationDescription(convertCliArguments);

        IImageTransformerFactory factory =
                new ConvertToolImageTransformerFactory(convertCliArguments);
        return new ImageTransformation(transformationCode, label, description, factory);
    }

    private static String createImageMagicConvertTransformationDescription(
            String convertCliArguments)
    {
        return String.format("Transforms images with ImageMagic tool by calling: 'convert %s ...'",
                convertCliArguments);
    }

    private static String createImageMagicConvertTransformationLabel(String convertCliArguments,
            String labelOrNull)
    {
        return labelOrNull != null ? labelOrNull : String.format("Convert (%s)",
                convertCliArguments);
    }

    private String generateUniqueConvertTransformationCode()
    {
        int i = 1;
        Set<String> usedTransformationCodes = ensureTransformationCodesUnique();
        while (usedTransformationCodes.contains(getConvertTransformationCode(i)))
        {
            i++;
        }
        return getConvertTransformationCode(i);
    }

    private static String getConvertTransformationCode(int seqNumber)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "CONVERT_" + seqNumber;
    }
}
