package com.tramplin.backend.repository;

import com.tramplin.backend.model.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // Найти все отклики конкретного студента
    List<Application> findAllBySeekerId(Long seekerId);

    // Найти все отклики на вакансии конкретного работодателя
    List<Application> findAllByOpportunityEmployerId(Long employerId);

    // Проверка, откликался ли уже студент на эту вакансию
    boolean existsBySeekerIdAndOpportunityId(Long seekerId, Long opportunityId);
}