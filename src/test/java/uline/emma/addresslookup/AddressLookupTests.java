package uline.emma.addresslookup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

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
}