package com.example.contactlistwithheader

import android.os.Bundle
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
        initView()
        setupIndexing()
        setupFastScroll()
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
    }

    private fun initView() {
        // Create vertical Layout Manager
        rv_contactsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ContactsAdapter(items)
        }
    }

    private fun setupIndexing() {
        val byInitialName = items.groupBy { it["initialName"] }
        val initialNameList = byInitialName.values.toTypedArray()
        val a: MutableList<String> = mutableListOf()

        initialNameList.forEachIndexed { index, list ->
            a.add(initialNameList[index][0]["initialName"].toString())
        }

        indexing.setAlphabet(a)

        indexing.onSectionIndexClickListener { i: Int, _: String ->

            (rv_contactsList!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                i,
                0
            )
        }
    }

    private fun setupFastScroll() {
        rv_contactsList.layoutManager = object :
            LinearLayoutManager(this, VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State) {
                super.onLayoutCompleted(state)
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1
                //if all items are shown, hide the fast-scroller
                fastScroll.visibility =
                    if (rv_contactsList.adapter!!.itemCount > itemsShown) View.VISIBLE else View.GONE
            }
        }
        fastScroll.setRecyclerView(indexing)
        fastScroll.setViewsToUse(
            R.layout.recycler_view_fast_scroller__fast_scroller,
            R.id.fastscroller_bubble,
            R.id.fastscroller_handle
        )
    }

}