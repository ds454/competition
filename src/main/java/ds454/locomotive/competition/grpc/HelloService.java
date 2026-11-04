package ds454.locomotive.competition.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.competition.dbconnection.RepositoryProvider;
import ds454.locomotive.competition.repository.CompetitionRepository;
import ds454.locomotive.generated.grpc.GreetRequest;
import ds454.locomotive.generated.grpc.GreetResponse;
import ds454.locomotive.generated.grpc.HasMetRequest;
import ds454.locomotive.generated.grpc.HasMetResponse;
import ds454.locomotive.generated.grpc.HelloServiceGrpc.HelloServiceImplBase;
import io.grpc.stub.StreamObserver;

public class HelloService extends HelloServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(HelloService.class);
    
    CompetitionRepository userRepo = RepositoryProvider.getRepository(CompetitionRepository.class);
    
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
    }

    @Override
    public void hasMet(HasMetRequest request, StreamObserver<HasMetResponse> responseObserver) {
    }
    
}
