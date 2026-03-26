package com.tramplin.backend.repository;

import com.tramplin.backend.model.Opportunity;
import com.tramplin.backend.model.OpportunityStatus;
import com.tramplin.backend.model.WorkFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    // Оставляем этот метод для получения всех по статусу (без фильтров)
    List<Opportunity> findAllByStatus(OpportunityStatus status);
    List<Opportunity> findAllByEmployerId(Long employerId);

    // НАШ КРУТОЙ ПОИСК (Тут мы жестко прописали статус 'OPEN')
    @Query("SELECT DISTINCT o FROM Opportunity o LEFT JOIN o.tags t WHERE " +
            "o.status = 'OPEN' AND " +
            "(:city IS NULL OR o.city = :city) AND " +
            "(:format IS NULL OR o.workFormat = :format) AND " +
            "(:tag IS NULL OR t.name = :tag) AND " +
            "(:minSalary IS NULL OR o.salary >= :minSalary)")
    List<Opportunity> findByFilters(
            @Param("city") String city,
            @Param("format") WorkFormat format,
            @Param("tag") String tag,
            @Param("minSalary") Integer minSalary
    );
}