package ch.ethz.sis.openbis.generic.dss.api.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.beans.HasPropertyWithValue;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo.PathInfoFeeder;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;

public class PathInfoFeederTest
{

    public static final long DS_ID = 1l;

    @Test
    public void expansionCreatesRightAmountOfEntries()
    {
        Collection<PathEntryDTO> result = runWith(file().withPath("path/to/the/file/itself.txt"),
                directory().withPath("path/to/a/directory"));
        assertThat(result.size(), is(8)); // one entry for root directory
    }

    @Test
    public void expansionCreatesCorrectPaths()
    {
        Collection<PathEntryDTO> result = runWith(file().withPath("path/to/the/file/itself.txt"));
        Set<String> paths = new HashSet<>();
        for (PathEntryDTO entry : result)
        {
            paths.add(entry.getRelativePath());
        }

        Collection<String> expectedPaths = Arrays.asList("path", "path/to", "path/to/the", "path/to/the/file", "path/to/the/file/itself.txt");
        assertThat(paths.containsAll(expectedPaths), is(true));
    }

    @Test
    public void checksumTranslatedCorrectly()
    {
        Collection<PathEntryDTO> result = runWith(file().withChecksum(159));
        assertThat(fileFrom(result).getChecksumCRC32(), is(159));
    }

    @Test
    public void fileSizeTranslatedCorrectly()
    {
        Collection<PathEntryDTO> result = runWith(file().withFileLength(159l));
        assertThat(fileFrom(result).getSizeInBytes(), is(159l));
    }

    @Test
    public void pathTranslatedCorrectly()
    {
        Collection<PathEntryDTO> result = runWith(file().withPath("path/to/file.txt"));
        assertThat(fileFrom(result, "path/to/file.txt").getRelativePath(), is("path/to/file.txt"));
    }

    @Test
    public void fileNameTranslatedCorrectly()
    {
        Collection<PathEntryDTO> result = runWith(file().withPath("path/to/file.txt"));
        assertThat(fileFrom(result, "path/to/file.txt").getFileName(), is("file.txt"));
    }

    @Test
    public void dataSetIdTranslatedCorrectly()
    {
        Collection<PathEntryDTO> result = runWith(file());
        assertThat(fileFrom(result).getDataSetId(), is(DS_ID));
    }

    @Test
    public void modificationTimesSetCorrectly()
    {
        Date start = new Date();
        Iterable<PathEntryDTO> result = runWith(file().withPath("path/to/file.txt"), directory().withPath("another/path/to/directory"));
        for (PathEntryDTO pathEntryDTO : result)
        {
            System.out.println("RESULT:"+pathEntryDTO);
        }
        Date end = new Date();

        Date date = result.iterator().next().getLastModifiedDate();

        assertThat(date, is(greaterThanOrEqualTo(start)));
        assertThat(date, is(lessThanOrEqualTo(end)));
        assertThat(result, everyItem(HasPropertyWithValue.<PathEntryDTO> hasProperty("lastModifiedDate", is(date))));
    }

    @Test
    public void allIdsAreUnique()
    {
        Collection<PathEntryDTO> result = runWith(file().withPath("very/long/and/tough/path/that/eventually/leads/to/a/file.txt"));
        Set<Long> ids = new HashSet<>();
        for (PathEntryDTO entry : result)
        {
            ids.add(entry.getId());
        }
        assertThat(ids.size(), is(result.size()));
    }

    @Test
    public void parentIdsAreAssignedProperly()
    {
        Collection<PathEntryDTO> result = runWith(file().withPath("path/to/file.txt"));
        Long lowParentId = fileFrom(result, "path").getParentId();
        long lowId = fileFrom(result, "path").getId();
        Long middleParentId = fileFrom(result, "path/to").getParentId();
        long middleId = fileFrom(result, "path/to").getId();
        Long highParentId = fileFrom(result, "path/to/file.txt").getParentId();

        assertThat(lowParentId, is(not(nullValue())));
        assertThat(middleParentId, is(lowId));
        assertThat(highParentId, is(middleId));
    }

    @Test
    public void sizesAreCalculatedCorrectly()
    {
        Collection<PathEntryDTO> result = runWith(
                file().withPath("directory/file1.txt").withFileLength(10l),
                file().withPath("directory/file2.txt").withFileLength(20l));
        assertThat(fileFrom(result, "directory").getSizeInBytes(), is(30L));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void multipleFilesWithSameNameNotAccepted()
    {
        runWith(file().withPath("directory/file1.txt"), file().withPath("directory/file1.txt"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void absolutePathsAreNotAcceptedForFiles()
    {
        runWith(file().withPath("/absolute/path/to/file.txt"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyPathIsNotAcceptedForFiles()
    {
        runWith(file().withPath(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void pathIsRequiredForFiles()
    {
        runWith(file().withPath(null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void sizeIsRequiredForFiles()
    {
        runWith(file().withFileLength(null));
    }

    @Test
    public void checksumIsNotRequiredForFiles()
    {
        runWith(file().withChecksum((Integer) null));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void withChecksumButWithoutChecksumType()
    {
        runWith(file().withChecksum("caffee1234"));
    }

    @Test
    public void withChecksum()
    {
        Collection<PathEntryDTO> entries = runWith(file().withChecksum("caffee1234").withChecksumType("TEST"));
        assertThat(fileFrom(entries, "path/to/file.txt").getChecksum(), is("TEST:caffee1234"));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void absolutePathsAreNotAcceptedForDirectories()
    {
        runWith(directory().withPath("/absolute/path/to/file"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyPathIsNotAcceptedForDirectories()
    {
        runWith(directory().withPath(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void pathIsRequiredForDirectories()
    {
        runWith(directory().withPath(null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAssignChecksumToDirectory()
    {
        runWith(directory().withChecksum(0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAssignFileSizeToDirectory()
    {
        runWith(directory().withFileLength(0l));
    }

    private static PathEntryDTO fileFrom(Collection<PathEntryDTO> entries, String path)
    {
        for (PathEntryDTO entry : entries)
        {
            if (entry.getRelativePath().equals(path))
            {
                return entry;
            }
        }
        return null;
    }

    private static PathEntryDTO fileFrom(Collection<PathEntryDTO> entries)
    {
        return fileFrom(entries, "path/to/file.txt");
    }

    private DataSetFileCreationBuilder file()
    {
        return new DataSetFileCreationBuilder(false).withPath("path/to/file.txt");
    }

    private DataSetFileCreationBuilder directory()
    {
        return new DataSetFileCreationBuilder(true);
    }

    private static Collection<PathEntryDTO> runWith(DataSetFileCreationBuilder... files)
    {
        Set<DataSetFileCreation> input = new HashSet<>();
        for (DataSetFileCreationBuilder file : files)
        {
            input.add(file.build());
        }
        PathInfoFeeder creator = new PathInfoFeeder(1, "my-data-set", input);

        Mockery context = new Mockery();
        final IPathsInfoDAO mockDao = context.mock(IPathsInfoDAO.class);
        final Collection<PathEntryDTO> result = new ArrayList<PathEntryDTO>();
        context.checking(new Expectations()
            {
                {

                    Action action = new CustomAction("")
                        {
                            long counter = 1;

                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                PathEntryDTO entry = new PathEntryDTO();
                                entry.setId(counter);
                                entry.setDataSetId((long) invocation.getParameter(0));
                                if (invocation.getParameter(1) != null)
                                {
                                    entry.setParentId((long) invocation.getParameter(1));
                                }
                                entry.setRelativePath((String) invocation.getParameter(2));
                                entry.setFileName((String) invocation.getParameter(3));
                                entry.setSizeInBytes((long) invocation.getParameter(4));
                                entry.setDirectory((boolean) invocation.getParameter(5));
                                if (invocation.getParameter(6) != null)
                                {
                                    entry.setChecksumCRC32((int) invocation.getParameter(6));
                                }
                                entry.setChecksum((String) invocation.getParameter(7));
                                entry.setLastModifiedDate((Date) invocation.getParameter(8));

                                result.add(entry);
                                return counter++;
                            }

                        };

                    allowing(mockDao).createDataSetFile(with(any(long.class)), with(any(Long.class)), with(any(String.class)),
                            with(any(String.class)), with(any(long.class)), with(any(boolean.class)), with(any(Integer.class)), with(any(String.class)), 
                            with(any(Date.class)));
                    will(action);

                }
            });

        creator.storeFilesWith(mockDao);
        return result;
    }
}
