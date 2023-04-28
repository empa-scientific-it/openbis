/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

import java.util.Date;

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
            final VocabularyTermPE vocabularyTermOrNull, MaterialPE materialOrNull,
            SamplePE sampleOrNull, Long[] integerArrayOrNull, Double[] realArrayOrNull,
            String[] stringArrayOrNull, Date[] timestampArrayOrNull, String jsonOrNull)
    {
        assert valueOrNull != null || vocabularyTermOrNull != null
                || materialOrNull != null || sampleOrNull != null || integerArrayOrNull != null
                || realArrayOrNull != null || stringArrayOrNull != null || timestampArrayOrNull != null
                || jsonOrNull != null :
                "Either value, array value, json vocabulary term, material or sample should not be null.";
        if (sampleOrNull != null)
        {
            setSampleValue(sampleOrNull);
        } else
        {
            setSampleValue(null);
            super.setUntypedValue(valueOrNull, vocabularyTermOrNull, materialOrNull, sampleOrNull,
                    integerArrayOrNull, realArrayOrNull, stringArrayOrNull, timestampArrayOrNull,
                    jsonOrNull);
        }
    }

}
