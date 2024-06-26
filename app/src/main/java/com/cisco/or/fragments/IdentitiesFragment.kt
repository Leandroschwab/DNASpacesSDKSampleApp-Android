package com.cisco.or.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cisco.or.R
import com.cisco.or.activity.login.SelectSigningServiceActivity
import com.cisco.or.databinding.IdentityBinding
import com.cisco.or.databinding.IdentityDataGoogleBinding
import com.cisco.or.sdk.OpenRoaming
import com.cisco.or.sdk.models.UserDetail
import com.cisco.or.utils.Constant

class IdentitiesFragment : Fragment() {
    companion object {
        private val TAG: String = IdentitiesFragment::class.java.name
    }

    private lateinit var binding: IdentityBinding
    //private lateinit var bindingIdentityData: IdentityDataGoogleBinding
    var pushToken = ""
    var privacySettings = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = IdentityBinding.inflate(inflater, container, false)
        val identityView = binding.root
        identityView.setTag("identityView")
        try{
                OpenRoaming.getUserDetails { user ->
                    OpenRoaming.getPrivacySettings { privacySettings ->
                        this.privacySettings = privacySettings
                        loadFragment(user, inflater, container, privacySettings)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
        return identityView
    }

    private fun fillIdentity(userDetail: UserDetail, inflater: LayoutInflater, container: ViewGroup?, privacy: Boolean) {
        if (userDetail != null) {

            val bindingIdentityData = IdentityDataGoogleBinding.inflate(inflater, container, false)
            //val bindingIdentityData = IdentityDataGoogleBinding.bind(viewMain)

            binding.deleteaccount.setOnClickListener {
                deleteAccount(userDetail)
            }
            binding.deleteProfile.setOnClickListener {
                deleteProfile()
            }
            binding.updateButton.setOnClickListener {
                updateUser(userDetail)
            }
            binding.lastPushNotification.setOnClickListener {
                lastPushNotification()
            }

            var email = userDetail.email
            if (email.isNullOrBlank()) {
                email = "Not Available"
            }
            bindingIdentityData.emailText.text = email

            bindingIdentityData.switch1.isChecked = privacySettings
            bindingIdentityData.switch1.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) privacySettingsDialog(bindingIdentityData.switch1) else sendPrivacySettings(isChecked)
            }

            bindingIdentityData.pushSwitch.isChecked = OpenRoaming.isActiveAssociatePush()
            bindingIdentityData.pushSwitch.setOnCheckedChangeListener { _, isChecked ->
                setPushNotificationAssociate(isChecked)
            }

            binding.identityLayout.addView(bindingIdentityData.root)
        }
    }

    private fun sendPrivacySettings(checked: Boolean) {
        try {
            OpenRoaming.setPrivacySettings(checked) {
                activity?.runOnUiThread {
                    Toast.makeText(context, R.string.share_email_updated, Toast.LENGTH_SHORT).show()
                }
            }
        }
        catch (e : java.lang.Exception){
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    private fun setPushNotificationAssociate(checked: Boolean) {

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferencesName(this.context)
        pushToken = this.context!!.getSharedPreferences(sharedPrefs, Context.MODE_PRIVATE).getString(
            Constant.PUSH_TOKEN, null)
            .toString()

        try {
            if(checked){
                OpenRoaming.associatePushIdentifier(pushToken) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, R.string.push_notification_enable, Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                OpenRoaming.dissociatePushIdentifier {
                    activity?.runOnUiThread {
                        Toast.makeText(context, R.string.push_notification_disable, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        catch (e : java.lang.Exception){
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    private fun deleteAccount(userDetail: UserDetail) {
        AlertDialog.Builder(this.context)
            .setMessage(R.string.confirm_delete_account)
            .setTitle(R.string.delete_account)
            .setCancelable(false)
            .setPositiveButton(R.string.delete) { _, _ ->
                    deleteAccountFromServer()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create().show()
    }

    private fun deleteAccountFromServer() {
        OpenRoaming.deleteUserAccount {
            try {
                this.activity?.runOnUiThread {
                    val intent = Intent(this.activity, SelectSigningServiceActivity::class.java)
                    this.activity?.finish()
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
        }
    }

    private fun deleteProfile() {
        AlertDialog.Builder(this.context)
            .setMessage(R.string.confirm_delete_profile)
            .setTitle(R.string.delete_profile)
            .setCancelable(false)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteProfileFromServer()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create().show()
    }

    private fun deleteProfileFromServer() {
        OpenRoaming.deleteProfile {
            try {
                this.activity?.runOnUiThread {
                    val intent = Intent(this.activity, SelectSigningServiceActivity::class.java)
                    this.activity?.finish()
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
        }
    }

    private fun privacySettingsDialog(switch: Switch) {
        AlertDialog.Builder(this.context)
            .setMessage(R.string.confirm_not_sharing_email)
            .setTitle(R.string.dont_share_email)
            .setCancelable(false)
            .setPositiveButton(R.string.proceed) { _, _ ->
                switch.isChecked = false
                sendPrivacySettings(switch.isChecked)

            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                switch.isChecked = true
                sendPrivacySettings(switch.isChecked)
                dialog.cancel()
            }
            .create().show()
    }

    private fun updateUser(userDetail: UserDetail) {
        OpenRoaming.getUserDetails {
            val fragment = UpdateUserFragment(it)
            fragmentManager
                ?.beginTransaction()
                ?.add(R.id.container, fragment)
                ?.addToBackStack("UpdateUserFragment")
                ?.commit()
        }
    }

    private fun loadFragment(userDetail: UserDetail,inflater: LayoutInflater, container: ViewGroup?, privacy : Boolean) {
        activity?.runOnUiThread {
            fillIdentity(userDetail,inflater, container, privacy)
        }
    }


    private fun lastPushNotification() {
        val fragment = LastPushNotificationFragment()
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.add(R.id.container, fragment)
            ?.addToBackStack("LastPushNotificationFragment")
            ?.commit()
    }
}