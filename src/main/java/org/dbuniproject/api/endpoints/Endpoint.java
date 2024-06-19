package org.dbuniproject.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public abstract class Endpoint {
    public final String path;

    public Endpoint(String path) {
        this.path = path;
    }

    public static void beforeMatched(Context ctx) throws EndpointException {
        final String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .replaceAll("T|\\.\\d+$", " ")
                .stripTrailing();

        final HandlerType method = ctx.method();
        System.out.println("[" + now + "] " + method.name() + " " + ctx.matchedPath()
                           + "\nbody: " + ctx.body()
                           + "\nquery: " + ctx.queryString()
        );

        if (method == HandlerType.POST) {
            if (!Objects.equals(ctx.header(Header.CONTENT_TYPE), "application/json")) {
                throw new EndpointException(HttpStatus.BAD_REQUEST, "Content-Type header must be 'application/json'.");
            }

            // check if valid json
            ctx.bodyAsClass(JSONObject.class);
        }
    }

    public interface GetMethod {
        void get(Context ctx) throws EndpointException;
    }

    public interface PostMethod {
        void post(Context ctx) throws EndpointException;
    }

    public interface PutMethod {
        void put(Context ctx) throws EndpointException;
    }

    public interface PatchMethod {
        void patch(Context ctx) throws EndpointException;
    }

    public interface DeleteMethod {
        void delete(Context ctx) throws EndpointException;
    }
}
