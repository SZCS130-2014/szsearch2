package com.shopzilla.service.shoppingcart.resource;

/**
 * Created by divakarbala on 2/28/14.
 */

import com.shopzilla.Product;
import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.yammer.dropwizard.views.View;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.*;
import java.util.ArrayList;


@Path("/search")
@Produces(MediaType.TEXT_HTML)
public class SearchProductResource {
    private final SolrServer server;
    private final String solrUrl;
    private final int pageSize = 12;


    public SearchProductResource(SolrServer server, String solrUrl) {
        this.server = new HttpSolrServer(solrUrl);
        this.solrUrl = solrUrl;
    }

    private String QueryBooster(String q) {



        return q;
    }

    private void computeCounts(List<Product> products) {
        for (Product p : products) {
            p.setReviewCount(new Integer(p.getCommentRatings().length));
        }
    }

    private void computeAverageRatings(List<Product> products) {
        for (Product p : products) {
            int sum = 0;
            Double[] ratings;
            ratings = p.getCommentRatings();
            for (Double rating : ratings) {
                sum += rating.intValue();
            }
            List<String> list = new ArrayList<String>();

            for (int count = 0; count < (sum/ratings.length); count++) {
                list.add(".");
            }
            p.setAverageRating(list);
        }
    }

    private void computeAverageRatingsHalf(List<Product> products) {
        for (Product p : products) {
            double sum = 0;
            int sumi = 0;
            Double[] ratings;
            ratings = p.getCommentRatings();
            for (Double rating : ratings) {
                sum += rating.doubleValue();
                sumi += rating.intValue();
            }
            sum = sum/ratings.length;
            sumi = sumi/ratings.length;
            sum -= sumi;
            List<String> list = new ArrayList<String>();
            if (sum > 0.2)
                list.add(".");
            p.setAverageRatingHalf(list);
        }
    }

    private void computeStars(List<Product> products) {
        computeAverageRatings(products);
        computeAverageRatingsHalf(products);
    }


    public static class IndexView extends View {

        private List<Product> products;
        private Pagination pagination;
        private Categories categories;
        private Integer resultCount;
        private Integer ratingStars;
        private Boolean nameSort = false;
        private Boolean catSort = false;
        private Boolean ratingSort = false;
        private Boolean sortOrder = false;

        public static Double getAverage(Product product) {
            double sum = 0;
            Double[] ratings;
            ratings = product.getCommentRatings();
            for (Double rating : ratings) {
                sum += rating.doubleValue();
            }
            sum = sum/ratings.length;
            return (new Double(sum));
        }

        public void sortByName(List<Product> products, String order) {
            nameSort = true;
            if (order.equals("asc")) {
                sortOrder = true;
                Collections.sort(products, new Comparator<Product>(){
                    public int compare(Product o1, Product o2){
                        return o1.getProductTitle().compareTo(o2.getProductTitle());
                    }
                });
            } else {
                Collections.sort(products, new Comparator<Product>(){
                    public int compare(Product o1, Product o2){
                        return o2.getProductTitle().compareTo(o1.getProductTitle());
                    }
                });
            }
        }

        public void sortByCat(List<Product> products, String order) {
            catSort = true;
            if (order.equals("asc")) {
                sortOrder = true;
                Collections.sort(products, new Comparator<Product>(){
                    public int compare(Product o1, Product o2){
                        return o1.getCategory().compareTo(o2.getCategory());
                    }
                });
            } else {
                Collections.sort(products, new Comparator<Product>(){
                    public int compare(Product o1, Product o2){
                        return o2.getCategory().compareTo(o1.getCategory());
                    }
                });
            }
        }

        public void sortByRating(List<Product> products, String order) {
            ratingSort = true;
            if (order.equals("asc")) {
                sortOrder = true;
                Collections.sort(products, new Comparator<Product>(){
                    public int compare(Product o1, Product o2){
                        return getAverage(o1).compareTo(getAverage(o2));
                    }
                });
            } else {
                Collections.sort(products, new Comparator<Product>(){
                    public int compare(Product o1, Product o2){
                        return getAverage(o2).compareTo(getAverage(o1));
                    }
                });
            }
        }

        public static class Pagination {
            private final String baseURL = "http://localhost:8080/search?q=";
            private final int pageSize = 12;
            private String paginationText;

            protected Pagination(Integer resultsCount, Integer currentPage,
                                 String q, ArrayList<Optional<Boolean>> checkBoxes,
                                 Optional<String> sortField, String sortOrder) {
                String url = baseURL + q;
                for (int i = 0; i<checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isPresent())
                        url += "&c" + (i+1) + "=false";
                }
                if (sortField.isPresent()) {
                    url += "&sortfield=" + sortField.get();
                    url += "&sort=" + sortOrder;
                }
                url += "&p=%d";
                String previousURL = String.format(url, currentPage-1);
                String nextURL = String.format(url, currentPage+1);
                String previous;
                String next;
                int pageCount = resultsCount/pageSize + ((resultsCount%pageSize == 0) ? 0 : 1);
                if (currentPage.equals(0))
                    previous = "<li class=\"disabled\"><a href=\"#\">Previous</a></li>";
                else
                    previous = "<li><a href=\"" + previousURL + "\">Previous</a></li>";
                if (currentPage.equals(pageCount-1))
                    next = "<li class=\"disabled\"><a href=\"#\">Next</a></li>";
                else
                    next = "<li><a href=\"" + nextURL + "\">Next</a></li>";
                this.paginationText = "<ul class=\"pager\">" + String.format("%d-%d of %d ", currentPage*pageSize+1,
                        (((currentPage*pageSize)+pageSize > resultsCount) ? resultsCount : (currentPage*pageSize)+pageSize),
                        resultsCount) + " " + previous + next + "</ul>";
            }

            public String getPaginationText() {
                return this.paginationText;
            }
        }

        public static class Categories {
            public List<category> topCategories;

            public static class category {
                private String name;
                private Integer count;
                private Integer index;
                private Boolean enabled;

                protected category(String name, Integer count, Integer index) {
                    this.name = name;
                    this.count = count;
                    this.index = index;
                    this.enabled = true;
                }
                public void setEnabled(Boolean value) {
                    this.enabled = value;
                }
                public String getName() {
                    return this.name;
                }
                public Integer getCount() {
                    return this.count;
                }
                public Integer getIndex() {
                    return this.index;
                }
                public Boolean getEnabled() {
                    return this.enabled;
                }


            }

            protected Categories(List<Product> products) {
                HashMap<String, Integer> map = new HashMap<String, Integer>();
                for (Product p : products) {
                    String cat = (p.getCategory().equals("")) ? "Misc." : p.getCategory();
                    if (map.containsKey(cat))
                        map.put(cat, map.get(cat)+1);
                    else
                        map.put(cat, 1);
                }
                ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>();
                for (Map.Entry<String, Integer> entry : map.entrySet())
                    list.add(entry);

                Collections.sort(list, new Comparator<Map.Entry<String, Integer>>(){
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                List<Map.Entry<String, Integer>> topList = list.subList(0, ((7 > list.size()) ? list.size() : 7));
                this.topCategories = new ArrayList<category>();
                int count = 1;
                for (Map.Entry<String, Integer> entry : topList) {
                    this.topCategories.add(new category(entry.getKey(), entry.getValue(), count));
                    count++;
                }
            }

            public List<category> getTopCategories() {
                return this.topCategories;
            }
        }

        protected IndexView(String templateName) {
            super(templateName);
        }
        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public void setPagination(Integer resultsCount, Integer currentPage,
                                  String q, ArrayList<Optional<Boolean>> checkBoxes,
                                  Optional<String> sfield, String sorder) {
            this.pagination = new Pagination(resultsCount, currentPage, q, checkBoxes, sfield, sorder);
        }

        public void setCategories(List<Product> products) {
            this.categories = new Categories(products);
        }
        public void setRatingStars(Integer ratingStars) { this.ratingStars = ratingStars;}
        public void setResultCount(Integer size) { this.resultCount = size; }

        public Pagination getPagination() {
            return this.pagination;
        }
        public Categories getCategories() {
            return this.categories;
        }
        public List<Product> getProducts() {
            return this.products;
        }
        public Boolean getNameSort() { return this.nameSort; }
        public Boolean getCatSort() { return this.catSort; }
        public Boolean getRatingSort() { return this.ratingSort; }
        public Boolean getSortOrder() { return this.sortOrder; }
        public Integer getResultCount() { return this.resultCount; }
        public Integer getRatingStars() { return this.ratingStars; }

    }


    private void filterCategories(IndexView view, List<Product> beans) {
        for (IndexView.Categories.category c : view.categories.topCategories) {
            if (!c.getEnabled()) {
                for (Iterator<Product> iter = beans.iterator(); iter.hasNext();) {
                    Product p = iter.next();
                    if (p.getCategory().equals(((c.getName().equals("Misc.")) ? "" : c.getName()))) {
                        iter.remove();
                    }
                }
            }
        }
    }

    private void filterRatings(Double rating, List<Product> beans) {
        for (Iterator<Product> iter = beans.iterator(); iter.hasNext();) {
            Product p = iter.next();
            if (IndexView.getAverage(p)<rating) {
                iter.remove();
            }
        }
    }

    public static class ProductDescription {
        private Product product;
        private Double averageRating;
        protected ProductDescription(Product product) {
            this.product = product;
            this.averageRating = IndexView.getAverage(product);
        }
        public Product getProduct() {
            return this.product;
        }
        public Double getAverageRating() {
            return this.averageRating;
        }
    }


    public static String generateBoostedQuery(String rawQuery, ArrayList<String> categoryList) {

        // Fields on which search is performed
        String Category, DisplayName, PID, ProductTitle, CommentText, CommentTitle, CommentRating;
        Category = DisplayName = PID = ProductTitle = CommentText = CommentTitle = CommentRating = "";

        // Pre-fix for all search queries
        String pre = "";

        // If any term matches, perform a boost
        boolean catBoost = false;
        boolean ptBoost = false;
        boolean suppressCat = false;
        boolean finalCat = false;
        boolean isiProduct = false;

        // Formatted search term
        String newWord = "";

        // Take first search term, ex. "iPod touch"
        String line = rawQuery;
        line = line.replaceAll("[\\-()]", "");
        line = line.toLowerCase();

        // Array of word(s) search term consists of, ex. {"iPod", "touch"}
        String[] words = line.split("\\s+");

        int boostCountCat = 10;
        int boostCountPT = 20;
        int boostCountCT = 10;

        for (int j = 0; j < words.length; j++) {

            // See if any word matches a Category
            //  if yes, add the Category to our matchedCategory list
            for (int k = 0; k < categoryList.size(); k++ ) {
                if (words[j].equalsIgnoreCase("iPad")) {
                    Category = "(Tablet Computers)^20";
                    finalCat=true;
                    break;
                }
                if (words[j].equalsIgnoreCase("iPhone")) {
                    Category = "(Cell Phones)^20+(Cell Phone Accessories)^20";
                    finalCat=true;
                    break;
                }
                if (words[j].equalsIgnoreCase("iPod")) {
                    Category = "(MP3 and Media Players)^20";
                    finalCat=true;
                    break;
                }

                if (categoryList.get(k).equalsIgnoreCase(line + ' ')) {
                    String newCat = categoryList.get(k).trim() + "^10";
                    Category = newCat;
                    finalCat=true;
                    break;
                } else if (!finalCat && categoryList.get(k).contains(words[j]) && !Category.contains(words[j])) {
                    if (Category.isEmpty()) {
                        Category += "(" + words[j] + ")^" + boostCountCat;
//                      boostCountCat = boostCountCat + 5;
                    }
                    else {
                        Category +=  "+(" + words[j] + ")^" + boostCountCat;
//                      boostCountCat = boostCountCat + 5;
                    }
                }
            }

            ptBoost=true;
            if (newWord.isEmpty()) {
                newWord = "(" + words[j] + ")^10";
                if (words[j].equalsIgnoreCase("iPod") || words[j].equalsIgnoreCase("iPad") ||
                        words[j].equalsIgnoreCase("iPhone"))
                    ProductTitle = "((" + words[j] + ")^1000";
                else
                    ProductTitle = "((" + words[j] + ")^10";
                CommentTitle = "(" + words[j] + ")^5";
                CommentText = "(" + words[j] + ")^5";
                if (j == words.length-1) {
                    newWord += "0";
                    ProductTitle += "0";
                    CommentTitle += "0";
                    CommentText += "0";
                }

            } else {
                newWord += "+(" + words[j] + ")";
                if (words[j].equalsIgnoreCase("iPod") || words[j].equalsIgnoreCase("iPad") ||
                        words[j].equalsIgnoreCase("iPhone")) {
                    ProductTitle += "+(" + words[j] + ")^1000";
                    isiProduct = true;
                }
                if (j == words.length-1) {
                    boostCountPT = boostCountPT + 10;
                    boostCountCT = boostCountCT + 10;
                    if (!isiProduct)
                        ProductTitle += "+(" + words[j] + ")^200";
                    CommentTitle += "+(" + words[j] + ")^50";
                }
                else {
                    if (!isiProduct)
                        ProductTitle += "+(" + words[j] + ")^10";
                    CommentTitle += "+(" + words[j] + ")^5";
                }

                // Boosting last term in search for CommentText

                if (j == words.length-1)
                    CommentText += "+(" + words[j] + ")^50";
                else
                    CommentText += "+(" + words[j] + ")";

//              boostCountPT = boostCountPT + 5;
//              boostCountCT = boostCountCT + 5;
            }

            // Formatted search term
            if (!newWord.contains(words[j])) {
                if (newWord.isEmpty())
                    newWord = words[j];
                else
                    newWord += "+" + words[j];
            }
        }

        // Add parentheses around term if more than one word
        if (words.length > 1)
            newWord = "(" + newWord + ")";
        if (words.length < 2)
            suppressCat=true;

        // If search term does not match any categories, use the term but don't boost
        if (Category.isEmpty())
            Category = newWord;

        Category = "(" + Category;
        if (suppressCat)
            // TODO: this may not work because accessories can be one word searches (i.e. earbuds)
            Category += "+(accessories)^-100.0";
        Category += ")";

        if (ProductTitle.isEmpty())
            ProductTitle = newWord;
        if (CommentTitle.isEmpty())
            CommentTitle = newWord;

        String output = pre + "Category:" + Category + "+"
                + "DisplayName:" + newWord + "+"
                + "ProductTitle:" + ProductTitle + ")+"
                + "CommentText:(" + CommentText + ")+"
                + "CommentTitle:(" + newWord + ")";
        System.out.println(output);
        return output;

    }


    @GET
    public IndexView findProducts(@QueryParam("q") String q,
                                  @QueryParam("p") Optional<Integer> p,
                                  @QueryParam("c1") Optional<Boolean> c1,
                                  @QueryParam("c2") Optional<Boolean> c2,
                                  @QueryParam("c3") Optional<Boolean> c3,
                                  @QueryParam("c4") Optional<Boolean> c4,
                                  @QueryParam("c5") Optional<Boolean> c5,
                                  @QueryParam("c6") Optional<Boolean> c6,
                                  @QueryParam("c7") Optional<Boolean> c7,
                                  @QueryParam("sortfield") Optional<String> sfield,
                                  @QueryParam("sort") Optional<String> sorder,
                                  @QueryParam("rs") Optional<Integer> rStars) {

        static String cats[] = { "Pens", "Binders", "Binder Pockets and Index Dividers", "Office Chairs", "Printers", "Paper Shredders", "Pencils", "Fans", "Paper", "Cash Registers and POS Equipment", "Ink, Toner and Inkjet Cartridges", "Notebooks and Writing Pads", "Clipboards", "Coffee Makers", "Phones", "Glue", "Vacuums", "Headphones", "Binder Rings", "Dishwashers", "Sheet Protectors", "Backpacks", "Clocks", "Camping and Hiking Gear", "Outdoor Power Equipment", "MP3 and Media Players", "Heaters", "Outdoor Games and Fun", "Computer Monitors", "Hard Drives", "Shaving Appliances", "Digital Cameras", 
        "Laptop Computers", "Video Games", "Tablet Computers", "GPS", "Stereo Speakers", "Refrigerators", "TV", "Memory Cards", "Microwave Ovens", "Blu-Ray Disc Players", "Digital Book Reader Accessories", "Digital Book Readers", "Pointing Devices", "Wine and Beverage Coolers", "Learning Toys", "Desktop Computers", "Ranges", "Projectors", "Trimmers", "Hair Care Appliances", "Home Theater Systems", "Electric Toothbrushes", "Network Switches", "Computer Towers", "Cable and DSL Modems", "Miscellaneous Software", "Other Laptop Accessories", "MP3 Player Accessories", "Camcorders", "Cell Phone Accessories", 
        "Blenders", "Computer Game Controllers", "Home Security", "Portable Radios", "Headsets and Microphones", "Camera Lenses", "Washers and Dryers", "Food Processors", "Wii Consoles", "Scanners", "Xbox Accessories", "Toasters", "Computer Power Supplies", "Toaster Ovens", "Radar Detectors and Jammers", "Kitchen Supplies and Utensils", "Network Routers and Bridges", "Dehumidifiers", "Operating Systems", "Portable Tape and Digital Recorders", "Camera Flashes", "Treadmills", "Media Storage", "Computer Speakers", "Mixers", "Electric Skillets and Woks", "Vacuum Accessories", "Electric Irons", "Washer and Dryer Accessories", 
        "Desks", "Cookware", "Receivers", "Can Openers", "CPUs and Computer Processor Upgrades", "Graphics Cards", "CD and DVD Drives", "Computer Keyboards", "Blood Pressure Monitors", "Personal Scales", "Air Conditioners", "PDA and Handheld Computers", "Juicers", "Media Hubs", "Sewing Machines", "Garment Steamers", "Deep Fryers", "Strollers/Joggers", "Printer Accessories", "Baby Rattles and Teethers", "Projector Accessories", "Camcorder Accessories", "Turntables", "Air Purifiers", "Remote Controls", "System Cooling", "Crock Pots and Slow Cookers", "Motherboards", "Cutlery", "Paper Shredder Waste Bags", "Marine Electronics", 
        "Breadmakers", "Calculators", "Popcorn Makers", "Indoor Grills", "Nursing and Feeding", "Tea Kettles", "Massagers", "Portable Cassette Players", "Cassette Decks", "Warming Drawers", "Food Dehydrators", "Weather Instruments", "Fax Machines", "Car Seat Accessories", "NIC", "Hunting and Archery Equipment", "Original Luggage", "Binoculars and Telescopes", "Humidifiers", "XM and Sirius Satellite Radios", "Monitor Accessories", "Office, Tax and Accounting Software", "Car Seats", "Computer Cables and Adapters", "Dolls", "Sound Cards", "Baby and Toddler Safety", "Games and Puzzles", "Rechargeable and Replacement Batteries", 
        "Stroller Accessories", "Two-Way Radios", "Briefcases", "DVR", "Car Amplifiers", "Cooktops", "Bakeware", "Trash Compactors", "Range Hoods", "Waffle Makers", "Sandwich Makers", "Yoga and Pilates Equipment", "Integrated Amplifiers", "Meat Grinders", "Boomboxes", "Refrigerator Accessories", "Freezers", "Mixer Accessories", "Floor Cleaners", "Air Conditioner Accessories", "Coffee Grinders", "Water Filters", "Car Subwoofers", "Camera Cases", "DVD/VCR Combos", "Fishing Gear", "Food Slicers", "Snow Cone and Ice Shavers", "Electric Knife Sharpeners", "IO Controllers", "Fever Thermometers", "Karaoke Equipment", "Elliptical and Cross Trainer Machines", 
        "Watches", "WebCams", "Home Gyms" };
        ArrayList<String> allCategories = new ArrayList<String>();
        ArrayList<String> categoryL = new ArrayList<String>(Arrays.asList(cats));

        // try {
        //     String line;
        //     BufferedReader br = new BufferedReader(new InputStreamReader(
        //             new FileInputStream("/Users/divakarbala/Dropbox/CS projects/CS 130/dropwizard-crud-archetype/categories.txt")));
        //     while ((line = br.readLine()) != null)
        //         categoryL.add(line);
        //     br.close();
        // } catch (Exception e) {}

        for (int z = 0; z < categoryL.size(); z++) {
            String temp=categoryL.get(z);
            temp = temp.replaceAll("[\\-()]", "");
            allCategories.add(temp.toLowerCase());
        }

        for (int i = 0; i < allCategories.size(); i++)
            allCategories.set(i, allCategories.get(i).toLowerCase());

        String boostQuery = generateBoostedQuery(q, allCategories);

        SolrQuery query = new SolrQuery();

        query.setQuery(boostQuery);
        query.setRows(100000);
        QueryResponse rsp = null;
        try {
            rsp = server.query(query);
        } catch (Exception e) {}
        List<Product> beans = rsp.getBeans(Product.class);

        SolrQuery titleQuery = new SolrQuery();

        q = q.trim();
        String lastQueryWord = q.substring(q.lastIndexOf(" ")+1);
        lastQueryWord = lastQueryWord.toLowerCase();

        titleQuery.setQuery("ProductTitle:" + lastQueryWord);
        titleQuery.setRows(100000);
        QueryResponse titleRsp = null;

        try {
            titleRsp = server.query(titleQuery);
        } catch (Exception e) {}
        List<Product> titleBeans = titleRsp.getBeans(Product.class);

        for (Iterator<Product> iter = titleBeans.iterator(); iter.hasNext();) {
            Product titleProduct = iter.next();
            String lastTitleWord = titleProduct.getProductTitle().substring(titleProduct.getProductTitle().lastIndexOf(" ")+1);
            lastTitleWord = lastTitleWord.toLowerCase();
            if (!lastTitleWord.equals(lastQueryWord)) {
                iter.remove();
            }
        }

        HashSet<String> beanPids = new HashSet<String>();

        for (Product beanProduct : beans) {
            beanPids.add(beanProduct.getPID());
        }

        for (Product titleBeanProduct : titleBeans) {
            if (!beanPids.contains(titleBeanProduct.getPID()))
                beans.add(0, titleBeanProduct);
        }

        IndexView view = new IndexView("/displayProducts.mustache");
        view.setCategories(beans);
        view.setRatingStars(rStars.or(0));

        ArrayList<Optional<Boolean>> checkBoxes = new ArrayList<Optional<Boolean>>();
        checkBoxes.add(c1);
        checkBoxes.add(c2);
        checkBoxes.add(c3);
        checkBoxes.add(c4);
        checkBoxes.add(c5);
        checkBoxes.add(c6);
        checkBoxes.add(c7);

        for (int i = 0; i<checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isPresent())
                view.categories.topCategories.get(i).setEnabled(false);
        }

        filterCategories(view, beans);
        filterRatings(new Double(rStars.or(0)), beans);
        view.setResultCount(beans.size());

        if (sfield.isPresent()) {
            if (sfield.get().equals("name"))
                view.sortByName(beans, sorder.get());
            else if (sfield.get().equals("cat"))
                view.sortByCat(beans, sorder.get());
            else
                view.sortByRating(beans, sorder.get());
        }

        List<Product> products = beans.subList(pageSize*(p.or(0)),
                (((p.or(0)*pageSize)+pageSize > beans.size()) ? beans.size() : (p.or(0)*pageSize)+pageSize));
        computeCounts(products);
        computeStars(products);
        view.setPagination(beans.size(), p.or(0), q, checkBoxes, sfield, sorder.or(""));
        view.setProducts(products);

        return view;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("viewProduct")
    public ProductDescription getProduct(@QueryParam("PID") String pid) {
        String q = "PID:" + pid;
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        QueryResponse rsp = null;
        try {
            rsp = server.query(query);
        } catch (Exception e) {}
        List<Product> beans = rsp.getBeans(Product.class);
        if (beans.get(0).getCategory().equals(""))
            beans.get(0).setCategory("Misc.");
        ProductDescription pd = new ProductDescription(beans.get(0));
        return pd;
    }

}
