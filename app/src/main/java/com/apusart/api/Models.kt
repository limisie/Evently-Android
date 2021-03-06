package com.apusart.api

import android.content.Intent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apusart.evently_android.guest.initial_activity.InitialActivity

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    enum class Status {
        SUCCESS,
        ERROR,
        PENDING
    }

    companion object {
        fun<T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun<T> error(message: String?, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        fun<T> pending(data: T? = null): Resource<T> {
            return Resource(Status.PENDING, data, null)
        }
    }
}

//To tylko test, z powodu tego ze jeśli zmienimy jakoś baze to trzeba potem zrobić migrację to jeśli nie bedzie działać to polecam
// wywalić appkę z VM
@Entity
data class Event(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") val title: String
)

fun <T> handleResource(
    res: Resource<T>,
    onSuccess: ((data: T?) -> Unit) = { _ -> },
    onPending: ((data: T?) -> Unit) = { _ -> },
    onError: ((message: String?, data: T?) -> Unit) = { _, _ -> }
) {
    when(res.status) {
        Resource.Status.SUCCESS -> {
            onSuccess(res.data)
        }
        Resource.Status.PENDING -> {
            onPending(res.data)
        }
        Resource.Status.ERROR -> {
            onError(res.message, res.data)
        }
    }
}
