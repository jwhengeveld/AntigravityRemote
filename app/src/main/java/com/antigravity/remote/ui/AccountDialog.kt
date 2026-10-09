package com.antigravity.remote.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.antigravity.remote.data.AccountManager
import com.antigravity.remote.model.AntigravityAccount
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object AccountDialog {

    fun showAccountSwitcher(
        context: Context,
        accountManager: AccountManager,
        onAccountSelected: (AntigravityAccount) -> Unit
    ) {
        val accounts = accountManager.getAccounts()
        val accountNames = accounts.map { "${it.name} (${it.email})" }.toTypedArray()

        if (accounts.isEmpty()) {
            showAddAccountDialog(context, accountManager, onAccountSelected)
            return
        }

        val activeAccount = accountManager.getActiveAccount()
        val checkedIndex = accounts.indexOfFirst { it.id == activeAccount?.id }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(context)
            .setTitle("Switch Antigravity Account")
            .setSingleChoiceItems(accountNames, checkedIndex) { dialog, which ->
                val selected = accounts[which]
                accountManager.saveCurrentCookiesForActiveAccount()
                accountManager.setActiveAccount(selected.id)
                accountManager.applyCookiesForAccount(selected)
                onAccountSelected(selected)
                dialog.dismiss()
            }
            .setPositiveButton("Add Account") { dialog, _ ->
                dialog.dismiss()
                showAddAccountDialog(context, accountManager, onAccountSelected)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showAddAccountDialog(
        context: Context,
        accountManager: AccountManager,
        onAccountCreated: (AntigravityAccount) -> Unit
    ) {
        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val inputName = EditText(context).apply {
            hint = "Account Name (e.g. Work, Personal)"
        }
        val inputEmail = EditText(context).apply {
            hint = "Email address"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        container.addView(inputName)
        container.addView(inputEmail)

        MaterialAlertDialogBuilder(context)
            .setTitle("Add Antigravity Account")
            .setView(container)
            .setPositiveButton("Save & Login") { dialog, _ ->
                val name = inputName.text.toString().trim()
                val email = inputEmail.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty()) {
                    val newAccount = AntigravityAccount(name = name, email = email)
                    accountManager.saveAccount(newAccount)
                    accountManager.setActiveAccount(newAccount.id)
                    accountManager.applyCookiesForAccount(newAccount)
                    onAccountCreated(newAccount)
                } else {
                    Toast.makeText(context, "Please enter name and email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
