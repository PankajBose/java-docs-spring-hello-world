package uline.emma.addresslookup;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.micrometer.core.instrument.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
@RestController
public class AddressLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressLookup.class);
    private static final Map<String, Map<String, NameBean>> siteData = new HashMap<>();
    private static CosmosContainer container;
    private static final String welcomeContent = IOUtils.toString(AddressLookup.class.getClassLoader().getResourceAsStream("welcome-message.html"));
    private static long loaded = 0;

    public static void main(String[] args) {
        loadFromCosmosDB();

        SpringApplication.run(AddressLookup.class, args);
    }

    @RequestMapping(value = "/", produces = "text/html")
    String welcome() {
        return welcomeContent + " Loaded " + loaded + " documents.";
    }

    @GetMapping(value = "/search", produces = "application/json")
    public static List<Map<String, String>> search(@RequestParam String sites, @RequestParam String query) {
        query = query.toLowerCase();
        Set<MatchedBean> foundNameBeans = new HashSet<>();
        final String[] siteNames = sites.split(",");

        for (String siteName : siteNames) {
            Map<String, NameBean> personInfo = siteData.get(siteName);
            if (personInfo != null) for (Map.Entry<String, NameBean> entry : personInfo.entrySet()) {
                String email = entry.getKey().toLowerCase();
                final NameBean nameBean = entry.getValue();
                String firstname = nameBean.getFirstname().toLowerCase();
                String lastname = nameBean.getLastname().toLowerCase();
                String displayname = nameBean.getDisplayname().toLowerCase();

                if (email.startsWith(query) || firstname.startsWith(query) || lastname.startsWith(query) || displayname.startsWith(query)) {
                    foundNameBeans.add(new MatchedBean(email, nameBean.getName(), nameBean.getLastusedtime()));
                }
            }
        }

        List<MatchedBean> nameBeans = new ArrayList<>(foundNameBeans);
        nameBeans.sort((o1, o2) -> o2.getLastUsed().compareTo(o1.getLastUsed()));

        List<Map<String, String>> matchedData = new ArrayList<>();
        for (MatchedBean bean : nameBeans) {
            Map<String, String> data = new HashMap<>();
            data.put("e", bean.getEmail());
            data.put("n", bean.getName());
            matchedData.add(data);
        }

        return matchedData;
    }

    @GetMapping(value = "/searchdb", produces = "application/json")
    public static List<Map<String, String>> searchDB(@RequestParam String sites, @RequestParam String query) {
        final String[] siteNames = sites.toLowerCase().split(",");
        List<String> siteParams = new ArrayList<>(Arrays.asList(siteNames));

        SqlQuerySpec spec = new SqlQuerySpec("SELECT c.emailaddress,c.firstname,c.lastname " +
                "FROM ulineaddressbook c where array_contains(@sites,lower(c.sitename)) and " +
                "(startswith(c.emailaddress,@query,true) or startswith(c.firstname,@query,true) or startswith(c.lastname,@query,true) " +
                "or startswith(c.displayname,@query,true)) " +
                "order by c.lastusedtime desc",
                new SqlParameter("@sites", siteParams),
                new SqlParameter("@query", query));

        CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems(spec, new CosmosQueryRequestOptions(), SiteBean.class);
        List<Map<String, String>> matchedData = new ArrayList<>();
        Set<String> addedEmails = new HashSet<>();
        for (SiteBean bean : familiesPagedIterable) {
            final String email = bean.getEmailaddress().toLowerCase();
            if (addedEmails.contains(email)) continue;

            addedEmails.add(email);
            Map<String, String> data = new HashMap<>();
            data.put("e", email);
            data.put("n", getName(bean.getFirstname(), bean.getLastname()));
            matchedData.add(data);
        }
        return matchedData;
    }

    private static String getName(String firstname, String lastname) {
        if (firstname == null && lastname == null) return "";

        if (lastname == null) return firstname;

        if (firstname == null) return lastname;

        return firstname + " " + lastname;
    }

    @PostMapping("/add")
    public static String add(@Valid @RequestBody List<AddRequest> addRequests) {
        for (AddRequest request : addRequests) {
            request.setSite(request.getSite().toLowerCase());

            Map<String, NameBean> personInfo = siteData.computeIfAbsent(request.getSite(), k -> new HashMap<>());
            final String email = request.getEmail().toLowerCase();
            final NameBean nameBean = personInfo.get(email);
            final Date currentDate = new Date();
            final String id = UUID.nameUUIDFromBytes(email.getBytes()).toString();
            SiteBean siteBean = new SiteBean(id, request.getSite(), request.getFirstname(), request.getLastname(), request.getDisplayname(), email, currentDate);

            if (nameBean == null) {
                personInfo.put(email, new NameBean(request.getFirstname(), request.getLastname(), request.getDisplayname(), currentDate));

                container.upsertItem(siteBean, new PartitionKey(siteBean.getSitename()), new CosmosItemRequestOptions());
            } else {
                nameBean.setLastusedtime(currentDate);

                container.upsertItem(siteBean, new PartitionKey(siteBean.getSitename()), new CosmosItemRequestOptions());
            }
        }

        return "Data added";
    }

    @PostMapping("/update")
    public static void update(@RequestParam String date) {
        Date updateDate = NameBean.defualtDate;
        if (date == null || date.trim().length() == 0) ;
        else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                updateDate = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems("SELECT c.id,c.sitename FROM ulineaddressbook c " +
                "where not is_defined(c.lastusedtime)", new CosmosQueryRequestOptions(), SiteBean.class);
        Map<String, Set<String>> idsToUpdate = new HashMap<>();
        for (SiteBean siteBean : familiesPagedIterable) {
            String id = siteBean.getId();
            String sitename = siteBean.getSitename();

            Set<String> ids = idsToUpdate.computeIfAbsent(sitename, k -> new HashSet<>());
            ids.add(id);
        }

        for (Map.Entry<String, Set<String>> entry : idsToUpdate.entrySet()) {
            String site = entry.getKey();
            Set<String> ids = entry.getValue();

            CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(site));
            for (String id : ids) {
                CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();
                cosmosPatchOperations.add("/lastusedtime", updateDate);
                cosmosBatch.patchItemOperation(id, cosmosPatchOperations);

                if (cosmosBatch.getOperations().size() >= 100) {
                    container.executeCosmosBatch(cosmosBatch);
                    cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(site));
                }
            }

            if (cosmosBatch.getOperations().size() > 0)
                container.executeCosmosBatch(cosmosBatch);
        }
    }

    private static void loadFromCosmosDB() {
        new Thread(() -> {
            CosmosClient client = new CosmosClientBuilder()
                    .endpoint(AccountSettings.HOST)
                    .key(AccountSettings.MASTER_KEY)
                    .consistencyLevel(ConsistencyLevel.EVENTUAL)
                    .buildClient();

            CosmosDatabase database = client.getDatabase("my-database");
            LOGGER.info("Database connected : my-database");

            container = database.getContainer("ulineaddressbook");
            LOGGER.info("Container read successful : ulineaddressbook");

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setQueryMetricsEnabled(true);

            CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems("SELECT c.sitename,c.emailaddress,c.firstname,c.lastname,c.displayname,c.lastusedtime FROM ulineaddressbook c where c.emailaddress !=''", queryOptions, SiteBean.class);
            LOGGER.info("Container query created");

            for (SiteBean bean : familiesPagedIterable) {
                if (loaded % 10_00_000 == 0) LOGGER.info("loaded data = " + loaded);

                bean.setSitename(bean.getSitename().toLowerCase());
                Map<String, NameBean> personInfo = siteData.computeIfAbsent(bean.getSitename(), k -> new HashMap<>());
                personInfo.put(bean.getEmailaddress(), new NameBean(bean.getFirstname(), bean.getLastname(), bean.getDisplayname(), bean.getLastusedtime()));

                loaded++;
            }
        }).start();
    }
}