package ds454.locomotive.competition.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @Setter
    private String email;
    
    @Getter
    @Setter
    private String steamAccountName;

    @Getter
    @Setter
    private String steamAccountId;
    
    // List of competitions
    
    // List of friends

    public User() {
    }

    public User(String username, String email, String steamAccountName, String steamAccountId) {
        this.username = username;
        this.email = email;
        this.steamAccountName = steamAccountName;
        this.steamAccountId = steamAccountId;
        // TODO: initialize friends list and competitions list.
    }
    
    
}
