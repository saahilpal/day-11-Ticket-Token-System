package com.example.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;
import java.util.UUID;

public class BookingHandler implements Handler<RoutingContext> {
    private final MongoClient mongo;
    private final MailClient mail;

    public BookingHandler(MongoClient mongo, MailClient mail) {
        this.mongo = mongo;
        this.mail = mail;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String eventId = ctx.pathParam("eventId");
        String email = ctx.getBodyAsJson().getString("email");
        if (eventId == null || email == null) {
            ctx.response().setStatusCode(400).end("eventId/email required");
            return;
        }
        mongo.findOne("users", new JsonObject().put("email", email), null, ar1 -> {
            if (ar1.failed() || ar1.result() == null) {
                ctx.response().setStatusCode(404).end("User not found"); return;
            }
            String userId = ar1.result().getString("_id");
            JsonObject bookingQuery = new JsonObject()
                    .put("userId", userId)
                    .put("eventId", eventId);
            mongo.findOne("bookings", bookingQuery, null, ar2 -> {
                if (ar2.succeeded() && ar2.result() != null) {
                    ctx.response().setStatusCode(400).end("Already booked");
                    return;
                }
                mongo.findOne("events", new JsonObject().put("_id", eventId), null, ar3 -> {
                    JsonObject ev = ar3.result();
                    if (ev == null || ev.getInteger("availableTokens", 0) <= 0) {
                        ctx.response().setStatusCode(400).end("No tokens");
                        return;
                    }
                    mongo.updateCollection("events", new JsonObject().put("_id", eventId),
                            new JsonObject().put("$inc", new JsonObject().put("availableTokens", -1)), uar -> {
                                String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                                JsonObject booking = new JsonObject()
                                        .put("userId", userId)
                                        .put("eventId", eventId)
                                        .put("tokenCode", code);
                                mongo.insert("bookings", booking, bir -> {
                                    MailMessage msg = new MailMessage()
                                            .setTo(email)
                                            .setFrom("no-reply@example.com")
                                            .setSubject("Your booking token")
                                            .setText("Your token: " + code);
                                    mail.sendMail(msg, mr ->
                                            ctx.response().end(mr.succeeded() ? "Booked" : "Email failed")
                                    );
                                });
                            });
                });
            });
        });
    }
}
