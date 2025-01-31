package com.antonioteca.cc42.dao.daoroom;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.antonioteca.cc42.model.LocalAttendanceList;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(LocalAttendanceList user);

    @Query("SELECT * FROM local_attendance_list " +
            "WHERE campus_id =:campusId AND cursus_id =:cursusId AND event_id =:eventId AND uid = :userId")
    Single<List<LocalAttendanceList>> userAlreadyLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId,
            long userId);
}
