package com.example.handlers;

import com.example.services.PasswordUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.web.RoutingContext;

public class RegisterHandler implements Handler<RoutingContext> {
    private final MongoClient mongo;
    private final MailClient mail;

    public RegisterHandler(MongoClient mongo, MailClient mail) {
        this.mongo = mongo;
        this.mail = mail;
    }

    @Override
    public void handle(RoutingContext ctx) {
        JsonObject b = ctx.getBodyAsJson();
        String email = b.getString("email"), name = b.getString("name");
        if (email == null || name == null) {
            ctx.response().setStatusCode(400).end("email/name required");
            return;
        }
        mongo.findOne("users", new JsonObject().put("email", email), null, ar -> {
            if (ar.succeeded() && ar.result() != null) {
                ctx.response().setStatusCode(400).end("User exists");
                return;
            }
            String pwd = PasswordUtils.randomPwd(8);
            String hashed = PasswordUtils.hash(pwd);
            JsonObject user = new JsonObject()
                    .put("email", email).put("name", name).put("password", hashed);
            mongo.insert("users", user, res -> {
                if (res.succeeded()) {
                    MailMessage msg = new MailMessage()
                            .setTo(email)
                            .setFrom("no-reply@example.com")
                            .setSubject("Your password")
                            .setText("Your password: " + pwd);
                    mail.sendMail(msg, mr ->
                            ctx.response().end(mr.succeeded() ? "Registered" : "Email failed")
                    );
                } else {
                    ctx.response().setStatusCode(500).end("DB error");
                }
            });
        });
    }
}
