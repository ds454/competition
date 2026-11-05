package ds454.locomotive.competition.service;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    private final CompetitionRepository compRepo;

    public CompetitionService(CompetitionRepository compRepo) {
        this.compRepo = compRepo;
    }

    public Result<ds454.locomotive.generated.grpc.competition.Competition, ProblemsList> makeCompetition(String uuid,
            MakeCompetitionRequest request) {

        var compProblems = getCompetitionProblems(request);
        if (compProblems.isPresent()) {
            return new Result.Failure<>(compProblems.get());
        }

        ds454.locomotive.competition.model.Competition newCompetition = new ds454.locomotive.competition.model.Competition(
                request.getName(), request.getGameId(), new User(uuid, "todoaddsteamid"), request.getStartTime(),
                request.getEndTime());

        var success = compRepo.save(newCompetition);
        var result = Competition
                .newBuilder()
                .setId(success.getUuid().toString())
                .setName(success.getName())
                .setHostUserId(success.getHost().getUuid())
                .setGameId(success.getGameId())
                .setStartTime(success.getStartTime())
                .setEndTime(success.getEndTime())
                .addCompetitorUuids(uuid)
                .build();

        return new Result.Success<>(result);
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
}
