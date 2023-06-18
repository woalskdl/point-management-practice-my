package me.jay.fcp.point.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointReservationRepository extends JpaRepository<PointReservation, Long>, PointReservationCustomRepository {
}
