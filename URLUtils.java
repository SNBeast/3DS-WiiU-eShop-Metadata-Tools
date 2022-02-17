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
        dumpLinkContentsToFile("https://www.google.com", false, "google.html");
        dumpLinkContentsToFile(etcInfo + "/country/US", false, "ninjaCountryUS.xml");
    }
}
