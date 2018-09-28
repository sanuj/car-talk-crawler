package org.tamu.nlp.CrawlWeb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

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
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver;

        BufferedWriter writer = new BufferedWriter(new FileWriter("curr.csv", true));
        writer.write("post id, post url, title, category, tags,  reply id, userid, reply");
        writer.newLine();

        String mainLink = DOMAIN + "/c/repair-and-maintenance";
        Set<String> links = getLinks(mainLink);

        System.out.println("\nGoing to crawl " + links.size() + " links.");
        int i = 0;
        for (String link : links) {
            System.out.print(i + " " + link);

            String[] str = link.split("/");
            String curr = str[str.length - 1] + "," + link + ",";
            processLinks(writer, curr, link);

            System.out.println();
            i++;
        }

        writer.close();
        driver.quit();
    }

    //Get links from the main page
    private static Set<String> getLinks(String mainLink) {
        driver.get(mainLink);

        //This will scroll the web page n times.
        for (int i = 0; i < 60; i++) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            waitForLoad(driver);
        }

        Elements links = Jsoup.parse(driver.getPageSource()).getElementsByClass("link-top-line");
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
                res += typeLinks.text().replaceAll("\\,", " ") + ",";
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception 7");
                res += " ,";
            }

            try {
                Element typeBreadCrumbs = doc.getElementsByClass("title-wrapper").get(0);
                Elements typeLinksBreadCrumbs = typeBreadCrumbs.getElementsByClass("category-name");
                for (Element type : typeLinksBreadCrumbs) {
                    res += type.text().replaceAll("\\,", " ") + ",";
                }
            } catch (Exception e) {
                System.out.println("Exception 3: title wrapper not present.");
                e.printStackTrace();
                res += " ,";
            }
            try {
                Element typesTag = doc.getElementsByClass("discourse-tags").get(0);
                Elements typeSpan = typesTag.select("a");
                String tags = "||";
                for (Element type : typeSpan) {
                    tags += type.text().replaceAll("\\,", " ") + "||";
                }
                tags += ",";
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
                    res += idi + ",";
                    Element username = ele.getElementsByClass("username").get(0);
                    res += username.text() + ",";
                    Elements quotes = ele.select("blockquote > p");
                    Elements paras = ele.getElementsByClass("cooked");
                    String content = "";
                    for (Element para : paras) {
                        if (quotes.contains(para)) {
                            content += " [ " + para.text() + " ] ";
                        } else {
                            content += para.text();
                        }

                    }
                    content = content.replaceAll("\\,", " ");
                    content = content.replaceAll("\"", "\\\"");
                    res += content;
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
        System.out.print("++");
        ExpectedCondition<Boolean> pageLoadCondition = d -> {
            Boolean pageLoaded = ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
            if (pageLoaded) {
                return Jsoup.parse(d.getPageSource()).select(".spinner").size() == 0;
            }
            return pageLoaded;
        };
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(pageLoadCondition);
        System.out.print("xx");
    }

    public static void waitToLoadHeader(WebDriver driver) {
        System.out.print(" ..");
        ExpectedCondition<Boolean> pageLoadCondition = d -> Jsoup.parse(d.getPageSource()).select("h1").size() > 0;
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(pageLoadCondition);
        System.out.print("**");
    }
}
