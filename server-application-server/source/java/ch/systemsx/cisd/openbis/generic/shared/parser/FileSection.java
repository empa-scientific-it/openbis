package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.string.UnicodeUtils;

public class FileSection
{
    private static final String SECTION_FILE_DEFAULT = "DEFAULT";

    private final String contentOrNull;

    private final InputStream contentStreamOrNull;

    private final String sectionName;

    // assumption that the given string is encoded using Unicode
    public static FileSection createFromString(String content, String sectionName)
    {
        return new FileSection(content, sectionName, null);
    }

    public static FileSection createFromInputStream(InputStream contentStream, String sectionName)
    {
        return new FileSection(null, sectionName, contentStream);
    }

    private FileSection(String contentOrNull, String sectionName, InputStream contentStreamOrNull)
    {
        assert (contentOrNull != null && contentStreamOrNull == null)
                || (contentOrNull == null && contentStreamOrNull != null);
        this.sectionName = sectionName;
        this.contentOrNull = contentOrNull;
        this.contentStreamOrNull = contentStreamOrNull;
    }

    public Reader getContentReader()
    {
        if (contentOrNull != null)
        {
            return new StringReader(contentOrNull);
        } else
        {
            return UnicodeUtils.createReader(contentStreamOrNull);
        }
    }

    public String getSectionName()
    {
        return sectionName;
    }

    public static List<FileSection> extractSections(Reader reader)
    {
        List<FileSection> sections = new ArrayList<FileSection>();
        try
        {
            LineIterator it = IOUtils.lineIterator(reader);
            StringBuilder sb = null;
            String sectionName = null;
            while (it.hasNext())
            {
                String line = it.nextLine();
                String newSectionName = tryGetSectionName(line);
                if (newSectionName != null)
                {
                    if (sectionName != null && sb != null)
                    {
                        if (sectionName.equals(newSectionName))
                        {
                            continue;
                        }
                        if (newSectionName.equals(SECTION_FILE_DEFAULT))
                        {
                            writeLine(sb, line);
                        } else
                        {
                            sections.add(FileSection.createFromString(sb.toString(), sectionName));
                            sectionName = newSectionName;
                            sb = new StringBuilder();
                        }
                    } else
                    {
                        sectionName = newSectionName;
                        sb = new StringBuilder();
                    }
                } else if (sectionName == null || sb == null)
                {
                    throw new UserFailureException("Discovered the unnamed section in the file");
                } else
                {
                    writeLine(sb, line);
                }
                if (it.hasNext() == false)
                {
                    sections.add(FileSection.createFromString(sb.toString(), sectionName));
                }
            }
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
        return sections;
    }

    private static String tryGetSectionName(String line)
    {
        final String beginSection = "[";
        final String endSection = "]";
        if (line == null)
        {
            return null;
        }
        String trimmedLine = line.trim();

        if (trimmedLine.startsWith(beginSection) && trimmedLine.endsWith(endSection))
        {
            return trimmedLine.substring(1, trimmedLine.length() - 1);
        } else
        {
            return null;
        }
    }

    private static void writeLine(StringBuilder sb, String line)
    {
        if (sb.length() != 0)
        {
            sb.append("\n");
        }
        sb.append(line);
    }
}
