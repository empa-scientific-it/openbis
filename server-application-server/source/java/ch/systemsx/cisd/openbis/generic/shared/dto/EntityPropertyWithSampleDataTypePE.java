package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

@MappedSuperclass
public abstract class EntityPropertyWithSampleDataTypePE extends EntityPropertyPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private SamplePE sample;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SAMPLE_PROP_COLUMN)
    public SamplePE getSampleValue()
    {
        return sample;
    }

    public void setSampleValue(SamplePE sample)
    {
        this.sample = sample;
    }

    @Override
    public final String tryGetUntypedValue()
    {
        if (getSampleValue() != null)
        {
            return getSampleValue().getPermId();
        }
        return super.tryGetUntypedValue();
    }

    @Override
    public final void setUntypedValue(final String valueOrNull,
            final VocabularyTermPE vocabularyTermOrNull, MaterialPE materialOrNull, SamplePE sampleOrNull)
    {
        assert valueOrNull != null || vocabularyTermOrNull != null
                || materialOrNull != null || sampleOrNull != null : 
                    "Either value, vocabulary term, material or sample should not be null.";
        if (sampleOrNull != null)
        {
            setSampleValue(sampleOrNull);
        } else
        {
            setSampleValue(null);
            super.setUntypedValue(valueOrNull, vocabularyTermOrNull, materialOrNull, sampleOrNull);
        }
    }

}
