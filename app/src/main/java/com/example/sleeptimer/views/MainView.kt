package com.example.sleeptimer.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sleeptimer.R
import com.example.sleeptimer.components.MediaVolumeWorker
import com.example.sleeptimer.components.Screen
import com.example.sleeptimer.components.SleepTimerWorker
import com.example.sleeptimer.model.TimerSingleton
import com.example.sleeptimer.notification.TimerNotificationService
import com.example.sleeptimer.ui.theme.SleepTimerTheme
import com.example.sleeptimer.viewModel.HomeScreenViewModel
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.atan2

@Composable
fun MainView() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainView.route) {
        composable(Screen.MainView.route) {
            HomeScreen(navController)
        }
        composable(Screen.SettingsView.route) {
            SettingsView(navController)
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = viewModel( )
) {
    val context = LocalContext.current
    val customCardColors = CardDefaults.cardColors(
        contentColor = MaterialTheme.colorScheme.primary,
        containerColor = Color.Cyan,//MaterialTheme.colorScheme.primaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface,
    )
    val customCardElevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp,
        pressedElevation = 1.dp,
        focusedElevation = 2.dp
    )
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            hasNotificationPermission = result
            if (hasNotificationPermission) {
                // Permission granted, proceed with notification-related tasks
                Toast.makeText(context, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
                viewModel.changeNotificationPermissionState(true)
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(context, "Notification Permission Denied by User. Enable Setting Manually!", Toast.LENGTH_SHORT).show()
                viewModel.changeNotificationPermissionState(false)
            }
        }
    val applicationWorkManager = WorkManager.getInstance(context)
    var sleepTimerWorkRequest:OneTimeWorkRequest
    var mediaVolumeWorkRequest:PeriodicWorkRequest
    val isTimerRunning by TimerSingleton.isTimerRunning.collectAsStateWithLifecycle()
    val isMediaVolumeChecked by viewModel.isMediaVolumeChecked.collectAsStateWithLifecycle()
    val timerNotificationService = TimerNotificationService(context)

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            TimerSingleton.startTimer(viewModel.processedMins,timerNotificationService, hasNotificationPermission)
            sleepTimerWorkRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
                .setInitialDelay(Duration.ofMinutes(TimerSingleton.liveTimer.toLong()))
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.LINEAR,
                    duration = Duration.ofSeconds(10)
                )
                .addTag("sleep_timer_work")
                .build()
            applicationWorkManager.enqueueUniqueWork("sleep_timer_work",ExistingWorkPolicy.REPLACE,sleepTimerWorkRequest)
            if (isMediaVolumeChecked) {
                mediaVolumeWorkRequest = PeriodicWorkRequestBuilder<MediaVolumeWorker>(15, TimeUnit.MINUTES)
                    .setInitialDelay(15,TimeUnit.MINUTES)
                    .addTag("media_volume_work")
                    .build()
                applicationWorkManager.enqueueUniquePeriodicWork("media_volume_work",ExistingPeriodicWorkPolicy.UPDATE,mediaVolumeWorkRequest)
            }
        } else {
            TimerSingleton.stopTimer(timerNotificationService,applicationWorkManager)
        }
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black)
            .fillMaxHeight()
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ){
            Card(
                colors = customCardColors,
                elevation = customCardElevation,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "Sleep Timer ",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Image(
                        painter = painterResource(R.drawable.timer_icon),
                        "Header timer",
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1.0f))
                    IconButton(
                        onClick = {
                            navController.navigate("SettingsView")
                        },
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.5f))
        //ShowTimePicker(context, 0, 0)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 2f, fill = true)
        ) {
            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
            ) {
                TimerCanvas(isTimerRunning, viewModel)
            }
            if (isTimerRunning) {
                ShimmeringText(
                    text = "Remaining Minutes: ${TimerSingleton.liveTimer}",
                    shimmerColor = Color.White,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Text(
                    text = "Minutes: ${viewModel.processedMins}",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Row(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                ) {
                    Checkbox(
                        checked = isMediaVolumeChecked,
                        onCheckedChange = { viewModel.toggleMediaVolumeCheckbox() }
                    )
                    Text(
                        text = "Reduce Media Volume every 15 minutes",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier
                            .padding(0.dp,15.dp,0.dp,0.dp)
                    )
                }
            }

            if (!hasNotificationPermission) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(3.dp),
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 15.dp,
                        bottom = 10.dp
                    ),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(
                        text = "Enable Notification Permission !",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.5f))
        Column (
            verticalArrangement = Arrangement.Bottom
        ){
            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Button(
                    modifier = Modifier.fillMaxWidth(0.333f),
                    onClick = {
                        TimerSingleton.extendSleepTimer(timerNotificationService)
                        applicationWorkManager.cancelAllWorkByTag("sleep_timer_work")
                        val updateWorkRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
                            .setInitialDelay(Duration.ofMinutes(TimerSingleton.liveTimer.toLong()))
                            .setBackoffCriteria(
                                backoffPolicy = BackoffPolicy.LINEAR,
                                duration = Duration.ofSeconds(10)
                            )
                            .addTag("sleep_timer_work")
                            .build()
                        applicationWorkManager.enqueueUniqueWork("sleep_timer_work",ExistingWorkPolicy.REPLACE,updateWorkRequest)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(3.dp),
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 15.dp,
                        bottom = 10.dp
                    ),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(
                        text = "Extend",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = {
                        if (viewModel.processedMins > 0) { TimerSingleton.toggleTimer() }
                        //if (hasNotificationPermission) { viewModel.toggleTimerNotification(context) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(3.dp),
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 15.dp,
                        bottom = 10.dp
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    enabled = !isTimerRunning
                ) {
                    Text(
                        text = "Start",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(1f),
                    onClick = {
                        if (viewModel.processedMins > 0) { TimerSingleton.toggleTimer() }
                        applicationWorkManager.cancelAllWorkByTag("sleep_timer_work")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(3.dp),
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 15.dp,
                        bottom = 10.dp
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    enabled = isTimerRunning
                ) {
                    Text(
                        text = "Stop",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SleepTimerTheme {
        MainView()
    }
}

@Composable
fun TimerCanvas(
    isTimerRunning: Boolean,
    viewModel: HomeScreenViewModel
) {
    var radius by remember {
        mutableFloatStateOf(0f)
    }
    var shapeCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    var handleCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    var angle by remember {
        mutableDoubleStateOf(0.0)
    }
    //val isTimerRunning = rememberUpdatedState(isTimerRunning)

    Canvas(
        modifier = Modifier
            .size(if (isTimerRunning) 0.dp else 270.dp)
            //.fillMaxSize()
            .alpha(if (isTimerRunning) 0.5f else 1f)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    handleCenter += dragAmount
                    angle = getRotationAngle(handleCenter, shapeCenter)
                    if((angle>=180)&&(viewModel.processedMins<=0)) {
                        angle = 0.0
                    }
                    viewModel.onAngleChanged(angle)
                    change.consume()
                }
        }
            .padding(30.dp)

    ) {
        shapeCenter = center
        radius = size.minDimension / 2
        val x = (shapeCenter.x + kotlin.math.cos(Math.toRadians(angle)) * radius).toFloat()
        val y = (shapeCenter.y + kotlin.math.sin(Math.toRadians(angle)) * radius).toFloat()
        handleCenter = Offset(x, y)
        drawCircle(color = Color.White, style = Stroke(20f), radius = radius) //Color.Black.copy(alpha = 0.10f)
        drawArc(
            //color = Color.,
            startAngle = 0f,
            sweepAngle = angle.toFloat(),
            useCenter = false,
            style = Stroke(30f),
            brush = Brush.sweepGradient( // !!! that what
                0f to Color(0x00EF7B7B),
                0.5f to Color.Yellow,
                1f to Color.Green, // there was a problem with start of the gradient, maybe there way to solve it better
                //1f to Color(0x00EF7B7B)
            ),
        )
        if (!isTimerRunning) {
            drawCircle(color = Color.Cyan, center = handleCenter, radius = 40f)
        }
    }

}

private fun getRotationAngle(currentPosition: Offset, center: Offset): Double {
    val (dx, dy) = currentPosition - center
    val theta = atan2(dy, dx).toDouble()

    var angle = Math.toDegrees(theta)
    if (angle < 0) {
        angle += 360.0
    }

    return angle
}

@Composable
fun ShimmeringText(
    text: String,
    shimmerColor: Color,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    animationSpec: DurationBasedAnimationSpec<Float> = tween(3000, 0, LinearEasing)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmeringTextTransition")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animationSpec),
        label = "ShimmerProgress"
    )

    val brush = remember(shimmerProgress) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                // Define the starting X offset, beginning outside the left edge of the text
                val initialXOffset = -size.width
                // Total distance the shimmer will sweep across (double the text width for full coverage)
                val totalSweepDistance = size.width * 2
                // Calculate the current position of the shimmer based on the animation progress
                val currentPosition = initialXOffset + totalSweepDistance * shimmerProgress

                return LinearGradientShader(
                    colors = listOf(Color.Transparent, shimmerColor, Color.Transparent),
                    from = Offset(currentPosition, 0f),
                    to = Offset(currentPosition + size.width, 0f)
                )
            }
        }
    }

    Text(
        text = text,
        modifier = modifier,
        style = textStyle.copy(brush = brush)
    )
}

/*

@Composable
fun ShowTimePicker(context: Context, initHour: Int, initMinute: Int) {
    val time = remember { mutableStateOf("") }
    val timePickerDialog = TimePickerDialog(
        context,
        {_, hour : Int, minute: Int ->
            time.value = "$hour:$minute"
            Toast.makeText(context, time.value, Toast.LENGTH_SHORT).show()
        }, initHour, initMinute, true
    )
    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                timePickerDialog.show()
            }
        ) {
            Text(text = "Open Time Picker")
        }
    }
}

    val stripeWidthPx = 10
    val stripeGapWidthPx = stripeWidthPx / 3.5f
    val brushSizePx = stripeGapWidthPx + stripeWidthPx
    val stripeStart = stripeGapWidthPx / brushSizePx
    val brush = Brush.linearGradient(
        stripeStart to Color.Transparent,
        stripeStart to Color.Blue,
        start = Offset(0f, 0f),
        end = Offset(brushSizePx, brushSizePx),
        tileMode = TileMode.Repeated
    )

fun Density.createStripeBrush(
        stripeColor: Color,
        stripeWidth: Dp,
        stripeToGapRatio: Float
    ): Brush {
        val stripeWidthPx = stripeWidth.toPx()
        val stripeGapWidthPx = stripeWidthPx / stripeToGapRatio
        val brushSizePx = stripeGapWidthPx + stripeWidthPx
        val stripeStart = stripeGapWidthPx / brushSizePx

        return Brush.linearGradient(
            stripeStart to Color.Transparent,
            stripeStart to stripeColor,
            start = Offset(0f, 0f),
            end = Offset(brushSizePx, brushSizePx),
            tileMode = TileMode.Repeated
        )
    }

//            brush = Brush.sweepGradient( // !!! that what
//                0f to Color.White,
//                0.5f to Color.Yellow,
//                1f to Color.Green, // there was a problem with start of the gradient, maybe there way to solve it better
//                //1f to Color(0x00EF7B7B)
//            ),

 */