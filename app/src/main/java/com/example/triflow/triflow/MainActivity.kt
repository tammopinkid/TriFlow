package com.example.triflow.triflow

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row_main.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.data



class MainActivity : AppCompatActivity() {
    private var data = arrayListOf<PatientLog>()
    private var tempData = arrayListOf<PatientLog>()

    private fun convertToJSON(jsonString: String) {
        val json = JSONObject(jsonString)
        val result = json.getJSONArray("data")
                .getJSONObject(0)
                .getJSONArray("values")

        for (item in 0 until result.length()){
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = result.getJSONArray(item).getString(0).toLong()
            calendar.timeZone = TimeZone.getTimeZone("GMT+7")
            val date = SimpleDateFormat("dd/MM/yyyy    HH:mm").format(calendar.time)

            data.add(PatientLog(date, result.getJSONArray(item).getString(1)))

        }
        val lastest = json.getJSONArray("lastest_data")
                .getJSONObject(0)
                .getJSONArray("values")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastest.getJSONArray(0).getString(0).toLong()
        calendar.timeZone = TimeZone.getTimeZone("GMT+7")
        val date = SimpleDateFormat("dd/MM/yyyy    HH:mm").format(calendar.time)

        data.add(PatientLog(date, lastest.getJSONArray(0).getString(1)))

        lungCapacity.text = lastest.getJSONArray(0).getString(1)

        Log.d("Size", "= " + data.size)
        var i = data.size - 1
        while (i >= 0) {
            tempData.add(data[i])

            i--
        }

        listView.adapter = ListViewAdapter(tempData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weekBtn.setOnClickListener {
            val intent = Intent(this, WeekActivity::class.java)

            startActivity(intent)
        }

        val queue = Volley.newRequestQueue(this)
        val url = "http://api.netpie.io/feed/testfeedtriflow?apikey=Xbyt3j3SfOtecny5kSOdYqAsEgPuri9z&granularity=1hour&since=30day"
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    convertToJSON(response.toString())
                },
                Response.ErrorListener { Log.d("Test", "HUa Door")  })
        queue.add(stringRequest)
    }

    private class ListViewAdapter(var objects: ArrayList<PatientLog>): BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val userLog = objects[position]

            if(convertView == null) {
                val layoutInflater = LayoutInflater.from(parent!!.context)
                view = layoutInflater.inflate(R.layout.row_main, parent, false)

                val viewHolder = ViewHolder(view.timeTextView, view.lungTextView)
                view.tag = viewHolder
            }
            else {
                view = convertView
            }
            val trueLung = String.format("%.2f", userLog.patientLung.toDouble())

            val viewHolder = view.tag as ViewHolder

            viewHolder.timeTextView.text = userLog.patientTime
            viewHolder.lungTextView.text = trueLung + " cc"

            return view
        }
        override fun getItem(position: Int): Any {
            return objects[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getCount(): Int {
            return objects.size
        }

    }

    private class ViewHolder(val timeTextView: TextView, val lungTextView: TextView)
}
