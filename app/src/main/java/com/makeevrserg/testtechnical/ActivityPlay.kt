package com.makeevrserg.testtechnical

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_play.*
import org.json.JSONObject
import utils.*
import utils.TimeZone
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random


class ActivityPlay : AppCompatActivity() {
    val TAG = "ActivityPlay"

    var player: SimpleExoPlayer? = null
    var crossfadePlayer: SimpleExoPlayer? = null
    var handler: Handler? = null
    val mediaPlayerItems: MutableList<MediaItem> = mutableListOf()
    var mediaCrossfadeItems: MutableList<MediaItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val profile: Profile = (intent.getSerializableExtra("PROFILE") as? Profile)!!
        //В Calendar неделя начинается с воскресенья, а не понедельника
        var dayInt = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
        //Это значит, что сегодня воскресенье. У нас это последний, а не первый день
        if (dayInt == -1)
            dayInt = 6
        val currentTime = SimpleDateFormat("HH:mm", Locale.CANADA).format(Date())
        //<playlsitId,proportion>
        var playlistMap: HashMap<Int, Int> = hashMapOf<Int, Int>()
        //Берем сегодняшний день
        for (timeZone: TimeZone in profile.days[dayInt].timeZones) {
            val timeStart = SimpleDateFormat("HH:mm", Locale.CANADA).parse(timeZone.from)
            val timeEnd = SimpleDateFormat("HH:mm", Locale.CANADA).parse(timeZone.to)
            val time = SimpleDateFormat("HH:mm", Locale.CANADA).parse(currentTime)
            //Проверяем на правильное время
            if (time.after(timeStart) && time.before(timeEnd))
                for (id in timeZone.playlists.keys)
                    if (playlistMap[id] == null)
                        playlistMap[id] = timeZone.playlists[id]!!
                    else
                        playlistMap[id] = playlistMap[id]!! + timeZone.playlists[id]!!
        }

        Log.d(TAG, "mapSize=${playlistMap.size} ")
        if (playlistMap.size <= 0) {
            Toast.makeText(this, "В данный момент нет активных плейлистов", Toast.LENGTH_SHORT)
                .show()
            this.finish()
            return
        }
        if (playlistMap.size > 1)
            playlistMap = playlistMap.toList().sortedBy { (_, value) -> value }.reversed()
                .toMap() as HashMap<Int, Int>
        println(playlistMap)
        setSupportActionBar(toolbarPlay)
        toolbarPlay.title = profile.playlistById[playlistMap.keys.elementAt(0)]!!.name
        val toPlay: MutableList<PlaylistToPlay> = mutableListOf()
        for (id in playlistMap.keys) {
            val plst: PlaylistToPlay = PlaylistToPlay(
                applicationContext,
                id,
                playlistMap[id]!!,
                profile.playlistById[id]!!.files
            )
            toPlay.add(plst)
        }

        player = SimpleExoPlayer.Builder(applicationContext).build()
        crossfadePlayer = SimpleExoPlayer.Builder(applicationContext).build()


        //var fileById: HashMap<Int, String> = HashMap()
        for (plst: PlaylistToPlay in toPlay) {
            Log.d(
                TAG,
                "Playlist ${profile.playlistById[plst.id]!!.name} maxIter = ${plst.mMaxIter}"
            )
            for (i in 1..plst.mMaxIter) {
                val path = plst.GetRandomPath() ?: continue
                Log.d(
                    TAG,
                    "${i}/${plst.maxIter} Playlist ${profile.playlistById[plst.id]!!.name} Add path = ${path[0]}"
                )

                val jsonTag = JSONObject()
                val mediaItem: MediaItem =
                    MediaItem.Builder().setUri(path[0])
                        .setMediaId(profile.playlistById[plst.id]!!.name)
                        .setMediaMetadata(MediaMetadata.Builder().setTitle(path[1]).build())
                        .build()
                mediaPlayerItems.add(mediaItem)
                player!!.addMediaItem(
                    mediaItem
                )
            }
        }
        if (mediaPlayerItems.size <= 0) {
            Toast.makeText(this, "Нет верных треков", Toast.LENGTH_SHORT).show()
            finish()
        }
        mediaCrossfadeItems = mediaPlayerItems.toMutableList()
        Collections.rotate(mediaPlayerItems, -1)
        for (mediaItem in mediaPlayerItems)
            crossfadePlayer!!.addMediaItem(mediaItem)

        crossfadePlayer!!.repeatMode = Player.REPEAT_MODE_ALL
        player!!.repeatMode = Player.REPEAT_MODE_ALL


        ImageButtonPlayStop.setOnClickListener {
            if (player!!.isPlaying) {
                StopPlayer()
            } else {
                player!!.prepare()
                player!!.play()
                InitCrossfade()
                ImageButtonPlayStop.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_pause
                    )
                )
                ChangeText()
            }
        }
    }

    fun ChangeText() {
        textViewCurrentSong.text = player!!.currentMediaItem!!.mediaMetadata.title
        toolbarPlay.title = player!!.currentMediaItem!!.mediaId
    }

    fun StopPlayer() {

        if (handler != null)
            handler!!.removeCallbacksAndMessages(null)
        player!!.pause()
        player!!.next()
        player!!.volume = 1.0f
        crossfadePlayer!!.volume = 1.0f
        crossfadePlayer!!.pause()
        crossfadePlayer!!.next()
        ImageButtonPlayStop.setImageDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.ic_play
            )
        )
    }

    fun InitCrossfade() {

        if (handler != null)
            handler!!.removeCallbacksAndMessages(null)



        handler = Handler()
        handler!!.postDelayed(object : Runnable {
            override fun run() {
                if (player == null) {
                    println("Playes is null")
                    return
                }
                val length = player!!.contentDuration
                val toEnd = length - player!!.contentPosition
                if (toEnd < 0 || !player!!.isPlaying || player!!.contentPosition > length) {
                    handler!!.postDelayed(this, 500)
                    return
                }


                if (toEnd < 5000 && player!!.isPlaying) {
                    if (!crossfadePlayer!!.isPlaying) {
                        crossfadePlayer!!.prepare()
                        crossfadePlayer!!.play()
                    }
                    val mSound: Float = (1.0f - (toEnd.toFloat() / (5000)))
                    crossfadePlayer!!.volume = mSound
                    player!!.volume = 1.0f - mSound
                }
                Log.d(TAG, "run: toEnd=${toEnd}")
                if (toEnd < 500) {
                    Log.d(TAG, "run: ToEnd<500")
                    val oldCrossfade = crossfadePlayer
                    crossfadePlayer = player
                    player = oldCrossfade
                    crossfadePlayer!!.next()
                    crossfadePlayer!!.next()
                    crossfadePlayer!!.pause()
                    ChangeText()
                    handler!!.postDelayed(this, 500)

                } else
                    handler!!.postDelayed(this, 500)

            }

        }, 0)
    }


    override fun onPause() {
        StopPlayer()

        super.onPause()
    }

    class PlaylistToPlay(
        var context: Context,
        var mId: Int,
        var mMaxIter: Int,
        var mFiles: MutableList<jFile>
    ) {
        val id: Int
        val maxIter: Int
        val files: MutableList<jFile>
        var currIter = 0
        val mContext: Context

        init {
            id = mId
            maxIter = mMaxIter
            files = mFiles
            currIter = maxIter
            mContext = context
        }

        fun GetRandomPath(): Array<String>? {
            var size = files.size
            if (files.size == 0)
                return null
            var rand = Random.nextInt(0, size)

            return arrayOf(
                mContext.cacheDir.absolutePath + "/" + files[rand].name,
                files[rand].name
            )
        }

    }
}