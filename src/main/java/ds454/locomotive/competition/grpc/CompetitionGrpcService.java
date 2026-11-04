package ds454.locomotive.competition.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.competition.service.CompetitionService;
import ds454.locomotive.competition.spring.BeanProvider;
import ds454.locomotive.competition.util.Result;
import ds454.locomotive.generated.grpc.competition.*;
import ds454.locomotive.generated.grpc.problem.ProblemsList;
import io.grpc.stub.StreamObserver;

public class CompetitionGrpcService extends CompetitionServiceGrpc.CompetitionServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(CompetitionGrpcService.class);

    private final CompetitionService compService = BeanProvider.getBean(CompetitionService.class);

    @Override
    public void makeCompetition(MakeCompetitionRequest request,
            StreamObserver<MakeCompetitionResponse> responseObserver) {
        log.debug("Received make competition request");
        var result = MakeCompetitionResponse.newBuilder();
        var svcResult = compService.makeCompetition(UserUuidInterceptor.UserUuid.get(), request);
        if (svcResult == null) {
            log.error("Impossible state reached");
            return;
        }

        switch (svcResult) {
            case Result.Success(Competition comp) -> {
                log.debug("Received success from competition service");
                result.setSuccess(comp);
            }
            case Result.Failure(ProblemsList pList) -> {
                log.debug("Received failure from competition service");
                result.setProblems(pList);
            }
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCompetitions(GetCompetitionsRequest request,
            StreamObserver<GetCompetitionsResponse> responseObserver) {
        log.debug("Received get competitions request");
    }
}
