/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * An {@link IQueuePersister} that is based on records in a file. This class uses Java serialization on the queue elements and thus requires queue
 * elements to be serializable.
 * 
 * @author Pawel Glyzewski
 */
public class QueuePersister<E extends Serializable> implements IQueuePersister<E>
{
    private static final int MAX_RETRIES_ON_FAILURE = 3;

    private static final long MILLIS_TO_SLEEP_ON_FAILURE = 3000L;

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            QueuePersister.class);

    final static int QUEUE_IMPLEMENTATION_MARKER = -123456789;

    private final static int HEADER_LENGTH = 3 * 4; // 3 * sizeof(int)

    private final static int RECORD_HEADER_LENGTH = 4; // sizeof(int)

    private final static int MAX_SLICK = 100000;

    private final Queue<E> queue;

    private final File queueFile;

    private final File newQueueFile;

    private final boolean autoSync;

    private RandomAccessFile randomAccessFile;

    private int firstRecord = HEADER_LENGTH;

    private int lastRecord;

    /**
     * Returns a list of the content of the <var>queueFile</var>.
     */
    public static <E> List<E> list(Class<E> clazz, File queueFile)
    {
        final File newQueueFile = new File(queueFile.getParentFile(), queueFile.getName() + ".new");
        List<E> result = new ArrayList<E>();
        RandomAccessFile randomAccessFile = null;
        try
        {
            if (queueFile.exists() == false && newQueueFile.exists())
            {
                randomAccessFile = new RandomAccessFile(newQueueFile, "r");
            } else
            {
                randomAccessFile = new RandomAccessFile(queueFile, "r");
            }
            if (randomAccessFile.readInt() != QUEUE_IMPLEMENTATION_MARKER)
            {
                return LegacyQueuePersister.list(clazz, queueFile);
            } else
            {
                final int firstRecord = randomAccessFile.readInt();
                final int lastRecord = randomAccessFile.readInt();
                load(randomAccessFile, result, firstRecord, lastRecord);
            }
        } catch (IOException ex)
        {
            return Collections.emptyList();
        } finally
        {
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException ex)
                {
                    // Silence
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public QueuePersister(Queue<E> queue, File queueFile)
    {
        this(queue, queueFile, false);
    }

    /**
     * Create a {@link QueuePersister} for <var>queue</var>.
     * 
     * @param queue The queue to persist.
     * @param queueFile The file to persist the queue in.
     * @param autoSync If <code>true</code>, the underlying file will be synchronized after each write operation. This is safer, but costs a lot of
     *            performance.
     */
    public QueuePersister(Queue<E> queue, File queueFile, boolean autoSync)
    {
        this.queue = queue;
        this.queueFile = queueFile;
        this.newQueueFile = new File(queueFile.getParentFile(), queueFile.getName() + ".new");
        this.autoSync = autoSync;
        if (queueFile.exists() == false && newQueueFile.exists())
        {
            if (newQueueFile.renameTo(queueFile) == false)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(new IOException("Cannot rename file '"
                        + newQueueFile.getPath() + "' to '" + queueFile.getPath() + "'"));
            }
        }
        open();
    }

    private void open() throws IOExceptionUnchecked
    {
        synchronized (queueFile)
        {
            for (int i = 0; true /* See EXIT LOOP below */; ++i)
            {
                try
                {
                    this.randomAccessFile = new RandomAccessFile(queueFile, "rw");
                    if (randomAccessFile.length() >= 4
                            && randomAccessFile.readInt() != QUEUE_IMPLEMENTATION_MARKER)
                    {
                        LegacyQueuePersister<E> oldPersister =
                                new LegacyQueuePersister<E>(queue, queueFile);
                        oldPersister.close();
                        persist();
                    } else
                    {

                        if (randomAccessFile.length() < HEADER_LENGTH)
                        {
                            writeFullHeader(randomAccessFile, firstRecord, lastRecord);
                        } else
                        {
                            this.firstRecord = randomAccessFile.readInt();
                            if (this.firstRecord < 0)
                            {
                                this.firstRecord = HEADER_LENGTH;
                            }
                            this.lastRecord = randomAccessFile.readInt();
                            if (this.lastRecord < 0)
                            {
                                this.lastRecord = 0;
                            }
                        }
                        boolean complete = load(randomAccessFile, queue, firstRecord, lastRecord);
                        if (complete == false)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
                            try
                            {
                                FileUtils.copyFile(queueFile, new File(queueFile.getAbsolutePath() + "." + sdf.format(new Date()) + ".broken"));
                            } catch (IOException e)
                            {
                                System.out.println("No space left on device - service can't start");
                                System.exit(-1);
                            }
                        }

                        // Clean up
                        if (firstRecord > 0)
                        {
                            persist();
                        }
                    }
                    // EXIT LOOP: break on successful execution
                    break;
                } catch (IOException ex)
                {
                    operationLog.error(String.format("Error opening queue file '%s', position %d, "
                            + "trying to re-open.", queueFile.getPath(), lastRecord), ex);
                    closeQuietly();
                    if (i == MAX_RETRIES_ON_FAILURE)
                    {
                        // EXIT LOOP: throw exception on unsuccessful execution
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                    ConcurrencyUtilities.sleep(MILLIS_TO_SLEEP_ON_FAILURE);
                }
            }
        }
    }

    private byte[] toByteArray(E o)
    {
        final ByteArrayOutputStream ba = new ByteArrayOutputStream();
        try
        {
            final ObjectOutputStream oo = new ObjectOutputStream(ba);
            oo.writeObject(o);
            oo.close();
            return ba.toByteArray();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static Object objFromByteArray(byte[] data)
    {
        final ByteArrayInputStream bi = new ByteArrayInputStream(data);
        try
        {
            final ObjectInputStream oi = new ObjectInputStream(bi);
            return oi.readObject();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void writeHeader() throws IOException
    {
        writeFullHeader(randomAccessFile, firstRecord, lastRecord);
    }

    private static void writeFullHeader(RandomAccessFile raf, int firstRecord, int lastRecord)
            throws IOException
    {
        raf.seek(0L);
        raf.writeInt(QUEUE_IMPLEMENTATION_MARKER);
        raf.writeInt(firstRecord);
        raf.writeInt(lastRecord);
    }

    private static <E> boolean load(RandomAccessFile randomAccessFile, Collection<E> collection,
            int firstRecord, int lastRecord) throws IOException
    {
        long pos = firstRecord;
        while (pos < lastRecord)
        {
            randomAccessFile.seek(pos);
            try
            {
                final int len = randomAccessFile.readInt();
                pos += len + RECORD_HEADER_LENGTH;
                final byte[] data = new byte[len];
                randomAccessFile.read(data, 0, len);
                deserializeAndAdd(collection, data);
            } catch (EOFException e)
            {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <E> void deserializeAndAdd(Collection<E> collection, final byte[] data)
    {
        collection.add((E) objFromByteArray(data));
    }

    //
    // IQueuePersister
    //
    @Override
    public void persist()
    {
        synchronized (queueFile)
        {
            try
            {
                closeQuietly();
                fillNewQueueFile();
                if (queueFile.delete() == false)
                {
                    throw new IOException("Cannot delete file '" + queueFile.getPath() + "'");
                }
                if (newQueueFile.renameTo(queueFile) == false)
                {
                    throw new IOException("Cannot rename file '" + newQueueFile.getPath()
                            + "' to '" + queueFile.getPath() + "'");
                }
                randomAccessFile = new RandomAccessFile(queueFile, "rw");
                if (autoSync)
                {
                    sync();
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private void fillNewQueueFile() throws IOException
    {
        RandomAccessFile newRandomAccessFile = null;
        try
        {
            newRandomAccessFile = new RandomAccessFile(newQueueFile, "rw");
            firstRecord = HEADER_LENGTH;
            lastRecord = -1;
            writeFullHeader(newRandomAccessFile, firstRecord, lastRecord);
            int pos = HEADER_LENGTH;
            int elementSize = 0;
            for (E elem : queue)
            {
                pos += elementSize;
                newRandomAccessFile.seek(pos);
                final byte[] data = toByteArray(elem);
                elementSize = data.length + RECORD_HEADER_LENGTH;
                newRandomAccessFile.writeInt(data.length);
                newRandomAccessFile.write(data);
            }
            lastRecord = pos + elementSize;
            writeFullHeader(newRandomAccessFile, firstRecord, lastRecord);
        } finally
        {
            if (newRandomAccessFile != null)
            {
                newRandomAccessFile.close();
            }
        }
    }

    @Override
    public void addToTail(E elem)
    {
        synchronized (queueFile)
        {
            for (int i = 0; true /* See EXIT LOOP below */; ++i)
            {
                try
                {
                    randomAccessFile.seek(lastRecord);
                    final byte[] data = toByteArray(elem);
                    final int elementSize = data.length + RECORD_HEADER_LENGTH;
                    randomAccessFile.writeInt(data.length);
                    randomAccessFile.write(data);
                    lastRecord += elementSize;
                    writeHeader();
                    if (autoSync)
                    {
                        sync();
                    }
                    // EXIT LOOP: break on successful execution
                    break;
                } catch (IOException ex)
                {
                    operationLog.error(String.format(
                            "Error adding to tail of queue file '%s', position %d, "
                                    + "trying to re-open.",
                            queueFile.getPath(), lastRecord), ex);
                    closeQuietly();
                    if (i == MAX_RETRIES_ON_FAILURE)
                    {
                        // EXIT LOOP: throw exception on unsuccessful execution
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                    ConcurrencyUtilities.sleep(MILLIS_TO_SLEEP_ON_FAILURE);
                    open();
                }
            }
        }
    }

    private void closeQuietly()
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.close();
            } catch (IOException ex)
            {
                operationLog.error(String.format("Error on closing file '%s'", queueFile));
            }
        }
    }

    @Override
    public void removeFromHead(E elem) throws IOExceptionUnchecked
    {
        synchronized (queueFile)
        {
            for (int i = 0; true /* See EXIT LOOP below */; ++i)
            {
                try
                {
                    if (firstRecord > MAX_SLICK)
                    {
                        persist();
                    } else
                    {
                        randomAccessFile.seek(firstRecord);
                        firstRecord += randomAccessFile.readInt() + RECORD_HEADER_LENGTH;
                        writeHeader();
                        if (autoSync)
                        {
                            sync();
                        }
                    }
                    // EXIT LOOP: break on successful execution
                    break;
                } catch (IOException ex)
                {
                    operationLog.error(String.format(
                            "Error removing from head of queue file '%s', position %d, "
                                    + "trying to re-open.",
                            queueFile.getPath(), lastRecord), ex);
                    closeQuietly();
                    if (i == MAX_RETRIES_ON_FAILURE)
                    {
                        // EXIT LOOP: throw exception on unsuccessful execution
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                    ConcurrencyUtilities.sleep(MILLIS_TO_SLEEP_ON_FAILURE);
                    open();
                }
            }
        }
    }

    @Override
    public void check() throws IllegalStateException
    {
        synchronized (queueFile)
        {
            try
            {
                if ((false == randomAccessFile.getFD().valid())
                        || (false == randomAccessFile.getChannel().isOpen()))
                {
                    this.randomAccessFile = new RandomAccessFile(queueFile, "rw");
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public void close()
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.close();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public void sync()
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.getFD().sync();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    static class LegacyQueuePersister<E> implements IQueuePersister<E>
    {
        final static int DEFAULT_INITIAL_RECORD_SIZE = 32;

        private final static int LEGACY_HEADER_LENGTH = 3 * 4; // 3 * sizeof(int)

        private final static int LEGACY_RECORD_HEADER_LENGTH = 4; // sizeof(int)

        private final static int LEGACY_MAX_SLICK = 1000;

        private final Queue<E> queue;

        private final File queueFile;

        private final File newQueueFile;

        private final boolean autoSync;

        private int recordSize;

        private RandomAccessFile randomAccessFile;

        private int firstRecord;

        private int lastRecord;

        /**
         * Returns a list of the content of the <var>queueFile</var>.
         */
        static <E> List<E> list(Class<E> clazz, File queueFile)
        {
            final File newQueueFile =
                    new File(queueFile.getParentFile(), queueFile.getName() + ".new");
            final List<E> result = new ArrayList<E>();
            RandomAccessFile randomAccessFile = null;
            try
            {
                if (queueFile.exists() == false && newQueueFile.exists())
                {
                    randomAccessFile = new RandomAccessFile(newQueueFile, "r");
                } else
                {
                    randomAccessFile = new RandomAccessFile(queueFile, "r");
                }
                final int firstRecord = randomAccessFile.readInt();
                final int lastRecord = randomAccessFile.readInt();
                final int recordSize = randomAccessFile.readInt();
                load(randomAccessFile, result, firstRecord, lastRecord, recordSize);
            } catch (IOException ex)
            {
                return Collections.emptyList();
            } finally
            {
                if (randomAccessFile != null)
                {
                    try
                    {
                        randomAccessFile.close();
                    } catch (IOException ex)
                    {
                        // Silence
                    }
                }
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Create a {@link QueuePersister.LegacyQueuePersister} for <var>queue</var>. (Uses a default initial record size of 32 and switches off
         * auto-sync.)
         * 
         * @param queue The queue to persist.
         * @param queueFile The file to persist the queue in.
         */
        LegacyQueuePersister(Queue<E> queue, File queueFile)
        {
            this(queue, queueFile, DEFAULT_INITIAL_RECORD_SIZE, false);
        }

        /**
         * Create a {@link QueuePersister.LegacyQueuePersister} for <var>queue</var>.
         * 
         * @param queue The queue to persist.
         * @param queueFile The file to persist the queue in.
         * @param initialRecordSize The initial size of the record. If an element of the queue is larger than this, the whole queue file has to be
         *            re-written with a larger record size.
         * @param autoSync If <code>true</code>, the underlying file will be synchronized after each write operation. This is safer, but costs a lot
         *            of performance.
         */
        LegacyQueuePersister(Queue<E> queue, File queueFile, int initialRecordSize, boolean autoSync)
        {
            this.queue = queue;
            this.queueFile = queueFile;
            this.newQueueFile = new File(queueFile.getParentFile(), queueFile.getName() + ".new");
            this.autoSync = autoSync;
            if (queueFile.exists() == false && newQueueFile.exists())
            {
                if (newQueueFile.renameTo(queueFile) == false)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(new IOException(
                            "Cannot rename file '" + newQueueFile.getPath() + "' to '"
                                    + queueFile.getPath() + "'"));
                }
            }
            try
            {
                this.randomAccessFile = new RandomAccessFile(queueFile, "rw");
                if (randomAccessFile.length() < LEGACY_HEADER_LENGTH)
                {
                    this.recordSize = initialRecordSize;
                    writeFullHeader(randomAccessFile, firstRecord, lastRecord, initialRecordSize);
                } else
                {
                    this.firstRecord = randomAccessFile.readInt();
                    if (this.firstRecord < 0)
                    {
                        this.firstRecord = 0;
                    }
                    this.lastRecord = randomAccessFile.readInt();
                    if (this.lastRecord < 0)
                    {
                        this.lastRecord = 0;
                    }
                    this.recordSize = randomAccessFile.readInt();
                    if (this.recordSize < 0)
                    {
                        this.recordSize = 0;
                    }
                }
                load(randomAccessFile, queue, firstRecord, lastRecord, recordSize);
                // Clean up
                if (firstRecord > 0)
                {
                    persist();
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        private byte[] toByteArray(E o)
        {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream();
            try
            {
                final ObjectOutputStream oo = new ObjectOutputStream(ba);
                oo.writeObject(o);
                oo.close();
                return ba.toByteArray();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        private static Object objFromByteArray(byte[] data)
        {
            final ByteArrayInputStream bi = new ByteArrayInputStream(data);
            try
            {
                final ObjectInputStream oi = new ObjectInputStream(bi);
                return oi.readObject();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        private void writeHeader() throws IOException
        {
            randomAccessFile.seek(0L);
            randomAccessFile.writeInt(firstRecord);
            randomAccessFile.writeInt(lastRecord);
        }

        private static void writeFullHeader(RandomAccessFile raf, int firstRecord, int lastRecord,
                int initialRecordSize) throws IOException
        {
            raf.seek(0L);
            raf.writeInt(firstRecord);
            raf.writeInt(lastRecord);
            raf.writeInt(initialRecordSize);
        }

        private static <E> void load(RandomAccessFile randomAccessFile, Collection<E> collection,
                int firstRecord, int lastRecord, int recordSize)
        {
            long pos = LEGACY_HEADER_LENGTH + ((long) recordSize) * firstRecord;
            for (int i = firstRecord; i < lastRecord; ++i)
            {
                try
                {
                    randomAccessFile.seek(pos);
                    pos += recordSize;
                    final int len = randomAccessFile.readInt();
                    final byte[] data = new byte[len];
                    randomAccessFile.read(data, 0, len);
                    deserializeAndAdd(collection, data);
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static <E> void deserializeAndAdd(Collection<E> collection, final byte[] data)
        {
            collection.add((E) objFromByteArray(data));
        }

        private static int getNewRecordSize(int oldRecordSize, int elementSize)
        {
            return (oldRecordSize < 1) ? elementSize
                    : oldRecordSize
                            * (elementSize / oldRecordSize + 1);
        }

        //
        // IQueuePersister
        //

        @Override
        public void persist()
        {
            primPersist(recordSize);
        }

        private void primPersist(int newRecordSize)
        {
            synchronized (queueFile)
            {
                try
                {
                    randomAccessFile.close();
                    recordSize = fillNewQueueFile(newRecordSize);
                    if (queueFile.delete() == false)
                    {
                        throw new IOException("Cannot delete file '" + queueFile.getPath() + "'");
                    }
                    if (newQueueFile.renameTo(queueFile) == false)
                    {
                        throw new IOException("Cannot rename file '" + newQueueFile.getPath()
                                + "' to '" + queueFile.getPath() + "'");
                    }
                    randomAccessFile = new RandomAccessFile(queueFile, "rw");
                    if (autoSync)
                    {
                        sync();
                    }
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

        private int fillNewQueueFile(int newRecordSize) throws IOException
        {
            RandomAccessFile newRandomAccessFile = null;
            try
            {
                newRandomAccessFile = new RandomAccessFile(newQueueFile, "rw");
                firstRecord = 0;
                lastRecord = queue.size();
                writeFullHeader(newRandomAccessFile, firstRecord, lastRecord, newRecordSize);
                long pos = LEGACY_HEADER_LENGTH;
                for (E elem : queue)
                {
                    newRandomAccessFile.seek(pos);
                    pos += newRecordSize;
                    final byte[] data = toByteArray(elem);
                    final int elementSize = data.length + LEGACY_RECORD_HEADER_LENGTH;
                    if (elementSize > newRecordSize)
                    {
                        newRandomAccessFile.close();
                        return fillNewQueueFile(getNewRecordSize(newRecordSize, elementSize));
                    }
                    newRandomAccessFile.writeInt(data.length);
                    newRandomAccessFile.write(data);
                }
                return newRecordSize;
            } finally
            {
                if (newRandomAccessFile != null)
                {
                    newRandomAccessFile.close();
                }
            }
        }

        @Override
        public void addToTail(E elem)
        {
            synchronized (queueFile)
            {
                try
                {
                    long pos = LEGACY_HEADER_LENGTH + ((long) lastRecord) * recordSize;
                    randomAccessFile.seek(pos);
                    final byte[] data = toByteArray(elem);
                    final int elementSize = data.length + LEGACY_RECORD_HEADER_LENGTH;
                    if (elementSize > recordSize)
                    {
                        primPersist(getNewRecordSize(recordSize, elementSize));
                        return;
                    }
                    randomAccessFile.writeInt(data.length);
                    randomAccessFile.write(data);
                    ++lastRecord;
                    writeHeader();
                    if (autoSync)
                    {
                        sync();
                    }
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

        @Override
        public void removeFromHead(E elem)
        {
            synchronized (queueFile)
            {
                try
                {
                    if (firstRecord > LEGACY_MAX_SLICK)
                    {
                        persist();
                    } else
                    {
                        ++firstRecord;
                        writeHeader();
                        if (autoSync)
                        {
                            sync();
                        }
                    }
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

        @Override
        public void check() throws IllegalStateException
        {
            synchronized (queueFile)
            {
                try
                {
                    if (randomAccessFile.getFD().valid() == false)
                    {
                        throw new IllegalStateException("Cannot persist: file is closed.");
                    }
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

        @Override
        public void close()
        {
            synchronized (queueFile)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

        @Override
        public void sync()
        {
            synchronized (queueFile)
            {
                try
                {
                    randomAccessFile.getFD().sync();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

    }
}
