package com.simplemobiletools.gallery

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v7.app.ActionBar
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.webkit.MimeTypeMap
import com.simplemobiletools.filepicker.extensions.*
import com.simplemobiletools.gallery.models.Medium
import java.io.File

class Utils {
    companion object {
        fun getFilename(path: String): String {
            return path.substring(path.lastIndexOf("/") + 1)
        }

        fun showToast(context: Context, resId: Int) {
            context.toast(resId)
        }

        fun getActionBarHeight(context: Context, res: Resources): Int {
            val tv = TypedValue()
            var height = 0
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, res.displayMetrics)
            }
            return height
        }

        fun getStatusBarHeight(res: Resources): Int {
            val id = res.getIdentifier("status_bar_height", "dimen", "android")
            return if (id > 0) {
                res.getDimensionPixelSize(id)
            } else
                0
        }

        fun getNavBarHeight(res: Resources): Int {
            val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (id > 0) {
                res.getDimensionPixelSize(id)
            } else
                0
        }

        fun hasNavBar(act: Activity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val display = act.windowManager.defaultDisplay

                val realDisplayMetrics = DisplayMetrics()
                display.getRealMetrics(realDisplayMetrics)

                val realHeight = realDisplayMetrics.heightPixels
                val realWidth = realDisplayMetrics.widthPixels

                val displayMetrics = DisplayMetrics()
                display.getMetrics(displayMetrics)

                val displayHeight = displayMetrics.heightPixels
                val displayWidth = displayMetrics.widthPixels

                realWidth - displayWidth > 0 || realHeight - displayHeight > 0
            } else {
                val hasMenuKey = ViewConfiguration.get(act).hasPermanentMenuKey()
                val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
                !hasMenuKey && !hasBackKey
            }
        }

        fun hasStoragePermission(context: Context) = context.hasStoragePermission()

        fun getMimeType(url: String): String {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return if (extension != null) {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            } else
                ""
        }

        fun shareMedium(medium: Medium, activity: Activity) {
            val shareTitle = activity.resources.getString(R.string.share_via)
            val intent = Intent()
            val file = File(medium.path)
            val uri = Uri.fromFile(file)
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = getMimeType(medium)
            activity.startActivity(Intent.createChooser(intent, shareTitle))
        }

        fun getMimeType(medium: Medium) = if (medium.isVideo) "video/*" else "image/*"

        fun showSystemUI(actionbar: ActionBar?, window: Window) {
            actionbar?.show()

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        fun hideSystemUI(actionbar: ActionBar?, window: Window) {
            actionbar?.hide()

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE
        }

        fun getRealPathFromURI(context: Context, uri: Uri): String? {
            var cursor: Cursor? = null
            try {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                val index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                return cursor.getString(index)
            } finally {
                cursor?.close()
            }
        }

        fun needsStupidWritePermissions(context: Context, path: String) = context.needsStupidWritePermissions(path)

        fun getFileDocument(context: Context, path: String, treeUri: String) = context.getFileDocument(path, treeUri)

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun saveTreeUri(context: Context, resultData: Intent) {
            val treeUri = resultData.data
            Config.newInstance(context).treeUri = treeUri.toString()

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
        }

        fun scanFiles(context: Context, paths: Array<String>) = context.rescanFiles(paths)
    }
}
