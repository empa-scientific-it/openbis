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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import javax.persistence.*;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.type.DbTimestampType;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence entity representing entity property.
 *
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@MappedSuperclass
@TypeDefs({ @TypeDef(name = "transaction_timestamp", typeClass = DbTimestampType.class),
        @TypeDef(name = "long_array_type", typeClass = LongArrayType.class),
        @TypeDef(name = "double_array_type", typeClass = DoubleArrayType.class),
        @TypeDef(name = "string_array_type", typeClass = StringArrayType.class),
        @TypeDef(name = "timestamp_array_type", typeClass = TimestampArrayType.class),
        @TypeDef(name = "json_string_type", typeClass = JsonStringType.class)})
public abstract class EntityPropertyPE extends HibernateAbstractRegistrationHolder implements
        IUntypedValueSetter, IEntityPropertyHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    protected IEntityPropertiesHolder entity;

    /**
     * The value of this entity property.
     * <p>
     * Like in the database, no value is set if <code>value</code> is from controlled vocabulary.
     * </p>
     */
    private String value;

    private Long[] integerArrayValue;

    private Double[] realArrayValue;

    private Date[] timestampArrayValue;

    private String[] stringArrayValue;

    private String jsonValue;

    /**
     * The vocabulary term.
     * <p>
     * Not <code>null</code> if <code>value</code> is from controlled vocabulary.
     * </p>
     */
    private VocabularyTermPE vocabularyTerm;

    /**
     * If the property is of MATERIAL, this field is not <code>null</code> and {@link #value} and
     * {@link #vocabularyTerm} fields are set to
     * <code>null</code>.
     */
    private MaterialPE material;

    protected transient Long id;

    protected EntityTypePropertyTypePE entityTypePropertyType;

    /**
     * Person who modified this entity.
     * <p>
     * This is specified at update time.
     * </p>
     */
    private PersonPE author;

    private Date modificationDate;

    protected boolean entityFrozen;

    public <T extends EntityTypePropertyTypePE> void setEntityTypePropertyType(
            final T entityTypePropertyType)
    {
        this.entityTypePropertyType = entityTypePropertyType;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public void setEntityFrozen(boolean frozen)
    {
        this.entityFrozen = frozen;
    }

    public void setValue(final String value)
    {
        this.value = value;
    }

    private void clearValues() {
        this.value = null;
        this.material = null;
        this.vocabularyTerm = null;
        this.integerArrayValue = null;
        this.stringArrayValue = null;
        this.realArrayValue = null;
        this.timestampArrayValue = null;
        this.jsonValue = null;
    }

    @Column(name = ColumnNames.VALUE_COLUMN)
    public String getValue()
    {
        return value;
    }

    public void setVocabularyTerm(final VocabularyTermPE vt)
    {
        this.vocabularyTerm = vt;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.VOCABULARY_TERM_COLUMN)
    public VocabularyTermPE getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.MATERIAL_PROP_COLUMN)
    public MaterialPE getMaterialValue()
    {
        return material;
    }

    public void setMaterialValue(MaterialPE material)
    {
        this.material = material;
    }

    @Column(name = ColumnNames.INTEGER_ARRAY_VALUE_COLUMN)
    @Type(type = "long_array_type")
    public Long[] getIntegerArrayValue()
    {
        return integerArrayValue;
    }

    public void setIntegerArrayValue(final Long[] values)
    {
        this.integerArrayValue = values;
    }


    public void setRealArrayValue(final Double[] values)
    {
        this.realArrayValue = values;
    }

    @Column(name = ColumnNames.REAL_ARRAY_VALUE_COLUMN)
    @Type(type = "double_array_type")
    public Double[] getRealArrayValue()
    {
        return realArrayValue;
    }

    public void setTimestampArrayValue(final Date[] values)
    {
        this.timestampArrayValue = values;
    }

    @Column(name = ColumnNames.TIMESTAMP_ARRAY_VALUE_COLUMN)
    @Type(type = "timestamp_array_type")
    public Date[] getTimestampArrayValue()
    {
        return timestampArrayValue;
    }

    public void setStringArrayValue(final String[] values)
    {
        this.stringArrayValue = values;
    }

    @Column(name = ColumnNames.STRING_ARRAY_VALUE_COLUMN)
    @Type(type = "string_array_type")
    public String[] getStringArrayValue()
    {
        return stringArrayValue;
    }

    @Column(name = ColumnNames.JSON_VALUE_COLUMN)
    @Type(type = "json_string_type")
    public String getJsonValue()
    {
        return jsonValue;
    }

    public void setJsonValue(String jsonValue)
    {
        this.jsonValue = jsonValue;
    }

    //
    // IUntypedValueSetter
    //

    @Override
    public void setUntypedValue(final String valueOrNull,
            final VocabularyTermPE vocabularyTermOrNull, MaterialPE materialOrNull,
            SamplePE sampleOrNull, Long[] integerArrayOrNull, Double[] realArrayOrNull,
            String[] stringArrayOrNull, Date[] timestampArrayOrNull, String jsonOrNull)
    {
        assert valueOrNull != null || vocabularyTermOrNull != null || materialOrNull != null
                || integerArrayOrNull != null || realArrayOrNull != null
                || stringArrayOrNull != null || timestampArrayOrNull != null
                || jsonOrNull != null : "Either value, array value, json, vocabulary term or material should not be null.";
        clearValues();
        if (vocabularyTermOrNull != null)
        {
            assert materialOrNull == null;
            setVocabularyTerm(vocabularyTermOrNull);
        } else if (materialOrNull != null)
        {
            setMaterialValue(materialOrNull);
        }else if (integerArrayOrNull != null) {
            setIntegerArrayValue(integerArrayOrNull);
        } else if (realArrayOrNull != null) {
            setRealArrayValue(realArrayOrNull);
        }else if (stringArrayOrNull != null) {
            setStringArrayValue(stringArrayOrNull);
        }else if (timestampArrayOrNull != null) {
            setTimestampArrayValue(timestampArrayOrNull);
        }else if (jsonOrNull != null) {
            setJsonValue(jsonOrNull);
        } else
        {
            setValue(valueOrNull);
        }
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    @Type(type = "transaction_timestamp")
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_AUTHOR_COLUMN, nullable = false, updatable = true)
    public PersonPE getAuthor()
    {
        return author;
    }

    public void setAuthor(PersonPE author)
    {
        this.author = author;
    }

    /**
     * Sets the <var>entity</var> of this property.
     * <p>
     * <i>Do not use directly, instead, call
     * {@link IEntityPropertiesHolder#addProperty(EntityPropertyPE)} with <code>this</code>
     * object!</i>
     */
    void setEntity(final IEntityPropertiesHolder entity)
    {
        this.entity = entity;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("entityTypePropertyType", getEntityTypePropertyType());
        builder.append("value", tryGetUntypedValue());
        return builder.toString();
    }

    //
    // IEntityProperty
    //

    @Override
    public String tryGetUntypedValue()
    {
        if (getVocabularyTerm() != null)
        {
            final String labelOrNull = getVocabularyTerm().getLabel();
            return getVocabularyTerm().getCode()
                    + (labelOrNull != null ? " " + getVocabularyTerm().getLabel() : "");
        } else if (getMaterialValue() != null)
        {
            return createMaterialIdentifier(getMaterialValue()).print();
        } else
        {
            if (this.integerArrayValue != null)
            {
                return convertArrayToString(this.integerArrayValue);
            }
            if (getRealArrayValue() != null)
                return convertArrayToString(this.realArrayValue);
            if (getTimestampArrayValue() != null)
                return convertTimestampArrayToString(this.timestampArrayValue);
            if (getStringArrayValue() != null)
                return convertArrayToString(this.stringArrayValue);
            if (getJsonValue() != null)
                return getJsonValue();
            return getValue();
        }
    }

    private String convertTimestampArrayToString(Date[] array) {
        if (array == null || array.length == 0)
            return "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss XX");
        return Stream.of(array)
                .map(dateFormat::format)
                .reduce((x, y) -> x + ", " + y)
                .get();
    }

    private String convertArrayToString(Object[] array) {
        if (array == null || array.length == 0)
            return "";
        return Stream.of(array)
                .map(String::valueOf)
                .reduce((x, y) -> x + ", " + y)
                .get();
    }

    private static MaterialIdentifier createMaterialIdentifier(MaterialPE material)
    {
        return new MaterialIdentifier(material.getCode(), material.getMaterialType().getCode());
    }

    /**
     * Creates an {@link EntityPropertyPE} from given <var>entityKind</var>.
     */
    public final static <T extends EntityPropertyPE> T createEntityProperty(
            final EntityKind entityKind)
    {
        assert entityKind != null : "Unspecified entity kind";
        return ClassUtils.createInstance(entityKind.<T> getEntityPropertyClass());
    }

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getEntityTypePropertyType(), "etpt");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityPropertyPE == false)
        {
            return false;
        }
        final EntityPropertyPE that = (EntityPropertyPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getEntity(), that.getEntity());
        builder.append(getEntityTypePropertyType(), that.getEntityTypePropertyType());
        builder.append(tryGetUntypedValue(), that.tryGetUntypedValue());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getEntity());
        builder.append(getEntityTypePropertyType());
        builder.append(tryGetUntypedValue());
        return builder.toHashCode();
    }

}
