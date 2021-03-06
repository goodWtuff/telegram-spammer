package com.example.telegramspam.data.telegram

import com.example.telegramspam.API_HASH
import com.example.telegramspam.API_ID
import com.example.telegramspam.SOCKS5
import com.example.telegramspam.models.*
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.logDb
import com.example.telegramspam.utils.removeEmpty
import kotlinx.coroutines.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File
import java.util.*
import kotlin.collections.HashMap

object TelegramClientUtil {
    private val clients = HashMap<String, Client?>()

    suspend fun watchPosts(client: Client?, chatUsername: String): Boolean {
        val chat = getChatInfo(client, chatUsername)
        return if (chat != null) {
            val lastMsgId = chat.lastMessage?.id ?: 0
            suspendCancellableCoroutine { continuation ->
                client?.send(
                    TdApi.GetChatHistory(
                        chat.id,
                        lastMsgId,
                        0,
                        100,
                        false
                    )
                ) {response ->
                    if (response is TdApi.Messages) {
                        val idList = LongArray(response.messages.size + 1)

                        for ((i, msg) in response.messages.withIndex()) {
                            idList[i] = msg.id
                        }
                        if(lastMsgId != 0L) idList[idList.size - 1] = lastMsgId

                        client.send(TdApi.OpenChat(chat.id)){
                            if(it is TdApi.Ok){
                                for (i in 0..5){
                                    client.send(TdApi.ViewMessages(chat.id, idList, true)) {}
                                }
                                continuation.resume(true){}
                            }else{
                                log(it)
                                if(it is TdApi.Error) logDb(it.message)
                                continuation.resume(false) {}
                            }
                        }
                    } else {
                        log(response)
                        if (response is TdApi.Error) logDb(response.message)
                        continuation.resume(false) {}
                    }
                }
            }

        } else {
            false
        }

    }

    suspend fun inviteUser(client: Client?, username: String, chat: String): Boolean {
        val chatId = getChatInfo(client, chat)?.id
        val userId = getUserId(client, username)

        return if (chatId != null && userId != null) {
            suspendCancellableCoroutine { continuation ->
                client?.send(TdApi.AddChatMember(chatId, userId, 100)) {
                    if (it is TdApi.Ok) {
                        continuation.resume(true) {}
                    } else {
                        log("add chat member error")
                        if (it is TdApi.Error) logDb(it.message)
                        continuation.resume(false) {}
                    }
                }
            }
        } else {
            false
        }

    }

    private suspend fun getUserId(client: Client?, username: String): Int? {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.SearchPublicChat(username)) { chat ->
                if (chat is TdApi.Chat) {
                    val chatType = chat.type
                    if (chatType is TdApi.ChatTypePrivate) {
                        continuation.resume(chatType.userId) {}
                    } else {
                        log("get user error ")
                        if (chatType is TdApi.Error) logDb(chatType.message)
                        continuation.resume(null) {}
                    }
                } else {
                    log("get user error")
                    if (chat is TdApi.Error) logDb(chat.message)
                    continuation.resume(null) {}
                }
            }
        }

    }

    private suspend fun getChatInfo(client: Client?, username: String): TdApi.Chat? {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.SearchPublicChat(username)) {
                if (it is TdApi.Chat) {
                    continuation.resume(it) {}
                } else {
                    if (it is TdApi.Error) logDb(it.message)
                    continuation.resume(null) {}
                }
            }
        }

    }

    suspend fun joinChat(client: Client?, chat: String): Boolean {
        val chatId = getChatInfo(client, chat)?.id
        return if (chatId != null) {
            suspendCancellableCoroutine { continuation ->
                client?.send(TdApi.JoinChat(chatId)) {
                    if (it is TdApi.Ok) {
                        continuation.resume(true) {}
                    } else {
                        if (it is TdApi.Error) logDb(it.message)
                        continuation.resume(false) {}
                    }
                }

            }
        } else {
            false
        }

    }

    suspend fun downloadFile(id: Int?): File? {
        if (id == null) return null
        val values = clients.values.toTypedArray()
        val client = values[Random().nextInt(values.size)] ?: return null
        return suspendCancellableCoroutine { continuation ->
            client.send(TdApi.DownloadFile(id, 32, 0, 0, false)) {
                if (it is TdApi.File) {
                    if (!it.local.path.isNullOrEmpty()) {
                        continuation.resume(File(it.local.path)) {}
                    } else {
                        CoroutineScope(Dispatchers.IO).launch { continuation.resume(downloadFile(id)) {} }
                    }
                } else {
                    continuation.resume(null) {}
                }


            }
        }

    }

    suspend fun loadMessages(
        client: Client?,
        chatId: Long,
        fromMsgId: Long,
        limit: Int = 100,
        offset: Int = 0
    ): GetMessagesResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.GetChatHistory(chatId, fromMsgId, offset, limit, false)) {
                when (it) {
                    is TdApi.Messages -> continuation.resume(GetMessagesResult.Success(it)) {}
                    is TdApi.Error -> {
                        log(it.message)
                        continuation.resume(GetMessagesResult.Error()) {}
                    }
                    else -> {
                        continuation.resume(GetMessagesResult.Error()) {}
                    }
                }
            }
        }
    }

    suspend fun loadChat(client: Client?, chatId: Long): GetChatInfoResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.GetChat(chatId)) {
                when (it) {
                    is TdApi.Chat -> {
                        continuation.resume(GetChatInfoResult.Success(it)) {}
                    }
                    is TdApi.Error -> {
                        log(it.message)
                        continuation.resume(GetChatInfoResult.Error()) {}
                    }
                    else -> {
                        continuation.resume(GetChatInfoResult.Error()) {}
                    }
                }
            }

        }
    }

    suspend fun loadChats(client: Client?): GetChatsResult {
        return suspendCancellableCoroutine { continuation ->

            client?.send(TdApi.GetChats(TdApi.ChatListMain(), Long.MAX_VALUE, 0, 250)) {
                when (it) {
                    is TdApi.Chats -> {
                        continuation.resume(GetChatsResult.Success(it)) {}
                    }
                    is TdApi.Error -> {
                        log(it.message)
                        continuation.resume(GetChatsResult.Error()) {}
                    }
                    else -> {
                        continuation.resume(GetChatsResult.Error()) {}
                    }
                }
            }
        }
    }

    private suspend fun createClient(account: Account): ClientCreateResult {
        return suspendCancellableCoroutine { continuation ->
            var client: Client? = null
            client = Client.create({ state ->
                when (state) {
                    is TdApi.UpdateAuthorizationState -> {
                        when (state.authorizationState.constructor) {
                            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                                val params =
                                    generateParams(account.databasePath)
                                client?.send(TdApi.SetTdlibParameters(params)) {
                                    if (it is TdApi.Error) {
                                        continuation.resume(ClientCreateResult.Error(it.message)) {}
                                    }
                                }
                            }
                            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                                client?.send(TdApi.CheckDatabaseEncryptionKey()) {
                                    if (it is TdApi.Error) {
                                        continuation.resume(ClientCreateResult.Error(it.message)) {}
                                    }
                                }
                            }
                            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                                if (client != null) {
                                    clients[account.phoneNumber] = client
                                    continuation.resume(ClientCreateResult.Success(client!!)) {}
                                }
                            }
                        }
                    }
                }
            }, null, null)


        }
    }

    suspend fun provideClient(account: Account): ClientCreateResult {
        val savedClient = clients[account.phoneNumber]
        if (savedClient == null || savedClient.isClosed()) {
            return createClient(account)
        } else {
            return suspendCancellableCoroutine { continuation ->

                savedClient.send(TdApi.GetMe()) {
                    if (it is TdApi.User) {
                        continuation.resume(ClientCreateResult.Success(savedClient)) {}
                    } else {
                        continuation.resume(ClientCreateResult.Error("Something went wrong")) {}
                    }
                }


            }
        }

    }

    private suspend fun Client?.isClosed(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            this?.send(TdApi.GetMe()) {
                if (it is TdApi.Error)
                    continuation.resume(true) {}
                else
                    continuation.resume(false) {}
            }
        }
    }

    fun stopClient(phoneNumber: String) {
        clients[phoneNumber]?.let {
            it.close()
            clients.remove(phoneNumber)
        }

    }

    suspend fun disableProxy(client: Client?) {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.DisableProxy()) {
                if (it is TdApi.Error) log(it)
                continuation.resume(Unit) {}
            }
        }
    }

    suspend fun addProxy(
        client: Client?,
        account: Account
    ): Int? {
        return suspendCancellableCoroutine { continuation ->
            val type = if (account.proxyType == SOCKS5) {
                TdApi.ProxyTypeSocks5(account.proxyUsername, account.proxyPassword)
            } else {
                TdApi.ProxyTypeHttp(account.proxyUsername, account.proxyPassword, true)
            }
            val function = TdApi.AddProxy(account.proxyIp, account.proxyPort, true, type)
            client?.send(function) {
                if (it is TdApi.Proxy) {
                    log(it)
                    continuation.resume(it.id) {}
                } else {
                    continuation.resume(null) {}
                }

            }
        }


    }

    suspend fun addProxy(
        client: Client?,
        proxyIp: String = "",
        proxyPort: Int = 0,
        proxyUsername: String = "",
        proxyPassword: String = "",
        proxyType: String = ""
    ): Int? {
        return suspendCancellableCoroutine { continuation ->
            val type = if (proxyType == SOCKS5) {
                TdApi.ProxyTypeSocks5(proxyUsername, proxyPassword)
            } else {
                TdApi.ProxyTypeHttp(proxyUsername, proxyPassword, true)
            }
            val function = TdApi.AddProxy(proxyIp, proxyPort, true, type)
            client?.send(function) {
                if (it is TdApi.Proxy) {
                    continuation.resume(it.id) {}
                } else {
                    continuation.resume(null) {}
                }

            }
        }


    }


    fun generateParams(dbPath: String): TdApi.TdlibParameters {
        return TdApi.TdlibParameters().apply {
            databaseDirectory = dbPath
            useMessageDatabase = true
            systemLanguageCode = "ru"
            useSecretChats = false
            apiId = API_ID
            apiHash = API_HASH
            deviceModel = "Desktop"
            systemVersion = "Unknown"
            applicationVersion = "1.0"
            enableStorageOptimizer = true
        }
    }

    fun checkUserBySettings(
        user: TdApi.User, accountSettings: AccountSettings
    ): Boolean {
        if (user.profilePhoto != null == accountSettings.havePhoto) {
            if (checkOnline(user.status, accountSettings.maxOnlineDifference)) {
                val emptyStatus = user.status.constructor == TdApi.UserStatusEmpty.CONSTRUCTOR
                if (emptyStatus == accountSettings.hiddenStatus) {
                    return true
                }
            }
        }
        return false
    }

    fun checkOnline(status: TdApi.UserStatus, maxOnlineDifference: Long): Boolean {
        return when (status) {
            is TdApi.UserStatusOffline -> {
                val minOnline = System.currentTimeMillis() / 1000 - maxOnlineDifference
                return minOnline <= status.wasOnline
            }
            is TdApi.UserStatusRecently -> true
            is TdApi.UserStatusEmpty -> true
            is TdApi.UserStatusOnline -> true
            else -> false
        }
    }

    suspend fun getChatMembers(
        client: Client?,
        chat: TdApi.ChatTypeSupergroup,
        offset: Int
    ): GetChatMembersResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(
                TdApi.GetSupergroupMembers(
                    chat.supergroupId,
                    null,
                    offset,
                    200
                )
            ) { members ->
                if (members is TdApi.ChatMembers) {
                    continuation.resume(GetChatMembersResult.Success(members)) {}
                } else {
                    continuation.resume(GetChatMembersResult.Error()) {}
                    log(members)
                }
            }
        }
    }

    fun blockUser(client: Client?, userId: Int) {
        client?.send(TdApi.BlockUser(userId)) {
            if (it is TdApi.Error) {
                log(it)
            }
        }
    }

    suspend fun sendMessage(
        client: Client?,
        photos: List<String>,
        message: String,
        chatId: Int
    ) = suspendCancellableCoroutine<Boolean> { continuation ->
        if (client != null) {
            val caption = TdApi.FormattedText(message, null)

            if (photos.isEmpty()) {
                val content = TdApi.InputMessageText(caption, false, false)
                val function = TdApi.SendMessage(chatId.toLong(), 0, null, null, content)
                sendMsg(client, continuation, function, chatId)
            } else {
                val function = TdApi.SendMessageAlbum(
                    chatId.toLong(),
                    0,
                    null,
                    createPhotos(caption, photos)
                )
                sendMsg(client, continuation, function, chatId)
            }
        }

    }

    private fun createPhotos(
        caption: TdApi.FormattedText,
        photos: List<String>
    ): Array<TdApi.InputMessageContent> {
        val paths = photos.removeEmpty()
        var contentList = emptyArray<TdApi.InputMessageContent>()

        for ((index, path) in paths.withIndex()) {
            val photo = TdApi.InputFileLocal(path)
            val content = if (index == 0) {
                TdApi.InputMessagePhoto(photo, null, null, 300, 300, caption, 0)
            } else {
                TdApi.InputMessagePhoto(photo, null, null, 300, 300, null, 0)
            }
            contentList += content

        }
        return contentList
    }

    private fun sendMsg(
        client: Client,
        continuation: CancellableContinuation<Boolean>,
        function: TdApi.Function,
        userId: Int
    ) {
        client.send(function) {
            if (it is TdApi.Error) {
                if (it.code == 5) {
                    client.send(TdApi.CreatePrivateChat(userId, false)) { result ->
                        if (result is TdApi.Error) {
                            continuation.resume(false) {}
                        } else {
                            client.send(function) { res ->
                                if (res is TdApi.Error) {
                                    continuation.resume(false) {}
                                } else {
                                    continuation.resume(true) {}
                                }
                            }
                        }
                    }
                } else {
                    continuation.resume(false) {}
                }
            } else {
                continuation.resume(true) {}
            }

        }
    }

    suspend fun getChat(client: Client?, id: Long): GetChat {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.GetChat(id)) { chat ->
                if (chat is TdApi.Chat) {
                    continuation.resume(GetChat.Success(chat)) {}
                } else {
                    continuation.resume(GetChat.Error()) {}
                    log(chat)
                }
            }
        }
    }


    suspend fun getChat(client: Client?, link: String): GetChatResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.SearchPublicChat(link)) { chat ->
                if (chat is TdApi.Chat && chat.type is TdApi.ChatTypeSupergroup) {
                    val type = chat.type
                    if (type is TdApi.ChatTypeSupergroup) {

                        continuation.resume(GetChatResult.Success(type)) {}
                    }
                } else {
                    continuation.resume(GetChatResult.Error()) {}
                    log(chat)
                }
            }
        }
    }


    suspend fun getUser(client: Client?, userId: Int): GetUserResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.GetUser(userId)) { user ->
                if (user is TdApi.User) {

                    continuation.resume(GetUserResult.Success(user)) {}
                } else {
                    continuation.resume(GetUserResult.Error()) {}
                    log(user)
                }
            }
        }
    }
}

