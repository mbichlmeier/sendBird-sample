package de.maibornwolff.sendbirdtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelListQuery
import com.sendbird.android.GroupChannelListQuery.GroupChannelListQueryResultHandler
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.uikit.SendBirdUIKit
import com.sendbird.uikit.adapter.SendBirdUIKitAdapter
import com.sendbird.uikit.fragments.ChannelFragment
import com.sendbird.uikit.interfaces.UserInfo
import timber.log.Timber


class CustomChatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_chat, container, false)
    }

    override fun onDestroy() {
        // TODO SENDBIRD-SUPPORT ref:_00D6F1ogkx._5006F2c6g4m:ref
        /*
        When doing a back press the fragment is destroyed. Even after destroying this Fragment including the inflated ChannelFragment,
        the read recognition icons are still set when when you were leaving the chat window.
        Only when the app goes into background you are handled as "offline" and other messages sent to you are not marked as read.
         */
        Timber.d("Fragment lifecycle onDestroy")
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSendBird()
        doOnChannelUrl() { channelUrl ->
            Timber.d("Opening ChannelFragment with url $channelUrl")

            // TODO SENDBIRD-SUPPORT ref:_00D6F1ogkx._5006F2edIrD:ref
            /* When passing an invalid channelUrl, the app is crashing and there is no chance to catch the SendBirdException.
               This is throwing the following exception:
                E/SBUIKIT: [20:45:20.191 ChannelFragment:lambda$onReady$1$ChannelFragment():146] com.sendbird.android.SendBirdException: "Channel" not found.
                at com.sendbird.android.APIClient.processResponse(APIClient.java:5201)
                at com.sendbird.android.APIClient$116.onResponse(APIClient.java:5075)
                at com.sendbird.android.shadow.okhttp3.RealCall$AsyncCall.execute(RealCall.java:203)
                at com.sendbird.android.shadow.okhttp3.internal.NamedRunnable.run(NamedRunnable.java:32)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                at java.lang.Thread.run(Thread.java:919)

                Passing a valid ChannelUrl works as expected. Also passing an empty String does not crash the app.
                Only when passing an invalid channelUrl, the app is crashing.
             */
            try {
                parentFragmentManager.beginTransaction().replace(
                    R.id.sendbird_chat_fragment,
                    ChannelFragment.Builder(channelUrl) // passing an invalid [channelUrl] like "ThisIsNotAValidChannelUrl" will cause an app crash
                        .setUseHeader(true)
                        .setUseTypingIndicator(true)
                        .setInputHint("Enter a message here")
                        .build()
                ).commit()
            } catch (e: SendBirdException) {
                Timber.e(
                    e,
                    "Could not open ChannelFragment. Even catching the exception does not work!"
                )
            }
        }
    }

    private fun doOnChannelUrl(callback: (String) -> Unit) {
        val channelListQuery = GroupChannel.createMyGroupChannelListQuery()
        channelListQuery.isIncludeEmpty = true
        channelListQuery.order =
            GroupChannelListQuery.Order.LATEST_LAST_MESSAGE // CHRONOLOGICAL, LATEST_LAST_MESSAGE, CHANNEL_NAME_ALPHABETICAL, and METADATA_VALUE_ALPHABETICAL

        channelListQuery.limit = 15

        channelListQuery.next(GroupChannelListQueryResultHandler { list, e ->
            if (e != null) {    // Error.
                return@GroupChannelListQueryResultHandler
            }
            Timber.d("List of Channels: $list")
            // Just use the first group channel to receive the channelUrl
            callback.invoke(list.first().url)
        })
    }

    private fun setupSendBird() {
        // The sample SendBird chat is used here (https://sample.sendbird.com/basic/chat.html)
        val appId = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"
        val userId = "Arnold" // other user "Michael"
        val nickname = "Arnold" // other user "Michael"

        Timber.d("Start init and connect of SendBird AND SendBirdUiKit")
        setupSendBird(appId, userId)
        setupSendBirdUiKit(userId, nickname, appId)
    }

    private fun setupSendBird(
        appId: String,
        userId: String
    ) {
        SendBird.init(appId, context)
        SendBird.connect(userId) { user, sendBirdException ->
            if (sendBirdException != null) { // Error.
                Timber.e(sendBirdException, "sendBirdException in SendBird.connect")
                return@connect
            }
            Timber.d("SendBird connected with user $user")
        }
    }

    private fun setupSendBirdUiKit(
        userId: String,
        nickname: String,
        appId: String
    ) {
        SendBirdUIKit.init(object : SendBirdUIKitAdapter {
            override fun getUserInfo(): UserInfo {
                return object : UserInfo {
                    override fun getUserId(): String {
                        return userId
                    }

                    override fun getNickname(): String {
                        return nickname
                    }

                    override fun getProfileUrl(): String {
                        return ""
                    }
                }
            }

            override fun getAppId(): String {
                return appId
            }

            override fun getAccessToken(): String {
                return ""
            }
        }, requireContext())
        SendBirdUIKit.connect { user, sendBirdException ->
            if (sendBirdException != null) { // Error.
                Timber.e(sendBirdException, "sendBirdException in SendBirdUIKit.connect")
                return@connect
            }
            Timber.d("SendBirdUiKit connected with user $user")
        }
        SendBirdUIKit.setDefaultThemeMode(SendBirdUIKit.ThemeMode.Dark)
    }
}