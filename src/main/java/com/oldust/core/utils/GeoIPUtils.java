package com.oldust.core.utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Clase de utilidades
 * que brinda algunos servicios
 * de la API: https://ipstack.com/
 */


public class GeoIPUtils {
    private static final String API_KEY = "5c02ffde58329490645cb469cecba6e4";
    private static final String REQUEST = "http://api.ipstack.com/%s?access_key=" + API_KEY + "&format=0";
    private static final Gson GSON = new Gson();

    private GeoIPUtils() {
    }

    public static GeoResponse gatherIpInfo(String ip) throws IOException {
        CUtils.warnSyncCall();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = String.format(REQUEST, ip);
            HttpGet httpGet = new HttpGet(url);

            String responseBody = httpClient.execute(httpGet, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();

                if (status < 200 || status >= 300) {
                    throw new IOException("Received unexpected status code: " + status);
                }

                HttpEntity entity = httpResponse.getEntity();

                return entity != null ? EntityUtils.toString(entity) : null;
            });

            return GSON.fromJson(responseBody, GeoResponse.class);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class GeoResponse {
        @SerializedName("continent_code")
        private final String continentCode;

        @SerializedName("country_name")
        private final String countryName;

        @SerializedName("region_name")
        private final String regionName;
        private final String city;
    }

}
