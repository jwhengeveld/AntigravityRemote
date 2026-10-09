package com.antigravity.remote.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.antigravity.remote.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

object PromptBottomSheet {

    fun showPromptHelper(
        context: Context,
        onPromptSubmitted: (String) -> Unit,
        onMediaAttachRequested: () -> Unit,
        onVoiceDictationRequested: () -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_prompt_helper, null)

        val inputPrompt = view.findViewById<EditText>(R.id.inputPromptText)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSendPrompt)
        val btnAttach = view.findViewById<ImageButton>(R.id.btnAttachMedia)
        val btnVoice = view.findViewById<ImageButton>(R.id.btnVoiceInput)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupSlashCommands)

        val slashCommands = listOf("/goal", "/plan", "/schedule", "/browser", "/boost", "/grill-me")

        chipGroup.removeAllViews()
        for (cmd in slashCommands) {
            val chip = Chip(context).apply {
                text = cmd
                isClickable = true
                setOnClickListener {
                    val currentText = inputPrompt.text.toString()
                    if (currentText.isBlank()) {
                        inputPrompt.setText("$cmd ")
                    } else {
                        inputPrompt.setText("$currentText $cmd ")
                    }
                    inputPrompt.setSelection(inputPrompt.text.length)
                }
            }
            chipGroup.addView(chip)
        }

        btnSend.setOnClickListener {
            val text = inputPrompt.text.toString().trim()
            if (text.isNotEmpty()) {
                onPromptSubmitted(text)
                dialog.dismiss()
            }
        }

        btnAttach.setOnClickListener {
            dialog.dismiss()
            onMediaAttachRequested()
        }

        btnVoice.setOnClickListener {
            dialog.dismiss()
            onVoiceDictationRequested()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
