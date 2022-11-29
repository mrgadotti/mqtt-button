package br.com.hussan.coffeeiot.ui.coffee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import br.com.hussan.coffeeiot.R
import br.com.hussan.coffeeiot.extensions.color
import br.com.hussan.coffeeiot.extensions.hide
import br.com.hussan.coffeeiot.extensions.show
import br.com.hussan.coffeeiot.mqtt.MqttClient
//import kotlinx.android.synthetic.main.activity_coffee.btnCoffee
//import kotlinx.android.synthetic.main.activity_coffee.imgCoffee
import kotlinx.android.synthetic.main.activity_coffee.lytRoot

import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject

class CoffeeActivity : AppCompatActivity() {

    private lateinit var broker: String
    private lateinit var topic: String
    private var users = arrayOf<String?>()

    private val prefs: PrefsRespository by lazy {
        PrefsRespository(this)
    }
    private val mqttClient: MqttClient by lazy {
        MqttClient(this)
    }

    companion object {
        const val CONFIG_REQUEST = 123
    }

    private var onOff = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_coffee)

        getLocalData()

        connectAndSubscribe()
/*
        btnCoffee.setOnClickListener {

            onOff = if (!onOff) {
                mqttClient.publishMessage(topic, "on")
                true
            } else {
                mqttClient.publishMessage(topic, "off")
                false
            }
        }*/
    }

    private fun getLocalData() {
        broker = prefs.getBroker()
        topic = prefs.getTopic()
    }

    private fun connectAndSubscribe() {
        mqttClient.connect(broker)
        mqttClient.setCallBack(arrayOf(topic), ::updateButton)
    }

    fun <T> append(arr: Array<T>, element: T): Array<T?> {
        val array = arr.copyOf(arr.size + 1)
        array[arr.size] = element
        return array
    }
    private fun updateButton(topic: String, message: MqttMessage) {
        //imgCoffee.show()

        val arrayAdapter: ArrayAdapter<*>

        val mListView = findViewById<ListView>(R.id.userlist)
        //users = append(users, "Frequency: 14.074")
        Log.d("PAYLOAD", message.payload.toString())
        users = append(users, String(message.payload))
        var data = message.payload.toString()

        //Log.d("PAYLOAD2", answer.toString())

        mListView.adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, users)
        mListView.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL;
        mListView.isStackFromBottom = true;
        mListView.smoothScrollToPosition(mListView.adapter.count -1);

        /*
        onOff = if (String(message.payload) == "on") {
            btnCoffee.setColorFilter(color(R.color.colorPrimary))
            imgCoffee.setImageDrawable(ContextCompat.getDrawable(this@CoffeeActivity, R.drawable.led_on))
            lytRoot.setBackgroundColor(color(android.R.color.white))

            true
        } else {
            btnCoffee.setColorFilter(color(android.R.color.darker_gray))
            imgCoffee.setImageDrawable(ContextCompat.getDrawable(this@CoffeeActivity, R.drawable.led_off))
            lytRoot.setBackgroundColor(color(android.R.color.black))
            false
        }
*/
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttClient.close()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.config -> {
                startActivityForResult(Intent(this, ConfigActivity::class.java), CONFIG_REQUEST)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CONFIG_REQUEST && resultCode == Activity.RESULT_OK) {
            getLocalData()
            connectAndSubscribe()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_config, menu)
        return true
    }
}
