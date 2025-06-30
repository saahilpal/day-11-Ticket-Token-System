package com.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.auth.mongo.MongoAuthentication;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.web.Router;
import com.example.handlers.*;

public class MainVerticle extends AbstractVerticle {
    private MongoClient mongo;
    private MongoAuthentication authProvider;
    private MailClient mailClient;

    @Override
    public void start(Promise<Void> startPromise) {
        // MongoDB client
        mongo = MongoClient.createShared(vertx, new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "eventdb"));

        // Authentication provider
        authProvider = MongoAuthentication.create(mongo, new MongoAuthenticationOptions());

        // SMTP mail client
        MailConfig mailConfig = new MailConfig()
                .setHostname("smtp.example.com")
                .setPort(587)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername("me@example.com")
                .setPassword("yourpassword");
        mailClient = MailClient.createShared(vertx, mailConfig);

        // Setup routes
        Router router = Router.router(vertx);
        router.post("/api/register").handler(new RegisterHandler(mongo, mailClient));
        router.post("/api/login").handler(new LoginHandler(authProvider));
        router.get("/api/events").handler(new EventHandler(mongo));
        router.post("/api/events/:eventId/book").handler(new BookingHandler(mongo, mailClient));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, ar -> {
                    if (ar.succeeded()) startPromise.complete();
                    else startPromise.fail(ar.cause());
                });
    }
}
