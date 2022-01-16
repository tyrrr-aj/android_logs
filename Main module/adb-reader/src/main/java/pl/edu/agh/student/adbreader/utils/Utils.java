package pl.edu.agh.student.adbreader.utils;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pl.edu.agh.student.adbreader.requests.Request;
import pl.edu.agh.student.adbreader.requests.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static pl.edu.agh.student.adbreader.utils.Const.SERVER_URL;

public class Utils {
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static SimpleDateFormat dateFormat;

    public static Log parseLogLine(String line) throws ParseException {
        String[] splitLine = line.split("\\s+");
        long time = getTime(parseLogDate(splitLine));
        int pid = Integer.parseInt(splitLine[2]);
        int idx = line.indexOf(splitLine[4]);
        String log = line.substring(idx).replaceFirst(" ", "/");

        return new Log(pid, time, log);
    }

    private static String parseLogDate(String[] splitLine) {
        return Calendar.getInstance().get(Calendar.YEAR) + "-" + splitLine[0] + " " + splitLine[1];
    }

    private static long getTime(String date) throws ParseException {
        if (dateFormat == null) {
            dateFormat = getSimpleDateFormat();
        }

        return dateFormat.parse(date).getTime();
    }

    private static long parseDate(String date) throws ParseException {
        if (dateFormat == null) {
            dateFormat = getSimpleDateFormat();
        }

        return dateFormat.parse(date).getTime();
    }

    private static SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        TimeZone gmt = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(gmt);

        return sdf;
    }

    public static Response sendLogsInPack(Request request) {
        HttpPost postRequest = new HttpPost(SERVER_URL + "/pack");
        postRequest.setEntity(new StringEntity(new Gson().toJson(request), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            String stringResponse = EntityUtils.toString(closeableHttpResponse.getEntity());
            return new Gson().fromJson(stringResponse, Response.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return new Response(0, 0);
    }
}
