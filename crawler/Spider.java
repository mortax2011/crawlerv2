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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author mohamedmortada
 */
public class Spider {
  private final  Atomicvariables pagesvisited_pagecount;
  private List<String> pagesToVisit = new LinkedList<>();
  private static  int id ; 
  public Spider(Atomicvariables atomicvar,int id){
      pagesvisited_pagecount=atomicvar;
      this.id=id ; 
  }
  public Spider(Atomicvariables atomicvar, List<String> listtovisit,int id){
      pagesvisited_pagecount=atomicvar;
      pagesToVisit=listtovisit;
      this.id=id ; 
    }
  private String nextUrl() throws Exception{
    String nextUrl;
            do
            {
              if(this.pagesToVisit.size()>0)
                    nextUrl = this.pagesToVisit.remove(0);
              else
                    return null ;
            } while(this.pagesvisited_pagecount.getvisited().contains(nextUrl));
    return nextUrl;
    }
  public void searchofthreads(String url) throws Exception{
        while( pagesvisited_pagecount.getcount()< 5000)
        {
            String currentUrl;
            SpiderThread thread = new SpiderThread();
            if(this.pagesToVisit.isEmpty())
            {
                currentUrl = url;
                if( this.pagesvisited_pagecount.getvisited().contains(currentUrl))
                break;
            }
            else
            {
                currentUrl = this.nextUrl();
                if(currentUrl==null){
                        System.out.println("to visit list is empty");
                        break;
                }
            }
              if(checkRobots(currentUrl))
            {
                if(thread.spread(currentUrl,pagesvisited_pagecount))
                {
                    this.pagesToVisit.addAll(thread.getLinks());
                    savelisttovisit( pagesToVisit);
                }
            }
          
        }	
        System.out.println(String.format("**Done** Visited %s web page(s)", this.pagesvisited_pagecount.getvisited().size()));
    }
  public static void savelisttovisit(List<String> list)throws Exception {
     try (BufferedWriter writer = new BufferedWriter(new FileWriter("tovisit"+Thread.currentThread().getName()+".txt"))) {
         for (String s:list) {
             writer.write(s);
             writer.newLine();
         }
		 writer.close();
     }catch(Exception e){
		System.out.println("failed to write to visit");
     }
   }
  static boolean checkRobots(String link) throws InterruptedException, MalformedURLException, IOException{
		//getting the hostname
		int slashes = link.indexOf("//") + 2;
		String root;
		String extension;
		try{ 
		   root = "https://"+link.substring(slashes,link.indexOf('/', slashes));
		}catch(Exception x){
                     root = "https://"+link.substring(slashes);
                }
		try{
		  extension=link.substring(link.indexOf('/', slashes));
		}
		catch(Exception x)
		{
		  extension="";
		}
		System.out.println("Host "+root);
		System.out.println("extension "+extension);
		System.out.println("Link "+link);
		int delay = 0;
		boolean crawlDelay=false;
		URL robot;
		try{
			robot = new URL(root+"/robots.txt");
		}catch (MalformedURLException e){
		  return false;
		}
		System.out.println("Robots File: "+robot.toString());
		System.out.println("-------------------------------------");
		//looking at the robots.txt
                try (BufferedReader robotstxt = new BufferedReader(new InputStreamReader(robot.openStream()))) {
                    String line="";
                    int countnull =0; 
                    boolean checked=false;
                    while((line = robotstxt.readLine()) != null&&!checked){ 
                       if(line.startsWith("User-agent: *")){
                            line = robotstxt.readLine();
                            while(line!=null&&countnull<5&&!line.startsWith("User-agent:")){
                            if(line.startsWith("Disallow")){
                                    //System.out.println("ROBOTS: "+line);
                                    if(line.length()>11)
                                    line = line.substring(10, line.length()).trim();
                                    else
                                            line = line.substring(9, line.length()).trim();
                                    if(line.startsWith("/*/"))
                                            line = line.substring(2, line.length()).trim();
                                    //System.out.println("---- "+line);
                                  
                                    String pattern = "(.*)"+line;
                                    if (pattern.contains("?")){
                                               pattern.indexOf("?");
                                             pattern=  pattern.substring(0,pattern.indexOf("?"))+"\\?";
                                               
                                    }
                              
                                    //          extension.replaceAll("/","//");
                                    Pattern r = Pattern.compile(pattern);
                                    Matcher m = r.matcher(extension);
                                    if( m.find()){
                                            System.out.println("ROBOTS: "+line);
                                            System.out.println("*STATUS: CANNOT CRAWL!");
                                            return false;
                                    }
                            }else if(countnull<5&&line.startsWith("Crawl-delay")){
                                System.out.println("ROBOTS: "+line);
                                crawlDelay=true;
                                delay = Integer.parseInt(line.substring(13, line.length()).trim());
                                System.out.println("*STATUS: CRAWL DELAY FOUND: "+delay +" SEC");
                            }
                            line = robotstxt.readLine();
                            while(line ==null)
                            {
                                     countnull++;
                                     line = robotstxt.readLine();
                                     if (countnull>10)
                                             break;
                            }
                            countnull=0;
                            checked=true; // this we will comment for later checking is the delay for the whole crawler ?
                            }
                        }
                       
                    }
                    if(crawlDelay==true){
                            System.out.println("*STATUS: DEALYING CRAWL BY: " + delay +" SEC");
                            Thread.sleep(delay * 1000);
                    }
                    else{
                            System.out.println("*STATUS: DEALYING CRAWL BY: 1 SEC");
                            Thread.sleep(1 * 1000);
                    }
                }catch (IOException e) { // no robots.txt
                        System.out.println("*STATUS: NO ROBOTS. SAFE TO CRAWL");
                        return true;
                }
    return true;
    }
}
