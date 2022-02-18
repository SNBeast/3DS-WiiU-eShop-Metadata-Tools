package src;

public class Constants {
    public static final String[] regions = {"JP", "AI", "AG", "AR", "AW", "BS", "BB", "BZ", "BO", "BR", "VG", "CA", "KY", "CL", "CO", "CR", "DM", "DO", "EC", "SV", "GF", "GD", "GP", "GT", "GY", "HT", "HN", "JM", "MQ", "MX", "MS", "AN", "NI", "PA", "PY", "PE", "KN", "LC", "VC", "SR", "TT", "TC", "US", "UY", "VI", "VE", "AL", "AU", "AT", "BE", "BA", "BW", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IS", "IE", "IT", "LV", "LS", "LI", "LT", "LU", "MK", "MT", "ME", "MZ", "NA", "NL", "NZ", "NO", "PL", "PT", "RO", "RU", "RS", "SK", "SI", "ZA", "ES", "SZ", "SE", "CH", "TR", "GB", "ZM", "ZW", "AZ", "MR", "ML", "NE", "TD", "SD", "ER", "DJ", "SO", "AD", "GI", "GG", "IM", "JE", "MC", "TW", "KR", "HK", "MO", "ID", "SG", "TH", "PH", "MY", "CN", "AE", "IN", "EG", "OM", "QA", "KW", "SA", "SY", "BH", "JO", "SM", "VA", "BM"};
    public static final String titleInfo = "https://samurai.ctr.shop.nintendo.net/samurai/ws";
    public static final String etcInfo = "https://ninja.ctr.shop.nintendo.net/ninja/ws";
    public static final String priceAPI = "https://api.ec.nintendo.com/v1/price";

    public static final String contents = titleInfo + "/%s/contents";
    public static final String titles = titleInfo + "/%s/titles";
    public static final String title = titleInfo + "/%s/title/%d";
    public static final String aocs = title + "/aocs";
    public static final String demo = titleInfo + "/%s/demo/%d";
    public static final String movies = titleInfo + "/%s/movies";
    public static final String movie = titleInfo + "/%s/movie/%d";
    public static final String news = titleInfo + "/%s/news";
    public static final String telops = titleInfo + "/%s/telops?shop_id=%d";
    public static final String directories = titleInfo + "/%s/directories";
    public static final String directoryInt = titleInfo + "/%s/directory/%d";
    public static final String directoryString = titleInfo + "/%s/directory/~%s";
    public static final String genres = titleInfo + "/%s/genres";
    public static final String publishers = titleInfo + "/%s/publishers";
    public static final String platforms = titleInfo + "/%s/platforms";
    public static final String languages = titleInfo + "/%s/languages";

    public static final String ec_info = etcInfo + "/%s/title/%d/ec_info";
    public static final String id_pair_give_titleID = etcInfo + "/titles/id_pair?title_id[]=%016X";
    public static final String id_pair_give_nsUID = etcInfo + "/titles/id_pair?ns_uid[]=%d";
    public static final String tax_locations = etcInfo + "/%s/tax_locations?postal_code=%d";
    public static final String service_hosts = etcInfo + "/service_hosts?country=%s&shop_id=%d";
    public static final String country = etcInfo + "/country/%s";
    public static final String replenish_amounts = country + "/replenish_amounts";

    public static final String price = priceAPI + "?country=%s&lang=%s&ids=%d";
}
