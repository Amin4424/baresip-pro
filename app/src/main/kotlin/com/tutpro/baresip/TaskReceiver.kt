package com.tutpro.baresip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d(TAG, "TaskReceiver: received intent ${intent.action}")

        when (intent.action) {

            "io.github.amin4424.baresip.pro.REGISTER", "com.tutpro.baresip.REGISTER",
            "io.github.amin4424.baresip.pro.UNREGISTER", "com.tutpro.baresip.UNREGISTER" -> {
                var aor = intent.getStringExtra("aor")
                if (aor == null) {
                    Log.i(TAG, "TaskReceiver: 'aor' extra is missing")
                    return
                }
                if (!aor.startsWith("sip:"))
                    aor = "sip:$aor"
                val ua = UserAgent.ofAor(aor)
                if (ua == null) {
                    Log.i(TAG, "TaskReceiver: user agent of AoR $aor is not found")
                    return
                }
                val acc = ua.account
                if (intent.action?.endsWith(".REGISTER") == true) {
                    Log.d(TAG, "TaskReceiver: registering $aor")
                    Api.account_set_regint(acc.accp, REGISTRATION_INTERVAL)
                    Api.ua_register(ua.uap)
                    acc.regint = Api.account_regint(acc.accp)
                    Account.saveAccounts()
                }
                else {
                    Log.d(TAG, "TaskReceiver: un-registering $aor")
                    Api.account_set_regint(acc.accp, 0)
                    Api.ua_unregister(ua.uap)
                    acc.regint = Api.account_regint(acc.accp)
                    Account.saveAccounts()
                }
            }

            "io.github.amin4424.baresip.pro.QUIT", "com.tutpro.baresip.QUIT" -> {
                Log.d(TAG, "TaskReceiver: quiting")
                val baresipService = Intent(context, BaresipService::class.java)
                if (BaresipService.isServiceRunning) {
                    baresipService.action = "Stop"
                    context.startService(baresipService)
                }
            }

        }
    }

}
