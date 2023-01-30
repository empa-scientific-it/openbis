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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateVocabularyGui;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class VocabularyBuilder implements Builder<Vocabulary>
{

    private String code;

    private String description;

    private Set<String> terms;

    private String url;

    public VocabularyBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
        this.description = "";
        this.terms = new HashSet<String>();
        this.terms.add("term1");
        this.url = "http://test.com/${term}";
    }

    public VocabularyBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public VocabularyBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public VocabularyBuilder withTerms(String... content)
    {
        this.terms = new HashSet<String>();
        for (String term : content)
        {
            this.terms.add(term);
        }
        return this;
    }

    @Override
    public Vocabulary build(Application openbis, Ui ui)
    {
        Vocabulary vocabulary = new VocabularyDsl(code, description, terms, url);
        return openbis.execute(new CreateVocabularyGui(vocabulary));
    }
}
