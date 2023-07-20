import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static final String DATE_FORM = "dd.MM.yyyy";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        String Date = enterDate();
        String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + Date.toString() + "&VAL_NM_RQ=R01235";
        String result = Main.sendRequest(url, null, null);
        System.out.println(result);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(result)));
        Element rootElement = document.getDocumentElement();
        System.out.println(rootElement.getElementsByTagName("CharCode"));


    }

    public static String enterDate(){

        System.out.println("Введите дату (dd.MM.yyyy)");
        Scanner in = new Scanner(System.in);
        String date = in.next();

        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORM);
            LocalDate parse = LocalDate.parse(date, formatter);
            return parse.format(formatter);
        } catch (Exception e) {
            System.out.println("Неправильный формат даты");
            return enterDate();
        }
    }

    public static String sendRequest(String url, Map<String, String> headers, String request) {
        String result = null;
        HttpURLConnection urlConnection = null;
        try {
            URL requestURL = new URL(url);
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setReadTimeout(20000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");

            if (request != null) {
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                outputStream.writeBytes(request);
                outputStream.flush();
                outputStream.close();
            }
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            int status = urlConnection.getResponseCode();
            System.out.println("Status: " + status);

            if (status == HttpURLConnection.HTTP_OK) {
                result = getStringFromStream(urlConnection.getInputStream());
            }
        } catch (Exception e){
            System.out.println("Ошибка отправки запроса");
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return result;
    }

    private static String getStringFromStream(InputStream inputStream) throws IOException {
        final int BUFFER_SIZE = 4096;
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            resultStream.write(buffer, 0, length);
        }


        return resultStream.toString("UTF-8");

    }
}