/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.DESCRIPTOR_ACCESSION_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.DESCRIPTOR_ONTOLOGY_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.DESCRIPTOR_ONTOLOGY_VERSION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.INHERITED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PREDICATE_ACCESSION_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PREDICATE_ONTOLOGY_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PREDICATE_ONTOLOGY_VERSION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author pkupczyk
 */
public class SemanticAnnotationProvider
{

    private static final int DEFAULT_WIDTH = 200;

    public void addMoreColumns(TypedTableModelBuilder<?> builder, boolean inherited)
    {
        builder.addColumn(PERM_ID).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        builder.addColumn(PREDICATE_ONTOLOGY_ID).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        builder.addColumn(PREDICATE_ONTOLOGY_VERSION).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        builder.addColumn(PREDICATE_ACCESSION_ID).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        builder.addColumn(DESCRIPTOR_ONTOLOGY_ID).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        builder.addColumn(DESCRIPTOR_ONTOLOGY_VERSION).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        builder.addColumn(DESCRIPTOR_ACCESSION_ID).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);

        if (inherited)
        {
            builder.addColumn(INHERITED).hideByDefault().withDefaultWidth(DEFAULT_WIDTH);
        }
    }

    public void addMoreCells(TypedTableModelBuilder<?> builder, List<SemanticAnnotation> annotations, Boolean inherited)
    {
        List<SemanticAnnotation> sortedAnnotations = new ArrayList<SemanticAnnotation>();

        if (annotations != null)
        {
            sortedAnnotations.addAll(annotations);
            sortedAnnotations.sort(new Comparator<SemanticAnnotation>()
                {
                    @Override
                    public int compare(SemanticAnnotation o1, SemanticAnnotation o2)
                    {
                        return o1.getPermId().getPermId().compareTo(o2.getPermId().getPermId());
                    }
                });
        }

        builder.column(PERM_ID).addString(new PermIdFormatter().format(sortedAnnotations));
        builder.column(PREDICATE_ONTOLOGY_ID).addString(new PredicateOntologyIdFormatter().format(sortedAnnotations));
        builder.column(PREDICATE_ONTOLOGY_VERSION).addString(new PredicateOntologyVersionFormatter().format(sortedAnnotations));
        builder.column(PREDICATE_ACCESSION_ID).addString(new PredicateAccessionIdFormatter().format(sortedAnnotations));
        builder.column(DESCRIPTOR_ONTOLOGY_ID).addString(new DescriptorOntologyIdFormatter().format(sortedAnnotations));
        builder.column(DESCRIPTOR_ONTOLOGY_VERSION).addString(new DescriptorOntologyVersionFormatter().format(sortedAnnotations));
        builder.column(DESCRIPTOR_ACCESSION_ID).addString(new DescriptorAccessionIdFormatter().format(sortedAnnotations));

        if (inherited != null)
        {
            builder.column(INHERITED).addString(SimpleYesNoRenderer.render(inherited));
        }
    }

    private abstract class Formatter
    {

        public String format(Collection<SemanticAnnotation> annotations)
        {
            StringBuilder text = new StringBuilder();

            if (annotations != null)
            {
                Iterator<SemanticAnnotation> iter = annotations.iterator();

                while (iter.hasNext())
                {
                    SemanticAnnotation annotation = iter.next();
                    String value = getValue(annotation);

                    if (value != null)
                    {
                        text.append(value);
                    }

                    if (iter.hasNext())
                    {
                        text.append("\n\r");
                    }
                }
            }

            return text.toString();
        }

        protected abstract String getValue(SemanticAnnotation annotation);

    }

    private class PermIdFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getPermId().getPermId();
        }

    }

    private class PredicateOntologyIdFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getPredicateOntologyId();
        }

    }

    private class PredicateOntologyVersionFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getPredicateOntologyVersion();
        }

    }

    private class PredicateAccessionIdFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getPredicateAccessionId();
        }

    }

    private class DescriptorOntologyIdFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getDescriptorOntologyId();
        }

    }

    private class DescriptorOntologyVersionFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getDescriptorOntologyVersion();
        }

    }

    private class DescriptorAccessionIdFormatter extends Formatter
    {

        @Override
        protected String getValue(SemanticAnnotation annotation)
        {
            return annotation.getDescriptorAccessionId();
        }

    }

}
