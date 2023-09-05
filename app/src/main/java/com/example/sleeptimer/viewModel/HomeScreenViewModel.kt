package com.example.sleeptimer.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class HomeScreenViewModel : ViewModel() {

    private var _processedAngle : MutableState<Int> = mutableIntStateOf(0)
    private var _previousAngle : MutableState<Int> = mutableIntStateOf(0)
    private var totalHours : MutableState<UInt> = mutableStateOf(0U)

    val processedMins : Int
        get() = _processedAngle.value.div(6).plus(totalHours.value.toInt() * 60)

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

}