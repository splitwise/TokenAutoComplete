package com.tokenautocomplete

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.InputContentInfo

class LoggedInputConnectionWrapper(target: InputConnection?,
                                   mutable: Boolean
) : InputConnectionWrapper(target, mutable) {
    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence? {
        Log.d("TOKEN_INPUT", "getTextBeforeCursor($n, $flags))")
        return super.getTextBeforeCursor(n, flags)
    }

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? {
        Log.d("TOKEN_INPUT", "getTextAfterCursor($n, $flags)")
        return super.getTextAfterCursor(n, flags)
    }

    override fun getSelectedText(flags: Int): CharSequence? {
        Log.d("TOKEN_INPUT", "getSelectedText($flags)")
        return super.getSelectedText(flags)
    }

    override fun getCursorCapsMode(reqModes: Int): Int {
        Log.d("TOKEN_INPUT", "getCursorCapsMode($reqModes)")
        return super.getCursorCapsMode(reqModes)
    }

    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? {
        Log.d("TOKEN_INPUT", "getExtractedText($request, $flags)")
        return super.getExtractedText(request, flags)
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        Log.d("TOKEN_INPUT", "deleteSurroundingText($beforeLength, $afterLength)")
        return super.deleteSurroundingText(beforeLength, afterLength)
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        Log.d("TOKEN_INPUT", "deleteSurroundingTextInCodePoints($beforeLength, $afterLength)")
        return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        Log.d("TOKEN_INPUT", "setComposingText($text, $newCursorPosition)")
        return super.setComposingText(text, newCursorPosition)
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        Log.d("TOKEN_INPUT", "setComposingRegion($start, $end)")
        return super.setComposingRegion(start, end)
    }

    override fun finishComposingText(): Boolean {
        Log.d("TOKEN_INPUT", "finishComposingText()")
        return super.finishComposingText()
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        Log.d("TOKEN_INPUT", "commitText($text, $newCursorPosition)")
        return super.commitText(text, newCursorPosition)
    }

    override fun commitCompletion(text: CompletionInfo?): Boolean {
        Log.d("TOKEN_INPUT", "commitCompletion($text)")
        return super.commitCompletion(text)
    }

    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
        Log.d("TOKEN_INPUT", "commitCorrection($correctionInfo)")
        return super.commitCorrection(correctionInfo)
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        Log.d("TOKEN_INPUT", "setSelection($start, $end)")
        return super.setSelection(start, end)
    }

    override fun performEditorAction(editorAction: Int): Boolean {
        Log.d("TOKEN_INPUT", "performEditorAction($editorAction)")
        return super.performEditorAction(editorAction)
    }

    override fun performContextMenuAction(id: Int): Boolean {
        Log.d("TOKEN_INPUT", "performContextMenuAction($id)")
        return super.performContextMenuAction(id)
    }

    override fun beginBatchEdit(): Boolean {
        Log.d("TOKEN_INPUT", "beginBatchEdit()")
        return super.beginBatchEdit()
    }

    override fun endBatchEdit(): Boolean {
        Log.d("TOKEN_INPUT", "endBatchEdit()")
        return super.endBatchEdit()
    }

    override fun sendKeyEvent(event: KeyEvent?): Boolean {
        Log.d("TOKEN_INPUT", "sendKeyEvent($event)")
        return super.sendKeyEvent(event)
    }

    override fun clearMetaKeyStates(states: Int): Boolean {
        Log.d("TOKEN_INPUT", "clearMetaKeyStates($states)")
        return super.clearMetaKeyStates(states)
    }

    override fun reportFullscreenMode(enabled: Boolean): Boolean {
        Log.d("TOKEN_INPUT", "reportFullscreenMode($enabled)")
        return super.reportFullscreenMode(enabled)
    }

    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
        Log.d("TOKEN_INPUT", "performPrivateCommand($action, $data)")
        return super.performPrivateCommand(action, data)
    }

    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
        Log.d("TOKEN_INPUT", "requestCursorUpdates($cursorUpdateMode)")
        return super.requestCursorUpdates(cursorUpdateMode)
    }

    override fun getHandler(): Handler? {
        Log.d("TOKEN_INPUT", "getHandler()")
        return super.getHandler()
    }

    override fun closeConnection() {
        Log.d("TOKEN_INPUT", "closeConnection()")
        super.closeConnection()
    }

    override fun commitContent(
        inputContentInfo: InputContentInfo,
        flags: Int,
        opts: Bundle?
    ): Boolean {
        Log.d("TOKEN_INPUT", "commitContent($inputContentInfo, $flags, $opts)")
        return super.commitContent(inputContentInfo, flags, opts)
    }
}