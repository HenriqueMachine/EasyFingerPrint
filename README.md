# EasyFingerPrint

<p align="center">
  <img src="https://github.com/HenriqueMachine/EasyFingerPrint/blob/master/images/ezgif.com-video-to-gif.gif" width="300"/></p>

**EasyFingerPrint** 
It is a digital reading library that has been designed with the focus of facilitating the implementation of *FingerPrint* in a simple and clear way without taking out its robustness and dynamism, besides following all the *guideline* that the 
[Material Design](https://material.io/design/platform-guidance/android-fingerprint.html) provides.
**EasyFingerPrint** already comes with a layout file ready and fully customizable.<br>

***#FingerPrint / #MaterialDesign / #MakeItEasy***

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:
 
```gradle
dependencies {
     compile 'com.github.HenriqueMachine:easyfingerprint:1.0.0'
}
 
```
configure:

```java
EasyFingerPrint(this)
                    .setTittle("Sign in")
                    .setSubTittle("account@account.com.br")
                    .setDescription("In order to use the Fingerprint sensor we need your authorization first.e")
                    .setColorPrimary(R.color.colorPrimary)
                    .setIcon(ContextCompat.getDrawable(this,R.mipmap.ic_launcher_round))
                    .setListern(object : EasyFingerPrint.ResultFingerPrintListern{
                        override fun onError(mensage: String, code: Int) {

                            when(code){
                                EasyFingerPrint.CODE_ERRO_CANCEL -> { } // TO DO
                                EasyFingerPrint.CODE_ERRO_GREATER_ANDROID_M -> { } // TO DO
                                EasyFingerPrint.CODE_ERRO_HARDWARE_NOT_SUPPORTED -> { } // TO DO
                                EasyFingerPrint.CODE_ERRO_NOT_ABLED -> { } // TO DO
                                EasyFingerPrint.CODE_ERRO_NOT_FINGERS -> { } // TO DO
                                EasyFingerPrint.CODE_NOT_PERMISSION_BIOMETRIC -> { } // TO DO
                            }

                            Toast.makeText(this@MainActivity,"Error: $mensage / $code",Toast.LENGTH_SHORT).show()
                        }

                        override fun onSucess(cryptoObject: FingerprintManagerCompat.CryptoObject?) {
                            Toast.makeText(this@MainActivity,"Sucess",Toast.LENGTH_SHORT).show()
                        }

                    })
                    .startScan()
```
