package com.antigravity.remote.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antigravity.remote.R
import com.antigravity.remote.data.AccountManager
import com.antigravity.remote.data.SessionManager
import com.antigravity.remote.model.AntigravitySession
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object SessionSwitcherBottomSheet {

    fun showSessionSwitcher(
        context: Context,
        accountManager: AccountManager,
        sessionManager: SessionManager,
        onSessionSelected: (AntigravitySession) -> Unit
    ) {
        val activeAccount = accountManager.getActiveAccount() ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_session_switcher, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerSessions)
        val btnNewSession = view.findViewById<MaterialButton>(R.id.btnNewSession)
        val inputSearch = view.findViewById<EditText>(R.id.inputSearchSessions)

        recyclerView.layoutManager = LinearLayoutManager(context)

        var allSessions = sessionManager.getSessionsForAccount(activeAccount.id)
        val activeSession = sessionManager.getActiveSession(activeAccount.id)

        fun updateAdapter(filteredList: List<AntigravitySession>) {
            recyclerView.adapter = SessionAdapter(
                sessions = filteredList,
                activeSessionId = activeSession.id,
                onSessionClick = { selected ->
                    sessionManager.setActiveSession(selected.id)
                    onSessionSelected(selected)
                    dialog.dismiss()
                },
                onSessionDelete = { toDelete ->
                    sessionManager.removeSession(toDelete.id)
                    allSessions = sessionManager.getSessionsForAccount(activeAccount.id)
                    updateAdapter(allSessions)
                }
            )
        }

        updateAdapter(allSessions)

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase()?.trim() ?: ""
                val filtered = if (query.isEmpty()) {
                    allSessions
                } else {
                    allSessions.filter { it.title.lowercase().contains(query) || it.workspaceName.lowercase().contains(query) }
                }
                updateAdapter(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnNewSession.setOnClickListener {
            showCreateSessionDialog(context, activeAccount.id, sessionManager) { newSession ->
                onSessionSelected(newSession)
                dialog.dismiss()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showCreateSessionDialog(
        context: Context,
        accountId: String,
        sessionManager: SessionManager,
        onCreated: (AntigravitySession) -> Unit
    ) {
        val input = EditText(context).apply {
            hint = "Session / Workspace Title (e.g. Android TV Refactor)"
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("New Antigravity Session")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    val session = AntigravitySession(
                        title = title,
                        workspaceName = "Mobile Workspace",
                        accountId = accountId
                    )
                    sessionManager.saveSession(session)
                    sessionManager.setActiveSession(session.id)
                    onCreated(session)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private class SessionAdapter(
        private val sessions: List<AntigravitySession>,
        private val activeSessionId: String,
        private val onSessionClick: (AntigravitySession) -> Unit,
        private val onSessionDelete: (AntigravitySession) -> Unit
    ) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtTitle: TextView = view.findViewById(R.id.txtSessionTitle)
            val txtWorkspace: TextView = view.findViewById(R.id.txtSessionWorkspace)
            val indicatorActive: View = view.findViewById(R.id.indicatorActive)
            val btnDelete: View = view.findViewById(R.id.btnDeleteSession)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_session, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val session = sessions[position]
            holder.txtTitle.text = session.title
            holder.txtWorkspace.text = session.workspaceName

            val isActive = session.id == activeSessionId
            holder.indicatorActive.visibility = if (isActive) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                onSessionClick(session)
            }

            holder.btnDelete.setOnClickListener {
                onSessionDelete(session)
            }
        }

        override fun getItemCount(): Int = sessions.size
    }
}
