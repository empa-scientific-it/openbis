/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Abstract super class of all persistent entities for property history.
 * 
 * @author Franz-Josef Elmer
 */
@MappedSuperclass
public abstract class AbstractEntityPropertyHistoryPE implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String value;

    private String vocabularyTerm;

    private String material;

    private Date validFromDate;

    private Date validUntilDate;

    private PersonPE author;

    protected IIdHolder entity;

    protected EntityTypePropertyTypePE entityTypePropertyType;

    @Id
    public Long getId()
    {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.VALUE_COLUMN)
    public String getValue()
    {
        return value;
    }

    @SuppressWarnings("unused")
    private void setValue(String value)
    {
        this.value = value;
    }

    @Column(name = ColumnNames.VOCABULARY_TERM_IDENTIFIER_COLUMN)
    public String getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    @SuppressWarnings("unused")
    private void setVocabularyTerm(String vocabularyTerm)
    {
        this.vocabularyTerm = vocabularyTerm;
    }

    @Column(name = ColumnNames.MATERIAL_IDENTIFIER_COLUMN)
    public String getMaterial()
    {
        return material;
    }

    @SuppressWarnings("unused")
    private void setMaterial(String material)
    {
        this.material = material;
    }

    @Column(name = ColumnNames.VALID_FROM_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    public Date getValidFromDate()
    {
        return validFromDate;
    }

    @SuppressWarnings("unused")
    private void setValidFromDate(Date validFromDate)
    {
        this.validFromDate = validFromDate;
    }

    @Column(name = ColumnNames.VALID_UNTIL_TIMESTAMP_COLUMN, nullable = true, insertable = false, updatable = false)
    public Date getValidUntilDate()
    {
        return validUntilDate;
    }

    @SuppressWarnings("unused")
    private void setValidUntilDate(Date validUntilDate)
    {
        this.validUntilDate = validUntilDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_AUTHOR_COLUMN, nullable = true, updatable = true)
    public PersonPE getAuthor()
    {
        return author;
    }

    @SuppressWarnings("unused")
    private void setAuthor(PersonPE author)
    {
        this.author = author;
    }

    @Transient
    public IIdHolder getEntity()
    {
        return entity;
    }

    @Transient
    public EntityTypePropertyTypePE getEntityTypePropertyType()
    {
        return entityTypePropertyType;
    }

}
