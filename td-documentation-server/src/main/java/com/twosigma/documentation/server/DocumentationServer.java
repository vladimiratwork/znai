package com.twosigma.documentation.server;

import com.twosigma.console.ConsoleOutputs;
import com.twosigma.console.ansi.AnsiConsoleOutput;
import com.twosigma.console.ansi.Color;
import com.twosigma.documentation.html.HtmlPage;
import com.twosigma.documentation.html.reactjs.HtmlReactJsPage;
import com.twosigma.documentation.html.reactjs.ReactJsNashornEngine;
import com.twosigma.documentation.server.docpreparation.DocumentationPreparationHandlers;
import com.twosigma.documentation.server.docpreparation.DocumentationPreparationWebSocketHandler;
import com.twosigma.documentation.server.sockets.JsonWebSocketHandler;
import com.twosigma.documentation.server.sockets.JsonWebSocketHandlerComposition;
import com.twosigma.documentation.server.upload.FileUploadHandler;
import com.twosigma.documentation.server.upload.UnzipTask;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.RoutingContextDecorator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mykola
 */
public class DocumentationServer {
    private final JsonWebSocketHandlerComposition socketHandlers;
    private final DocumentationPreparationWebSocketHandler documentationPreparationWebSocketHandler;
    private final Vertx vertx;
    private ReactJsNashornEngine nashornEngine;
    private Path deployRoot;

    public DocumentationServer(ReactJsNashornEngine nashornEngine, Path deployRoot) {
        this.nashornEngine = nashornEngine;
        this.deployRoot = deployRoot;

        System.setProperty("vertx.cwd", deployRoot.toString());
        System.setProperty("file.encoding","UTF-8");

        createDirs(deployRoot);
        vertx = Vertx.vertx();

        socketHandlers = new JsonWebSocketHandlerComposition();
        documentationPreparationWebSocketHandler = new DocumentationPreparationWebSocketHandler(vertx);
        
        socketHandlers.add(documentationPreparationWebSocketHandler);
    }

    public void addSocketHandler(JsonWebSocketHandler socketHandler) {
        socketHandlers.add(socketHandler);
    }

    public HttpServer create() {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        StaticHandler pagesStaticHandler = StaticHandler.create(".").
                setFilesReadOnly(false).setMaxAgeSeconds(0);

        StaticHandler staticCommonResources =
                StaticHandler.create("static").setMaxAgeSeconds(600);

        // delegate each documentation static resources to a central copy
        // to make existing docs served centrally upgrade all at once
        router.get("/:docId/static/*").handler(ctx -> {
            MultiMap params = ctx.request().params();

            staticCommonResources.handle(new RoutingContextDecorator(
                    router.route("/" + params.get("docId") + "/static"), ctx));
        });

        router.route("/upload/:docId").handler(ctx -> {
            MultiMap params = ctx.request().params();
            String docId = params.get("docId");
            new FileUploadHandler(vertx, deployRoot, docId, (p) -> unzip(deployRoot, docId, p)).handle(ctx.request());
        });

        router.get("/static/*").handler(staticCommonResources);

        router.get("/*").handler(ctx -> {
            String docId = extractDocId(ctx);
            if (DocumentationPreparationHandlers.isReady(docId)) {
                pagesStaticHandler.handle(ctx);
            } else if (isNonDocPageRequest(ctx)) {
                // mdoc documentations are single page apps
                // page refresh won't happen during navigation from one page to another
                // but images will still be requested if they are present on a page
                // in that case we will serve what is currently available and not force documentation update
                pagesStaticHandler.handle(ctx);
            } else {
                serveDocumentationPreparationPage(ctx, docId);
            }
        });

        server.websocketHandler(socketHandlers);
        server.requestHandler(router::accept);

        return server;
    }

    private void serveDocumentationPreparationPage(RoutingContext ctx, String docId) {
        HtmlReactJsPage htmlReactJsPage = new HtmlReactJsPage(nashornEngine);
        Map<String, Object> props = new LinkedHashMap<>();

        props.put("docId", docId);
        props.put("statusMessage", "checking documentation cache");
        props.put("progressPercent", 0);
        props.put("keyValues", Collections.emptyList());

        HtmlPage htmlPage = htmlReactJsPage.createWithClientSideOnly("test title",
                "DocumentationPreparationScreen", props);

        ctx.response().end(htmlPage.render(docId));
    }

    private static String extractDocId(RoutingContext ctx) {
        String uri = ctx.request().uri();
        String[] parts = uri.split("/");

        return parts.length < 2 ? "" : parts[1];
    }

    private static boolean isNonDocPageRequest(RoutingContext ctx) {
        String uri = ctx.request().uri();
        int dotIdx = uri.lastIndexOf('.');
        if (dotIdx == -1) {
            return false;
        }

        int slashIdx = uri.lastIndexOf('/');
        return dotIdx > slashIdx;
    }

    private static void createDirs(Path deployRoot) {
        try {
            Files.createDirectories(deployRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void unzip(Path root, String docId, Path path) {
        ConsoleOutputs.out(Color.BLUE, "unzipping docs: ", Color.PURPLE, path, Color.BLACK, " to ",
                Color.PURPLE, root);

        UnzipTask unzipTask = new UnzipTask(root.resolve(path), root);
        unzipTask.execute();
        ConsoleOutputs.out(Color.BLUE, "unzipped docs: ", Color.PURPLE, root);
    }

    // this is entry point for local development and testing
    // official mdoc start is through com.twosigma.documentation.cli.DocumentationCliApp
    public static void main(String[] args) {
        ConsoleOutputs.add(new AnsiConsoleOutput());

        HttpServer server = new DocumentationServer(new ReactJsNashornEngine(), Paths.get("")).create();
        server.listen(3333);
        System.out.println("test server started");
    }
}
