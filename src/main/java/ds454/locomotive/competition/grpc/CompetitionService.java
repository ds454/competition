package ds454.locomotive.competition.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.competition.dbconnection.RepositoryProvider;
import ds454.locomotive.competition.repository.CompetitionRepository;
import ds454.locomotive.competition.repository.UserRepository;
import ds454.locomotive.generated.grpc.competition.*;
import io.grpc.stub.StreamObserver;

public class CompetitionService extends CompetitionServiceGrpc.CompetitionServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(CompetitionService.class);

    private final CompetitionRepository compRepo = RepositoryProvider.getRepository(CompetitionRepository.class);
    private final UserRepository userRepo = RepositoryProvider.getRepository(UserRepository.class);

    @Override
    public void makeCompetition(MakeCompetitionRequest request,
            StreamObserver<MakeCompetitionResponse> responseObserver) {
        log.debug("Received make competition request");
    }

    @Override
    public void getCompetitions(GetCompetitionsRequest request,
            StreamObserver<GetCompetitionsResponse> responseObserver) {
        log.debug("Received get competitions request");
    }
}
