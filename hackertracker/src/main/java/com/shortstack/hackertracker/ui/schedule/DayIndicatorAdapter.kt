package com.shortstack.hackertracker.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.R
import com.shortstack.hackertracker.models.Day
import com.shortstack.hackertracker.models.DayIndicator
import com.shortstack.hackertracker.models.local.Event
import java.text.SimpleDateFormat
import java.util.*

class DayIndicatorAdapter : ListAdapter<DayIndicator, DayIndicatorAdapter.DayIndicatorViewHolder>(IndicatorDiff) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayIndicatorViewHolder {
        return DayIndicatorViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: DayIndicatorViewHolder, position: Int) {
        holder.render(getItem(position))
    }

    fun getRange(begin: Date, end: Date): IntRange {
        val instance = Calendar.getInstance()

        instance.time = begin
        instance.set(Calendar.HOUR_OF_DAY, 0)
        instance.set(Calendar.MINUTE, 0)
        instance.set(Calendar.SECOND, 0)
        instance.set(Calendar.MILLISECOND, 0)

        val beginDay = instance.time

        instance.time = end
        instance.set(Calendar.HOUR_OF_DAY, 0)
        instance.set(Calendar.MINUTE, 0)
        instance.set(Calendar.SECOND, 0)
        instance.set(Calendar.MILLISECOND, 0)

        val endDay = instance.time

        val collection = ArrayList<DayIndicator>()
        for(i in 0 until itemCount) {
            collection.add(getItem(i))
        }

        val dates = collection.map { Date(it.day.time) }

        val first = dates.indexOfFirst { it.time == beginDay.time }
        val last = dates.indexOfFirst { it.time == endDay.time }

        Logger.d("Setting Range: $first .. $last")

        return first..last

    }

    class DayIndicatorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        companion object {

            fun inflate(parent: ViewGroup): DayIndicatorViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.view_day_indicator, parent, false)
                return DayIndicatorViewHolder(view)
            }
        }


        fun render(day: DayIndicator) {
            val format = SimpleDateFormat("MMM d")

            val textView = view as CheckedTextView
            textView.text = format.format(day.day.time)
            textView.isChecked = day.checked
        }
    }

    object IndicatorDiff : DiffUtil.ItemCallback<DayIndicator>() {
        override fun areItemsTheSame(oldItem: DayIndicator, newItem: DayIndicator) = oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: DayIndicator, newItem: DayIndicator) = oldItem.areUiContentsTheSame(newItem)

    }


}