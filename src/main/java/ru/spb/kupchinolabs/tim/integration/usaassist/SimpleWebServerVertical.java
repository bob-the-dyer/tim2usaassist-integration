package ru.spb.kupchinolabs.tim.integration.usaassist;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.Set;
import java.util.logging.Logger;

public class SimpleWebServerVertical extends AbstractVerticle {

    private final static Logger log = Logger.getLogger(SimpleWebServerVertical.class.getName());

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        System.setProperty("vertx.cwd", "target/classes");
        System.setProperty("vertx.disableFileCaching", "true");
        Vertx.vertx().deployVerticle(new SimpleWebServerVertical());
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions bridgeOptions = new BridgeOptions()
//                .addOutboundPermitted(new PermittedOptions().setAddress("response"))
                .addInboundPermitted(new PermittedOptions().setAddress("request"));
        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);
        router.route().handler(StaticHandler.create());
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        vertx.eventBus().consumer("request", this::handleRequest);
    }

    private void handleRequest(Message<JsonObject> message) {
        final JsonObject jsonObject = message.body();
        //TODO add params from request
        HttpClientOptions options = new HttpClientOptions().setDefaultHost("www.usa-assist.com");
        HttpClient client = vertx.createHttpClient(options);
        Buffer buffer = Buffer.buffer()
                .appendString("affiliate=669").appendString("&")
                .appendString("password=travel123").appendString("&")
                .appendString("test=true").appendString("&");
        final Set<String> fieldNames = jsonObject.fieldNames();
        for (String key: fieldNames){
            final String value = (String) jsonObject.getValue(key);
            buffer.appendString(key).appendString("=").appendString(value).appendString("&");
        }
        String body = buffer.toString();
        log.info("request to usa-assist: " + body);
        client.request(HttpMethod.POST, "/modules/transact", response -> {
            response.handler(bfr -> {
                final JsonObject json = new JsonObject(bfr.toString());
                log.info("response from usa-assist: " + json.encode());
                message.reply(json);
            });
        }).putHeader("Content-Type", "application/x-www-form-urlencoded")
                .putHeader("Content-Language", "en-US")
                .end(body);
    }
}
