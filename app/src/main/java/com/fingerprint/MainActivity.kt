package com.fingerprint

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.widget.Toast
import com.easyfingerprint.EasyFingerPrint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {

            EasyFingerPrint(this)
                    .setTittle("Sign in")
                    .setSubTittle("account@account.com.br")
                    .setDescription("In order to use the Fingerprint sensor we need your authorization first.e")
                    .setColorPrimary(R.color.colorPrimary)
                    .setIcon(ContextCompat.getDrawable(this,R.mipmap.ic_launcher_round))
                    .setListern(object : EasyFingerPrint.ResultFingerPrintListern{
                        override fun onError(mensage: String, code: Int) {
                            Toast.makeText(this@MainActivity,"ERROR:$mensage / $code",Toast.LENGTH_SHORT).show()
                        }

                        override fun onSucess(cryptoObject: FingerprintManagerCompat.CryptoObject?) {
                            Toast.makeText(this@MainActivity,"SUCESS",Toast.LENGTH_SHORT).show()
                        }

                    })
                    .startScan()

        }
    }
}
