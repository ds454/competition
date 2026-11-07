package ds454.locomotive.competition;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ds454.locomotive.competition.grpc.CompetitionGrpcService;
import ds454.locomotive.competition.grpc.HeaderInterceptor;
import ds454.locomotive.competition.spring.BeanProvider;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static int grpcPort = Integer.valueOf(System.getenv().get("GRPC_PORT"));

    public static void main(String[] args) throws Exception {
        log.info("Starting Competition service...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("ds454.locomotive.competition");
        BeanProvider.setContext(context);

        log.info("Starting gRPC server on port " + Main.grpcPort);
        Server s;

        s = ServerBuilder.forPort(Main.grpcPort)
                .addService(ServerInterceptors.intercept(new CompetitionGrpcService(), new HeaderInterceptor()))
                .build()
                .start();
        log.info("gRPC server started.");

        s.awaitTermination();
    }
}