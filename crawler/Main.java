/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.crawler;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mohamedmortada
 */
    public class Main
{
    public static void main(String[] args) throws FileNotFoundException, IOException{
       int threadnumber=0;
       int numberofthreads=0;
       Atomicvariables avar =new   Atomicvariables(); 
       List<String> seeds=new LinkedList<>();
       List<LinkedList> tovisitlist=new LinkedList<>();
       // opening start text
       try(BufferedReader br = new BufferedReader(new FileReader("start.txt"))) {
       StringBuilder sb = new StringBuilder();
       String line = br.readLine();
       //assumed that start text begins with number of threads then seeds 
        if (line != null) {
                 numberofthreads=Integer.parseInt(line);
                 line = br.readLine();
        }
        // copying seeds in list and creating to visit list with each new seed representing a thread
         int var1=0;
         while (var1<numberofthreads) {
             seeds.add(line);
             line = br.readLine();
             if (line==null)
             {
                 numberofthreads=var1;
                 break;
             }
             tovisitlist.add(new LinkedList<>());
             var1++;
             
            }

        }
       
       boolean paused[]=new boolean [numberofthreads];
       for(int i = 0 ; i <numberofthreads;i++)
           paused[i]=false;
        // now we will try to see wether this is a new crawl or a crawl that was interupted
        try(BufferedReader br = new BufferedReader(new FileReader("visited.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();   
            while (line != null) {
                avar.addurltovisited(line);
                line = br.readLine();
            }
            avar.setcount(avar.getvisited().size());
            for ( threadnumber = 0; threadnumber <numberofthreads ; threadnumber++)   
            {  try(BufferedReader br1 = new BufferedReader(new FileReader("tovisitpool-1-thread-"+String.valueOf(threadnumber+1)+".txt"))) {
                    paused[threadnumber]=true;
                    line = br1.readLine();
                   while (line != null) {
                    tovisitlist.get(threadnumber).add(line);
                    line = br1.readLine();
                    }
                }
                catch(Exception e){
                    System.out.print("there is no to visit lists yet in thread number");
                    System.out.println(threadnumber);
                    paused[threadnumber]=false;
                }
            }
        }catch(Exception e){
             System.out.println("there is no visited list yet");
       
             }
         // from here we either are starting new crawl or continuing an old one 
        ExecutorService executor = Executors.newFixedThreadPool(numberofthreads); 
        Runnable workers[]=new Runnable [numberofthreads];
        for (int i = 0; i <numberofthreads ; i++) { 
            workers[i] = new wakingspider( seeds.get(i),avar,i,paused[i],tovisitlist.get(i));  
        }
        for (int i = 0; i <numberofthreads ; i++) { 
        executor.execute(workers[i]);//calling execute method of ExecutorService  
        }
        
                executor.shutdown();  
                while (!executor.isTerminated()) {   }  
                System.out.println("Finished all threads");  
              
    }  
}
class wakingspider implements Runnable{
   private final String urllink ;
   private final  int id;
   private final Atomicvariables avar;
   private final  boolean cpy;
   private List<String> tovisit=new LinkedList<>();    
   public wakingspider(String url,Atomicvariables atomicvars,int id,boolean cpy,List<String> tovisit){
     this.avar=atomicvars ;
     this.urllink=url;
     this.id=id;
     this.cpy=cpy;
     Thread.currentThread().setName(String.valueOf(id));
     this.tovisit=tovisit;    
    } 
@Override
   public void run () {
           Spider spider;
           if(!cpy){
                    spider  = new Spider(avar,id);
                    try {
                          spider.searchofthreads(urllink);
                    }catch (Exception ex) {
                          Logger.getLogger(wakingspider.class.getName()).log(Level.SEVERE, null, ex);
                    }
           }else{
                      spider = new Spider(avar,tovisit,id);  
                       try {
                           if(!tovisit.isEmpty())
                               spider.searchofthreads(tovisit.get(0));
                           else
                              spider.searchofthreads(urllink); 
                        } catch (Exception ex) {
                            Logger.getLogger(wakingspider.class.getName()).log(Level.SEVERE, null, ex);
                        }
            }
	}
}

    

