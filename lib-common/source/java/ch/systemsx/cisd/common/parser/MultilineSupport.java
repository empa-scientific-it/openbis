package ch.systemsx.cisd.common.parser;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class MultilineSupport
{
    public static CSVFormat FORMAT = CSVFormat.DEFAULT.withHeader((String[]) null).withDelimiter('\t').withRecordSeparator('\n');

    private static abstract class ClosableMapIterator<FROM, TO> extends MapIterator<FROM, TO> implements AutoCloseable
    {
        public ClosableMapIterator(Iterator<FROM> iter)
        {
            super(iter);
        }
    }

    public static ClosableMapIterator<CSVRecord, ILine<String>> addTo(Iterator<ILine<String>> lineIterator) throws IOException
    {
        final CSVParser csvParser = new CSVParser(new ChainReader(new MapIterator<ILine<String>, CharArrayReader>(lineIterator)
            {
                @Override
                CharArrayReader convert(ILine<String> t)
                {
                    return new CharArrayReader((t.getText() + "\n").toCharArray());
                }

            }), FORMAT);

        return new ClosableMapIterator<CSVRecord, ILine<String>>(csvParser.iterator())
            {
                int row = 0;

                @Override
                ILine<String> convert(CSVRecord t)
                {
                    Iterator<String> iterator = t.iterator();
                    StringWriter sw = new StringWriter();
                    try (CSVPrinter printer =
                            new CSVPrinter(sw, FORMAT))
                    {
                        List<String> data = new ArrayList<String>();
                        while (iterator.hasNext())
                        {
                            data.add(iterator.next());
                        }
                        printer.printRecord(data);
                        row++;
                        return new Line(row, sw.getBuffer().toString());

                    } catch (IOException e)
                    {
                        throw new ParserException(e.getMessage(), e);
                    }
                }

                @Override
                public void close() throws Exception
                {
                    csvParser.close();
                }
            };
    }

    private static abstract class MapIterator<FROM, TO> implements Iterator<TO>
    {

        private Iterator<FROM> iter;

        abstract TO convert(FROM t);

        public MapIterator(Iterator<FROM> iter)
        {
            this.iter = iter;
        }

        @Override
        public boolean hasNext()
        {
            return iter.hasNext();
        }

        @Override
        public TO next()
        {
            return convert(iter.next());
        }

        @Override
        public void remove()
        {
            iter.remove();
        }

    }

    private static class ChainReader extends Reader
    {

        private Iterator<CharArrayReader> iter;

        private CharArrayReader current = null;

        public ChainReader(Iterator<CharArrayReader> iter)
        {
            this.iter = iter;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
            int readSoFar = 0;
            while (readSoFar < len)
            {
                if (current == null)
                {
                    if (iter.hasNext())
                    {
                        current = iter.next();
                    } else
                    {
                        break;
                    }
                }
                int amount = current.read(cbuf, off + readSoFar, len - readSoFar);
                if (amount == -1)
                {
                    current.close();
                    current = null;
                    continue;
                }
                readSoFar += amount;
            }

            if (readSoFar == 0)
            {
                return -1;
            }
            return readSoFar;
        }

        @Override
        public void close() throws IOException
        {
            if (current != null)
            {
                current.close();
            }
        }
    }
}
