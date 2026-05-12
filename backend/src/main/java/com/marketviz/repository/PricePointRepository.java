package com.marketviz.repository;

import com.marketviz.model.PricePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** JPA repository for {@link PricePoint} entities. */
public interface PricePointRepository extends JpaRepository<PricePoint, Long> {

    List<PricePoint> findByAssetIdAndTimestampBetweenOrderByTimestampAsc(
            Long assetId, LocalDate from, LocalDate to
    );

    Optional<PricePoint> findByAssetIdAndTimestamp(Long assetId, LocalDate timestamp);

    /**
     * Returns the most recent date for which a price record exists for the given asset.
     * Used by the fetch service to determine how far back a new download needs to go.
     */
    @Query("SELECT MAX(p.timestamp) FROM PricePoint p WHERE p.asset.id = :assetId")
    Optional<LocalDate> findLatestTimestampByAssetId(@Param("assetId") Long assetId);
}
