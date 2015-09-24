package ru.spb.kupchinolabs.tim.integration.usaassist;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class StarterVertical extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        System.setProperty("vertx.cwd", "target/classes");
        System.setProperty("vertx.disableFileCaching", "true");
        Vertx.vertx().deployVerticle(new StarterVertical());
    }

    @Override
    public void start() throws Exception {
        HttpClientOptions options = new HttpClientOptions().setDefaultHost("www.usa-assist.com");
        HttpClient client = vertx.createHttpClient(options);
        Buffer buffer = Buffer.buffer()
                .appendString("affiliate=669").appendString("&")
                .appendString("password=travel123")
                .appendString("test=true").appendString("&")
                ;
        String body = buffer.toString();
        client.request(HttpMethod.POST, "/modules/transact", response -> {
            System.out.println(response.statusMessage());
            System.out.println(response.statusCode());
            response.handler(bfr -> {
                final JsonObject json = new JsonObject(bfr.toString());
                System.out.println(json.encodePrettily());
            });
        }).putHeader("Content-Type", "application/x-www-form-urlencoded")
                .putHeader("Content-Language", "en-US")
                .end(body);
    }

}
