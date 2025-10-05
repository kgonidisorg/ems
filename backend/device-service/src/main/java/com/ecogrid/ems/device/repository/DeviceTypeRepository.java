package com.ecogrid.ems.device.repository;

import com.ecogrid.ems.device.entity.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeviceType entity
 */
@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {

    /**
     * Find device type by name
     */
    Optional<DeviceType> findByName(String name);

    /**
     * Find device types by category
     */
    List<DeviceType> findByCategory(String category);

    /**
     * Find all device types ordered by name
     */
    @Query("SELECT dt FROM DeviceType dt ORDER BY dt.name")
    List<DeviceType> findAllOrderByName();

    /**
     * Check if device type exists by name
     */
    boolean existsByName(String name);
}