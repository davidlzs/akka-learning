package com.dliu.natsio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.nats.client.Connection;
import io.nats.client.Nats;

public class PublishJSON {
    static class Stock {
        public final String symbol;
        public final float price;

        public Stock(String symbol, float price) {
            this.symbol = symbol;
            this.price = price;
        }
    }

    public static void main(String[] args) {
        try (Connection nc = Nats.connect("nats://localhost:4222")) {
            Stock stock = new Stock("GOOG", 1321.01f);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String json = gson.toJson(stock);

            nc.publish("updates", json.getBytes(StandardCharsets.UTF_8));

            nc.flush(Duration.ZERO);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
