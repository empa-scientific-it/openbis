/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author pkupczyk
 */
public class EmailUtil
{

    private static final String EMAIL_DIR = "targets/email";

    private static final String EMAIL_PATTERN = "Date: (.*)\nFrom: (.*)\nTo: (.*)\nSubject: (.*)\nContent:\n(.*)";

    public static Email findLatestEmail()
    {
        File emailDir = new File(EMAIL_DIR);

        if (emailDir.exists())
        {
            File[] emails = emailDir.listFiles();

            if (emails != null && emails.length > 0)
            {
                Arrays.sort(emails, new Comparator<File>()
                    {
                        @Override
                        public int compare(File f1, File f2)
                        {
                            return -f1.getName().compareTo(f2.getName());
                        }
                    });

                File latestEmail = emails[0];
                try
                {
                    String latestEmailContent = FileUtils.readFileToString(latestEmail);
                    Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.DOTALL);

                    Matcher m = pattern.matcher(latestEmailContent);
                    if (m.find())
                    {
                        Email email = new Email();
                        email.timestamp = latestEmail.lastModified();
                        email.from = m.group(2);
                        email.to = m.group(3);
                        email.subject = m.group(4);
                        email.content = m.group(5);
                        return email;
                    } else
                    {
                        throw new RuntimeException("Latest email content does not match the expected email pattern. The latest email content was:\n"
                                + latestEmailContent + "\nThe expected email pattern was:\n" + EMAIL_PATTERN);
                    }

                } catch (IOException e)
                {
                    throw new RuntimeException("Could not read the latest email " + latestEmail.getAbsolutePath(), e);
                }

            } else
            {
                return null;
            }
        } else
        {
            throw new RuntimeException("Email directory " + emailDir.getAbsolutePath() + " does not exist");
        }
    }

    public static class Email
    {

        public long timestamp;

        public String from;

        public String to;

        public String subject;

        public String content;

        @Override
        public String toString()
        {
            return new ReflectionToStringBuilder(this).toString();
        }
    }

}
