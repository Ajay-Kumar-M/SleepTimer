package com.example.sleeptimer.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {

    private var _processedAngle : MutableState<Int> = mutableIntStateOf(0)
    private var _previousAngle : MutableState<Int> = mutableIntStateOf(0)
    private var totalHours : MutableState<UInt> = mutableStateOf(0U)
    private var hasNotificationPermission: Boolean = false
    val processedMins : Int
        get() = _processedAngle.value.div(6).plus(totalHours.value.toInt() * 60)
    private val _isMediaVolumeChecked = MutableStateFlow(false)
    val isMediaVolumeChecked: StateFlow<Boolean>
        get() = _isMediaVolumeChecked.asStateFlow()

    fun onAngleChanged(currentAngle: Double){
        val tempCurrentAngle = currentAngle.toInt()
        if((_previousAngle.value>350)&&(tempCurrentAngle<10)){
            totalHours.value  = totalHours.value.plus(1u)
        } else if((_previousAngle.value<10)&&(tempCurrentAngle>350)){
            totalHours.value = totalHours.value.minus(1u)
        }
        _processedAngle.value = tempCurrentAngle
        _previousAngle.value = tempCurrentAngle
    }

    fun changeNotificationPermissionState(permissionState: Boolean) {
        hasNotificationPermission = permissionState
    }

    fun toggleMediaVolumeCheckbox() {
        _isMediaVolumeChecked.value = !_isMediaVolumeChecked.value
    }
}

/*

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
//            .setOnAudioFocusChangeListener { focusChange ->
//                when (focusChange) {
//                    AudioManager.AUDIOFOCUS_LOSS -> {
//                        // Pause your media playback
//                    }
//                    AudioManager.AUDIOFOCUS_GAIN -> {
//                        // Resume your media playback
//                    }
//                    // Handle other focus changes as needed
//                }
//            }
            .build()
 */