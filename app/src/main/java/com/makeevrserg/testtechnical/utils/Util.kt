package utils

import android.media.audiofx.EnvironmentalReverb
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.sql.Time

enum class enumDays(val day: String) {
    MONDAY("Понедельник"),
    TUESDAY("Вторник"),
    WEDNESDAY("Среда"),
    THURSDAY("Четверг"),
    FRIDAY("Пятница"),
    SATURDAY("Суббота"),
    SUNDAY("Воскресенье")
}

class Profile(mId: Int, mName: String) : Serializable {

    var playlists: MutableList<Playlist> = mutableListOf()
    var playlistById: HashMap<Int, Playlist> = HashMap()
    val id: Int = mId
    val name: String = mName
    var days: MutableList<Day> = mutableListOf()
    fun GetDays(): MutableList<Day> {
        return days
    }
}


//schedule->playlist_singular
class Playlist(cacheDir: String, json: JSONObject) : Serializable {
    val id: Int = json.getInt("id")
    val name: String = json.getString("name")
    val random: Boolean = json.getBoolean("random")
    val files: MutableList<jFile> = mutableListOf()
    var hasBroken: Boolean = false // Результат проверки на md5
    var fileById: HashMap<Int, jFile> = HashMap()

    init {
        println("Playlist ${name} id ${id} random ${random}")
        val jArr = json.getJSONArray("files")

        for (i in 0 until jArr.length()) {
            val file = jArr.get(i)
            val jF: jFile = jFile(file as JSONObject)
            try {
                jF.broken = !download(cacheDir, jF)
                if (!hasBroken)
                    hasBroken = jF.broken
                println("File ${jF.name} broken=${hasBroken}")

            } catch (e: FileNotFoundException) {//В некоторых файлах из json-файлы не было файлов
                e.printStackTrace()
                //e.printStackTrace()
            }
            if (!jF.broken) {
                fileById[jF.id] = jF
                files.add(jF)
            }

        }
    }

}

//schedule->playlists->file_singular
class jFile(json: JSONObject) : Serializable {
    val id: Int = json.getInt("id")
    val file_name: String = json.getString("file_name")
    val name: String = json.getString("name")
    var md5_file: String = json.getString("md5_file")
    var order: Int = json.getInt("order")
    var size: Int = json.getInt("size")
    var broken: Boolean = true

    init {
        println("File ${file_name}; Name=${name}; id=${id}")
    }
}

//schedule->days
class Day(json: JSONObject) : Serializable {
    var day: String = json.getString("day")
    val timeZones: MutableList<TimeZone> = mutableListOf()

    init {
        val jArr = json.getJSONArray("timeZones")
        for (i in 0 until jArr.length()) {
            val jObj: JSONObject = jArr.getJSONObject(i)
            val timeZone: TimeZone = TimeZone(jObj)
            timeZones.add(timeZone)
        }
    }

}

class TimeZone(json: JSONObject) : Serializable {
    var from: String = json.getString("from")
    var to: String = json.getString("to")
    var activeId: Int
    val playlists: HashMap<Int, Int> = HashMap<Int, Int>()

    init {
        val jArr = json.getJSONArray("playlists")
        activeId = jArr.getJSONObject(0).getInt("playlist_id")
        for (i in 0 until jArr.length()) {
            val jObj: JSONObject = jArr.getJSONObject(i)
            playlists[jObj.getInt("playlist_id")] = jObj.getInt("proportion")
        }
    }
}

fun getFileChecksum(fileName: String): String {

    val digest = MessageDigest.getInstance("MD5")
    val file: File = File(fileName)
    val size: Int = file.length().toInt()
    var bytes: ByteArray = ByteArray(size)
    val biStream: BufferedInputStream = BufferedInputStream(FileInputStream(file))
    biStream.read(bytes, 0, bytes.size)
    biStream.close()

    //digest.update(Files.readAllBytes(Paths.get(fileName)))
    digest.update(bytes)


    bytes = digest.digest()
    val sb = StringBuilder()
    for (b in bytes)
        sb.append(String.format("%02x", b))

    return sb.toString()
}


fun download(cacheDir: String, file: jFile): Boolean {
    //Если файл существует и он верный - возвращаем
    val checkFile: File = File(file.name)
//    if (checkFile.exists() && getFileChecksum(file.name) == file.md5_file) {
//        println("CheckSum =${getFileChecksum(file.name) == file.md5_file}")
//        return true
//    }


    //Если файла нет - скачиваем
    val url: URL = URL(file.file_name)
    val connection: URLConnection = url.openConnection()
    connection.connect()
    var fileLength = connection.contentLength
    val iStream: InputStream = BufferedInputStream(url.openStream(), 1024)
    val oStream: OutputStream =
        FileOutputStream(cacheDir + "/" + file.name)
    val data: ByteArray = ByteArray(1024)
    var downloadProgress: Long = 0
    var count = iStream.read(data);
    while (count != -1) {
        downloadProgress += count
        oStream.write(data, 0, count)
        //println("Downloading ${file.name} ${total}/${fileLength}")
        count = iStream.read(data)
    }
    //println(checksum)
    //println(file.md5_file)
    oStream.flush()
    oStream.close()
    iStream.close()
    println(
        "CheckSum for ${cacheDir + "/" + file.name} ${getFileChecksum(cacheDir + "/" + file.name)} = ${file.md5_file} ; ${
            getFileChecksum(
                cacheDir + "/" + file.name
            ) == file.md5_file
        }"
    )
    return (getFileChecksum(cacheDir + "/" + file.name) == file.md5_file)
}