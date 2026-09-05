package io.github.amin4424.baresip.pro

import androidx.compose.runtime.MutableState

data class Codec(val name: String, var enabled: MutableState<Boolean>)