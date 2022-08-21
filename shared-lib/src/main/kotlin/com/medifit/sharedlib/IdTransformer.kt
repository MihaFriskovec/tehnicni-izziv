package com.medifit.sharedlib

object IdTransformer {
    fun toExternal(id: Long?): String {
        return "EXTERNAL_$id"
    }

    fun toInternal(id: String): Long {
        return id.split("_")[1].toLong()
    }
}
