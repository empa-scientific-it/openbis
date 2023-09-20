/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.hotfix;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ELNAnnotationsMigration {

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ELNAnnotationsMigration.class);

    private static boolean contains(String permId, List<Sample> samples) {
        for (Sample sample:samples) {
            if (sample.getPermId().getPermId().equals(permId)) {
                return true;
            }
        }
        return false;
    }

    public static void beforeUpgrade(String sessionToken) throws Exception {
        operationLog.info("ELNAnnotationsMigration beforeUpgrade START");
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withProperty("$ANNOTATIONS_STATE");
        SampleFetchOptions options = new SampleFetchOptions();
        options.withProperties();
        options.withParents();
        options.withChildren();
        options.sortBy().permId();
        options.from(0);
        options.count(0);

        SearchResult<Sample> results = api.searchSamples(sessionToken, criteria, options);
        int from = 0;
        int count = 10000;
        int total = results.getTotalCount();
        operationLog.info("ELNAnnotationsMigration from: " + from + " count: " + count + " total: " + total);

        List<SampleUpdate> sampleUpdates = new ArrayList<>();

        int exceptionsTotal = 0;
        List<String> exceptionsPermIds = new ArrayList<>();
        int skippedTotal = 0;
        List<String> skippedPermIds = new ArrayList<>();

        while (total > from) {
            options.from(from);
            options.count(count);
            operationLog.info("ELNAnnotationsMigration from: " + from + " count: " + count + " total: " + total);
            for (Sample sample:api.searchSamples(sessionToken, criteria, options).getObjects()) {
                SampleUpdate sampleUpdate = null;
                String annotations = (String) sample.getProperty("$ANNOTATIONS_STATE");
                // XML Properties can potentially contain garbage values, ignore those
                if (!annotations.isEmpty() && !annotations.startsWith("�")) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    ByteArrayInputStream input = new ByteArrayInputStream(annotations.getBytes("UTF-8"));
                    try {
                        Document document = builder.parse(input);
                        Element documentElement = document.getDocumentElement();
                        if (documentElement.hasChildNodes()) {
                            NodeList sampleNodes = documentElement.getChildNodes();
                            for (int sampleIdx = 0; sampleIdx < sampleNodes.getLength(); sampleIdx++) {
                                Node sampleNode = sampleNodes.item(sampleIdx);
                                NamedNodeMap sampleNodeAttributes = sampleNode.getAttributes();
                                // Every sample node has 3 information attributes, this was to paint the interface when using manage properties on the GWT Core UI
                                // To contain annotations the node needs to have more than 3 attributes
                                int sampleNodeAttributesNumber = sampleNodeAttributes.getLength();
                                if (sampleNodeAttributesNumber > 3) {
                                    // permId is always be present for every sample node is one of the 3 information attributes
                                    String samplePermId = sampleNodeAttributes.getNamedItem("permId").getNodeValue();
                                    for (int attributeIdx = 0; attributeIdx < sampleNodeAttributes.getLength(); attributeIdx++) {
                                        Node attributeNode = sampleNodeAttributes.item(attributeIdx);
                                        String name = attributeNode.getNodeName();
                                        // Sample nodes contain three information properties by just attaching something as a parent/children
                                        boolean isInfoProperty = name.equals("permId") || name.equals("identifier") || name.equals("sampleType");
                                        // If is not an information property is an annotation
                                        if (!isInfoProperty) {
                                            String value = attributeNode.getNodeValue();
                                            // If the annotations belongs to a parent or children needs to be checked
                                            boolean isParent = contains(samplePermId, sample.getParents());
                                            boolean isChildren = contains(samplePermId, sample.getChildren());
                                            // Add annotations to Sample Update
                                            if (isParent || isChildren) {
                                                if (sampleUpdate == null) {
                                                    sampleUpdate = new SampleUpdate();
                                                    sampleUpdate.setSampleId(sample.getPermId());
                                                }
                                                if (isParent) {
                                                    sampleUpdate.relationship(new SamplePermId(samplePermId)).addParentAnnotation(name, value);
                                                }
                                                if (isChildren) {
                                                    sampleUpdate.relationship(new SamplePermId(samplePermId)).addChildAnnotation(name, value);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        exceptionsTotal++;
                        exceptionsPermIds.add(sample.getPermId().getPermId());
                        operationLog.info("ELNAnnotationsMigration FAILED: " + sample.getPermId().getPermId() + " $ANNOTATIONS_STATE = " + annotations);
                        ex.printStackTrace();
                    }
                } else {
                    skippedTotal++;
                    skippedPermIds.add(sample.getPermId().getPermId());
                    operationLog.info("ELNAnnotationsMigration SKIPPED: " + sample.getPermId().getPermId() + " $ANNOTATIONS_STATE = " + annotations);
                }

                if (sampleUpdate != null) {
                    sampleUpdates.add(sampleUpdate);
                }
            }
            from = from + count;
        }

        operationLog.info("=== ELNAnnotationsMigration Summary ===");
        operationLog.info("ELNAnnotationsMigration exceptionsTotal: " + exceptionsTotal);
        operationLog.info("ELNAnnotationsMigration skippedTotal: " + skippedTotal);
        operationLog.info("ELNAnnotationsMigration updateSamples sampleUpdates.size: " + sampleUpdates.size());
        api.updateSamples(sessionToken, sampleUpdates);
        operationLog.info("ELNAnnotationsMigration exceptionsPermIds: " + exceptionsPermIds);
        operationLog.info("ELNAnnotationsMigration skippedPermIds: " + skippedPermIds);
        operationLog.info("ELNAnnotationsMigration beforeUpgrade END");
    }
}