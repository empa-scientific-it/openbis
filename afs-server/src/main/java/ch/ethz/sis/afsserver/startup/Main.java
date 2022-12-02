/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afsserver.startup;

import ch.ethz.sis.afs.manager.TransactionManager;
import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;
import ch.ethz.sis.afsserver.http.APIResponse;
import ch.ethz.sis.afsserver.http.HttpServerHandler;
import ch.ethz.sis.afsserver.http.netty.NettyHttpServer;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogFactoryFactory;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.startup.Configuration;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Main {

    public static List getParameterClasses() {
        return List.of(AtomicFileSystemParameter.class);
    }

    // Please start with --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true
    public static void main(String[] args) throws Exception {
        System.out.println("Current Working Directory: " + (new File("")).getCanonicalPath());
        Configuration configuration = new Configuration(getParameterClasses(), "../afs-server/src/main/resources/afs-server-config.properties");

        // Initializing LogManager
        LogFactoryFactory logFactoryFactory = new LogFactoryFactory();
        LogFactory logFactory = logFactoryFactory.create(configuration.getStringProperty(AtomicFileSystemParameter.logFactoryClass));
        logFactory.configure(configuration.getStringProperty(AtomicFileSystemParameter.logConfigFile));
        LogManager.setLogFactory(logFactory);

        // Initializing Transaction Manager
        JSONObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemParameter.jsonObjectMapperClass);
        String writeAheadLogRoot = configuration.getStringProperty(AtomicFileSystemParameter.writeAheadLogRoot);
        String storageRoot = configuration.getStringProperty(AtomicFileSystemParameter.storageRoot);

        TransactionManager transactionManager = new TransactionManager(jsonObjectMapper, writeAheadLogRoot, storageRoot);
        transactionManager.reCommitTransactionsAfterCrash();

        //Initializing Http Server
        int port = configuration.getIntegerProperty(AtomicFileSystemServerParameter.port);
        int maxContentLength = configuration.getIntegerProperty(AtomicFileSystemServerParameter.maxContentLength);
        String uri = configuration.getStringProperty(AtomicFileSystemServerParameter.uri);

        NettyHttpServer nettyHttpServer = new NettyHttpServer();
        nettyHttpServer.start(port, maxContentLength, uri, new HttpServerHandler() {
            @Override
            public APIResponse process(InputStream requestBody, Map<String, List<String>> parameters) {
                return new APIResponse(true, APIResponse.CONTENT_TYPE_JSON, new byte[0]);
            }
        });
        Thread.currentThread().join();
    }
}
