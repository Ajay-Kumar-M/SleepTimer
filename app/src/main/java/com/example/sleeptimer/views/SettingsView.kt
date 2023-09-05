package com.example.sleeptimer.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sleeptimer.R
import com.example.sleeptimer.components.Screen
import com.example.sleeptimer.ui.theme.SleepTimerTheme

@Composable
fun SettingsView(navController: NavHostController) {

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

    Column (
        modifier = Modifier
            .background(Color.Black)
            .fillMaxHeight()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Card(
                colors = customCardColors,
                elevation = customCardElevation,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier.clickable {
                            navController.popBackStack(Screen.SettingsView.route,true)
                        }
                    )

                    Text(
                        text = "  Sleep Timer ",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Left,
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
                    Icon(
                        Icons.Default.Build,
                        contentDescription = "",
                        tint = Color.Black
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SleepTimerTheme {
        SettingsView(navController = NavHostController(LocalContext.current))
    }
}