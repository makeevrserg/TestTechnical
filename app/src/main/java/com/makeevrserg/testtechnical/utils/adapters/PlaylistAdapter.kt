package com.makeevrserg.testtechnical.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.makeevrserg.testtechnical.R
import utils.Day
import utils.Profile

//Адаптер RecyclerView'а для Плейлистов в дне
class PlaylistAdapter(
    private val mContext: Context,
    private val dayVal: Int,
    private val dataSet: Profile
) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewWarning: ImageView
        val textViewTiming: TextView
        val spinnerPlaylist: Spinner
        val imageButtonSub: ImageButton
        val textViewProportion: TextView
        val imageButtonAdd: ImageButton

        init {
            // Define click listener for the ViewHolder's View.
            imageViewWarning = view.findViewById(R.id.ImageViewWarning)
            textViewTiming = view.findViewById(R.id.TextViewTiming)
            spinnerPlaylist = view.findViewById(R.id.spinnerPlaylist)
            imageButtonSub = view.findViewById(R.id.ImageButtonSub)
            textViewProportion = view.findViewById(R.id.TextViewProportion)
            imageButtonAdd = view.findViewById(R.id.ImageButtonAdd)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.playlist_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val day: Day = dataSet.days[dayVal]
        for (playlistId: Int in day.timeZones[position].playlists.keys)
            if (dataSet.playlistById[playlistId]!!.hasBroken)
                viewHolder.imageViewWarning.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_warning))



        viewHolder.textViewTiming.text =
            "${day.timeZones[position].from}-${day.timeZones[position].to}"


        val list: MutableList<String> = mutableListOf()
        for (id in day.timeZones[position].playlists.keys)
            list.add(dataSet.playlistById[id]!!.name)
        var adapter =
            ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, list)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        viewHolder.spinnerPlaylist.adapter = adapter

        viewHolder.textViewProportion.text =
            day.timeZones[position].playlists[day.timeZones[position].activeId].toString()

        //Есть ещё два способоа по приминению clickListener'а в RecyclerView, но этот самый простой
        viewHolder.spinnerPlaylist.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                day.timeZones[position].activeId =
                    day.timeZones[position].playlists.keys.elementAt(pos)
                viewHolder.textViewProportion.text =
                    day.timeZones[position].playlists[day.timeZones[position].activeId].toString()


                if (dataSet.playlistById[day.timeZones[position].activeId]!!.hasBroken)
                    viewHolder.imageViewWarning.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_warning))
                else
                    viewHolder.imageViewWarning.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_check))


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }

        }


        viewHolder.imageButtonAdd.setOnClickListener { v: View ->
            val pos: Int = position
            val id: Int = day.timeZones[pos].activeId
            day.timeZones[pos].playlists[id] = day.timeZones[pos].playlists[id]!! + 1
            viewHolder.textViewProportion.text =
                day.timeZones[pos].playlists[id].toString()

            println("Clicked ${pos} ${day.timeZones[pos].activeId}")
        }
        viewHolder.imageButtonSub.setOnClickListener { v: View ->
            val pos: Int = position
            val id: Int = day.timeZones[pos].activeId
            if (day.timeZones[pos].playlists[id]!! <= 1)
                day.timeZones[pos].playlists[id] = 1
            else
                day.timeZones[pos].playlists[id] = day.timeZones[pos].playlists[id]!! - 1

            viewHolder.textViewProportion.text =
                day.timeZones[pos].playlists[id].toString()
            println("Clicked ${pos} ${day.timeZones[pos].activeId}")
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.days[dayVal].timeZones.size

}
