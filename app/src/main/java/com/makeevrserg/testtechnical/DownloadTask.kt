package com.makeevrserg.testtechnical

import android.os.AsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import utils.Day
import utils.Playlist
import utils.Profile
import java.net.URL
import kotlin.contracts.contract

class DownloadTask(activity: MainActivity) : AsyncTask<URL, Int, Profile>() {
    var mActivity = activity


    override fun doInBackground(vararg params: URL?): Profile? {
        val jsonStr: JSONObject

        try {
            jsonStr = JSONObject(params.get(0)!!.readText())
        } catch (e: JSONException) {
            e.printStackTrace()
            //Toast с ошибкой
            return null
        }
        val jsonProfileName = jsonStr.getString("name")
        val jsonId = jsonStr.getInt("id")

        val playlists: MutableList<Playlist> = mutableListOf()
        val jsonPlaylists: JSONArray = jsonStr.getJSONObject("schedule").getJSONArray("playlists")
        var playlistById:HashMap<Int,Playlist> = HashMap()
        for (i in 0 until jsonPlaylists.length()) {
            val playlist:Playlist = Playlist(mActivity.cacheDir.absolutePath,jsonPlaylists.getJSONObject(i))
            playlistById[playlist.id] = playlist
            playlists.add(playlist)
        }

        val profile: Profile = Profile(jsonId, jsonProfileName)
        profile.playlistById = playlistById

        val days: MutableList<Day> = mutableListOf()
        val jsonDays: JSONArray = jsonStr.getJSONObject("schedule").getJSONArray("days")
        for (i in 0 until jsonDays.length())
            days.add(Day(jsonDays.getJSONObject(i)))

        profile.days = days
        profile.playlists = playlists
        return profile
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }


    override fun onPostExecute(result: Profile) {
        super.onPostExecute(result)
        mActivity.SetProfile(result)
        mActivity.my_toolbar.title = result.name
        mActivity.progressDialog!!.dismiss()
    }
}