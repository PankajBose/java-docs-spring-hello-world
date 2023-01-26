package uline.emma.addresslookup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@RestController
public class DemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);
	private static final Map<String, Map<String, String>> siteData = new HashMap<>();
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping("/")
	String sayHello() {
		return "Hello World!";
	}

	@GetMapping("/search")
    public static String search(@RequestParam String siteName, @RequestParam String displayName) {
        //long l = System.currentTimeMillis();

        Map<String, String> matchedData = new HashMap<>();

        Map<String, String> personInfo = siteData.get(siteName);
        if (personInfo != null) for (Map.Entry<String, String> entry : personInfo.entrySet()) {
            String personName = entry.getKey();
            String email = entry.getValue();

            if (personName.toLowerCase().contains(displayName) || email.toLowerCase().contains(displayName))
                matchedData.put(personName, email);
        }


        //LOGGER.info("time taken = " + (System.currentTimeMillis() - l) + "ms");
        return matchedData.toString();
    }

    @GetMapping("/load")
    public static String loadFromCosmosDB() {
        try (CosmosClient client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
             //   .preferredRegions(Collections.singletonList("East US 2"))
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient()) {

            CosmosDatabase database = client.getDatabase("my-database");
            LOGGER.info("Database connected : my-database");

         //   CosmosDatabaseResponse read = database.read();
         //   System.out.println("read = " + read);
         //   LOGGER.info("Database read sucecssful : my-database");

            CosmosContainer container = database.getContainer("UlineAddressBookPOC");
        //    CosmosContainerResponse read1 = container.read();
        //    System.out.println("read1 = " + read1);
            LOGGER.info("Container read sucecssful : UlineAddressBookPOC");

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
            return "data load successful!";
        }
        catch(Exception e)
        {
            LOGGER.error("Database connection error:", e);
            return e.getMessage();
        }
    }
}
