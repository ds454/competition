package ds454.locomotive.competition.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class UserUuidInterceptor implements ServerInterceptor {

    // What the gRPC implementation actually gets
    public static final Context.Key<String> UserUuid = Context.key(System.getenv("UUID_METADATA_KEY"));

    // Key of the metadata that has the uuid from auth
    private static final Metadata.Key<String> UserUuidHeader =
            Metadata.Key.of(System.getenv("UUID_METADATA_KEY"), Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        // Get the uuid from the header
        String uuid = headers.get(UserUuidHeader);

        // If it's missing, return unauthenticated
        if (uuid == null || uuid.isBlank()) {
            call.close(Status.UNAUTHENTICATED.withDescription("unauthenticated"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
        
        // Otherwise, add it to the context
        Context ctx = Context.current().withValue(UserUuid, uuid);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}