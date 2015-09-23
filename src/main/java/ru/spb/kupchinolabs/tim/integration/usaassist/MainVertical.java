package ru.spb.kupchinolabs.tim.integration.usaassist;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class MainVertical extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        System.setProperty("vertx.cwd", "target/classes");
        System.setProperty("vertx.disableFileCaching", "true");
        Vertx.vertx().deployVerticle(new MainVertical());
    }

    @Override
    public void start() throws Exception {
        HttpClientOptions options = new HttpClientOptions().setDefaultHost("www.usa-assist.com");
        HttpClient client = vertx.createHttpClient(options);
        Buffer buffer = Buffer.buffer()
                .appendString("test=1,")
                .appendString("affiliate=669,")
                .appendString("password=password");
        final JsonObject json = new JsonObject();
        json.put("affiliate", 669);
        json.put("password", "1234");
        String body = buffer.toString();
        client.request(HttpMethod.POST, "/modules/transact", response -> {
            response.handler(System.out::println);
        }).putHeader("content-type", "application/json")
                .end(buffer);
    }

}
