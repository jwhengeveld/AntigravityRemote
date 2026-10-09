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

data class CommandItem(
    val trigger: String,
    val description: String,
    val isSlash: Boolean = true
)

object CommandPaletteDialog {

    private val availableCommands = listOf(
        CommandItem("/goal", "Run long-running autonomous task without stopping"),
        CommandItem("/plan", "Create step-by-step architectural plan before execution"),
        CommandItem("/schedule", "Set recurring automation or one-time delayed timer"),
        CommandItem("/browser", "Automate web browsing, search, and web app testing"),
        CommandItem("/boost", "Deep reasoning, strategic planning, and verification"),
        CommandItem("/grill-me", "Interactive interview to align on design decisions"),
        CommandItem("/learn", "Persist corrected agent behaviors for future tasks"),
        CommandItem("@file", "Attach specific codebase file as context", false),
        CommandItem("@folder", "Attach folder workspace directory as context", false),
        CommandItem("@terminal", "Attach terminal session or log context", false),
        CommandItem("@mcp", "Attach Model Context Protocol tool or server", false)
    )

    fun showCommandPalette(
        context: Context,
        onCommandSelected: (String) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_command_palette, null)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerCommands)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = CommandAdapter(availableCommands) { selected ->
            onCommandSelected(selected.trigger)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private class CommandAdapter(
        private val items: List<CommandItem>,
        private val onClick: (CommandItem) -> Unit
    ) : RecyclerView.Adapter<CommandAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtTrigger: TextView = view.findViewById(R.id.txtCommandTrigger)
            val txtDesc: TextView = view.findViewById(R.id.txtCommandDesc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_command, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.txtTrigger.text = item.trigger
            holder.txtDesc.text = item.description

            if (item.isSlash) {
                holder.txtTrigger.setTextColor(holder.itemView.context.getColor(R.color.accent))
            } else {
                holder.txtTrigger.setTextColor(holder.itemView.context.getColor(R.color.status_orange))
            }

            holder.itemView.setOnClickListener {
                onClick(item)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
