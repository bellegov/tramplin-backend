package com.tramplin.backend.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerProfile {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private String companyName;
    private String inn;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String industry;
    private String website;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "logo_url")
    private String logoUrl;
}
