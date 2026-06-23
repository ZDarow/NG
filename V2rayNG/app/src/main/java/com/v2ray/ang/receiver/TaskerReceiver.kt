package com.v2ray.ang.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Process
import android.text.TextUtils
import com.v2ray.ang.AppConfig
import com.v2ray.ang.core.CoreServiceManager
import com.v2ray.ang.util.LogUtil

class TaskerReceiver : BroadcastReceiver() {

    companion object {
        private const val TASKER_PACKAGE = "net.dinglisch.android.taskerm"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Verify the caller is Tasker or our own app
        val callingUid = Binder.getCallingUid()
        if (callingUid != Process.myUid() && !isTaskerPackage(context, callingUid)) {
            LogUtil.w(AppConfig.TAG, "TaskerReceiver: blocked call from UID $callingUid")
            return
        }

        try {
            val bundle = intent?.getBundleExtra(AppConfig.TASKER_EXTRA_BUNDLE)
            val switch = bundle?.getBoolean(AppConfig.TASKER_EXTRA_BUNDLE_SWITCH, false)
            val guid = bundle?.getString(AppConfig.TASKER_EXTRA_BUNDLE_GUID).orEmpty()

            if (switch == null || TextUtils.isEmpty(guid)) {
                return
            } else if (switch) {
                if (guid == AppConfig.TASKER_DEFAULT_GUID) {
                    CoreServiceManager.startVServiceFromToggle(context)
                } else {
                    CoreServiceManager.startVService(context, guid)
                }
            } else {
                CoreServiceManager.stopVService(context)
            }
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Error processing Tasker broadcast", e)
        }
    }

    private fun isTaskerPackage(context: Context, uid: Int): Boolean {
        return try {
            val packages = context.packageManager.getPackagesForUid(uid) ?: return false
            packages.any { it == TASKER_PACKAGE }
        } catch (_: Exception) {
            false
        }
    }
}
