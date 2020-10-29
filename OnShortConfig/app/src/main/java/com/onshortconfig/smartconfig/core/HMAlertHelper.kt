package com.onshortconfig.smartconfig.core

import android.content.Context
import android.content.DialogInterface
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import cn.pedant.SweetAlert.SweetAlertDialog

class HMAlertHelper(var context: Context) : HMLifeCycleBehaviour {

    override var lifecycle: Lifecycle? = null
    private var alertDialog: SweetAlertDialog? = null
    private var toast: Toast? = null


    // ------------  Alert Dialog --------------
    fun alert(
        titleText: String? = null,
        contentText: String? = null,
        confirmText: String? = null,
        cancelText: String? = null,
        alertType: Int = SweetAlertDialog.NORMAL_TYPE,
        canceledOnTouchOutside: Boolean = false,
        onClose: ((dialog: DialogInterface?) -> Unit)? = null,
        onCancel: ((dialog: DialogInterface?) -> Unit)? = null,
        onConfirm: ((dialog: DialogInterface?) -> Unit)? = null
    ) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        this@HMAlertHelper.releaseAlertDialogIfNeeded()

        alertDialog = SweetAlertDialog(context, alertType)
        alertDialog?.setCanceledOnTouchOutside(canceledOnTouchOutside)

        updateData(
            titleText,
            contentText,
            confirmText,
            cancelText,
            alertType,
            onClose,
            onCancel,
            onConfirm
        )
        this@HMAlertHelper.alertDialog?.show()
    }

    fun alertProgress(contentText: String? = null, canceledOnTouchOutside: Boolean = false) {
        alertProgress(
            titleText = null,
            confirmText = null,
            contentText = contentText,
            cancelText = null,
            alertType = SweetAlertDialog.PROGRESS_TYPE,
            canceledOnTouchOutside = canceledOnTouchOutside
        )
    }

    fun alertProgress(
        contentText: String? = null,
        canceledOnTouchOutside: Boolean = false,
        onClose: ((dialog: DialogInterface?) -> Unit)? = null
    ) {
        alertProgress(
            titleText = null,
            confirmText = null,
            contentText = contentText,
            cancelText = null,
            alertType = SweetAlertDialog.PROGRESS_TYPE,
            canceledOnTouchOutside = canceledOnTouchOutside,
            onClose = onClose
        )
    }

    fun alertProgress(
        titleText: String? = null,
        contentText: String? = null,
        confirmText: String? = null,
        cancelText: String? = null,
        alertType: Int = SweetAlertDialog.PROGRESS_TYPE,
        canceledOnTouchOutside: Boolean = false,
        onClose: ((dialog: DialogInterface?) -> Unit)? = null
    ) {

        alert(
            titleText,
            contentText,
            confirmText,
            cancelText,
            alertType,
            canceledOnTouchOutside,
            onClose
        )
    }

    fun updateError(
        titleText: String? = null,
        contentText: String? = null, onClose: ((dialog: DialogInterface?) -> Unit)? = null
    ) {
        updateError(
            titleText = titleText, contentText = contentText, onClose = onClose, confirmText = "Ok",
            cancelText = null, alertType = SweetAlertDialog.ERROR_TYPE
        )
    }

    fun updateError(
        titleText: String? = null,
        contentText: String? = null,
        confirmText: String? = null,
        cancelText: String? = null,
        alertType: Int = SweetAlertDialog.ERROR_TYPE,
        onClose: ((dialog: DialogInterface?) -> Unit)? = null
    ): SweetAlertDialog? {
        return updateData(titleText, contentText, confirmText, cancelText, alertType, onClose)
    }

    fun updateSuccess(
        titleText: String? = null,
        contentText: String? = null,
        confirmText: String? = null,
        cancelText: String? = null,
        alertType: Int = SweetAlertDialog.SUCCESS_TYPE,
        onClose: ((dialog: DialogInterface?) -> Unit)? = null
    ) {
        updateData(titleText, contentText, confirmText, cancelText, alertType, onClose)
    }

    fun releaseAlertDialogIfNeeded() {
        if (Looper.getMainLooper() != Looper.myLooper()) return

        this@HMAlertHelper.alertDialog?.let { unwrapped ->
            if (unwrapped.isShowing) {
                unwrapped.dismiss()
            }
        }
        this@HMAlertHelper.alertDialog = null
    }

    fun dismisProgress() = releaseAlertDialogIfNeeded()

    private fun updateData(
        titleText: String? = null,
        contentText: String? = null,
        confirmText: String?,
        cancelText: String?,
        alertType: Int = SweetAlertDialog.NORMAL_TYPE,
        onClose: ((dialog: DialogInterface?) -> Unit)? = null,
        onCancel: ((dialog: DialogInterface?) -> Unit)? = null,
        onConfirm: ((dialog: DialogInterface?) -> Unit)? = null
    ): SweetAlertDialog? {
        if (alertDialog == null) {
            alertDialog = SweetAlertDialog(context)
        }
        updateDataToView(titleText, AlertContentType.TITLE)
        updateDataToView(contentText, AlertContentType.CONTENT)
        updateDataToView(confirmText, AlertContentType.CONFIRM)
        updateDataToView(cancelText, AlertContentType.CANCEL)
        alertDialog?.changeAlertType(alertType)
        alertDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }

        alertDialog?.setOnDismissListener {
            releaseAlertDialogIfNeeded()
            onClose?.invoke(it)
        }

        alertDialog?.setConfirmClickListener {
            onConfirm?.invoke(it)
            releaseAlertDialogIfNeeded()
        }

        alertDialog?.setCancelClickListener {
            onCancel?.invoke(it)
        }

        return alertDialog
    }

    private fun updateDataToView(content: String? = null, type: AlertContentType) {
        if (content.isNullOrEmpty()) {
            return
        }

        when (type) {
            AlertContentType.TITLE -> this@HMAlertHelper.alertDialog?.titleText = content
            AlertContentType.CONTENT -> this@HMAlertHelper.alertDialog?.contentText = content
            AlertContentType.CONFIRM -> this@HMAlertHelper.alertDialog?.confirmText = content
            AlertContentType.CANCEL -> this@HMAlertHelper.alertDialog?.cancelText = content
//            AlertContentType.NEUTRAL -> this@HMAlertHelper.alertDialog?.contentText = content//setNeutralText(content)
            else -> {

            }
        }
    }

    enum class AlertContentType {
        TITLE,
        CONTENT,
        CANCEL,
        CONFIRM,
        NEUTRAL
    }

    // ------------  Toast --------------
    fun toast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) = toast(context.getString(message), duration)

    fun toast(
        content: String? = null,
        duration: Int = Toast.LENGTH_LONG,
        isMobile: Boolean = false
    ) {
        val biggerText = SpannableStringBuilder(content)
        biggerText.setSpan(RelativeSizeSpan(1.5f), 0, content?.length ?: 0, 0)
        toast?.cancel()
        toast = Toast.makeText(context, if (isMobile) content else biggerText, duration)
        toast?.show()
    }
}