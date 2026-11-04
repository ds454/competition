package ds454.locomotive.competition;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ds454.locomotive.competition.dbconnection.RepositoryProvider;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static int grpcPort = Integer.valueOf(System.getenv().get("GRPC_PORT"));

    public static void main(String[] args) throws Exception {
        log.info("Starting Competition service...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("ds454.locomotive.competition");
        RepositoryProvider.setContext(context);

        log.info("Starting gRPC server on port " + Main.grpcPort);
        Server s;

        s = ServerBuilder.forPort(Main.grpcPort)
                .build()
                .start();
        log.info("gRPC server started.");

        s.awaitTermination();
    }
}