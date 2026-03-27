package com.tramplin.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private EmployerProfile employer;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private OpportunityType type;

    @Enumerated(EnumType.STRING)
    private WorkFormat workFormat;

    private String city;
    private String exactAddress;

    // Координаты для карты (Яндекс карты/Leaflet)
    private Double latitude;
    private Double longitude;

    private LocalDateTime publishedAt;
    private LocalDateTime deadline; // Срок действия
    private Integer salary;

    @Enumerated(EnumType.STRING)
    private OpportunityStatus status = OpportunityStatus.OPEN;

    @Builder.Default // Чтобы Lombok Builder не затирал инициализацию
    @ManyToMany
    @JoinTable(
            name = "opportunity_tags",
            joinColumns = @JoinColumn(name = "opportunity_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "image_url")
    private String imageUrl;
}
