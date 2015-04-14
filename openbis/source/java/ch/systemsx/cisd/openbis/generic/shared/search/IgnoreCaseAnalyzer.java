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

package ch.systemsx.cisd.openbis.generic.shared.search;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

/**
 * @author pkupczyk
 */
public final class IgnoreCaseAnalyzer extends Analyzer
{

    private static class LowerCaseTokenizer extends CharTokenizer
    {

        public LowerCaseTokenizer(Reader input)
        {
            super(Version.LUCENE_31, input);
        }

        @Override
        protected boolean isTokenChar(int c)
        {
            return true;
        }

        @Override
        protected int normalize(int c)
        {
            return Character.toLowerCase(c);
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader)
    {
        Tokenizer source = new LowerCaseTokenizer(reader);
        TokenStream filter = new LowerCaseFilter(source);
        return new TokenStreamComponents(source, filter);
    }
}
