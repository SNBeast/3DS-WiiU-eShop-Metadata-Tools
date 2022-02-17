import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

public class URLUtils {
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
    
    public static final String[] regions = {"JP", "AI", "AG", "AR", "AW", "BS", "BB", "BZ", "BO", "BR", "VG", "CA", "KY", "CL", "CO", "CR", "DM", "DO", "EC", "SV", "GF", "GD", "GP", "GT", "GY", "HT", "HN", "JM", "MQ", "MX", "MS", "AN", "NI", "PA", "PY", "PE", "KN", "LC", "VC", "SR", "TT", "TC", "US", "UY", "VI", "VE", "AL", "AU", "AT", "BE", "BA", "BW", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IS", "IE", "IT", "LV", "LS", "LI", "LT", "LU", "MK", "MT", "ME", "MZ", "NA", "NL", "NZ", "NO", "PL", "PT", "RO", "RU", "RS", "SK", "SI", "ZA", "ES", "SZ", "SE", "CH", "TR", "GB", "ZM", "ZW", "AZ", "MR", "ML", "NE", "TD", "SD", "ER", "DJ", "SO", "AD", "GI", "GG", "IM", "JE", "MC", "TW", "KR", "HK", "MO", "ID", "SG", "TH", "PH", "MY", "CN", "AE", "IN", "EG", "OM", "QA", "KW", "SA", "SY", "BH", "JO", "SM", "VA", "BM"};

    private static SSLSocketFactory ctrSSLSocketFactory = null;
    private static SSLSocketFactory wiiuSSLSocketFactory = null;
    private static SSLSocketFactory defaultSSLSocketFactory = null;

    // mostly from https://stackoverflow.com/a/7016969, by bobz32
    static {
        KeyStore ctrClientStore = null;
        KeyStore wiiuClientStore = null;
        try {
            ctrClientStore = KeyStore.getInstance("PKCS12");
            ctrClientStore.load(new FileInputStream("certs/ctr-common-1.p12"), "alpine".toCharArray());
            wiiuClientStore = KeyStore.getInstance("PKCS12");
            wiiuClientStore.load(new FileInputStream("certs/WIIU_COMMON_1_CERT.p12"), "alpine".toCharArray());
        } catch (CertificateException | IOException e) {
            System.err.println("You need to have the CTR common prod cert, as \"ctr-common-1.p12\", and the Wii U common prod cert, as \"WIIU_COMMON_1_CERT.p12\", both with password \"alpine\" and both in a \"certs\" subdirectory");
            System.exit(1);
        } catch (KeyStoreException e) {
            System.err.println("Your system does not support PKCS12 certs, idk why");
            System.exit(-1);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        KeyManager[] ctrKMs = null;
        KeyManager[] wiiuKMs = null;
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ctrClientStore, "alpine".toCharArray());
            ctrKMs = kmf.getKeyManagers();
            kmf.init(wiiuClientStore, "alpine".toCharArray());
            wiiuKMs = kmf.getKeyManagers();
        } catch (UnrecoverableKeyException e) {
            System.err.println("key password is not set to \"alpine\"!");
            System.exit(1);
        } catch (KeyStoreException e) {
            System.err.println("error loading keymanagers: " + e.getMessage());
            System.exit(-1);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("your default KeyManagerFactory algorithm doesn't exist? huh?");
            System.exit(-1);
        }

        // this was surprisingly difficult to find, from https://stackoverflow.com/a/58557415 by Johannes Brodwall
        KeyStore nintendoTrustStore = null;
        try {
            nintendoTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            nintendoTrustStore.load(null, null);
            X509Certificate result = (X509Certificate)CertificateFactory.getInstance("X509").generateCertificate(new FileInputStream("certs/CACERT_NINTENDO_CA_G3.der"));
            nintendoTrustStore.setCertificateEntry("CACERT_NINTENDO_CA_G3", result);
        } catch (CertificateException | IOException e) {
            System.err.println("You need to have the Nintendo CA G3 cert, as \"CACERT_NINTENDO_CA_G3.der\" in a \"certs\" subdirectory");
            System.exit(1);
        } catch (KeyStoreException e) {
            System.err.println("error creating truststore: " + e.getMessage());
            System.exit(-1);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        TrustManager[] nintendoTrustManagers = null;
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(nintendoTrustStore);
            nintendoTrustManagers = tmf.getTrustManagers();
        } catch (KeyStoreException e) {
            System.err.println("error initting the truststore: " + e.getMessage());
            System.exit(-1);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("your default KeyManagerFactory algorithm doesn't exist? huh?");
            System.exit(-1);
        }

        // the moment you've all been waiting for
        try {
            SSLContext ctrSSLContext = SSLContext.getInstance("SSL");
            ctrSSLContext.init(ctrKMs, nintendoTrustManagers, new SecureRandom());
            ctrSSLSocketFactory = ctrSSLContext.getSocketFactory();

            SSLContext wiiuSSLContext = SSLContext.getInstance("SSL");
            wiiuSSLContext.init(wiiuKMs, nintendoTrustManagers, new SecureRandom());
            wiiuSSLSocketFactory = wiiuSSLContext.getSocketFactory();

            defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SSL is not present!");
            System.exit(-1);
        } catch (KeyManagementException e) {
            System.err.println("error initting the sslcontexts: " + e.getMessage());
        }
    }

    private static InputStream openStreamWithFallback (URL url, boolean wiiu) {
        try {
            return url.openStream();
        } catch (SSLHandshakeException e) {
            SSLSocketFactory currentSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
            if (currentSSLSocketFactory.equals(defaultSSLSocketFactory)) {
                HttpsURLConnection.setDefaultSSLSocketFactory(wiiu ? wiiuSSLSocketFactory : ctrSSLSocketFactory);
            }
            else {
                HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
            }
            InputStream is = null;
            try {
                is = url.openStream();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(currentSSLSocketFactory);
            return is;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    public static void dumpInputStreamToFile (InputStream is, String filename) {
        try {
            Files.copy(is, Paths.get("out/" + filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static InputStream getInputStreamFromLink (String link, boolean wiiu) {
        URL url = null;
        try {
            url = new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return openStreamWithFallback(url, wiiu);
    }

    public static Document retrieveXML (String url, boolean wiiu) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(getInputStreamFromLink(url, wiiu));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("while retrieving XML, aborting");
            System.exit(1);
            return null;
        }
    }

    public static void dumpLinkContentsToFile (String link, boolean wiiu, String filename) {
        dumpInputStreamToFile(getInputStreamFromLink(link, wiiu), filename);
    }

    public static void main (String[] args) {
        HttpsURLConnection.setDefaultSSLSocketFactory(ctrSSLSocketFactory);

        dumpLinkContentsToFile(String.format(directoryString, "US", "4d53d011eda0f429ded2eeefde73e6ee"), false, "directorySelects.xml");
    }
}
