package uline.emma.addresslookup;

import io.micrometer.core.instrument.util.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;

@SpringBootTest
class AddressLookupTests {
    @Test
    void contextLoads() {
        Map<String, String> test = new HashMap<>();
        test.put("a", "b");

        for (Map.Entry<String, String> entry : test.entrySet()) {
            test.put("c", "d");

            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        for (Map.Entry<String, String> entry : test.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    @Test
    void add() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://uline-hello-world.azurewebsites.net/add");
        final String body = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("add-request.json"));
        StringEntity params = new StringEntity(body);
        httpPost.addHeader("content-type", "application/json");
        httpPost.setEntity(params);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        Scanner sc = new Scanner(httpResponse.getEntity().getContent());

        //Printing the status line
        System.out.println(httpResponse.getStatusLine());
        while (sc.hasNext()) {
            System.out.println(sc.nextLine());
        }
    }

    @Test
    void descendingTest() {
        List<MatchedBean> foundNameBeans = new ArrayList<>();
        foundNameBeans.add(new MatchedBean("a", "a", new Date(2023, Calendar.FEBRUARY, 15)));
        foundNameBeans.add(new MatchedBean("b", "b", new Date(2023, Calendar.FEBRUARY, 14)));
        foundNameBeans.add(new MatchedBean("c", "c", new Date(2023, Calendar.FEBRUARY, 16)));

        foundNameBeans.sort((o1, o2) -> o2.getLastUsed().compareTo(o1.getLastUsed()));

        System.out.println("foundNameBeans = " + foundNameBeans);
    }
}