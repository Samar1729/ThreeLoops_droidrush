package com.threeloops.mirrorarchive.data.db

import androidx.room.TypeConverter
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ScanPhase

class DatabaseTypeConverters {
    @TypeConverter
    fun albumTypeToString(value: AlbumType): String = value.name

    @TypeConverter
    fun stringToAlbumType(value: String): AlbumType = AlbumType.valueOf(value)

    @TypeConverter
    fun scanPhaseToString(value: ScanPhase): String = value.name

    @TypeConverter
    fun stringToScanPhase(value: String): ScanPhase = ScanPhase.valueOf(value)
}
