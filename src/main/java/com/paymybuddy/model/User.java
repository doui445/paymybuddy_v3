package com.paymybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    //@JsonProperty(access = Access.WRITE_ONLY)
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("sent")
    private final Set<Transaction> sentTransactions = new HashSet<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("received")
    private final Set<Transaction> receivedTransactions = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "USER_CONNECTIONS",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "connected_user_id")
    )
    @JsonIgnore
    private final Set<User> connections = new HashSet<>();

    public void addConnection(User user) {
        if (user == null || user.equals(this)) return;

        this.connections.add(user);
        user.getConnections().add(this);
    }

    public void removeConnection(User user) {
        if (user == null) return;

        this.connections.remove(user);
        user.getConnections().remove(this);
    }

    @PreRemove
    public void removeConnections() {
        for (User connectedUser : this.connections) {
            connectedUser.getConnections().remove(this);
        }
    }
}
