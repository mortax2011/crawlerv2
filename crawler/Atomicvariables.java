/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mohamedmortada
 */
public class Atomicvariables {
    
    private  final AtomicInteger count = new AtomicInteger(0); //count of threads
    Set<String> pagesVisited = new HashSet<>();   //list of page visited
    public synchronized int setcount(int cnt){
       return this.count.getAndSet(cnt);
    }
    public synchronized int increment() throws Exception {  
         
         return count.incrementAndGet();
            
     }   
    public synchronized int getcount() throws Exception {  
         
         return count.get();      
     }   
    public synchronized static void savelistvisited(Set<String> urls)throws Exception {
         
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("visited.txt"))) {
         for (String s:urls) {
             writer.write(s);
             writer.newLine();
         }
         writer.close();
     }  
   }
    public synchronized Set<String> getvisited(){
        return pagesVisited;
    }
    public synchronized void  addurltovisited(String url) throws Exception{
         pagesVisited.add(url);
         savelistvisited(getvisited());
       }
    public synchronized static void savehtmldoc(String url,Atomicvariables AV,String html)throws Exception {
       if(AV.getcount()<5000&&!AV.getvisited().contains(url)){
        BufferedWriter writerx = new BufferedWriter(new FileWriter("C:\\Users\\mohamedmortada\\Desktop\\term 8\\apt\\webcrawlerv2\\Web Crawler\\sites\\"+AV.increment()+".html")); 
        if(writerx!=null)
        {
             writerx.write(url) ;
             writerx.newLine() ;
             writerx.write(html) ;
             writerx.close();
        }   
        AV.addurltovisited(url);
       }

   }
}
