package com.dzadafa.gitreak

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class GitreakWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == MainViewModel.ACTION_STREAK_UPDATED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget =
                android.content.ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("GITREAK_PREFS", Context.MODE_PRIVATE)
            val streakCount = prefs.getInt(MainViewModel.PREF_STREAK_COUNT, 0)
            val contributedToday =
                prefs.getBoolean(MainViewModel.PREF_CONTRIBUTED_TODAY, false)
            val isFrozen = 
                prefs.getBoolean(MainViewModel.PREF_IS_FROZEN, false)

            val displayStreak = if (contributedToday) streakCount + 1 else streakCount

            val views: RemoteViews =
                if (displayStreak == 0) {
                    RemoteViews(context.packageName, R.layout.gitreak_widget_zero_layout)
                } else {
                    RemoteViews(context.packageName, R.layout.gitreak_widget_layout).apply {
                        setTextViewText(R.id.tv_widget_streak, displayStreak.toString())
                        
                        val iconRes = when {
                            contributedToday -> R.drawable.ic_fire
                            isFrozen -> R.drawable.ic_freeze 
                            else -> R.drawable.ic_fire_off
                        }
                        
                        setImageViewResource(R.id.iv_widget_fire, iconRes)
                    }
                }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
