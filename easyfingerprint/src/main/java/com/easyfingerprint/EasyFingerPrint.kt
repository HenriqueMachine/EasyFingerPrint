package com.easyfingerprint

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.ColorRes
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * created at 26/07/2018
 * @author rafaelbarbosatec
 * @version 1.0
 *
 */
class EasyFingerPrint(private val activity: Activity?):FingerprintManagerCompat.AuthenticationCallback(){

    companion object {
        val CODE_ERRO_HARDWARE_NOT_SUPPORTED = 100
        val CODE_ERRO_NOT_ABLED = 200
        val CODE_ERRO_NOT_FINGERS = 300
        val CODE_ERRO_GREATER_ANDROID_M = 400
        val CODE_ERRO_CANCEL = 500
        val CODE_NOT_PERMISSION_BIOMETRIC = 600

    }

    private val TAG = "EasyFingerPrint"

    private var fingerprintManager: FingerprintManagerCompat? = null
    private var keyguardManager: KeyguardManager? = null
    private var keyStore: KeyStore? = null
    private var cipher: Cipher? = null
    private val KEY_NAME = "AndroidKey"
    private var listern: ResultFingerPrintListern? = null
    private var bottomSheetDialog: CustomBottomSheetDialog? = null
    private var cancellationSignal: CancellationSignal? = null
    private var AndroidKeyStore:String = "AndroidKeyStore"

    init {
        bottomSheetDialog = CustomBottomSheetDialog(activity)
        bottomSheetDialog?.setListern(object : CustomBottomSheetDialog.CustomBottomSheetListern{
            override fun open() {

            }

            override fun closed() {
                cancelScan()
            }

        })
    }

    /**
     * Method used to set Tittle in BottomDialog
     * @param tittle
     */
    fun setTittle(tittle:String): EasyFingerPrint{
        bottomSheetDialog?.setTittle(tittle)
        return this
    }

    /**
     * Method used to set SubTittle in BottomDialog
     * @param subTittle
     */
    fun setSubTittle(subTittle:String): EasyFingerPrint{
        bottomSheetDialog?.setSubTittle(subTittle)
        return this
    }

    /**
     * Method used to set Description in BottomDialog
     * @param description
     */
    fun setDescription(description:String): EasyFingerPrint{
        bottomSheetDialog?.setDescription(description)
        return this
    }

    /**
     * Method used to set error mensage when finger not reconized
     * @param error
     */
    fun setMsgError(error:String): EasyFingerPrint{
        bottomSheetDialog?.setMsgError(error)
        return this
    }

    /**
     * Method used to set icon to BottomDialog
     * @param icon
     */
    fun setIcon(icon: Drawable?): EasyFingerPrint{
        bottomSheetDialog?.setIcon(icon)
        return this
    }

    /**
     * Method used to set color primary to BottomDialog
     * @param color
     */
    fun setColorPrimary(@ColorRes color:Int): EasyFingerPrint{
        bottomSheetDialog?.setColorPrimary(color)
        return this
    }

    /**
     * Method used to set litern that recieve sucess or error of the scan finger
     * @param listern
     */
    fun setListern(listern: ResultFingerPrintListern): EasyFingerPrint{
        this.listern = listern
        return this
    }

    /**
     * Method used to start Scan fingerPrint
     * @param icon
     */
    fun startScan(){

        if (Util.isAndroidGraterM()){
            if (checkPermissionFingerPrint()){
                confFingerPrint()
            }else{
                logError("ADD <uses-permission android:name=\"android.permission.USE_FINGERPRINT\" /> in your AndroidManifest.xml")
                listern?.onError("ADD <uses-permission android:name=\"android.permission.USE_FINGERPRINT\" /> in your AndroidManifest.xml", CODE_NOT_PERMISSION_BIOMETRIC)
            }
        }else{
            logError(activity?.getString(R.string.text_android_marsmallow_required)!!)
            listern?.onError(activity?.getString(R.string.text_android_marsmallow_required)!!, CODE_ERRO_GREATER_ANDROID_M)
        }

    }

    /**
     * Method used to check permission "USE_FINGERPRINT"
     * @param icon
     */
    private fun checkPermissionFingerPrint():Boolean {

        return ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Method used to check permission "USE_FINGERPRINT"
     */
    @SuppressLint("NewApi")
    private fun confFingerPrint() {

        try {

            fingerprintManager = FingerprintManagerCompat.from(activity!!)
            keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE)
                    as KeyguardManager?

            if (fingerprintManager?.isHardwareDetected != true){
                logError(activity.getString(R.string.text_hardware_not_detected))
                listern?.onError(activity.getString(R.string.text_hardware_not_detected), CODE_ERRO_HARDWARE_NOT_SUPPORTED)
                return
            }

            if (!keyguardManager?.isKeyguardSecure!!){
                logError(activity.getString(R.string.text_add_lock_fingerprint))
                listern?.onError(activity.getString(R.string.text_add_lock_fingerprint), CODE_ERRO_NOT_ABLED)
                return
            }

            if (!fingerprintManager!!.hasEnrolledFingerprints()){
                logError(activity.getString(R.string.text_add_one_finger))
                listern?.onError(activity.getString(R.string.text_add_one_finger), CODE_ERRO_NOT_FINGERS)
                return
            }

            generateKey()

            if (cipherInit()){


                val cryptoObject = FingerprintManagerCompat.CryptoObject(cipher!!)
                cancellationSignal = CancellationSignal()
                fingerprintManager?.authenticate(
                        cryptoObject,
                        0,
                        cancellationSignal,
                        this,
                        null
                )

                bottomSheetDialog?.show()

            }

        }catch (e: Exception){
            logError(e.printStackTrace().toString())
        }

    }

    /**
     * Method used cancel scan FingerPrint
     */
    fun cancelScan(){

        if (cancellationSignal?.isCanceled != true){
            cancellationSignal?.cancel()
        }

        if (bottomSheetDialog?.isShowing() == true){
            bottomSheetDialog?.close()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKey() {

        try {

            keyStore = KeyStore.getInstance(AndroidKeyStore)
            val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore
            )

            keyStore?.load(null)
            keyGenerator.init(
                    KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_ENCRYPT or
                                    KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(
                                    KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build())
            keyGenerator.generateKey()


        }catch (e:Exception){

            logError(e.printStackTrace().toString())

        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun cipherInit(): Boolean {

        try {

            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + "/"
                    + KeyProperties.BLOCK_MODE_CBC
                    + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)

        }catch (e:Exception){

            logError(e.printStackTrace().toString())

        }

        try {

            keyStore?.load(null)

            var key: SecretKey = keyStore?.getKey(KEY_NAME,
                    null) as SecretKey

            cipher?.init(Cipher.ENCRYPT_MODE, key)

            return true

        }catch (e:Exception){

            logError(e.message.toString())
            return false

        }

    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        super.onAuthenticationError(errMsgId, errString)
        logError("Scan fingerPrint canceled")
        listern?.onError("Scan fingerPrint canceled", CODE_ERRO_CANCEL)
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        bottomSheetDialog?.showError()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        listern?.onSucess(result?.cryptoObject)
        bottomSheetDialog?.close()
    }

    fun logError(mensage:String){
        Log.e(TAG,mensage)
    }

    /**
     * Interface responsible to return responce of the scan result
     */
    interface ResultFingerPrintListern{
        fun onError(mensage: String, code:Int)
        fun onSucess(cryptoObject: FingerprintManagerCompat.CryptoObject?)
    }

}
