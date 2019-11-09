package io.legado.app.service.help

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import io.legado.app.constant.Action
import io.legado.app.data.entities.Book
import io.legado.app.model.WebBook
import io.legado.app.service.AudioPlayService
import io.legado.app.ui.audio.AudioPlayViewModel

object AudioPlay {
    var titleData = MutableLiveData<String>()
    var coverData = MutableLiveData<String>()
    var book: Book? = null
    var inBookshelf = false
    var chapterSize = 0
    var callBack: AudioPlayViewModel.CallBack? = null
    var durChapterIndex = 0
    var durPageIndex = 0
    var webBook: WebBook? = null
    val loadingChapters = arrayListOf<Int>()

    fun play(context: Context, title: String?, subtitle: String, url: String, position: Int) {
        val intent = Intent(context, AudioPlayService::class.java)
        intent.action = Action.play
        intent.putExtra("title", title)
        intent.putExtra("subtitle", subtitle)
        intent.putExtra("url", url)
        intent.putExtra("position", position)
        context.startService(intent)
    }

    fun pause(context: Context) {
        if (AudioPlayService.isRun) {
            val intent = Intent(context, AudioPlayService::class.java)
            intent.action = Action.pause
            context.startService(intent)
        }
    }

    fun resume(context: Context) {
        if (AudioPlayService.isRun) {
            val intent = Intent(context, AudioPlayService::class.java)
            intent.action = Action.resume
            context.startService(intent)
        }
    }

    fun stop(context: Context) {
        if (AudioPlayService.isRun) {
            val intent = Intent(context, AudioPlayService::class.java)
            intent.action = Action.stop
            context.startService(intent)
        }
    }

    fun adjustProgress(context: Context, position: Int) {
        if (AudioPlayService.isRun) {
            val intent = Intent(context, AudioPlayService::class.java)
            intent.action = Action.adjustProgress
            intent.putExtra("position", position)
            context.startService(intent)
        }
    }
}