package com.example.sleeptimer.views

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sleeptimer.R
import com.example.sleeptimer.components.Screen
import com.example.sleeptimer.ui.theme.SleepTimerTheme
import com.example.sleeptimer.viewModel.HomeScreenViewModel

@Composable
fun MainView() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainView.route) {
        composable(Screen.MainView.route) {
            HomeScreen(navController)
        }
        composable(Screen.SettingsView.route) {
            HomeScreen(navController)
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel( )
) {
    val context = LocalContext.current
    val customCardColors = CardDefaults.cardColors(
        contentColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface,
    )
    val customCardElevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp,
        pressedElevation = 1.dp,
        focusedElevation = 2.dp
    )

    Column {

        Row (
            modifier = Modifier.fillMaxWidth()
        ){
            Card(
                colors = customCardColors,
                elevation = customCardElevation,
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Sleep timer ",
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
                }
            }
        }

        Spacer(modifier = Modifier.weight(1.0f))

        ShowTimePicker(context, 0, 0)

        Spacer(modifier = Modifier.weight(1.0f))

        Column (
            verticalArrangement = Arrangement.Bottom
        ){
            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Button(
                    onClick = {
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = FloatingActionButtonDefaults.extendedFabShape,
                    // Uses ButtonDefaults.ContentPadding by default
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 15.dp,
                        bottom = 10.dp
                    )
                ) {
                    //Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = "Send Message",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1.0f))
                Button(
                    onClick = {
                    },
                    // Uses ButtonDefaults.ContentPadding by default
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 15.dp,
                        bottom = 10.dp
                    )
                ) {
                    //Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = "Send Message",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }

                Button(onClick = {
                    //your onclick code
                },
                    colors = ButtonDefaults.buttonColors(Color.DarkGray))

                {
                    Text(text = "Button with gray background",color = Color.White)
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
            Toast.makeText( context, time.value, Toast.LENGTH_SHORT).show();
        }, initHour, initMinute, true
    )
    Button(onClick = {
        timePickerDialog.show()

        }
    ) {
        Text(text = "Open Time Picker")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SleepTimerTheme {
        MainView()
    }
}