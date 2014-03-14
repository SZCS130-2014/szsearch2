package com.shopzilla.service.shoppingcart;

/**
 * Created by divakarbala on 2/28/14.
 */

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class ShopzillaSearcherConfiguration extends Configuration {

    @NotEmpty
    @JsonProperty
    private String solrUrl = "http://localhost:8983/solr/";

    public String getSolrUrl() {
        return solrUrl;
    }
}