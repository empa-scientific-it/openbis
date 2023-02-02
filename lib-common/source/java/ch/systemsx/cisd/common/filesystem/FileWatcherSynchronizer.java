/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.WeakHashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Kind of <code>FileWatcher</code> extension that allow registration of multiple <code>ChangeListener</code> for one file watched.
 * <p>
 * You can use this class as <i>singleton</i> calling {@link #getInstance()} or you may instance it with a constructor.<br />
 * Note that access to this class is <i>synchronized</i> and that this class internally uses a {@link WeakHashMap} to store the registered
 * <code>ChangeListener</code>s.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class FileWatcherSynchronizer extends TimerTask
{

    private static final ChangeListener[] LISTENER_EMPTY_ARRAY = new ChangeListener[0];

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileWatcherSynchronizer.class);

    private static FileWatcherSynchronizer instance;

    private final Map<FileWatcher, List<ChangeListener>> fileWatcherListeners;

    private final Map<File, FileWatcher> fileWatchers;

    public FileWatcherSynchronizer()
    {
        fileWatcherListeners = new HashMap<FileWatcher, List<ChangeListener>>();
        fileWatchers = new HashMap<File, FileWatcher>();
    }

    public final static synchronized FileWatcherSynchronizer getInstance()
    {
        if (instance == null)
        {
            instance = new FileWatcherSynchronizer();
        }
        return instance;
    }

    private final synchronized void fireStateChanged(final FileWatcher fileWatcher)
    {
        final ChangeEvent event = new ChangeEvent(fileWatcher.getFileToWatch());
        for (ChangeListener listener : fileWatcherListeners.get(fileWatcher))
        {
            listener.stateChanged(event);
        }
    }

    public final synchronized void addChangeListener(final File file,
            final ChangeListener changeListener)
    {
        FileWatcher fileWatcher = fileWatchers.get(file);
        if (fileWatcher == null)
        {
            fileWatcher = new FileWatcher(file)
                {

                    //
                    // FileWatcher
                    //

                    @Override
                    protected final void onChange()
                    {
                        fireStateChanged(this);
                    }
                };
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("A new watcher has been created for file '%s'",
                        file));
            }
            fileWatchers.put(file, fileWatcher);
        }
        List<ChangeListener> listeners = fileWatcherListeners.get(fileWatcher);
        if (listeners == null)
        {
            listeners = new ArrayList<ChangeListener>();
            fileWatcherListeners.put(fileWatcher, listeners);
        }
        listeners.add(changeListener);
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug(String
                            .format(
                                    "A new listener has been registered for file '%s'. Currently %d listener(s) registered.",
                                    file, listeners.size()));
        }
    }

    public final synchronized void removeChangeListener(final File file,
            final ChangeListener changeListener)
    {
        final FileWatcher fileWatcher = fileWatchers.get(file);
        if (fileWatcher == null)
        {
            return;
        }
        final List<ChangeListener> listeners = fileWatcherListeners.get(fileWatcher);
        if (listeners == null)
        {
            return;
        }
        listeners.remove(changeListener);
        final int size = listeners.size();
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug(String
                            .format(
                                    "A listener for file '%s' has been removed. Currently %d listener(s) registered.",
                                    file, size));
        }
        if (size == 0)
        {
            fileWatchers.remove(file);
        }
    }

    /**
     * For given <var>file</var> returns the registered <code>ChangeListener</code>s.
     * 
     * @return <code>null</code> if given <var>file</var> is unknown or if no <code>ChangeListener</code> could be found for given <var>file</var>.
     */
    public final synchronized ChangeListener[] getChangeListeners(final File file)
    {
        final FileWatcher fileWatcher = fileWatchers.get(file);
        if (fileWatcher == null)
        {
            return null;
        }
        final List<ChangeListener> listeners = fileWatcherListeners.get(fileWatcher);
        if (listeners == null)
        {
            return null;
        }
        return listeners.toArray(LISTENER_EMPTY_ARRAY);
    }

    /** Clears the <code>Map</code>s used internally. */
    public synchronized void destroy()
    {
        fileWatcherListeners.clear();
        fileWatchers.clear();
    }

    //
    // TimerTask
    //

    @Override
    public final synchronized void run()
    {
        for (final FileWatcher fileWatcher : fileWatcherListeners.keySet())
        {
            fileWatcher.run();
        }
    }
}