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
 * HydroTracker Large Widget (4x2)
 * Shows progress with quick add buttons
 */
class HydroLargeWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_QUICK_ADD = "com.cemcakmak.hydrotracker.QUICK_ADD"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CONTAINER = "container"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_QUICK_ADD) {
            val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
            val container = intent.getStringExtra(EXTRA_CONTAINER) ?: "Glass"
            
            widgetScope.launch {
                try {
                    val userRepository = UserRepository(context)
                    val waterRepository = DatabaseInitializer.getWaterIntakeRepository(context, userRepository)
                    
                    // Quick add water
                    waterRepository.addWaterIntake(
                        amount = amount,
                        containerPreset = com.cemcakmak.hydrotracker.data.models.ContainerPreset(
                            name = container,
                            volume = amount
                        )
                    )
                    
                    // Update all widgets
                    WidgetUpdateHelper.updateAllWidgets(context)
                    
                } catch (e: Exception) {
                    // Handle error silently for widgets
                }
            }
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
                
                val views = RemoteViews(context.packageName, R.layout.widget_hydro_large)
                
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
                
                // Set up quick add buttons
                setupQuickAddButtons(context, views, appWidgetId)
                
                // Set main click intent
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
    
    private fun setupQuickAddButtons(context: Context, views: RemoteViews, appWidgetId: Int) {
        // Glass button (250ml)
        val glassIntent = Intent(context, HydroLargeWidget::class.java).apply {
            action = ACTION_QUICK_ADD
            putExtra(EXTRA_AMOUNT, 250.0)
            putExtra(EXTRA_CONTAINER, "Glass")
        }
        val glassPendingIntent = PendingIntent.getBroadcast(
            context, 1001, glassIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_glass, glassPendingIntent)
        
        // Bottle button (500ml)
        val bottleIntent = Intent(context, HydroLargeWidget::class.java).apply {
            action = ACTION_QUICK_ADD
            putExtra(EXTRA_AMOUNT, 500.0)
            putExtra(EXTRA_CONTAINER, "Bottle")
        }
        val bottlePendingIntent = PendingIntent.getBroadcast(
            context, 1002, bottleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_bottle, bottlePendingIntent)
        
        // Cup button (200ml)
        val cupIntent = Intent(context, HydroLargeWidget::class.java).apply {
            action = ACTION_QUICK_ADD
            putExtra(EXTRA_AMOUNT, 200.0)
            putExtra(EXTRA_CONTAINER, "Cup")
        }
        val cupPendingIntent = PendingIntent.getBroadcast(
            context, 1003, cupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_cup, cupPendingIntent)
    }
    
    private fun updateWidgetWithDefaults(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_hydro_large)
        
        views.setTextViewText(R.id.widget_progress_text, "0ml / 2700ml")
        views.setTextViewText(R.id.widget_progress_percent, "0%")
        views.setTextViewText(R.id.widget_last_updated, "Tap to open HydroTracker")
        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        setupQuickAddButtons(context, views, appWidgetId)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
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