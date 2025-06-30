package com.example.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

public class EventHandler implements Handler<RoutingContext> {
    private final MongoClient mongo;

    public EventHandler(MongoClient mongo) {
        this.mongo = mongo;
    }

    @Override
    public void handle(RoutingContext ctx) {
        JsonObject query = new JsonObject().put("availableTokens", new JsonObject().put("$gt", 0));
        mongo.find("events", query, ar -> {
            if (ar.succeeded()) ctx.json(ar.result());
            else ctx.response().setStatusCode(500).end("DB error");
        });
    }
}
