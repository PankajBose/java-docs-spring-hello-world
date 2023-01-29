package uline.emma.addresslookup;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
        HttpPost httpPost = new HttpPost("https://uline-hello-world.azurewebsites.net/add?siteName=newSite&email=newEmail&firstname=newFirstName&lastname=newLastName");
        HttpResponse httpResponse = httpClient.execute(httpPost);
        Scanner sc = new Scanner(httpResponse.getEntity().getContent());

        //Printing the status line
        System.out.println(httpResponse.getStatusLine());
        while (sc.hasNext()) {
            System.out.println(sc.nextLine());
        }
    }
}