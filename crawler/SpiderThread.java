/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.crawler;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class SpiderThread
{
   private static final String USER_AGENT =
   "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36" ;
   private final List<String> links;
   private Document htmlDocument;
   public SpiderThread() {
        this.links = new LinkedList<>();
    }
   public List<String> getLinks() {
        return this.links;
    }
   public boolean spread(String url,Atomicvariables AV){
        try
        {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument1 = connection.get();
            String htmltxt = connection.get().html();
            this.htmlDocument = htmlDocument1;
            if(connection.response().statusCode() == 200) // 200 is the HTTP OK status code
            {
                System.out.println("\n**Visiting** Received web page at " + url);
            }
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("**Failure** Retrieved something other than HTML");
                return false;
            }
            
            Elements linksOnPage = htmlDocument1.select("a[href]");
            System.out.println("Found (" + linksOnPage.size() + ") links");
            try {
               
               AV.savehtmldoc(url,AV,htmltxt);
           
          
            } catch (Exception ex) {
                Logger.getLogger(SpiderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            linksOnPage.forEach((link) -> {
                this.links.add(link.absUrl("href"));
            });
            return true;
        }
        catch(Exception ioe)
        {
            // We were not successful in our HTTP request
                    System.out.println("**Failure** recievd 0 bytes");
                      return false;
        }
    } 
}
