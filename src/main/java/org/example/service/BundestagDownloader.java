package org.example.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * This class downloads data from Bundestag website
 */
public class BundestagDownloader {

    // URL from member data
    private static final String STAMMDATEN_ZIP_URL ="https://www.bundestag.de/resource/blob/472878/MdB-Stammdaten.zip";

    // Template for AJAX request
    private static final String AJAX_URL_TEMPLATE = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/%s-%s?limit=%d&noFilterSet=true&offset=%d";

    // List of IDs for 20 and 21 Wahlperiode.
    // 1058442 => 21.WP
    // 866354 => 20.WP
    private static final List<String> WAHLPERIODEN_IDS = Arrays.asList("1058442", "866354");

    public List<String> fetchProtokolleUrls(){

        List<String> xmlUrls = new ArrayList<>();
        int limit = 20; // 20 items per request

        System.out.print("Searching protocols...");

        for (String wpId : WAHLPERIODEN_IDS){

            System.out.print("..."+"Processing WP ID:"+wpId+"...");
            int offset = 0;
            boolean hasMore = true;

            while (hasMore) {
                // URL with current WP OD and offsets
                String url = String.format(AJAX_URL_TEMPLATE, wpId, wpId,limit, offset);
                System.out.println("Request" + url);

                try {
                    Document doc = Jsoup.connect(url).get();

                    // find xml links
                    Elements links = doc.select("a[href*='/resource/blob/'][href$='.xml']");

                    if (links.isEmpty()) {

                        hasMore = false;
                        System.out.println("No more protocols found for ID:"+wpId);
                    } else {

                        for (Element link : links) {
                            String xmlUrl = link.absUrl("href");
                            xmlUrls.add(xmlUrl);
                        }
                        System.out.println("Found"+links.size()+" protocols for ID:"+wpId);
                        offset += limit;
                    }

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    hasMore = false;
                }
            }
        }
        System.out.println("Protocols found across all WPs:"+xmlUrls.size());
        return xmlUrls;
    }

    // download member data zip
    public Path downloadStammdaten(){

        Path targetPath = Paths.get("MdB-Stammdaten.zip");
        System.out.println("Downloading data...");

        try (InputStream in = new URL(STAMMDATEN_ZIP_URL).openStream()){

            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath;
        } catch (IOException e){
            System.out.println("Downloading data failed");
            return null;
        }
    }

    //unzip file
    public boolean unzipFile(Path zipFilePath, Path destDirectory){
        System.out.println("Unzipping file..."+zipFilePath.getFileName());

        try {
            Files.createDirectories(destDirectory);

            try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFilePath))){
                ZipEntry entry = zipIn.getNextEntry();

                // read zip entries
                while (entry != null) {

                    Path filePath = destDirectory.resolve(entry.getName());

                    if (!entry.isDirectory()) {

                        Files.copy(zipIn, filePath);
                    }

                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }
            System.out.println("Unzipped file successfully");
            return true;

        } catch (IOException e){
            System.out.println("Unzipping file failed");
            return false;
        }
    }
}
