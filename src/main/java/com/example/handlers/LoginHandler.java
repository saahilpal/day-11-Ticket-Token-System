package com.example.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import io.vertx.ext.web.RoutingContext;

public class LoginHandler implements Handler<RoutingContext> {
    private final AuthenticationProvider auth;

    public LoginHandler(AuthenticationProvider auth) {
        this.auth = auth;
    }

    @Override
    public void handle(RoutingContext ctx) {
        JsonObject b = ctx.getBodyAsJson();
        auth.authenticate(new JsonObject()
                        .put("username", b.getString("email"))
                        .put("password", b.getString("password")),
                ar -> {
                    if (ar.succeeded()) ctx.response().end("Login success");
                    else ctx.response().setStatusCode(401).end("Invalid credentials");
                });
    }
}
