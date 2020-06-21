package com.example.contactlistwithheader

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactlistwithheader.adapter.ContactsAdapter
import com.example.contactlistwithheader.model.Contact
import com.example.contactlistwithheader.utils.JsonDataFromAsset.Companion.getJsonDataFromAsset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val items: ArrayList<HashMap<String, String>> = ArrayList()
    private var contactList: MutableList<Contact> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        retrieveData()
    }

    private fun retrieveData() {
        val jsonFileString = getJsonDataFromAsset(applicationContext, "data.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<Contact>>() {}.type

        val persons: List<Contact> = gson.fromJson(jsonFileString, listPersonType)
        persons.forEachIndexed { _, person ->
            contactList.add(person)
            items.add(
                hashMapOf(
                    "firstName" to person.firstName,
                    "lastName" to person.lastName,
                    "email" to person.email,
                    "address" to person.address,
                    "organization" to person.organization,
                    "phone" to person.phone,
                    "initialName" to person.firstName.substring(0, 1)
                )
            )
        }

        items.sortBy { it["firstName"] }
        initList(items)
    }

    private fun initList(itemData: ArrayList<HashMap<String, String>>) {
        // Create vertical Layout Manager
        val rv = findViewById<RecyclerView>(R.id.contactsList)
        rv.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        // Access RecyclerView Adapter and load the data
        val adapter =
            ContactsAdapter(itemData)
        rv.adapter = adapter

        rv.layoutManager = object :
            LinearLayoutManager(this, VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State) {
                super.onLayoutCompleted(state)
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                //if all items are shown, hide the fast-scroller
                fastScroll.visibility =
                    if (adapter.itemCount > itemsShown) View.VISIBLE else View.GONE
            }
        }

        indexing.onSectionIndexClickListener { i: Int, s: String ->

            (rv!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                i,
                0
            )
        }

        //String[] alphabet = {"A", "Y", "Z", "#"};
        //alphabetik.setAlphabet(alphabet);
        //alphabetik.setAlphabet(String customAlphabet[]);
        indexing.onSectionIndexClickListener { i: Int, s: String ->
            (rv!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                i,
                0
            )
        }

        fastScroll.setRecyclerView(indexing)
        fastScroll.setViewsToUse(
            R.layout.recycler_view_fast_scroller__fast_scroller,
            R.id.fastscroller_bubble,
            R.id.fastscroller_handle
        )

        Log.d("qsdqsd", "qsdqsd" + fastScroll.getTargetPostion())
    }

}