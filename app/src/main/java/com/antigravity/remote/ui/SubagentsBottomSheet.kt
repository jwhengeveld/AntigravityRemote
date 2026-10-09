package com.antigravity.remote.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antigravity.remote.R
import com.google.android.material.bottomsheet.BottomSheetDialog

data class SubagentTaskItem(
    val id: String,
    val role: String,
    val typeName: String,
    val state: String,
    val description: String
)

object SubagentsBottomSheet {

    fun showSubagentsMonitor(
        context: Context,
        subagents: List<SubagentTaskItem>,
        onStopSubagent: (String) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_subagents_monitor, null)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerSubagents)
        val txtEmpty = view.findViewById<TextView>(R.id.txtEmptySubagents)

        if (subagents.isEmpty()) {
            txtEmpty.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            txtEmpty.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = SubagentAdapter(subagents, onStopSubagent)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private class SubagentAdapter(
        private val items: List<SubagentTaskItem>,
        private val onStop: (String) -> Unit
    ) : RecyclerView.Adapter<SubagentAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtRole: TextView = view.findViewById(R.id.txtSubagentRole)
            val txtState: TextView = view.findViewById(R.id.txtSubagentState)
            val txtDesc: TextView = view.findViewById(R.id.txtSubagentDesc)
            val btnStop: View = view.findViewById(R.id.btnStopSubagent)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_subagent, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.txtRole.text = item.role
            holder.txtState.text = item.state.uppercase()
            holder.txtDesc.text = item.description

            if (item.state.equals("running", ignoreCase = true)) {
                holder.txtState.setTextColor(holder.itemView.context.getColor(R.color.status_green))
            } else {
                holder.txtState.setTextColor(holder.itemView.context.getColor(R.color.accent))
            }

            holder.btnStop.setOnClickListener {
                onStop(item.id)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
