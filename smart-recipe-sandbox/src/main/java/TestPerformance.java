import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@Slf4j
public class TestPerformance {


    public static void main(String[] args) {

        System.setProperty("webdriver.gecko.driver", "C:\\dev\\program\\geckodriver.exe");
        WebDriver driver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(driver, 10000);
        try {
            driver.get("https://www.doctolib.fr/vaccination-covid-19/75020-paris?ref_visit_motive_ids[]=6970&ref_visit_motive_ids[]=7005&ref_visit_motive_ids[]=7107&ref_visit_motive_ids[]=7945");
            List<WebElement> elements = driver.findElements(By.className("js-dl-search-results-calendar"));
            List<WebElement> elements2 = driver.findElements(By.className("dl-search-result-content"));
            List<WebElement> elements3 = driver.findElements(By.className("results"));
            for (int i = 0; i < elements.size(); i++) {
                System.out.println(elements2.get(i).getText());
                System.out.println(elements.get(i).getText());
                if (elements.get(i).getText().equalsIgnoreCase("")) {
                    System.out.println("PAS DE CRENEAU VACCIN => TRY AGAIN");
                }
                System.out.println("-------------");
            }

            printElements(elements3);
            //printElements(elements2);
            //printElements(elements3);

            System.out.println(elements.get(0).getText());
            //System.out.println(elements);
        } finally {
            driver.quit();
        }
    }

    private static void printElements(List<WebElement> elements) {
        elements.stream().forEach(item -> {
            try {
                System.out.println(item.getText());
                System.out.println("*****************");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main2(String args[]) throws Exception {

        StringBuffer document = new StringBuffer();
        URL url = new URL("https://www.doctolib.fr/vaccination-covid-19/75019-paris?ref_visit_motive_ids[]=6970&ref_visit_motive_ids[]=7005&ref_visit_motive_ids[]=7107&ref_visit_motive_ids[]=7945");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null)
            document.append(line + "\n");
        reader.close();
        System.out.println(document.toString());


    }
}
