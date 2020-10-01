package com.eziosoft.storm32control

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.eziosoft.storm32control.data.SATPosition
import com.eziosoft.storm32control.mavllink.Storm32
import com.robin.locationgetter.EasyLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.e175.klaus.solarpositioning.DeltaT
import net.e175.klaus.solarpositioning.SPA
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity() {
    //setup
    private val APIKey
        "WY8LM4-8AJ8Y6-PQ88ZE-4CV0" //https://www.n2yo.com/ api key PLEASE DON'T USE IT
    private val stormMAC = "20:15:12:17:72:52" // Bluetooth device MAC address
    private var lat = 48.00 //position latitude
    private var lon = 2.00 //position longitude
    private var alt = 0.00 //position altitude


    lateinit var bt: BluetoothSPP
    val storm32 = Storm32()
    var state = BluetoothState.STATE_NONE

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sb1.isEnabled = false
        sb2.isEnabled = false
        sb3.isEnabled = false
        disconnectB.isEnabled = false

        connectB.setOnClickListener {
            bt.connect(stormMAC)
        }

        disconnectB.setOnClickListener {
            bt.disconnect()
        }

        switch1.setOnCheckedChangeListener { _, on ->
            if (on) getSatellitePosition()
        }

        switch2.setOnCheckedChangeListener { _, on ->
            if (on) trackSun()
        }

        sb1.setOnSeekBarChangeListener(seekBarListener)
        sb2.setOnSeekBarChangeListener(seekBarListener)
        sb3.setOnSeekBarChangeListener(seekBarListener)


        bt = BluetoothSPP(this)
        bt.setupService()
        bt.startService(BluetoothState.DEVICE_OTHER)

        for (i in bt.pairedDeviceName.indices) {
            Log.d("aaa", bt.pairedDeviceAddress[i] + " " + bt.pairedDeviceName[i])
        }

        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceDisconnected() {
                Log.d("aaa", "onDeviceDisconnected")
                sb1.isEnabled = false
                sb2.isEnabled = false
                sb3.isEnabled = false
                connectB.isEnabled = true
                disconnectB.isEnabled = false
            }

            override fun onDeviceConnected(name: String?, address: String?) {
                sb1.isEnabled = true
                sb2.isEnabled = true
                sb3.isEnabled = true
                connectB.isEnabled = false
                disconnectB.isEnabled = true
                bt.send(storm32.setCameraAngle(0f, 00f, 00f), false)
            }

            override fun onDeviceConnectionFailed() {
                Log.d("aaa", "onDeviceConnectionFailed")
            }

        })

        bt.setBluetoothStateListener { state -> this.state = state }
        bt.setOnDataReceivedListener { data, message ->
            Log.d("aaa", "setOnDataReceivedListener : $message")
        }


        startLocation()

    }

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            if (state == BluetoothState.STATE_CONNECTED && !switch1.isChecked && !switch2.isChecked) { //not send command when in auto mode
                bt.send(
                    storm32.setCameraAngle(
                        sb1.progress.toFloat(),
                        sb2.progress.toFloat(),
                        sb3.progress.toFloat()
                    ), false
                )
            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        bt.disconnect()
        bt.stopService()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun trackSun() {
        CoroutineScope(Main).launch {
            while (switch2.isChecked) {
                val dateTime = GregorianCalendar.getInstance() as GregorianCalendar
//                dateTime.set(2020,4,17,16,0,0) //test
                val sunPosition = SPA.calculateSolarPosition(
                    dateTime,
                    lat, // latitude (degrees)
                    lon, // longitude (degrees)
                    alt, // elevation (m)
                    DeltaT.estimate(dateTime)
                )

                val sunElevation = 90 - sunPosition.zenithAngle
                val sunAzimuth = sunPosition.azimuth

                val sunElevation1 = -sunElevation
                var sunAzimuth1 = 360 - sunAzimuth
                if (sunAzimuth1 > 180.0)
                    sunAzimuth1 -= 360

                sb1.progress = sunElevation1.toInt()
                sb3.progress = sunAzimuth1.toInt()

                if (state == BluetoothState.STATE_CONNECTED) { //not send command when in auto mode
                    bt.send(
                        storm32.setCameraAngle(
                            sunElevation1.toFloat(),
                            sb2.progress.toFloat(),
                            sunAzimuth1.toFloat()
                        ), false
                    )
                }

                val s = "elev:$sunElevation , azi:$sunAzimuth\n" +
                        "$sunElevation1, $sunAzimuth1\n" + dateTime.time
                textView.text = s

                Log.d("aaa", s)

                delay(1000)
            }

        }
    }

    var querry = true //to not querry too fast
    private fun getSatellitePosition() {
        CoroutineScope(Main).launch {
            val retrofit = RetrofitSingleton.getInstance()

            while (switch1.isChecked) {
                delay(1000)
                if (querry) {
                    Log.d("aaa", "GET")
                    val call = retrofit.create(RetrofitInterface::class.java)
                        .getSatPosition(
                            catalogNumberET.text.toString().toInt(),
                            lat,
                            lon,
                            alt,
                            APIKey,
                            60
                        )
                    call.enqueue(object : Callback<SATPosition> {
                        override fun onFailure(call: Call<SATPosition>, t: Throwable) {
                            Log.d("aaa", "debug: onFailure " + t.message)
                        }

                        override fun onResponse(
                            call: Call<SATPosition>,
                            response: Response<SATPosition>
                        ) {

                            getSatData(response)
                        }
                    })
                }
                querry = false
            }

        }

    }

    private fun getSatData(response: Response<SATPosition>) {
        CoroutineScope(Main).launch {

            if (response.isSuccessful) {
                for (i in response.body()!!.positions.indices) {
                    Log.d("aaa", "debug: response is successful")
                    Log.d("aaa", "debug: " + response.raw().toString())
                    val satAzimuth = response.body()!!.positions[i].azimuth
                    val satElevation = response.body()!!.positions[i].elevation


                    val satElevation1 = -satElevation
                    var satAzimuth1 = 360 - satAzimuth
                    if (satAzimuth1 > 180.0)
                        satAzimuth1 -= 360

                    sb1.progress = satElevation1.toInt()
                    sb3.progress = satAzimuth1.toInt()

                    Log.i("aaa","UPDATE STORM32, $satElevation1, $satAzimuth1")//not send command when in auto mode
                    if (state == BluetoothState.STATE_CONNECTED) {
                        bt.send(
                            storm32.setCameraAngle(
                                satElevation1.toFloat(),
                                sb2.progress.toFloat(),
                                satAzimuth1.toFloat()
                            ), false
                        )
                    }

                    val s =
                        response.body()!!.info.satname + ": elev:$satElevation , azi:$satAzimuth\n" +
                                "$satElevation1, $satAzimuth1"
                    textView.text = s
                    delay(1000)
                }

                querry = true
            } else {
                Log.d("aaa", "debug: unsuccesful " + response.code())
                Log.d("aaa", "debug: " + response.raw())
            }
        }
    }


    private fun startLocation() {
        EasyLocation(this, object : EasyLocation.EasyLocationCallBack {
            override fun getLocation(location: Location) {
                lat = location.latitude
                lon = location.longitude
                alt = location.altitude

                textView.text = "location $lat:$lon alt:$alt"
            }

            override fun locationSettingFailed() {
                Log.i("Location", "setting failed")
                Toast.makeText(this@MainActivity, "location setting failed", Toast.LENGTH_SHORT)
                    .show()

            }

            override fun permissionDenied() {
                Toast.makeText(this@MainActivity, "location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

}
