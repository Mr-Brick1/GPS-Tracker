package com.mr_brick.gps_tracker.db
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert
    suspend fun insertTrack(trackItem: TrackItem)

    @Query("SELECT * FROM track")
    fun getAllTracks(): Flow<List<TrackItem>>
}