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
        log.info("Received make competition request");
        var result = MakeCompetitionResponse.newBuilder();
        var svcResult = compService.makeCompetition(
                HeaderInterceptor.UserUuid.get(),
                HeaderInterceptor.SteamId.get(),
                request);

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

        var result = GetCompetitionsResponse.newBuilder();
        var scvResult = compService.getCompetitions(
                HeaderInterceptor.UserUuid.get(),
                request);

        result.addAllCompetitions(scvResult);

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void inviteUserToCompetition(InviteUserToCompetitionRequest request,
            StreamObserver<InviteUserToCompetitionResponse> responseObserver) {

        log.debug("Receive inviteUserToCompetitionRequest");
        var result = InviteUserToCompetitionResponse.newBuilder();

        var svcResult = compService.inviteUserToCompetition(
                HeaderInterceptor.UserUuid.get(),
                request.getUuidToInvite(),
                request.getCompetitionUuid());

        if (svcResult.isPresent()) {
            result.setProblem(svcResult.get());
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCompetitionInvitations(GetCompetitionInvitationsRequest request,
            StreamObserver<GetCompetitionInvitationsResponse> responseObserver) {

        log.debug("Receive getCompetitionInvitations request");
        var result = GetCompetitionInvitationsResponse.newBuilder();

        var svcResult = compService.getCompetitionInvites(HeaderInterceptor.UserUuid.get());

        result.addAllCompetitions(svcResult);

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void handleCompetitionInvitation(HandleCompetitionInvitationRequest request,
            StreamObserver<HandleCompetitionInvitationResponse> responseObserver) {

        log.debug("Received handleCompetitionInvitation request");
        var result = HandleCompetitionInvitationResponse.newBuilder();

        var svcResult = compService.handleCompetitionInvitation(
                request.getCompetitionId(),
                HeaderInterceptor.UserUuid.get(),
                HeaderInterceptor.SteamId.get(),
                request.getAction());

        if (svcResult.isPresent()) {
            result.setProblem(svcResult.get());
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }

    @Override
    public void cancelCompetition(CancelCompetitionRequest request,
            StreamObserver<CancelCompetitionResponse> responseObserver) {

        log.debug("Received cancelCompetition request");
        var result = CancelCompetitionResponse.newBuilder();

        var svcResult = compService.cancelCompetition(
                request.getCompetitionId(),
                HeaderInterceptor.UserUuid.get());

        if (svcResult.isPresent()) {
            result.setProblem(svcResult.get());
        }

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }
}
