/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.common.interfaces.IPropertiesHolder")
public interface IPropertiesHolder
{

    Map<String, Serializable> getProperties();

    void setProperties(Map<String, Serializable> properties);

    Serializable getProperty(String propertyName);

    void setProperty(String propertyName, Serializable propertyValue);

    Long getIntegerProperty(String propertyName);

    void setIntegerProperty(String propertyName, Long propertyValue);

    String getStringProperty(String propertyName);

    void setStringProperty(String propertyName, String propertyValue);

    Double getRealProperty(String propertyName);

    void setRealProperty(String propertyName, Double propertyValue);

    ZonedDateTime getTimestampProperty(String propertyName);

    void setTimestampProperty(String propertyName, ZonedDateTime propertyValue);

    Boolean getBooleanProperty(String propertyName);

    void setBooleanProperty(String propertyName, Boolean propertyValue);

    String getControlledVocabularyProperty(String propertyName);

    void setControlledVocabularyProperty(String propertyName, String propertyValue);

    SamplePermId getSampleProperty(String propertyName);

    void setSampleProperty(String propertyName, SamplePermId propertyValue);

    String getHyperlinkProperty(String propertyName);

    void setHyperlinkProperty(String propertyName, String propertyValue);

    String getXmlProperty(String propertyName);

    void setXmlProperty(String propertyName, String propertyValue);

    Long[] getIntegerArrayProperty(String propertyName);

    void setIntegerArrayProperty(String propertyName, Long[] propertyValue);

    Double[] getRealArrayProperty(String propertyName);

    void setRealArrayProperty(String propertyName, Double[] propertyValue);

    String[] getStringArrayProperty(String propertyName);

    void setStringArrayProperty(String propertyName, String[] propertyValue);

    ZonedDateTime[] getTimestampArrayProperty(String propertyName);

    void setTimestampArrayProperty(String propertyName, ZonedDateTime[] propertyValue);

    String getJsonProperty(String propertyName);

    void setJsonProperty(String propertyName, String propertyValue);

    // Multi-value properties

    List<Long> getMultiValueIntegerProperty(String propertyName);

    void setMultiValueIntegerProperty(String propertyName, List<Long> propertyValues);

    List<String> getMultiValueStringProperty(String propertyName);

    void setMultiValueStringProperty(String propertyName, List<String> propertyValues);

    List<Double> getMultiValueRealProperty(String propertyName);

    void setMultiValueRealProperty(String propertyName, List<Double> propertyValue);

    List<ZonedDateTime> getMultiValueTimestampProperty(String propertyName);

    void setMultiValueTimestampProperty(String propertyName, List<ZonedDateTime> propertyValue);

    List<Boolean> getMultiValueBooleanProperty(String propertyName);

    void setMultiValueBooleanProperty(String propertyName, List<Boolean> propertyValue);

    List<String> getMultiValueHyperlinkProperty(String propertyName);

    void setMultiValueHyperlinkProperty(String propertyName, List<String> propertyValue);

    List<String> getMultiValueXmlProperty(String propertyName);

    void setMultiValueXmlProperty(String propertyName, List<String> propertyValue);

    List<String> getMultiValueControlledVocabularyProperty(String propertyName);

    void setMultiValueControlledVocabularyProperty(String propertyName, List<String> propertyValue);

    List<SamplePermId> getMultiValueSampleProperty(String propertyName);

    void setMultiValueSampleProperty(String propertyName, List<SamplePermId> propertyValue);

    List<Long[]> getMultiValueIntegerArrayProperty(String propertyName);

    void setMultiValueIntegerArrayProperty(String propertyName, List<Long[]> propertyValue);

    List<Double[]> getMultiValueRealArrayProperty(String propertyName);

    void setMultiValueRealArrayProperty(String propertyName, List<Double[]> propertyValue);

    List<String[]> getMultiValueStringArrayProperty(String propertyName);

    void setMultiValueStringArrayProperty(String propertyName, List<String[]> propertyValue);

    List<ZonedDateTime[]> getMultiValueTimestampArrayProperty(String propertyName);

    void setMultiValueTimestampArrayProperty(String propertyName, List<ZonedDateTime[]> propertyValue);

    List<String> getMultiValueJsonProperty(String propertyName);

    void setMultiValueJsonProperty(String propertyName, List<String> propertyValue);

}
