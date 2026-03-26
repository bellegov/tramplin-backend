package com.tramplin.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recommendation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "recommender_id")
    private SeekerProfile recommender; // Кто рекомендует

    @ManyToOne @JoinColumn(name = "recommended_id")
    private SeekerProfile recommended; // Кого рекомендуют

    @ManyToOne @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity; // На какую вакансию

    private String message;
}