package com.ricedotwho.rsm.utils.api;

import com.google.gson.*;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Floor;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@UtilityClass
public class HyApi {
    private static final String MOJANG_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String STATUS_URL = "https://api.hypixel.net/v2/status";
    private static final String HY_URL = "https://api.hypixel.net/v2/skyblock";
    private static final String COFL = "https://sky.coflnet.com/api";
    private static final String MOZILLA_AGENT = "Mozilla/5.0";
    private static final Gson gson = new Gson();

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

    public String getUUID(String name) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOJANG_UUID + name))
                .header("User-Agent", MOZILLA_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            ChatUtils.chat(ChatFormatting.RED + "Get request for UUID failed!");
            return null;
        }

        JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();
        return data.get("id").toString().replace("\"","");
    }

//    public JsonObject getSelectedProfile(String uuid) throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(SKYBLOCK_DATA.formatted(uuid)))
//                .header("User-Agent", MOZILLA_AGENT)
//                .GET()
//                .build();
//
//        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
//            ChatUtils.chat(ChatFormatting.RED + "Get request for skyblock data failed!");
//            return null;
//        }
//
//        JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();
//        for (JsonElement profileElement : data.getAsJsonArray("profiles")) {
//            JsonObject profile = profileElement.getAsJsonObject();
//            if (profile.get("selected").getAsBoolean()) {
//                return profile;
//            }
//        }
//        return null;
//    }

//    public JsonObject getDungeonsData(String uuid) throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(DUNGEONS_DATA.formatted(uuid)))
//                .header("User-Agent", MOZILLA_AGENT_DEVONIAN)
//                .GET()
//                .build();
//
//        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() != 200) {
//            ChatUtils.chat("§cGet request for skyblock data failed!");
//            return null;
//        }
//        return JsonParser.parseString(response.body()).getAsJsonObject();
//    }

//    public DungeonData getPbs(String name) throws IOException, InterruptedException {
//        JsonObject raw = getDungeonsData(name).getAsJsonObject("result").getAsJsonObject(name);
//        if (!raw.get("success").getAsBoolean()) return null;
//        JsonObject dungeons = raw.getAsJsonObject("data");
//        JsonObject catacombs = dungeons.getAsJsonObject("personal_best_normal");
//        JsonObject master = dungeons.getAsJsonObject("personal_best_master");
//        JsonObject s = catacombs.getAsJsonObject("s");
//        JsonObject sPlus = catacombs.getAsJsonObject("s_plus");
//        JsonObject ms = master.getAsJsonObject("s");
//        JsonObject msPlus = master.getAsJsonObject("s_plus");
//
//        Map<Floor, DungeonData.FloorData> data = new HashMap<>();
//
//        Floor.dungeonValues().forEach(f -> {
//            boolean m = f.getIndex() > 7;
//            String member = "floor_" + (m ? f.getIndex() - 7 : f.getIndex()) + "_ms";
//            data.put(f, new DungeonData.FloorData(getLong(m ? ms : s, member), getLong(m ? msPlus : sPlus, member)));
//        });
//
//        return new DungeonData(data, dungeons.get("secrets").getAsInt(), dungeons.get("averageSecrets").getAsFloat(), dungeons.get("magical_power").getAsInt());
//    }

    private long getLong(JsonObject obj, String member) {
        JsonPrimitive prim = obj.getAsJsonPrimitive(member);
        return prim == null ? -1 : prim.getAsLong();
    }

    public String simpleGet(String url) {
        try {
            URL urlForGetReq = URI.create(url).toURL();
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
            RSM.getLogger().error("Error while performing simple get to {}", url, e);
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
            URL urlForGetReq = URI.create(url).toURL();
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
            RSM.getLogger().error("Error whilst fetching prices from COFL", e);
            return null;
        }
    }

    public JsonObject get(String url, Pair<String, String> header) {
        HttpURLConnection connection = null;

        try {
            URL urlForGetReq = URI.create(url).toURL();
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
            RSM.getLogger().error("Error while performing get to {}", url, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}