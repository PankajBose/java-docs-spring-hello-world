package uline.emma.addresslookup;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.micrometer.core.instrument.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@RestController
public class AddressLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressLookup.class);
    private static final Map<String, Map<String, NameBean>> siteData = new HashMap<>();
    private static CosmosContainer container;
    private static final String welcomeContent = IOUtils.toString(AddressLookup.class.getClassLoader().getResourceAsStream("welcome-message.html"));

    public static void main(String[] args) {
        loadFromCosmosDB();

        SpringApplication.run(AddressLookup.class, args);
    }

    @RequestMapping(value = "/", produces = "text/html")
    String welcome() {
        return welcomeContent;
    }

    @GetMapping(value = "/search", produces = "application/json")
    public static Map<String, String> search(@RequestParam String sites, @RequestParam String query) {
        query = query.toLowerCase();
        Map<String, String> matchedData = new HashMap<>();
        final String[] siteNames = sites.split(",");

        for (String siteName : siteNames) {
            Map<String, NameBean> personInfo = siteData.get(siteName);
            if (personInfo != null) for (Map.Entry<String, NameBean> entry : personInfo.entrySet()) {
                String email = entry.getKey().toLowerCase();
                NameBean nameBean = entry.getValue();
                String firstName = nameBean.getFirstname().toLowerCase();
                String lastname = nameBean.getLastname().toLowerCase();

                if (email.startsWith(query) || firstName.startsWith(query) || lastname.startsWith(query)) {
                    matchedData.put("e", email);
                    matchedData.put("n", nameBean.getName());
                }
            }
        }

        return matchedData;
    }

    @PostMapping("/add")
    public static String add(@Valid @RequestBody List<AddRequest> addRequests) {
        for (AddRequest request : addRequests) {
            Map<String, NameBean> personInfo = siteData.computeIfAbsent(request.getSite(), k -> new HashMap<>());
            final String email = request.getEmail().toLowerCase();
            if (personInfo.containsKey(email)) continue;

            personInfo.put(email, new NameBean(request.getFirstname(), request.getLastname()));

            final String id = UUID.nameUUIDFromBytes(email.getBytes()).toString();
            SiteBean siteBean = new SiteBean(id, request.getSite(), request.getFirstname(), request.getLastname(), email);
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            CosmosItemResponse<SiteBean> item = container.upsertItem(siteBean, new PartitionKey(siteBean.getSitename()), cosmosItemRequestOptions);
            LOGGER.info("item = " + item);
        }

        return "Data added";
    }

    private static void loadFromCosmosDB() {
        CosmosClient client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        CosmosDatabase database = client.getDatabase("my-database");
        LOGGER.info("Database connected : my-database");

        container = database.getContainer("UlineAddressBookPOC3");
        LOGGER.info("Container read successful : UlineAddressBookPOC3");

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems("SELECT c.sitename,c.emailaddress,c.firstname,c.lastname FROM TestContainer c where c.displayname !=\"\"", queryOptions, SiteBean.class);
        LOGGER.info("Container query created");
        long i = 0;
        for (SiteBean bean : familiesPagedIterable) {
            if (i % 10_00_000 == 0) LOGGER.info("loaded data = " + i);

            Map<String, NameBean> personInfo = siteData.computeIfAbsent(bean.getSitename(), k -> new HashMap<>());
            personInfo.put(bean.getEmailaddress(), new NameBean(bean.getFirstname(), bean.getLastname()));

            i++;
        }
    }
}