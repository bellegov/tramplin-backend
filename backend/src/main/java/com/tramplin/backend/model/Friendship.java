package com.tramplin.backend.model;

import com.tramplin.backend.model.SeekerProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Friendship {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "seeker_id")
    private SeekerProfile seeker;

    @ManyToOne @JoinColumn(name = "friend_id")
    private SeekerProfile friend;

    private boolean accepted = false; // Статус запроса в друзья
}