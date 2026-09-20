package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val directory = File(context.filesDir, "kanban_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "kanban_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(directory, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
