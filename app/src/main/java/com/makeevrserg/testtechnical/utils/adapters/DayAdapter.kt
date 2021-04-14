package com.makeevrserg.testtechnical.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.makeevrserg.testtechnical.R
import utils.Day
import utils.Profile
import utils.enumDays

class DayAdapter(private val mContext:Context, private val adapters: MutableList<PlaylistAdapter>) :
    RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val recyclerView: RecyclerView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView)
            recyclerView = view.findViewById(R.id.recyclerView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.day_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        viewHolder.textView.text = enumDays.values()[position].day


        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        viewHolder.recyclerView.layoutManager = linearLayoutManager

        viewHolder.recyclerView.adapter = adapters[position]

        //viewHolder.textViewPlaylist.text = dataSet.playlistById[day.timeZones[position].playlists[0].] Какой плейлист должен отображаться?

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = adapters.size

}
