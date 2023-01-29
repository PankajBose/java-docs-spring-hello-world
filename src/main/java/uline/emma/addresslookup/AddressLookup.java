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
    private static final Map<String, Map<String, NameBean>> siteData = new HashMap<>();

    public static void main(String[] args) {
        loadFromCosmosDB();

        SpringApplication.run(AddressLookup.class, args);
    }

    @RequestMapping(value = "/", produces = "text/html")
    String welcome() {
        return "Welcome to EMMA address lookup application. Build: 2023-01-29 11:00<br>" +
                "Usages:<br/>" +
                "GET: /search?siteName=global&query=central<br/>" +
                "POST: /add?siteName=newSite&email=newEmail&firstname=newFirstName&lastname=newLastName";
    }

    @GetMapping(value = "/search", produces = "application/json")
    public static List<Map<String, String>> search(@RequestParam String siteName, @RequestParam String query) {
        query = query.toLowerCase();
        List<Map<String, String>> matchedData = new ArrayList<>();

        Map<String, NameBean> personInfo = siteData.get(siteName);
        if (personInfo != null) for (Map.Entry<String, NameBean> entry : personInfo.entrySet()) {
            String email = entry.getKey().toLowerCase();
            NameBean nameBean = entry.getValue();
            String firstName = nameBean.getFirstname().toLowerCase();
            String lastname = nameBean.getLastname().toLowerCase();

            if (email.startsWith(query) || firstName.startsWith(query) || lastname.startsWith(query)) {
                Map<String, String> data = new HashMap<>();
                data.put("e", email);
                data.put("n", nameBean.getName());
                matchedData.add(data);
            }
        }

        return matchedData;
    }

    @PostMapping("/add")
    public static String add(@RequestParam String siteName, @RequestParam String email, @RequestParam String firstname, @RequestParam String lastname) {
        Map<String, NameBean> personInfo = siteData.computeIfAbsent(siteName, k -> new HashMap<>());
        personInfo.put(email, new NameBean(firstname, lastname));

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

            CosmosContainer container = database.getContainer("UlineAddressBookPOC3");
            LOGGER.info("Container read successful : UlineAddressBookPOC3");

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            queryOptions.setQueryMetricsEnabled(true);

            CosmosPagedIterable<SiteBean> familiesPagedIterable = container.queryItems("SELECT c.sitename,c.emailaddress,c.firstname,c.lastname FROM UlineAddressBookPOC3 c", queryOptions, SiteBean.class);
            LOGGER.info("Container query created ");
            long i = 0;
            for (SiteBean bean : familiesPagedIterable) {
                if (i % 10_00_000 == 0) LOGGER.info("loaded data = " + i);

                Map<String, NameBean> personInfo = siteData.computeIfAbsent(bean.getSitename(), k -> new HashMap<>());
                personInfo.put(bean.getEmailaddress(), new NameBean(bean.getFirstname(), bean.getLastname()));

                i++;
            }
        }
    }
}