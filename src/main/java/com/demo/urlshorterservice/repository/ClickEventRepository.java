package com.demo.urlshorterservice.repository;

import com.demo.urlshorterservice.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository
        extends JpaRepository<ClickEvent, String> {


    long countByShortCode(String shortCode);

List<ClickEvent> findByShortCodeOrderByClickedAtDesc(
        String shortCode, Pageable pageable);

        @Query("SELECT e.country, COUNT(e) AS cnt " +
            "FROM ClickEvent e WHERE e.shortCode = :shortCode " +
            "GROUP BY e.country ORDER BY cnt DESC")
    List<Object[]> countByCountry(
            @Param("shortCode") String shortCode);

        @Query("SELECT e.device, COUNT(e) AS cnt " +
            "FROM ClickEvent e WHERE e.shortCode = :shortCode " +
            "GROUP BY e.device")
    List<Object[]> countByDevice(
            @Param("shortCode") String shortCode);


    @Query("SELECT DATE(e.clickedAt), COUNT(e) " +
            "FROM ClickEvent e WHERE e.shortCode = :shortCode " +
            "AND e.clickedAt >= :since " +
            "GROUP BY DATE(e.clickedAt)")
    List<Object[]> dailyClicksLastNDays(
            @Param("shortCode") String shortCode,
            @Param("since") LocalDateTime since);
}