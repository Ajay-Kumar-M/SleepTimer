package com.example.sleeptimer.views

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.sleeptimer.R
import com.example.sleeptimer.components.Screen
import com.example.sleeptimer.ui.theme.SleepTimerTheme
import com.example.sleeptimer.viewModel.HomeScreenViewModel
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
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            viewModel.pauseMedia(context,hasNotificationPermission)
        } else {
            viewModel.stopTimer(context,hasNotificationPermission)
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

        Spacer(modifier = Modifier.weight(1.0f))

        //ShowTimePicker(context, 0, 0)
        
        Content(isTimerRunning,viewModel)

        if (isTimerRunning) {
            Text(
                text = "Remaning Minutes: ${viewModel.delayInMillis}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .padding(10.dp)
            )
        } else {
            Text(
                text = "Minutes: ${viewModel.processedMins}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .padding(10.dp)
            )
        }

        if (!hasNotificationPermission) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(10.dp),
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
//        Text(
//            text = "Current Mins: ${viewModel.tempnumber.value}",
//            fontSize = 20.sp,
//            textAlign = TextAlign.Center,
//            color = Color.White
//        )

        Spacer(modifier = Modifier.weight(1.0f))

        Column (
            verticalArrangement = Arrangement.Bottom
        ){
            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Button(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = {
                        viewModel.extendSleepTimer()
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
                    modifier = Modifier.fillMaxWidth(1f),
                    onClick = {
                        if (viewModel.processedMins > 0) { viewModel.toggleTimer() }
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
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    if(isTimerRunning) {
                        Text(
                            text = "Stop",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Start",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
        }


    }
}

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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SleepTimerTheme {
        MainView()
    }
}



@Composable
fun Content(
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
            .size(if (isTimerRunning) 0.dp else 280.dp)
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


/*

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