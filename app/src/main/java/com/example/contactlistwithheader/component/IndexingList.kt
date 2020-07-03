package com.example.contactlistwithheader.component

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactlistwithheader.R
import com.example.contactlistwithheader.component.RecyclerViewFastScroller.BubbleTextGetter

class IndexingList(context: Context, attrs: AttributeSet) :
    RecyclerView(context, attrs) {
    private var alphabeticList: List<String>? = null

    private var _width = 0
    private var fontSize = 0f

    private var adapter: SectionIndexAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private fun getAttributes(
        context: Context,
        attrs: AttributeSet
    ) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.IndexingList)
        _width = ta.getDimensionPixelSize(R.styleable.IndexingList_width, 15)
        val defaultSize = 14.spToPixel(context).toInt()
        val attFontSizeValue =
            ta.getDimensionPixelSize(R.styleable.IndexingList_fontSize, defaultSize)
        fontSize = pixelsToSp(context, attFontSizeValue)
        val aItemsColor = R.styleable.IndexingList_itemsColor
        if (ta.hasValue(R.styleable.IndexingList_itemsColor)) {
            itemsColor = getColor(ta.getResourceId(aItemsColor, 0))
        }
        ta.recycle()
    }

    private fun getColor(id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    private fun pixelsToSp(context: Context, px: Int): Float {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return px / scaledDensity
    }

    private fun Int.spToPixel(context: Context): Float {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return this * scaledDensity
    }

    private fun initRecyclerView() {
        adapter = SectionIndexAdapter(context)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setHasFixedSize(true)
        setAdapter(adapter)
        this.layoutManager = linearLayoutManager
    }

    //LISTENER
    fun onSectionIndexClickListener(sectionIndexClickListener: SectionIndexClickListener?) {
        adapter!!.onSectionIndexClickListener(sectionIndexClickListener)
    }

    /**
     * Set letter to bold
     *
     * @param {String} "letter"
     * @method setLetterToBold
     */
    fun setLetterToBold(letter: String) {
        var index = alphabeticList!!.indexOf(letter)
        val regex = "[0-9]+".toRegex()
        if (letter.matches(regex)) {
            index = alphabeticList!!.size - 1
        }
        adapter!!.setBoldPosition(index)
        linearLayoutManager!!.scrollToPositionWithOffset(index, 0)
        adapter!!.notifyDataSetChanged()
    }

    fun setAlphabetic(alphabeticList: List<String>) {
        this.alphabeticList = alphabeticList
    }

    interface SectionIndexClickListener {
        fun onItemClick(position: Int, character: String?)
    }

    internal inner class SectionIndexAdapter(context: Context?) :
        Adapter<SectionIndexAdapter.ViewHolder>(),
        BubbleTextGetter {
        private var boldPosition = 0
        private val mInflater: LayoutInflater = LayoutInflater.from(context)
        private var sectionIndexClickListener: SectionIndexClickListener? = null

        //LISTENER
        fun onSectionIndexClickListener(sectionIndexClickListener: SectionIndexClickListener?) {
            this.sectionIndexClickListener = sectionIndexClickListener
        }

        fun setBoldPosition(position: Int) {
            boldPosition = position
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val view = mInflater.inflate(R.layout.item_letter, parent, false)
            return ViewHolder(
                view
            )
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            val letter = alphabeticList!![position]
            holder.tvLetter.text = letter

            //Set current position to bold
            val normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
            holder.tvLetter.typeface =
                if (position == boldPosition) boldTypeface else normalTypeface

            //Custom Font size
            holder.tvLetter.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
            holder.tvLetter.textSize = if (position == boldPosition) fontSize + 10 else fontSize

            //Custom color
            if (itemsColor != 0) {
                holder.tvLetter.setTextColor(itemsColor)
            }
        }

        override fun getItemCount(): Int {
            return alphabeticList!!.size
        }

        override fun getTextToShowInBubble(pos: Int): String {
            sectionIndexClickListener!!.onItemClick(pos, alphabeticList!![pos])
            setLetterToBold(alphabeticList!![pos])
            return alphabeticList!![pos]
        }

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), OnClickListener {
            val tvLetter: TextView = itemView.findViewById(R.id.tvLetter)
            override fun onClick(view: View) {
                if (sectionIndexClickListener != null) {
                    val character = tvLetter.text.toString()
                    sectionIndexClickListener!!.onItemClick(this.adapterPosition, character)
                    setLetterToBold(character)
                }
            }

            init {
                itemView.setOnClickListener(this)
            }
        }

    }

    companion object {
        private var itemsColor = 0
    }

    init {
        this.overScrollMode = View.OVER_SCROLL_NEVER
        getAttributes(context, attrs)
        initRecyclerView()
    }
}