package com.cemcakmak.hydrotracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cemcakmak.hydrotracker.MainActivity
import com.cemcakmak.hydrotracker.R
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.database.DatabaseInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build

/**
 * HydroTracker Compact Widget (2x1)
 * Shows progress in a compact circular format
 */
class HydroCompactWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WidgetUpdateService.scheduleUpdates(context)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WidgetUpdateService.cancelUpdates(context)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            try {
                val userRepository = UserRepository(context)
                val waterRepository = DatabaseInitializer.getWaterIntakeRepository(context, userRepository)
                
                val progress = waterRepository.getTodayProgress().first()
                val userProfile = userRepository.userProfile.first()
                
                val views = RemoteViews(context.packageName, R.layout.widget_hydro_compact)
                
                // Update progress text - compact format
                val currentText = formatCompact(progress.currentIntake)
                val goalText = userProfile?.let { formatCompact(it.dailyWaterGoal) } ?: "2.7L"
                val progressText = "$currentText / $goalText"
                
                views.setTextViewText(R.id.widget_progress_text, progressText)
                
                // Update progress percentage
                val progressPercent = (progress.progress * 100).toInt()
                views.setTextViewText(R.id.widget_progress_percent, "$progressPercent%")
                
                // Update time - compact format
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                views.setTextViewText(R.id.widget_last_updated, timeFormat.format(Date()))
                
                // Set circular progress
                views.setProgressBar(R.id.widget_progress_circle, 100, progressPercent, false)
                
                // Set click intent
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                // Apply theme
                applyTheme(context, views, userRepository)
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                updateWidgetWithDefaults(context, appWidgetManager, appWidgetId)
            }
        }
    }
    
    private fun updateWidgetWithDefaults(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_hydro_compact)
        
        views.setTextViewText(R.id.widget_progress_text, "0L / 2.7L")
        views.setTextViewText(R.id.widget_progress_percent, "0%")
        views.setTextViewText(R.id.widget_last_updated, "--:--")
        views.setProgressBar(R.id.widget_progress_circle, 100, 0, false)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun formatCompact(amount: Double): String {
        return when {
            amount >= 1000 -> String.format("%.1fL", amount / 1000)
            else -> "${amount.toInt()}ml"
        }
    }
    
    private fun applyTheme(context: Context, views: RemoteViews, userRepository: UserRepository) {
        try {
            val isDarkMode = context.resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val primaryColor = if (isDarkMode) {
                        context.getColor(android.R.color.system_accent1_200)
                    } else {
                        context.getColor(android.R.color.system_accent1_600)
                    }
                    val onSurfaceColor = if (isDarkMode) {
                        0xFFE6E1E5.toInt()
                    } else {
                        0xFF1D1B20.toInt()
                    }
                    val onSurfaceVariantColor = if (isDarkMode) {
                        0xFFCAC4D0.toInt()
                    } else {
                        0xFF49454F.toInt()
                    }
                    
                    views.setTextColor(R.id.widget_progress_text, onSurfaceColor)
                    views.setTextColor(R.id.widget_progress_percent, primaryColor)
                    views.setTextColor(R.id.widget_last_updated, onSurfaceVariantColor)
                    
                } catch (e: Exception) {
                    applyFallbackColors(views, isDarkMode)
                }
            } else {
                applyFallbackColors(views, isDarkMode)
            }
            
        } catch (e: Exception) {
            views.setTextColor(R.id.widget_progress_text, 0xFF000000.toInt())
            views.setTextColor(R.id.widget_progress_percent, 0xFF0077BE.toInt())
            views.setTextColor(R.id.widget_last_updated, 0xBB000000.toInt())
        }
    }
    
    private fun applyFallbackColors(views: RemoteViews, isDarkMode: Boolean) {
        val onSurfaceColor = if (isDarkMode) {
            0xFFE6E1E5.toInt()
        } else {
            0xFF1D1B20.toInt()
        }
        val primaryColor = if (isDarkMode) {
            0xFF64C0F8.toInt()
        } else {
            0xFF0077BE.toInt()
        }
        val mutedTextColor = if (isDarkMode) {
            0xBBE6E1E5.toInt()
        } else {
            0xBB1D1B20.toInt()
        }
        
        views.setTextColor(R.id.widget_progress_text, onSurfaceColor)
        views.setTextColor(R.id.widget_progress_percent, primaryColor)
        views.setTextColor(R.id.widget_last_updated, mutedTextColor)
    }
}