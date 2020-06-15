package com.example.telegramspam.ui.current_chat

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.telegramspam.data.Repository
import com.example.telegramspam.utils.log
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class CurrentChatViewModel(private val repository: Repository) : ViewModel() {
    private val _messages = MutableLiveData<List<TdApi.Message>>()
    val messages: LiveData<List<TdApi.Message>> = _messages

    val message = MutableLiveData("")

    val chat = MutableLiveData<TdApi.Chat>()
    val opponent = MutableLiveData<TdApi.User>()
    private val photos = ArrayList<String>()
    private var chatId: Long = 0
    var accountId: Int = 0

    private fun loadMessages() {
        viewModelScope.launch {
           _messages.value = repository.loadMessages(chatId,accountId)
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val success = repository.sendMessage(message.value!!, chatId.toInt(), accountId, photos)
            if (success) {
                message.value = ""
            }
        }

    }

    fun setupChat(chatId: Long, accountId: Int) {
        this.chatId = chatId
        this.accountId = accountId
        viewModelScope.launch {
            chat.value = repository.getChat(chatId, accountId)
            opponent.value = repository.getUser(chatId, accountId)
            val client = repository.provideClient(accountId)
            client?.setUpdatesHandler(updatesHandler)
            loadMessages()
        }

    }

    private val updatesHandler = Client.ResultHandler { update ->
        viewModelScope.launch {
            opponent.value = repository.getUser(chatId, accountId)
        }

        when (update.constructor) {
            TdApi.UpdateNewMessage.CONSTRUCTOR -> loadMessages()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class CurrentChatViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CurrentChatViewModel(
            repository
        ) as T
    }

}