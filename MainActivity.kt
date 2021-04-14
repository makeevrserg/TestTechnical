package com.makeevrserg.testtechnical

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.makeevrserg.testtechnical.utils.adapters.DayAdapter
import com.makeevrserg.testtechnical.utils.adapters.PlaylistAdapter
import kotlinx.android.synthetic.main.activity_main.*
import utils.Profile
import utils.enumDays
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    var profile: Profile? = null
    var TAG = "MainActivity"
    var progressDialog: ProgressDialog? = null
    val textViewsDays: MutableList<TextView> = mutableListOf()
    val recyclerViewsDays: MutableList<RecyclerView> = mutableListOf()
    val recyclerViewsAdapters: MutableList<PlaylistAdapter> = mutableListOf()



    fun SetProfile(pl: Profile) {
        this.profile = pl
        for (i in enumDays.values())
            recyclerViewsAdapters.add(PlaylistAdapter(applicationContext, i.ordinal, profile!!))

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewDays)

        println(recyclerViewsAdapters.size)
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        val adapter: DayAdapter = DayAdapter(applicationContext, recyclerViewsAdapters)
        recyclerView.adapter = adapter

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_current_player -> {
            Log.d(TAG, "onOptionsItemSelected: ")
            // User chose the "Settings" item, show the app settings UI...
            var intent: Intent = Intent(this, ActivityPlay::class.java).apply {
                putExtra("PROFILE", profile)
            }
            startActivity(intent)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.current_player, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(my_toolbar)
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (!(cm.activeNetworkInfo!=null && cm.activeNetworkInfo.isConnected)){
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressDialog =
            ProgressDialog.show(this, "Пожалуйста, подождите", "Идёт загрузка музыкального профиля")
        DownloadTask(this).execute(URL("https://raw.githubusercontent.com/merrytheberry/TestTechnical/main/test.json"))
    }


}