package com.example.contactlistwithheader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactlistwithheader.R
import com.example.contactlistwithheader.component.RecyclerViewFastScroller

class ContactsAdapter(contactsList: ArrayList<HashMap<String, String>>) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>(),
    RecyclerViewFastScroller.BubbleTextGetter {

    private val byInitialName = contactsList.groupBy { it["initialName"] }
    private val initialNameList = byInitialName.values.toMutableList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Update date label
        holder.groupName.text = initialNameList[position][0]["initialName"]
        // Create vertical Layout Manager
        holder.rv.layoutManager = LinearLayoutManager(
            holder.itemView.context,
            LinearLayoutManager.VERTICAL,
            false
        )


        holder.rv.addItemDecoration(
            DividerItemDecoration(
                holder.itemView.context,
                LinearLayoutManager.VERTICAL
            )
        )
        // Access RecyclerView Adapter and load the data
        val adapter =
            ContactAdapter(
                initialNameList[position] as ArrayList<HashMap<String, String>>
            )
        holder.rv.adapter = adapter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item_contacts, parent, false)
        return ViewHolder(
            v
        )
    }

    override fun getItemCount(): Int {
        return byInitialName.count()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.group_name)
        val rv: RecyclerView = itemView.findViewById(R.id.rv_contacts)
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return initialNameList[pos][0]["initialName"].toString()
    }


}
