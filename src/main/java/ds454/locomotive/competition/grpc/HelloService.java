package ds454.locomotive.competition.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.competition.dbconnection.RepositoryProvider;
import ds454.locomotive.competition.model.User;
import ds454.locomotive.competition.repository.UserRepository;
import ds454.locomotive.generated.grpc.GreetRequest;
import ds454.locomotive.generated.grpc.GreetResponse;
import ds454.locomotive.generated.grpc.HasMetRequest;
import ds454.locomotive.generated.grpc.HasMetResponse;
import ds454.locomotive.generated.grpc.HelloServiceGrpc.HelloServiceImplBase;
import io.grpc.stub.StreamObserver;

/**
 * HelloService
 * HelloService.java is a service implementation. It extends the base class and implements handlers for the associated RPC calls.
 * This defines functionalities that main.java can use. Main.java, of course, is what runs
 * on the server.
 * 
 */
public class HelloService extends HelloServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(HelloService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = RepositoryProvider.getRepository(UserRepository.class);
    
    /**    (non-Javadoc)
     * This is an example of a method that handles a request made to the server.
     * 
     * @see ds454.locomotive.generated.grpc.HelloServiceGrpc.AsyncService#greet(ds454.locomotive.generated.grpc.GreetRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        log.info("Received greet request!");

        var response = GreetResponse.newBuilder();
        String name = request.getName();
        User u = new User();

        try {
            u.setUsername(name);
            userRepo.save(u);
            response.setGreeting("Hello, " + name + "! Nice to meet you!.");
        }
        catch (Exception e) {
            response.setGreeting("Nice to see you again " + name + "!");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
    
    /**    (non-Javadoc)
     * This method responds to hasMet requests.
     * 
     * @see ds454.locomotive.generated.grpc.HelloServiceGrpc.AsyncService#hasMet(ds454.locomotive.generated.grpc.HasMetRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    public void hasMet(HasMetRequest request, StreamObserver<HasMetResponse> responseObserver) {
        var response = HasMetResponse.newBuilder();

        var user = userRepo.findByUsername(request.getName());

        if (user.isPresent()) {
            response.setHasMet(true);
        }
        else {
            response.setHasMet(false);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
    
}
