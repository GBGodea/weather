package org.godea;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

@WebServlet(urlPatterns = "/weather")
public class WeatherServlet extends HttpServlet {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Jedis redis;
    private static final int CACHE_TTL_SECONDS = 15 * 60;

    static {
        try {
            redis = new Jedis("localhost", 6379);
            redis.ping();
        } catch (JedisConnectionException e) {
            System.err.println("Warning: Could not connect to Redis, caching disabled: " + e.getMessage());
            redis = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String city = req.getParameter("city");
        if (city == null || city.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing city parameter");
            return;
        }

        try {
            String cacheKey = "weather:" + city.toLowerCase();
            JsonNode hourly = null;

            if (redis != null) {
                try {
                    String cached = redis.get(cacheKey);
                    if (cached != null) {
                        hourly = mapper.readTree(cached);
                    }
                } catch (JedisConnectionException e) {
                    System.err.println("Redis GET failed, fetching fresh data: " + e.getMessage());
                }
            }

            if (hourly == null) {
                JsonNode geo = fetchJson("https://geocoding-api.open-meteo.com/v1/search?name=" + encode(city));
                if (!geo.has("results") || geo.get("results").size() == 0) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "City not found");
                    return;
                }
                JsonNode loc = geo.get("results").get(0);
                double lat = loc.get("latitude").asDouble();
                double lon = loc.get("longitude").asDouble();

                String url = String.format(
                        "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=temperature_2m&forecast_days=1",
                        lat, lon
                );
                JsonNode weather = fetchJson(url);
                hourly = weather.get("hourly");

                if (redis != null) {
                    try {
                        redis.setex(cacheKey, CACHE_TTL_SECONDS, mapper.writeValueAsString(hourly));
                    } catch (JedisConnectionException e) {
                        System.err.println("Redis SETEX failed: " + e.getMessage());
                    }
                }
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            JsonNode times = hourly.get("time");
            JsonNode temps = hourly.get("temperature_2m");
            DateTimeFormatter outputFmt = DateTimeFormatter.ofPattern("HH:mm");
            for (int i = 0; i < times.size(); i++) {
                String t = times.get(i).asText();
                double temp = temps.get(i).asDouble();
                LocalDateTime dt = LocalDateTime.parse(t);
                String hourLabel = dt.format(outputFmt);
                dataset.addValue(temp, "Temp", hourLabel);
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Temperature Next 24h",
                    "Time",
                    "°C",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, false, false
            );
            chart.getCategoryPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

            BufferedImage img = chart.createBufferedImage(1600, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);

            resp.setContentType("image/png");
            resp.getOutputStream().write(baos.toByteArray());
            resp.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static JsonNode fetchJson(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return mapper.readTree(response.body().string());
        }
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}