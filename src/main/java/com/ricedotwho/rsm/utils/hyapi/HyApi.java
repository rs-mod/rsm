package com.ricedotwho.rsm.utils.hyapi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HyApi {
    private static final String MOJANG_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String STATUS_URL = "https://api.hypixel.net/v2/status";
    private static final String DUNGEON_DATA = "https://api.icarusphantom.dev/v1/sbecommands/cata/%s/selected";
    private static final String HY_URL = "https://api.hypixel.net/v2/skyblock";
    private static final String COFL = "https://sky.coflnet.com/api";
    private static final String MOZILLA_AGENT = "Mozilla/5.0";

    private static final Gson gson = new Gson();

    public static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder("?");

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                result.append("&");
            }

            String resultString = result.toString();
            return !resultString.isEmpty()
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }

    private JsonObject toJson(StringBuilder jsonStr) {
        return gson.fromJson(jsonStr.toString(), JsonObject.class);
    }

    private StringBuilder readInput(HttpURLConnection connection) throws IOException {
        String read;
        InputStreamReader isrObj = new InputStreamReader(connection.getInputStream());
        BufferedReader bf = new BufferedReader(isrObj);
        StringBuilder responseStr = new StringBuilder();
        while ((read = bf .readLine()) != null) {
            responseStr.append(read);
        }
        bf.close();
        return responseStr;
    }
    private StringBuilder readInput(InputStream connection) throws IOException {
        String read;
        InputStreamReader isrObj = new InputStreamReader(connection);
        BufferedReader bf = new BufferedReader(isrObj);
        StringBuilder responseStr = new StringBuilder();
        while ((read = bf .readLine()) != null) {
            responseStr.append(read);
        }
        bf.close();
        return responseStr;
    }
    public String getUUID(String name) throws IOException {
        URL urlForGetReq = new URL(MOJANG_UUID + name);
        HttpURLConnection connection = (HttpURLConnection) urlForGetReq.openConnection();
        connection.setRequestMethod("GET");

        int codeResponse = connection.getResponseCode();

        if (codeResponse != HttpURLConnection.HTTP_OK) {
            ChatUtils.chat(ChatFormatting.RED + "Get request for UUID failed!");
            return null;
        }


        StringBuilder responseStr = readInput(connection);

        connection.disconnect();

        Gson g = new Gson();
        JsonObject jsonObject = g.fromJson(String.valueOf(responseStr), JsonObject.class);
        return jsonObject.get("id").toString().replace("\"","");
    }
    public String simpleGet(String url) throws IOException {
        try {
            URL urlForGetReq = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlForGetReq.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", MOZILLA_AGENT);
            connection.setConnectTimeout(5000);

            int codeResponse = connection.getResponseCode();

            if (codeResponse != HttpURLConnection.HTTP_OK) {
                ChatUtils.chat(ChatFormatting.RED + "Get request to %s failed!", url);
                return null;
            }
            StringBuilder responseStr = readInput(connection);
            connection.disconnect();
            return String.valueOf(responseStr);
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }
    public String getPrice(String endpoint, String sbId, Map<String, String> params) throws IOException {
        // build url
        String url = COFL + String.format(endpoint, sbId);

        if(!params.isEmpty()) {
            String charset = "UTF-8";
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!queryBuilder.isEmpty()) queryBuilder.append("&");
                queryBuilder.append(URLEncoder.encode(entry.getKey(), charset));
                queryBuilder.append("=");
                queryBuilder.append(URLEncoder.encode(entry.getValue(), charset));
            }
            url += "?" + queryBuilder;
        }

        try {
            URL urlForGetReq = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlForGetReq.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", MOZILLA_AGENT);
            connection.setConnectTimeout(5000);

            int codeResponse = connection.getResponseCode();

            if (codeResponse != HttpURLConnection.HTTP_OK) {
                ChatUtils.chat("Get request to %s failed! (%s %s | %s)", url, codeResponse, connection.getResponseMessage(), connection.getErrorStream());
                return null;
            }
            StringBuilder responseStr = readInput(connection);
            connection.disconnect();
            return String.valueOf(responseStr);
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    public JsonObject get(String url, Pair<String, String> header) {
        HttpURLConnection connection = null;

        try {
            URL urlForGetReq = new URL(url);
            connection = (HttpURLConnection) urlForGetReq.openConnection();
            connection.setRequestMethod("GET");
            if(header != null) connection.addRequestProperty(header.getFirst(), header.getSecond());

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                ChatUtils.chat(ChatFormatting.RED + "Get request to " + url + " failed! (" + responseCode + ": " + connection.getResponseMessage() + " | " + connection.getErrorStream().toString() + ")");
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder responseStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseStr.append(line);
                }

                return toJson(responseStr);
            }
        } catch (IOException e) {
            ChatUtils.chat(ChatFormatting.RED + "Error getting from " + url + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}