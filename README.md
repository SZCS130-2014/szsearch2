<h2>Shopzilla Search Team 2</h2>

This project is extremely easy to setup and get running. Follow the instructions below after pulling this repository.<br>
<br>
Starting up SOLR: <br>
1. First navigate to "szsearch2/solr-4.5.0/example/"<br>
2. Unzip the dataset with the following command: <code>unzip dataset.xml.zip</code><br>
3. Run the following command: <code>java -jar start.jar</code><br>
4. Open up a web-browser and visit the following url: "http://localhost:8983/solr/dataimport?command=full-import"<br>
<br>
Starting up Dropwizard:<br>
5. Next navigate to "szsearch2/dropwizard/service"<br>
6. Run the following command:<br>
<br>
<code>java -classpath dist/solr-solrj-4.5.0.jar:dist/solrj-lib/commons-io-2.1.jar:dist/solrj-lib/httpcore-4.2.2.jar:dist/solrj-lib/httpmime-4.2.3.jar:dist/solrj-lib/httpclient-4.2.3.jar:dist/solrj-lib/slf4j-api-1.6.6.jar:dist/solrj-lib/slf4j-log4j12-1.6.6.jar:dist/solrj-lib/wstx-asl-3.2.7.jar:dist/solrj-lib/zookeeper-3.4.5.jar:dist/solrj-lib/jul-to-slf4j-1.6.6.jar:dist/solrj-lib/jcl-over-slf4j-1.6.6.jar:dist/solrj-lib/noggit-0.5.jar:dist/solrj-lib/log4j-1.2.16.jar -jar target/shoppingcart-service-1.0-SNAPSHOT.jar server service.yaml</code><br>
<br>
7. Finally visit the following example search page: "http://localhost:8080/search?q=tv"<br>
<br>
Everything is fully setup and ready to use!<br>
