package com.antonioteca.cc42.dao.daoroom;

import androidx.room.Dao;

@Dao
public interface UserDao {
    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(LocalAttendanceList user);

    @Query("SELECT * FROM local_attendance_list " +
            "WHERE campus_id =:campusId AND cursus_id =:cursusId AND event_id =:eventId AND uid = :userId")
    Single<List<LocalAttendanceList>> userAlreadyLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId,
            long userId);

    @Query("SELECT uid FROM local_attendance_list " +
            "WHERE campus_id =:campusId AND cursus_id =:cursusId AND event_id =:eventId")
    Single<List<Long>> geIdsUserLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId);

    @Query("DELETE FROM local_attendance_list " +
            "WHERE campus_id =:campusId AND cursus_id =:cursusId AND event_id =:eventId")
    Completable deleteLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId);*/
}
