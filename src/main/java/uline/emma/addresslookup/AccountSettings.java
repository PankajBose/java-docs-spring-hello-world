package uline.emma.addresslookup;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

public class AccountSettings {
    // Replace MASTER_KEY and HOST with values from your Azure Cosmos DB account.
    // The default values are credentials of the local emulator, which are not used in any production environment.
    // <!--[SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine")]-->
    public static String MASTER_KEY =
            System.getProperty("ACCOUNT_KEY",
                    StringUtils.defaultString(StringUtils.trimToNull(
                                    System.getenv().get("ACCOUNT_KEY")),
                            "wTwA19DqqxEwFBrEYDGv5rGAuLkr5Tk6ov9N1FSLAh9r7h7a35Xb9OAp8Yknup8AW5xYGYx1njWWvsjzW8GqEw=="));

    public static String HOST =
            System.getProperty("ACCOUNT_HOST",
                    StringUtils.defaultString(StringUtils.trimToNull(
                                    System.getenv().get("ACCOUNT_HOST")),
                            "https://emmacosmosdbsql.documents.azure.com:443/"));
}