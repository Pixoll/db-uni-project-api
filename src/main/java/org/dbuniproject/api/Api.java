package org.dbuniproject.api;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.dbuniproject.api.endpoints.*;
import org.dbuniproject.api.json.JSONMapper;

public class Api {
    public static final Dotenv DOTENV = Dotenv.load();

    public static void main(String[] args) {
        SessionTokenManager.loadSessionTokens();

        Javalin.create(config -> {
                    config.router.contextPath = "/api/v1";
                    config.router.ignoreTrailingSlashes = true;
                    config.bundledPlugins.enableCors(cors ->
                            cors.addRule(CorsPluginConfig.CorsRule::anyHost)
                    );
                    config.router.apiBuilder(() -> registerEndpoints(
                            new BrandsEndpoint(),
                            new ClientsEndpoint(),
                            new EmployeesSessionsEndpoint(),
                            new PingEndpoint(),
                            new ProductsColorsEndpoint(),
                            new ProductsEndpoint(),
                            new ProductsSizesEndpoint(),
                            new ProductsStocksEndpoint(),
                            new ProductsTypesEndpoint(),
                            new RegionsEndpoint(),
                            new SalesEndpoint(),
                            new StoresEndpoint()
                    ));
                    config.jsonMapper(new JSONMapper());
                })
                .beforeMatched(wrapHandlerWithErrorHandler(Endpoint::beforeMatched))
                .start(Integer.parseInt(DOTENV.get("PORT")));
    }

    private static void registerEndpoints(Endpoint... endpoints) {
        for (final Endpoint endpoint : endpoints) {
            ApiBuilder.path(endpoint.path, () -> {
                if (endpoint instanceof Endpoint.GetMethod method) {
                    ApiBuilder.get(wrapHandlerWithErrorHandler(method::get));
                }
                if (endpoint instanceof Endpoint.PostMethod method) {
                    ApiBuilder.post(wrapHandlerWithErrorHandler(method::post));
                }
                if (endpoint instanceof Endpoint.PutMethod method) {
                    ApiBuilder.put(wrapHandlerWithErrorHandler(method::put));
                }
                if (endpoint instanceof Endpoint.PatchMethod method) {
                    ApiBuilder.patch(wrapHandlerWithErrorHandler(method::patch));
                }
                if (endpoint instanceof Endpoint.DeleteMethod method) {
                    ApiBuilder.delete(wrapHandlerWithErrorHandler(method::delete));
                }
            });
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static Handler wrapHandlerWithErrorHandler(Handler handler) {
        return (ctx) -> {
            try {
                handler.handle(ctx);
            } catch (EndpointException e) {
                ctx.status(e.getStatusCode()).json(e.toJSON());
            } catch (RuntimeException e) {
                if (e.getCause() instanceof EndpointException ee) {
                    ctx.status(ee.getStatusCode()).json(ee.toJSON());
                    return;
                }

                e.printStackTrace();
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
}
