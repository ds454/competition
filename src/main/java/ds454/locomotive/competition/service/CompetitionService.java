package ds454.locomotive.competition.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ds454.locomotive.competition.model.CompetitionInvitation;
import ds454.locomotive.competition.model.User;
import ds454.locomotive.competition.repository.CompetitionRepository;
import ds454.locomotive.competition.util.Result;
import ds454.locomotive.generated.grpc.competition.*;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;
import ds454.locomotive.generated.grpc.problem.ProblemsList;

@Service
public class CompetitionService {
    private static final Logger log = LoggerFactory.getLogger(CompetitionService.class);

    private static final int compNameMaxLen = Integer.valueOf(System.getenv("COMPETITION_NAME_MAX_LEN"));
    private static final int compMinLen = Integer.valueOf(System.getenv("COMPETITION_MIN_LEN_SEC"));
    private static final int compMaxLen = Integer.valueOf(System.getenv("COMPETITION_MAX_LEN_SEC"));

    // 0 if a user can only accept competitions that have yet to start
    // 1 if a user can accept competitions that have already started
    private static final int acceptInvitationCondition = Integer.valueOf(System.getenv("ACCEPT_INVITATION_CONDITION"));

    private final CompetitionRepository compRepo;

    public CompetitionService(CompetitionRepository compRepo) {
        this.compRepo = compRepo;
    }

    public Result<ds454.locomotive.generated.grpc.competition.Competition, ProblemsList> makeCompetition(String uuid,
            String steamId,
            MakeCompetitionRequest request) {

        var compProblems = getCompetitionProblems(request);
        if (compProblems.isPresent()) {
            var problems = compProblems.get();
            log.debug("Found {} problems with makeCompetition request", problems.getProblemsCount());
            return new Result.Failure<>(problems);
        }

        log.debug("Making new competition model");
        var newCompetition = new ds454.locomotive.competition.model.Competition(
                request.getName(),
                request.getGameId(),
                new User(uuid, steamId),
                request.getStartTime(),
                request.getEndTime());

        log.debug("Saving new competition model to db");
        var success = compRepo.save(newCompetition);
        var result = modelToGrpc(success);

        return new Result.Success<>(result);
    }

    public List<ds454.locomotive.generated.grpc.competition.Competition> getCompetitions(String uuid,
            GetCompetitionsRequest request) {
        log.debug("Getting competitions for user {}", uuid);

        List<ds454.locomotive.competition.model.Competition> modelCompetitions = compRepo.findByCompetitorsUuid(uuid);

        if (modelCompetitions.isEmpty())
            log.debug("User {} is not in any competitions", uuid);
        else
            log.debug("User {} is in {} competitions", uuid, modelCompetitions.size());

        var grpcCompetitions = modelCompetitions.stream().map(CompetitionService::modelToGrpc).toList();

        return grpcCompetitions;
    }

    public Optional<Problem> inviteUserToCompetition(String requesterUuid, String inviteeUuid, String competitionId) {
        // Get the referenced competition
        var competitionFromDb = compRepo.findById(UUID.fromString(competitionId));
        if (competitionFromDb.isEmpty()) {
            log.debug("Competition id {} does not exist", competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.CompetitionDoesNotExist).build());
        }
        var competition = competitionFromDb.get();

        // Check to make sure the requester is the host
        if (!competition.getHost().getUuid().equals(requesterUuid)) {
            log.debug("Requester {} does not have invitation permissions for competition id {}", requesterUuid,
                    competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.InsufficientPermissions).build());
        }

        // Make sure the invitee isn't already in the competition
        if (competition.getCompetitors().contains(new User(inviteeUuid, "none"))) {
            log.debug("User {} is already in competition id {}", inviteeUuid, competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.RequestAllReadySent).build());
        }

        // Make sure the invitee hasn't already been invited
        if (competition.getInvitations().contains(new CompetitionInvitation(inviteeUuid))) {
            log.debug("User {} already invited to competition id {}", inviteeUuid, competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.RequestAllReadySent).build());
        }

        // Invite the user
        compRepo.inviteUserToCompetition(UUID.fromString(competitionId), inviteeUuid, Instant.now().getEpochSecond());

        return Optional.empty();
    }

    public List<ds454.locomotive.generated.grpc.competition.Competition> getCompetitionInvites(String requesterUuid) {
        var competitions = compRepo.getInvitations(requesterUuid);
        log.debug("User {} has {} invitations", requesterUuid, competitions.size());

        var grpcCompetitions = competitions.stream().map(CompetitionService::modelToGrpc).toList();

        return grpcCompetitions;
    }

    private Optional<ProblemsList> getCompetitionProblems(MakeCompetitionRequest request) {
        var problems = ProblemsList.newBuilder();

        // Check the name
        if (request.getName().isBlank()) {
            problems.addProblems(makeInvalidInput("name", "Competition name is missing"));
        }
        if (request.getName().length() > compNameMaxLen) {
            problems.addProblems(makeInvalidInput("name", "Competition name is too long, max 32 letters"));
        }

        // Check that the start time is valid
        var now = Instant.now().getEpochSecond();
        if (request.getStartTime() == 0) {
            problems.addProblems(makeInvalidInput("start_time", "Start time is required"));
        } else if (request.getStartTime() < now) {
            problems.addProblems(makeInvalidInput("start_time", "Start time must be in the future"));
        }

        // Check that the end time is valid
        if (request.getEndTime() == 0) {
            problems.addProblems(makeInvalidInput("end_time", "End time is required"));
        } else if (request.getEndTime() < now) {
            problems.addProblems(makeInvalidInput("end_time", "End time must be in the future"));
        }

        // Need the inputs to make sense before continuing
        if (problems.getProblemsCount() > 0)
            return Optional.of(problems.build());

        // Check the relationship between start and end time
        var compLen = request.getEndTime() - request.getStartTime();

        // End time was before start time
        if (compLen < 0) {
            problems.addProblems(makeInvalidInput("end_time", "End time must be after start time"));
        } else if (compLen < compMinLen) {
            problems.addProblems(makeInvalidInput("end_time", "Competition is too short"));
        } else if (compLen > compMaxLen) {
            problems.addProblems(makeInvalidInput("end_time", "Competition is too long"));
        }

        return problems.getProblemsCount() == 0 ? Optional.empty() : Optional.of(problems.build());
    }

    private Problem makeInvalidInput(String field, String message) {
        return Problem.newBuilder().setProblem(ProblemType.InvalidInput).setField(field).setMessage(message).build();
    }

    private static ds454.locomotive.generated.grpc.competition.Competition modelToGrpc(
            ds454.locomotive.competition.model.Competition comp) {

        return ds454.locomotive.generated.grpc.competition.Competition
                .newBuilder()
                .setId(comp.getUuid().toString())
                .setName(comp.getName())
                .setHostUserId(comp.getHost().getUuid())
                .setGameId(comp.getGameId())
                .setStartTime(comp.getStartTime())
                .setEndTime(comp.getEndTime())
                .addAllCompetitorUuids(comp.getCompetitors().stream().map((user) -> {
                    // Impossible
                    if (user.getUuid() == null)
                        throw new IllegalStateException();
                    return user.getUuid();
                }).toList())
                .build();
    }
}
