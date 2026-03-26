package com.tramplin.backend.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seeker_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeekerProfile {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String university;
    private Integer graduationYear;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    private String portfolioLinks; // Ссылки через запятую или JSON

    private boolean isPublic = true; // Настройка приватности (нетворкинг)

    @Column(name = "avatar_url")
    private String avatarUrl;
}
