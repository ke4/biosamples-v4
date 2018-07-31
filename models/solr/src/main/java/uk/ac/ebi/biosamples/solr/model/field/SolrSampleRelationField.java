package uk.ac.ebi.biosamples.solr.model.field;

import org.springframework.data.solr.core.query.Criteria;

import uk.ac.ebi.biosamples.model.filter.Filter;
import uk.ac.ebi.biosamples.model.filter.RelationFilter;
import uk.ac.ebi.biosamples.solr.model.strategy.FacetFetchStrategy;
import uk.ac.ebi.biosamples.solr.model.strategy.RegularFacetFetchStrategy;

import java.util.regex.Pattern;

public class SolrSampleRelationField extends SolrSampleField{

    public SolrSampleRelationField() {
        super();
    }

    /**
     * All subclasses should implement this constructor
     *
     * @param readableLabel
     * @param solrDocumentLabel
     */
    public SolrSampleRelationField(String readableLabel, String solrDocumentLabel) {
        super(readableLabel, solrDocumentLabel);
    }

    @Override
    public Pattern getFieldPattern() {
        return Pattern.compile("^[A-Z0-9_]+_or_ss$");
    }

    @Override
    public boolean isEncodedField() {
        return true;
    }

    @Override
    public SolrFieldType getSolrFieldType() {
        return SolrFieldType.RELATION;
    }

    @Override
    public FacetFetchStrategy getFacetCollectionStrategy() {
        return new RegularFacetFetchStrategy();
    }

    @Override
    public Criteria getFilterCriteria(Filter filter) {
        Criteria filterCriteria = null;

        if (filter instanceof RelationFilter) {

            filterCriteria = new Criteria(getSolrDocumentFieldName());

            RelationFilter relationFilter = (RelationFilter) filter;
            if (relationFilter.getContent().isPresent()) {
//                filterCriteria = filterCriteria.expression("/" + relationFilter.getContent().get() + "/");
                filterCriteria = filterCriteria.expression(String.format("\"%s\"", relationFilter.getContent().get()));
            } else {
                filterCriteria = filterCriteria.isNotNull();
            }
        }

        return filterCriteria;
    }
}
