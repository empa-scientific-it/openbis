package ch.systemsx.cisd.common.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ApacheLineTokenizer implements ILineTokenizer<String>
{
    @Override
    public void init()
    {
    }

    @Override
    public String[] tokenize(String line) throws ParserException
    {
        try
        {
            CSVParser parser = CSVParser.parse(line, MultilineSupport.FORMAT);
            List<CSVRecord> records = parser.getRecords();
            if (records.size() != 1)
            {
                throw new ParserException("Found wrong number of records on line " + line + ": " + parser.getRecords().size());
            }

            CSVRecord record = records.get(0);
            Iterator<String> iter = record.iterator();
            List<String> items = new ArrayList<String>();
            while (iter.hasNext())
                items.add(iter.next());

            return items.toArray(new String[0]);
        } catch (IOException e)
        {
            throw new ParserException("Failed to parse string " + line, e);
        }
    }

    @Override
    public void destroy()
    {
    }

}
