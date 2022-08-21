package com.medifit.scheduling.timeslots.model

import com.medifit.scheduling.doctors.model.Doctor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TimeslotRepository : CrudRepository<Timeslot, Long> {
    fun findByDoctor(doctor: Doctor): List<Timeslot>

    @Query("SELECT t FROM Timeslot t WHERE t.free = true AND t.doctor.id IN :doctors AND t.startTime >= :from AND t.endTime <= :to")
    fun findFreeTimeslots(
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime,
        @Param("doctors") doctors: List<Long>
    ): List<Timeslot>

    @Query("SELECT t FROM Timeslot t WHERE t.free = true AND t.startTime >= ?1 AND t.endTime <= ?2")
    fun findFreeTimeslots(from: LocalDateTime, to: LocalDateTime): List<Timeslot>

    @Query("SELECT t FROM Timeslot t WHERE t.doctor.id IN (?1) AND t.startTime >= ?2 AND t.endTime <= ?3")
    fun findAllTimeslots(doctors: List<Long>?, from: LocalDateTime, to: LocalDateTime): List<Timeslot>
}
