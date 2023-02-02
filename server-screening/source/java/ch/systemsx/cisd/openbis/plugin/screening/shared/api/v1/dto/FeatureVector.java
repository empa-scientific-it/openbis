package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Feature vector for one well.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("FeatureVector")
public class FeatureVector implements Serializable
{
    private static final long serialVersionUID = 1L;

    private WellPosition wellPosition;

    private double[] values;

    private boolean[] vocabularyFeatureFlags;

    private String[] vocabularyTerms;

    /**
     * Creates an instance for the specified well assuming all features are numbers.
     */
    public FeatureVector(WellPosition well, double[] values)
    {
        this(well, values, new boolean[values.length], new String[values.length]);
    }

    /**
     * Creates an instance for the specified well.
     * 
     * @param values Array with values of numerical features.
     * @param vocabularyFeatureFlags Array telling which feature is a numerical one ( <code>false</code>) or a vocabulary term (<code>true</code>).
     * @param vocabularyTerms Array with values of vocabulary-based features.
     * @throws IllegalArgumentException if all arrays have not the same length.
     */
    public FeatureVector(WellPosition well, double[] values, boolean[] vocabularyFeatureFlags,
            String[] vocabularyTerms)
    {
        this.wellPosition = well;
        this.values = values;
        this.vocabularyFeatureFlags = vocabularyFeatureFlags;
        this.vocabularyTerms = vocabularyTerms;
        if (values.length != vocabularyFeatureFlags.length
                || values.length != vocabularyTerms.length)
        {
            throw new IllegalArgumentException("Array lengths different: " + values.length + " "
                    + vocabularyFeatureFlags.length + " " + vocabularyTerms.length);
        }
    }

    /** Returns the well position on a plate. */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /**
     * Returns the array of numerical features. If the value is {@link Double#NaN} it means either an unknown value of the numerical feature or a
     * vocabulary-based feature.
     */
    public double[] getValues()
    {
        return values;
    }

    /**
     * Return the array of flags specifying the type of feature where <code>true</code> means vocabulary-based feature and <code>false</code>
     * numerical feature.
     */
    public final boolean[] getVocabularyFeatureFlags()
    {
        return vocabularyFeatureFlags;
    }

    /**
     * Returns the array of vocabulary-based features. If the value is <code>null</code> it means either an unknown value of the vocabulary-base
     * feature or a numerical feature.
     */
    public final String[] getVocabularyTerms()
    {
        return vocabularyTerms;
    }

    /**
     * Returns the feature vector as a list of objects. The list element is either an instance of String (vocabulary-based feature), an instance of
     * Double (numerical feature), or <code>null</code> if the feature is unknown.
     */
    @JsonIgnore
    public List<Object> getValueObjects()
    {
        ArrayList<Object> result = new ArrayList<Object>();
        for (int i = 0; i < values.length; i++)
        {
            if (vocabularyFeatureFlags[i])
            {
                result.add(vocabularyTerms[i]);
            } else
            {
                double number = values[i];
                result.add(Double.isNaN(number) ? null : number);
            }
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "wellPosition: " + wellPosition + ", values: " + getValueObjects();
    }

    //
    // JSON-RPC
    //

    private FeatureVector()
    {
    }

    private void setWellPosition(WellPosition wellPosition)
    {
        this.wellPosition = wellPosition;
    }

    private void setValues(double[] values)
    {
        this.values = values;
    }

    private void setVocabularyFeatureFlags(boolean[] vocabularyFeatureFlags)
    {
        this.vocabularyFeatureFlags = vocabularyFeatureFlags;
    }

    private void setVocabularyTerms(String[] vocabularyTerms)
    {
        this.vocabularyTerms = vocabularyTerms;
    }

}