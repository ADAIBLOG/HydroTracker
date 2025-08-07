package com.cemcakmak.hydrotracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.cemcakmak.hydrotracker.MainActivity
import com.cemcakmak.hydrotracker.R
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.database.DatabaseInitializer
import com.cemcakmak.hydrotracker.utils.WaterCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build

/**
 * HydroTracker Progress Widget
 * Shows daily hydration progress with circular progress indicator
 */
class HydroProgressWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widget instances
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Widget was added to home screen - start periodic updates
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WidgetUpdateService.scheduleUpdates(context)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Last widget was removed from home screen - stop periodic updates
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
                
                // Get current progress
                val progress = waterRepository.getTodayProgress().first()
                val userProfile = userRepository.userProfile.first()
                
                // Create widget layout
                val views = RemoteViews(context.packageName, R.layout.widget_hydro_progress)
                
                // Update progress text
                val currentText = WaterCalculator.formatWaterAmount(progress.currentIntake)
                val goalText = userProfile?.let { WaterCalculator.formatWaterAmount(it.dailyWaterGoal) } ?: "2700ml"
                val progressText = "$currentText / $goalText"
                
                views.setTextViewText(R.id.widget_progress_text, progressText)
                
                // Update progress percentage
                val progressPercent = (progress.progress * 100).toInt()
                views.setTextViewText(R.id.widget_progress_percent, "$progressPercent%")
                
                // Update last updated time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val lastUpdated = "Updated ${timeFormat.format(Date())}"
                views.setTextViewText(R.id.widget_last_updated, lastUpdated)
                
                // Set progress bar
                views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
                
                // Set click intent to open app
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                // Apply theme colors
                applyTheme(context, views, userRepository)
                
                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                // Fallback to default values if data unavailable
                updateWidgetWithDefaults(context, appWidgetManager, appWidgetId)
            }
        }
    }
    
    private fun updateWidgetWithDefaults(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_hydro_progress)
        
        // Set default values
        views.setTextViewText(R.id.widget_progress_text, "0ml / 2700ml")
        views.setTextViewText(R.id.widget_progress_percent, "0%")
        views.setTextViewText(R.id.widget_last_updated, "Tap to open HydroTracker")
        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
        
        // Set click intent
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun applyTheme(context: Context, views: RemoteViews, userRepository: UserRepository) {
        try {
            // Detect if system is in dark mode
            val isDarkMode = context.resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            // Use theme-appropriate colors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - Try to use dynamic colors with fallback
                try {
                    val primaryColor = if (isDarkMode) {
                        context.getColor(android.R.color.system_accent1_200)
                    } else {
                        context.getColor(android.R.color.system_accent1_600)
                    }
                    val onSurfaceColor = if (isDarkMode) {
                        0xFFE6E1E5.toInt() // Light text for dark mode
                    } else {
                        0xFF1D1B20.toInt() // Dark text for light mode
                    }
                    val onSurfaceVariantColor = if (isDarkMode) {
                        0xFFCAC4D0.toInt() // Muted light text
                    } else {
                        0xFF49454F.toInt() // Muted dark text
                    }
                    
                    views.setTextColor(R.id.widget_progress_text, onSurfaceColor)
                    views.setTextColor(R.id.widget_progress_percent, primaryColor)
                    views.setTextColor(R.id.widget_last_updated, onSurfaceVariantColor)
                    
                } catch (e: Exception) {
                    // Fallback to standard colors if dynamic colors fail
                    applyFallbackColors(views, isDarkMode)
                }
            } else {
                // Pre-Android 12 fallback
                applyFallbackColors(views, isDarkMode)
            }
            
        } catch (e: Exception) {
            // Emergency fallback
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
            0xFF64C0F8.toInt() // Light blue for dark mode
        } else {
            0xFF0077BE.toInt() // HydroTracker blue for light mode
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

/**
 * Widget update helper functions with enhanced error handling
 */
object WidgetUpdateHelper {
    
    fun updateAllWidgets(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            
            // Update Progress Widget (4x1)
            updateWidgetType<HydroProgressWidget>(context, appWidgetManager)
            
            // Update Compact Widget (2x1)  
            updateWidgetType<HydroCompactWidget>(context, appWidgetManager)
            
            // Update Large Widget (4x2)
            updateWidgetType<HydroLargeWidget>(context, appWidgetManager)
            
        } catch (e: Exception) {
            // Log error silently for production
            android.util.Log.w("WidgetUpdateHelper", "Failed to update widgets", e)
        }
    }
    
    private inline fun <reified T : AppWidgetProvider> updateWidgetType(
        context: Context, 
        appWidgetManager: AppWidgetManager
    ) {
        try {
            val component = android.content.ComponentName(context, T::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, T::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            // Handle individual widget type errors
            android.util.Log.w("WidgetUpdateHelper", "Failed to update ${T::class.simpleName}", e)
        }
    }
    
    fun updateSpecificWidget(context: Context, widgetClass: Class<out AppWidgetProvider>) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = android.content.ComponentName(context, widgetClass)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, widgetClass).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            android.util.Log.w("WidgetUpdateHelper", "Failed to update specific widget", e)
        }
    }
}