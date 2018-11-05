package org.tamu.nlp.CrawlWeb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.*;

import com.google.common.collect.Sets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Hello world!
 */
public class App {
    public static WebDriver driver;
    public static JavascriptExecutor js;
    public static final String DOMAIN = "https://community.cartalk.com";

    public static void main(String[] args) throws IOException {
        //Give the absolute path to chrome driver
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        Scanner sc=new Scanner(System.in);
        driver = new ChromeDriver();
        //driver.manage().window().maximize();
        js = (JavascriptExecutor) driver;
        
        //writeFile();
        //concatAllData();
        int sw=0;
        do {
        	System.out.println("Enter 1 to crawl, 2 to collect links, 3 to concat files, 4 to filter, 0 to exit");
        	sw= sc.nextInt();
        	switch(sw)
        	{
        		case 1:
        			crawlLinks();
        			break;
        		case 2:
        			writeFile(1);
        			break;
        		case 3:
        			concatAllData();
        			break;
        		case 4:
        			writeFile(2);
        			break;
        			
        	}
        	
        }while(sw != 0);
        driver.quit();
    }
    
    public static void crawlLinks()
    {
    	try {
    		BufferedWriter writer = new BufferedWriter(new FileWriter("curr.csv", true));
            writer.write("post id, post url, title, category, tags,  reply id, userid, reply");
            writer.newLine();
            processLinks(writer, "", "https://community.cartalk.com/t/a-c-clutch-on-2000-plymouth-voyager/71");
            
            String mainLink = DOMAIN + "/c/repair-and-maintenance?ascending=true&order=activity";
            Set<String> links = getLinks(mainLink, 2);
         
            int i = 0;       
            for (String link : links) {
                System.out.println(i + " " + link);
                String[] str = link.split("/");
                String curr = str[str.length - 1] + "," + link + ",";
                processLinks(writer, curr, link);
                i++;
            }

            writer.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public static void writeFile(int sw)
    {
    	String mainLink = DOMAIN + "/c/repair-and-maintenance?ascending=true&order=activity";
    	Set<String> links;
    	if(sw == 1)
    		links = getLinks(mainLink, 1);
    	else if(sw == 2)
    		links = filterLinks();
    	else
    		links = concatLinks();
    	
    	try {
	        BufferedWriter writer1 = new BufferedWriter(new FileWriter("links3.txt", true));
	        System.out.println("\nGoing to crawl " + links.size() + " links.");
	        int i = 0;
	        for (String link : links) {
	            //System.out.println(i + " " + link);
	            writer1.write(link);
	            writer1.newLine();
	            i++;
	        }
	
	        writer1.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    private static Set<String> filterLinks() {
    	Set<String> links = new HashSet<>();
    	try {
    		BufferedReader reader= new BufferedReader(new FileReader("links.txt"));
    		String line;
			while((line= reader.readLine()) != null)
    		{
				//line=line.substring(29);
    			links.add(line);
    		}
			reader.close();
			
			reader= new BufferedReader(new FileReader("res.txt"));
			while((line= reader.readLine()) != null)
    		{
				if(line.length() == 0)
					continue;
				line=line.split(" ")[1];
				//System.out.println(line);
				if(links.contains(line))
				{
					//System.out.println("aaaaaa");
					links.remove(line);
				}
    		}
			reader.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return links;
    }
    
    private static void concatAllData()
    {
    	Set<String> links = new HashSet<>();
    	try {
	    	for(int i=1; i <= 2; i ++)
	    	{
	    		BufferedReader reader= new BufferedReader(new FileReader("/Users/sambartikaguha/Desktop/CrawlLinks/Crawl_v"+i+"/res_v"+i+".txt"));
	    		String line;
	    		int count=0;
				while((line= reader.readLine()) != null)
	    		{
					//line=line.substring(29);
					if(line.length() == 0)
						continue;
					line=line.split(" ")[1];
					if(links.contains(line))
						count++;
	    			links.add(line);
	    		}
				reader.close();
				System.out.println("repetations "+count);
	    	}
	    	
	    	BufferedWriter writer1 = new BufferedWriter(new FileWriter("crawled_all.txt", true));
	        System.out.println("\nGoing to crawl " + links.size() + " links.");
	        for (String link : links) {
	            writer1.write(link);
	            writer1.newLine();
	        }
	
	        writer1.close();
	        
	        
	        writer1 = new BufferedWriter(new FileWriter("curr_all.csv", true));
            writer1.write("post id, post url, title, category, tags,  reply id, userid, reply");
            writer1.newLine();
            int c=0;
            for(int i=1; i <= 2; i ++)
	    	{
	    		BufferedReader reader= new BufferedReader(new FileReader("/Users/sambartikaguha/Desktop/CrawlLinks/Crawl_v"+i+"/curr_v"+i+".csv"));
	    		String line;
	    		int count=0;
	    		
	    		line= reader.readLine();
				while((line= reader.readLine()) != null)
	    		{
					if(line.length() == 0)
						continue;
					writer1.write(line);
					writer1.newLine();
	    			c++;
	    		}
				reader.close();
	    	}
            
			System.out.println("Total "+c);            
            writer1.close();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    private static Set<String> concatLinks() {
    	Set<String> links = new HashSet<>();
    	try {
    		BufferedReader reader= new BufferedReader(new FileReader("res_v1.txt"));
    		String line;
			while((line= reader.readLine()) != null)
    		{
				//line=line.substring(29);
				if(line.length() == 0)
					continue;
				//line=line.split(" ")[1];
    			links.add(line);
    		}
			reader.close();
			int count=0;
			reader= new BufferedReader(new FileReader("res.txt"));
			while((line= reader.readLine()) != null)
    		{
				if(line.length() == 0)
					continue;
				line=line.split(" ")[1];
				if(links.contains(line))
				{
					//System.out.println("aaaaaaa");
					count++;
				}
				links.add(line);
    		}
			reader.close();
			System.out.println("No of repeted links "+count);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return links;
    }

    //Get links from the main page
    private static Set<String> getLinks(String mainLink, int type) {
        driver.get(mainLink);
        Document doc=null;
        if(type == 1)
        {
	        //This will scroll the web page n times.
	        for (int i = 0; i < 200000; i++) {
	        	System.out.println(i);
	            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	            waitForLoad(driver);
	        }
	        doc= Jsoup.parse(driver.getPageSource());
	        
        }
        else if(type == 2)
        {
        	try {
        		BufferedReader reader= new BufferedReader(new FileReader("links.txt"));
        		String line;
        		Set<String> links = new HashSet<>();
    			while((line= reader.readLine()) != null)
        		{
        			links.add(line);
        		}
    			reader.close();
    			return links;
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        }
        else
        {
	    	
	    	try {
		    	File input = new File("/Users/sambartikaguha/Dropbox/Latest Maintenance_Repairs topics - Car Talk Community.htm");
		    	doc = Jsoup.parse(input, "UTF-8", "");
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
        }
        
        Elements links = doc.getElementsByClass("link-top-line");
        return links.stream().map(l -> DOMAIN + l.select("a").get(0).attr("href")).collect(Collectors.toSet());
    	
    }

    //Process each post thread
    private static void processLinks(BufferedWriter writer, String curr, String link) {
        // TODO Auto-generated method stub
        try {
            // Launch the application
            driver.get(link);
            waitToLoadHeader(driver);


            String src = driver.getPageSource(), newSrc = "";
            Set<String> sources = Sets.newHashSet();
            while (!newSrc.equals(src)) {
                sources.add(src);
                js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
                waitForLoad(driver);
                src = newSrc;
                newSrc = driver.getPageSource();
            }

            Document doc = Jsoup.parse(src);
            String res = curr;
            Elements types = doc.select("h1");
            try {
                Element typeLinks = types.get(0).select("a[href]").get(0);
                res = res+"\""+typeLinks.text().replaceAll("\"", "\"\"") + "\",";
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception 7");
                res += " ,";
            }

            try {
                Element typeBreadCrumbs = doc.getElementsByClass("title-wrapper").get(0);
                Elements typeLinksBreadCrumbs = typeBreadCrumbs.getElementsByClass("category-name");
                for (Element type : typeLinksBreadCrumbs) {
                    res = res+"\""+type.text().replaceAll("\"", "\"\"") + "\",";
                }
            } catch (Exception e) {
                System.out.println("Exception 3: title wrapper not present.");
                e.printStackTrace();
                res += " ,";
            }
            try {
                Element typesTag = doc.getElementsByClass("discourse-tags").get(0);
                Elements typeSpan = typesTag.select("a");
                String tags = "\"||";
                for (Element type : typeSpan) {
                    tags += type.text().replaceAll("\"", "\"\"") + "||";
                }
                tags += "\",";
                res += tags;
            } catch (Exception e) {
                System.out.println("Exception 4: discourse tags not present.");
                e.printStackTrace();
                res += " ,";
            }
            Set<String> st = Sets.newHashSet();
            for(String s : sources) {
                addPosts(writer, res, s, st);
            }

        } catch (Exception e) {
            System.out.println("Exception 5");
            e.printStackTrace();
        }
    }

    //Process each post
    public static void addPosts(BufferedWriter writer, String curr, String src, Set<String> st) {
        // TODO Auto-generated method stub
        try {
            // Converting unordered / ordered lists to paragraphs so they can be collected below.
            src = src.replace("<li>", "<p>").replace("<\\li>", "<\\p>");

            Elements typesTag = Jsoup.parse(src).getElementsByClass("topic-post");

            for (Element type : typesTag) {
                Element ele = type.getElementsByTag("article").get(0);
                String id = ele.attr("id");
                if (!st.contains(id)) {
                    String res = curr;
                    st.add(id);
                    int idi = Integer.parseInt(id.replaceAll("post_", ""));
                    res += idi + ",\"";
                    Element username = ele.getElementsByClass("username").get(0);
                    res += username.text() + "\",\"";
                    Elements quotes = ele.select("blockquote > p");
                    Elements paras = ele.getElementsByClass("regular").get(0).select("p,h1,h2,h3,h4,h5");
                    Element pa = ele.getElementsByClass("regular").get(0);
                    String content = "";
                    for (Element para : paras) {
                        if (quotes.contains(para)) {
                            content += " $ " + para.text() + " $ \n\n";
                        } else {
                        	if(content.length() > 0)
                        		content+= "\n\n";
                        	//System.out.println("para "+content);
                        	para.select("br").append("\\nl");
                            content += para.text().replaceAll("\\\\nl", "\n");
                            //System.out.println("para "+content);
                        }
                        
                    }
                    
                    if(content.length() == 0)
                    {
                    	Elements cooked = ele.getElementsByClass("cooked");
                    	for (Element para : cooked) {
                    		if(content.length() > 0)
                        		content+= "\n\n";
                    		//System.out.println("para "+content);
                    		content += para.text();
                    	}
                    }
                    
                    content = content.replaceAll("\"", "\"\"");
                    res = res+content+"\"";
                    writer.write(res);
                    writer.newLine();
                    st.add(id);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception 6");
            e.printStackTrace();
        }
    }

    public static void waitForLoad(WebDriver driver) {
        //System.out.print("++");
        ExpectedCondition<Boolean> pageLoadCondition = d -> {
            Boolean pageLoaded = ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
            if (pageLoaded) {
                return Jsoup.parse(d.getPageSource()).select(".spinner").size() == 0;
            }
            return pageLoaded;
        };
        WebDriverWait wait = new WebDriverWait(driver, 200);
        wait.until(pageLoadCondition);
        //System.out.print("xx");
    }

    public static void waitToLoadHeader(WebDriver driver) {
        //System.out.print(" ..");
        ExpectedCondition<Boolean> pageLoadCondition = d -> Jsoup.parse(d.getPageSource()).select("h1").size() > 0;
        WebDriverWait wait = new WebDriverWait(driver, 200);
        wait.until(pageLoadCondition);
        //System.out.print("**");
    }
}
