package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class PingEndpoint extends Endpoint implements Endpoint.GetMethod {
    public PingEndpoint() {
        super("/ping");
    }

    @Override
    public void get(Context ctx) {
        ctx.status(HttpStatus.OK);
    }
}
