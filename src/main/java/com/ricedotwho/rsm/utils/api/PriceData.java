package com.ricedotwho.rsm.utils.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.ChatFormatting;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class PriceData {
    @Getter
    private long lastFetched = 0;
    @Getter
    private  Map<String, Price> bazaarCache = new HashMap<>();
    @Getter
    private final Map<String, String> itemCache = new HashMap<>();
    @Getter
    private Map<String, Double> binCache = new HashMap<>();

    public void updatePrices(Runnable run) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        HyApi api = new HyApi();

        CompletableFuture<JsonObject> bazaarFuture = CompletableFuture.supplyAsync(() -> api.get("https://api.hypixel.net/skyblock/bazaar", new Pair<>("User-Agent", "Mozilla/5.0")), executor);
        CompletableFuture<JsonObject> itemsFuture = CompletableFuture.supplyAsync(() -> api.get("https://api.hypixel.net/v2/resources/skyblock/items", new Pair<>("User-Agent", "Mozilla/5.0")), executor);
        CompletableFuture<JsonObject> binFuture = CompletableFuture.supplyAsync(() -> api.get("https://moulberry.codes/lowestbin.json", new Pair<>("User-Agent", "Mozilla/5.0")), executor);

        CompletableFuture<Void> allDone = CompletableFuture.allOf(bazaarFuture, itemsFuture, binFuture);

        allDone.thenRun(() -> {
            try {
                parseBazaar(bazaarFuture.get());
                parseItems(itemsFuture.get());
                parseBin(binFuture.get());
                lastFetched = System.currentTimeMillis();
                run.run();
            } catch (Exception e) {
                ChatUtils.chat(ChatFormatting.RED + "Failed to get price data");
            }
            executor.shutdown();
        });
    }

    private void parseBazaar(JsonObject object) {
        JsonObject products = object.getAsJsonObject("products");

        Gson gson = new Gson();

        Type productType = new TypeToken<Map<String, JsonObject>>(){}.getType();
        Map<String, JsonObject> entries = gson.fromJson(products, productType);

        Map<String, Price> data = new HashMap<>();

        for (Map.Entry<String, JsonObject> entry : entries.entrySet()) {
            String id = entry.getKey();
            JsonObject info = entry.getValue();


            // Sell Order
            double sellOrderValue = info.getAsJsonObject("quick_status").get("buyPrice").getAsDouble();
            JsonArray buySummary = info.getAsJsonArray("buy_summary");
            if(!buySummary.isEmpty()) {
                double totalBuy = 0;
                for (int i = 0; i < buySummary.size(); i++) {
                    JsonElement element = buySummary.get(i);
                    totalBuy += element.getAsJsonObject().get("pricePerUnit").getAsDouble();
                }
                sellOrderValue = totalBuy / buySummary.size();
            }


            // Insta sell
            double buyOrderValue = info.getAsJsonObject("quick_status").get("buyPrice").getAsDouble();
            JsonArray sellSummary = info.getAsJsonArray("buy_summary");
            if (!sellSummary.isEmpty()) {
                double totalSell = 0;
                for (int i = 0; i < sellSummary.size(); i++) {
                    JsonElement element = sellSummary.get(i);
                    totalSell += element.getAsJsonObject().get("pricePerUnit").getAsDouble();
                }
                buyOrderValue = totalSell / sellSummary.size();
            }

            data.put(id, new Price(sellOrderValue, buyOrderValue));
        }

        bazaarCache = data;
    }

    private void parseItems(JsonObject object) {
        JsonArray items = object.getAsJsonArray("items");
        items.forEach(element -> {
            JsonObject obj = element.getAsJsonObject();
            itemCache.put(obj.get("id").getAsString(), obj.get("name").getAsString());
        });
    }

    private void parseBin(JsonObject object) {
        Type type = new TypeToken<Map<String, Double>>(){}.getType();
        binCache = new Gson().fromJson(object, type);
    }

    public record Price(double order, double instant) { }
}