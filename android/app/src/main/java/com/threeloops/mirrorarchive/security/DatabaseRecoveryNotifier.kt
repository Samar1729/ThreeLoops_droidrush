package com.threeloops.mirrorarchive.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object DatabaseRecoveryNotifier {
    private val mutableEvents = MutableStateFlow(0L)
    val events: StateFlow<Long> = mutableEvents

    fun notifyReset(context: Context) {
        context.getSharedPreferences(RecoveringSupportSQLiteOpenHelper.RECOVERY_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(RecoveringSupportSQLiteOpenHelper.RECOVERY_NOTICE_PENDING, true)
            .commit()
        mutableEvents.update { it + 1L }
    }

    @Synchronized
    fun consumePendingNotice(context: Context): Boolean {
        val prefs = context.getSharedPreferences(RecoveringSupportSQLiteOpenHelper.RECOVERY_PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(RecoveringSupportSQLiteOpenHelper.RECOVERY_NOTICE_PENDING, false)) return false
        prefs.edit().putBoolean(RecoveringSupportSQLiteOpenHelper.RECOVERY_NOTICE_PENDING, false).commit()
        return true
    }
}
