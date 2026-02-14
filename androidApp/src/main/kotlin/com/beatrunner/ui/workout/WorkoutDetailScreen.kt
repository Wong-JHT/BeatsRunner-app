package com.beatrunner.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beatrunner.ui.theme.*
import com.beatrunner.util.DateTimeUtils
import com.beatrunner.viewmodel.WorkoutHistoryViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutHistoryViewModel = koinInject()
) {
    val session = viewModel.getSessionById(workoutId)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "训练详情",
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "返回",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        }
    ) { innerPadding ->
        if (session == null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BackgroundDark, SurfaceDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "❌",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "未找到训练记录",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BackgroundDark, SurfaceDark)
                        )
                    )
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date and time
                Text(
                    text = "start time"/*DateTimeUtils.formatDateTime(session.startTime)*/,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceLight
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "训练概要", 
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailStatisticItem(
                                label = "时长",
                                value = DateTimeUtils.formatDuration(session.durationSeconds),
                                icon = "⏱️"
                            )
                            DetailStatisticItem(
                                label = "距离",
                                value = DateTimeUtils.formatDistance(session.distanceMeters),
                                icon = "🏃"
                            )
                            DetailStatisticItem(
                                label = "卡路里",
                                value = DateTimeUtils.formatCalories(session.caloriesBurned),
                                icon = "🔥"
                            )
                        }
                    }
                }
                
                // Speed and Incline Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceLight
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "速度与坡度", 
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DetailMetricRow(
                            label = "平均速度",
                            value = DateTimeUtils.formatSpeed(session.avgSpeed),
                            icon = "⚡"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailMetricRow(
                            label = "平均坡度",
                            value = DateTimeUtils.formatIncline(session.avgIncline),
                            icon = "📈"
                        )
                    }
                }
                
                // Heart Rate Card (if available)
                if (session.avgHeartRate != null || session.maxHeartRate != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceLight
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "心率数据", 
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            session.avgHeartRate?.let { avgHr ->
                                DetailMetricRow(
                                    label = "平均心率",
                                    value = DateTimeUtils.formatHeartRate(avgHr),
                                    icon = "❤️"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            session.maxHeartRate?.let { maxHr ->
                                DetailMetricRow(
                                    label = "最大心率",
                                    value = DateTimeUtils.formatHeartRate(maxHr),
                                    icon = "💓"
                                )
                            }
                        }
                    }
                }
                
                // Placeholder for future chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceLight
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "📊",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "速度/坡度图表",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                "即将推出",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailStatisticItem(
    label: String, 
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value, 
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label, 
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun DetailMetricRow(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = AccentGreen,
            fontWeight = FontWeight.SemiBold
        )
    }
}
