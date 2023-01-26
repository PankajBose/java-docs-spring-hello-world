package uline.emma.addresslookup;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@SpringBootApplication
@RestController
public class AddressLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressLookup.class);
    private static final Map<String, Map<String, String>> siteData = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        SpringApplication.run(AddressLookup.class, args);
        loadFromCosmosDB();
    }

    @RequestMapping("/")
    String sayHello() {
        return "Welcome to address lookup application";
    }

    @GetMapping(value = "/search", headers = "content-type=application/json")
    public static List<Map<String, String>> search(@RequestParam String siteName, @RequestParam String displayName) {
        List<Map<String, String>> matchedData = new ArrayList<>();

        Map<String, String> personInfo = siteData.get(siteName);
        if (personInfo != null) for (Map.Entry<String, String> entry : personInfo.entrySet()) {
            String personName = entry.getKey();
            String email = entry.getValue();

            if (personName.toLowerCase().contains(displayName) || email.toLowerCase().contains(displayName)) {
                Map<String, String> data = new HashMap<>();
                data.put("n", personName);
                data.put("e", email);
                matchedData.add(data);
            }
        }
        return matchedData;
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

            CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems(
                    "SELECT top 1000000 c.sitename,c.displayname,c.emailaddress FROM UlineAddressBookPOC c", queryOptions, SiteBean.class);
            LOGGER.info("Container query created ");
            for (SiteBean bean : familiesPagedIterable) {
                LOGGER.info("bean = " + bean);
                Map<String, String> personInfo = siteData.computeIfAbsent(bean.getSitename(), k -> new HashMap<>());
                personInfo.put(bean.getDisplayname(), bean.getEmailaddress());
            }
        }
    }
}