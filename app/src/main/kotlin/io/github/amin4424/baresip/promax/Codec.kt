package io.github.amin4424.baresip.promax

import androidx.compose.runtime.MutableState

data class Codec(val name: String, var enabled: MutableState<Boolean>)