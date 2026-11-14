package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.TerritoryDailyStay;
import com.bosshi.maeul.territory.repository.dto.RegionStay;
import com.bosshi.maeul.territory.repository.dto.UserStay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TerritoryDailyStayRepository extends JpaRepository<TerritoryDailyStay, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from TerritoryDailyStay d where d.userId = :userId and d.stayDate = :date")
    int deleteByUserIdAndStayDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 사용자의 시군구별 누적 점령 시간 */
    @Query("select new com.bosshi.maeul.territory.repository.dto.RegionStay(d.regionId, sum(d.staySeconds)) " +
            "from TerritoryDailyStay d where d.userId = :userId group by d.regionId")
    List<RegionStay> sumByRegion(@Param("userId") Long userId);

    /** 사용자의 [from, to) 기간 시군구별 점령 시간 */
    @Query("select new com.bosshi.maeul.territory.repository.dto.RegionStay(d.regionId, sum(d.staySeconds)) " +
            "from TerritoryDailyStay d where d.userId = :userId and d.stayDate >= :from and d.stayDate < :to group by d.regionId")
    List<RegionStay> sumByRegionBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 한 시군구의 [from, to) 기간 사용자별 점령 시간 */
    @Query("select new com.bosshi.maeul.territory.repository.dto.UserStay(d.userId, sum(d.staySeconds)) " +
            "from TerritoryDailyStay d where d.regionId = :regionId and d.stayDate >= :from and d.stayDate < :to group by d.userId")
    List<UserStay> sumByUserBetween(@Param("regionId") String regionId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 사용자가 한 시군구에 [from, to) 기간 동안 쌓은 점령 시간 */
    @Query("select coalesce(sum(d.staySeconds), 0) from TerritoryDailyStay d " +
            "where d.userId = :userId and d.regionId = :regionId and d.stayDate >= :from and d.stayDate < :to")
    long sumByUserAndRegionBetween(@Param("userId") Long userId, @Param("regionId") String regionId,
                                   @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 사용자가 한 시군구에 from 날짜부터 쌓은 점령 시간 */
    @Query("select coalesce(sum(d.staySeconds), 0) from TerritoryDailyStay d " +
            "where d.userId = :userId and d.regionId = :regionId and d.stayDate >= :from")
    long sumByUserAndRegionFrom(@Param("userId") Long userId, @Param("regionId") String regionId, @Param("from") LocalDate from);

    /** 사용자가 그날 점령 시간이 있는 시군구 */
    @Query("select d.regionId from TerritoryDailyStay d where d.userId = :userId and d.stayDate = :date and d.staySeconds > 0")
    List<String> findRegionIdsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 사용자의 전체 점령 시간 */
    @Query("select coalesce(sum(d.staySeconds), 0) from TerritoryDailyStay d where d.userId = :userId")
    long sumByUser(@Param("userId") Long userId);
}
