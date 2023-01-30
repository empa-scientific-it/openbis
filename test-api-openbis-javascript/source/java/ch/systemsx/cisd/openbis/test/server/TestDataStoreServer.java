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

package ch.systemsx.cisd.openbis.test.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author anttil
 */
public abstract class TestDataStoreServer
{

    private String name;

    private String dumpsPath;

    private String rootPath;

    private boolean deamon;

    private Integer port;

    private Integer debugPort;

    protected abstract String getCommand();
    
    protected abstract void cleanUp();

    public String start() throws Exception
    {
        TestDatabase.restoreDumps(getDumpsPath());
        cleanUp();

        System.out.println("STARTING DATA STORE: " + getName() + " COMMAND: " + getCommand());

        ProcessHandler p = new ProcessHandler(getCommand(), getRootPath());

        p.addListener(new Listener()
            {
                @Override
                public void newLine(String line)
                {
                    System.out.println("DATA STORE " + getName() + ": " + line);
                }
            });
        LogLineReader reader = new LogLineReader();
        p.addListener(reader);

        Thread t = new Thread(p);
        t.setDaemon(isDeamon());
        t.start();

        String line;
        while ((line = reader.readLine()) != null)
        {
            if (line.contains("Data Store Server ready and waiting for data"))
            {
                reader.disable();
                break;
            }
        }
        System.out.println("DATA STORE " + getName() + " STARTED");

        return "http://localhost:" + getPort();
    }

    private static class LogLineReader implements Listener
    {
        private LinkedBlockingQueue<String> queue;

        private boolean enabled;

        public LogLineReader()
        {
            this.queue = new LinkedBlockingQueue<String>();
            enabled = true;
        }

        public void disable()
        {
            enabled = false;
        }

        @Override
        public void newLine(String line)
        {
            try
            {
                if (enabled)
                {
                    queue.put(line);
                }
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }

        public String readLine()
        {
            try
            {
                return queue.take();
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
                return null;
            }
        }
    }

    private static interface Listener
    {
        public void newLine(String line);
    }

    private static class ProcessHandler implements Runnable
    {
        private final String[] command;

        private final String directory;

        private final Collection<Listener> listeners;

        public ProcessHandler(String command, String directory)
        {
            StringTokenizer tokenizer = new StringTokenizer(command, " ");
            List<String> strings = new ArrayList<String>();
            while (tokenizer.hasMoreElements())
            {
                strings.add(tokenizer.nextToken());
            }
            this.command = strings.toArray(new String[0]);

            this.directory = directory;
            this.listeners = new ArrayList<Listener>();
        }

        public void addListener(Listener listener)
        {
            this.listeners.add(listener);
        }

        @Override
        public void run()
        {
            try
            {
                Runtime runtime = Runtime.getRuntime();
                final Process process = runtime.exec(command, null, new File(directory));

                runtime.addShutdownHook(new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                process.getInputStream().close();
                                process.getErrorStream().close();
                                process.getOutputStream().close();
                                process.destroy();
                                process.waitFor();
                                System.out.println("DATA STORE " + getName() + " DESTROYED");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });

                InputStream in = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    for (Listener listener : listeners)
                    {
                        listener.newLine(line);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setDumpsPath(String dumpsPath)
    {
        this.dumpsPath = dumpsPath;
    }

    public String getDumpsPath()
    {
        return dumpsPath;
    }

    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }

    public String getRootPath()
    {
        return rootPath;
    }

    public void setDeamon(boolean deamon)
    {
        this.deamon = deamon;
    }

    public boolean isDeamon()
    {
        return deamon;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setDebugPort(Integer debugPort)
    {
        this.debugPort = debugPort;
    }

    public Integer getDebugPort()
    {
        return debugPort;
    }

}
