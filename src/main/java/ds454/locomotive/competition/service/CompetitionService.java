package ds454.locomotive.competition.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

        var now = Instant.now().getEpochSecond();

        log.debug("Making new competition model");
        var newCompetition = new ds454.locomotive.competition.model.Competition(
                request.getName(),
                request.getGameId(),
                new User(uuid, steamId),
                request.getStartTime(),
                request.getEndTime());

        newCompetition
                .getInvitations()
                .addAll(
                        request
                                .getOtherCompetitorsList()
                                .stream()
                                .map(otherCompetitor -> new CompetitionInvitation(otherCompetitor, now))
                                .toList());

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

    public Result<List<String>, Problem> getPendingInvitationsForCompetition(String requesterUuid,
            GetCompetitionPendingInvitationsRequest request) {

        // Make sure the competition exists
        var repoCompetition = compRepo.findById(UUID.fromString(request.getCompetitionUuid()));
        if (repoCompetition.isEmpty()) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.CompetitionDoesNotExist)
                    .setField("competition_uuid").setMessage("This competition does not exist").build());
        }

        // Make sure the requester is the host of the competition
        var competition = repoCompetition.get();
        if (!competition.getHost().getUuid().equals(requesterUuid)) {
            return new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.InsufficientPermissions)
                    .setField("uuid").setMessage("Insufficient Permissions").build());
        }

        return new Result.Success<List<String>, Problem>(
                competition
                        .getInvitations()
                        .stream()
                        .map(t -> t.getUserUuid())
                        .toList());
    }

    public List<ds454.locomotive.generated.grpc.competition.Competition> getCompetitionInvites(String requesterUuid) {
        var competitions = compRepo.getInvitations(requesterUuid);
        log.debug("User {} has {} invitations", requesterUuid, competitions.size());

        var grpcCompetitions = competitions.stream().map(CompetitionService::modelToGrpc).toList();

        return grpcCompetitions;
    }

    public Optional<Problem> handleCompetitionInvitation(
            String competitionId, String inviteeUuid, String inviteeSteamid, HandleCompetitionAction action) {

        var now = Instant.now().getEpochSecond();

        // Get the competition
        var repoCompetition = compRepo.findById(UUID.fromString(competitionId));
        if (repoCompetition.isEmpty()) {
            log.debug("Competition {} does not exist", competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.CompetitionDoesNotExist).build());
        }
        var competition = repoCompetition.get();

        // Make sure the user is actually invited
        if (!competition.getInvitations().contains(new CompetitionInvitation(inviteeUuid))) {
            log.debug("User {} is not invited to competition {}", inviteeUuid, competitionId);
            return Optional.of(
                    Problem.newBuilder().setProblem(ProblemType.UserNotInvitedToCompetition).build());
        }

        // Check that we can still accept the invitation
        switch (acceptInvitationCondition) {
            // 0 if a user can only accept competitions that have yet to start
            case 0:
                // If the competition has already started
                if (competition.getStartTime() < now)
                    return Optional.of(
                            Problem.newBuilder().setProblem(ProblemType.CompetitionAlreadyStarted).build());
                break;

            // 1 if a user can accept competitions that have already started, but not ended.
            case 1:
                if (competition.getEndTime() < now)
                    return Optional.of(
                            Problem.newBuilder().setProblem(ProblemType.CompetitionAlreadyEnded).build());
                break;

            default:
                log.error("Invalid acceptInvitationCondition option. Must be 0 or 1.");
                return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        }

        switch (action) {
            // If we accept the invitation, remove the user from the invitations array and
            // place them in the competitors array.
            case Accept:
                compRepo.acceptCompetitionInvitation(UUID.fromString(competitionId), inviteeUuid, inviteeSteamid);
                return Optional.empty();

            // If we decline the invitation, just remove the user from the invitations
            // array.
            case Decline:
                compRepo.declineCompetitionInvitation(UUID.fromString(competitionId), inviteeUuid);
                return Optional.empty();

            default:
            case UNRECOGNIZED:
                log.error("Received competition invitation action {}, which is unrecognized.", action);
                return Optional.of(Problem.newBuilder().setProblem(ProblemType.ServerError).build());
        }

    }

    public Optional<Problem> cancelCompetition(String competitionId, String userUuid) {
        var repoComp = compRepo.findById(UUID.fromString(competitionId));
        if (repoComp.isEmpty()) {
            log.debug("Competition {} does not exist", competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.CompetitionDoesNotExist).build());
        }
        var competition = repoComp.get();

        // Check to make sure the user is the host
        if (!competition.getHost().getUuid().equals(userUuid)) {
            log.debug("User {} is not the host of competition {}, and cannot cancel it.", userUuid, competitionId);
            return Optional.of(Problem.newBuilder().setProblem(ProblemType.InsufficientPermissions).build());
        }

        // Delete/Cancel the competition
        compRepo.deleteById(UUID.fromString(competitionId));

        return Optional.empty();
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
