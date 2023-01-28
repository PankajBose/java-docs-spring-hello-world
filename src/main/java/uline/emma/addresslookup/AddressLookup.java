package uline.emma.addresslookup;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class AddressLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressLookup.class);
    private static final Map<String, Map<String, String>> siteData = new HashMap<>();

    public static void main(String[] args) {
        loadFromCosmosDB();

        SpringApplication.run(AddressLookup.class, args);
    }

    @RequestMapping(value = "/", produces = "text/html")
    String welcome() {
        return "Welcome to address lookup application. Build: 2023-01-28 12:30<br>" +
                "Usages:<br/>" +
                "GET: /search?siteName=global&displayName=central<br/>" +
                "POST: /add?siteName=newSite&email=newEmail&displayName=newDisplayName";
    }

    @GetMapping(value = "/search", produces = "application/json")
    public static List<Map<String, String>> search(@RequestParam String siteName, @RequestParam String displayName) {
        List<Map<String, String>> matchedData = new ArrayList<>();

        Map<String, String> personInfo = siteData.get(siteName);
        if (personInfo != null) for (Map.Entry<String, String> entry : personInfo.entrySet()) {
            String email = entry.getKey();
            String personName = entry.getValue();

            if (personName.toLowerCase().contains(displayName) || email.toLowerCase().contains(displayName)) {
                Map<String, String> data = new HashMap<>();
                data.put("e", email);
                data.put("n", personName);
                matchedData.add(data);
            }
        }

        return matchedData;
    }

    @PostMapping("/add")
    public static String add(@RequestParam String siteName, @RequestParam String email, @RequestParam String displayName) {
        Map<String, String> personInfo = siteData.computeIfAbsent(siteName, k -> new HashMap<>());
        personInfo.put(email, displayName);

        return "Data added";
    }

    private static void loadFromCosmosDB() {
        try (CosmosClient client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient()) {

            CosmosDatabase database = client.getDatabase("my-database");
            LOGGER.info("Database connected : my-database");

            CosmosContainer container = database.getContainer("UlineAddressBookPOC");
            LOGGER.info("Container read successful : UlineAddressBookPOC");

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setQueryMetricsEnabled(true);

            CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems("SELECT c.sitename,c.displayname,c.emailaddress FROM UlineAddressBookPOC c", queryOptions, SiteBean.class);
            LOGGER.info("Container query created ");
            long i = 0;
            for (SiteBean bean : familiesPagedIterable) {
                if (i % 10_00_000 == 0) LOGGER.info("loaded data = " + i);

                Map<String, String> personInfo = siteData.computeIfAbsent(bean.getSitename(), k -> new HashMap<>());
                personInfo.put(bean.getEmailaddress(), bean.getDisplayname());

                i++;
            }
        }
    }
}