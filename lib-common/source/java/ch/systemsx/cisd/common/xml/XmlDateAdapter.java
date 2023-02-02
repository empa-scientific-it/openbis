/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.xml;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Allows to read dates from XML files using JAXB.
 * 
 * @author Tomasz Pylak
 */
public class XmlDateAdapter extends XmlAdapter<String, Date>
{
    public static final String DATE_PATTERN = "dd-MMM-yyyy HH:mm:ss";

    @Override
    public String marshal(Date date) throws Exception
    {
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    @Override
    public Date unmarshal(String dateString) throws Exception
    {
        return new SimpleDateFormat(DATE_PATTERN).parse(dateString);
    }

}
