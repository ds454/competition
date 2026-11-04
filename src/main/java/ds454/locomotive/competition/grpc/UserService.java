package ds454.locomotive.competition.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;
import ds454.locomotive.competition.dbconnection.RepositoryProvider;
import ds454.locomotive.competition.model.User;
import ds454.locomotive.competition.repository.UserRepository;
import ds454.locomotive.generated.grpc.AddFriendRequest;
import ds454.locomotive.generated.grpc.AddFriendResponse;
import ds454.locomotive.generated.grpc.AddUserRequest;
import ds454.locomotive.generated.grpc.AddUserResponse;
import ds454.locomotive.generated.grpc.DeleteAccountRequest;
import ds454.locomotive.generated.grpc.DeleteAccountResponse;
import ds454.locomotive.generated.grpc.GetUserRequest;
import ds454.locomotive.generated.grpc.GetUserResponse;
import ds454.locomotive.generated.grpc.HasMetResponse;
import ds454.locomotive.generated.grpc.RemoveFriendRequest;
import ds454.locomotive.generated.grpc.RemoveFriendResponse;
import ds454.locomotive.generated.grpc.UserServiceGrpc.UserServiceImplBase;

public class UserService extends UserServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(HelloService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = RepositoryProvider.getRepository(UserRepository.class);
    
    @Override
    public void addUser(AddUserRequest request, StreamObserver<AddUserResponse> responseObserver) {
        // Log that the request came in.
        log.info("Received greet request!");
        // TODO: Add server interceptor to get metadata from the request
        // Initialize variables
        var response = HasMetResponse.newBuilder();
        String username = request.getUsername();
        String email = request.getEmailAddress();
        String steamAccountName = request.getSteamAccountName();
        // Create the user, and don't forget error handling, especially with add requests.
        
        // Log the new user was created
    }
    
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        
    }
    
    @Override
    public void addFriend(AddFriendRequest request, StreamObserver<AddFriendResponse> responseObserver) {
        
    }
    
    @Override
    public void removeFriend(RemoveFriendRequest request, StreamObserver<RemoveFriendResponse> responseObserver) {
        
    }
    
    @Override 
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        
    }
}
