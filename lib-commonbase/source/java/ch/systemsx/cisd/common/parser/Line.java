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
package ch.systemsx.cisd.common.parser;

/**
 * A small object that represents a line in a <code>File</code> context.
 * 
 * @author Christian Ribeaud
 */
public final class Line implements ILine<String>
{
    private final String text;

    private final int number;

    public Line(final int number, final String text)
    {
        assert text != null : "Unspecified text.";
        this.number = number;
        this.text = text;
    }

    @Override
    public final String getText()
    {
        return text;
    }

    @Override
    public final int getNumber()
    {
        return number;
    }

    @Override
    public String getObject()
    {
        return text;
    }

}