package com.shopzilla.service.shoppingcart;

import com.shopzilla.service.shoppingcart.resource.SearchProductResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * Created by divakarbala on 2/28/14.
 */
public class ShopzillaSearcher extends Service<ShopzillaSearcherConfiguration> {

    public static void main(String[] args) throws Exception {
        new ShopzillaSearcher().run(args);
    }

    @Override
    public void initialize(Bootstrap<ShopzillaSearcherConfiguration> bootstrap) {
        bootstrap.setName("shopzilla-searcher");
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(ShopzillaSearcherConfiguration configuration, Environment environment) {
        final String solrUrl = configuration.getSolrUrl();
        SolrServer server = null;
        try {
            server = new HttpSolrServer(solrUrl);
        } catch (Exception e) {}
        environment.addResource(new SearchProductResource(server, solrUrl));
    }
}


