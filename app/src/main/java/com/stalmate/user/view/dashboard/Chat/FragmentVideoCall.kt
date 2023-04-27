package com.stalmate.user.view.dashboard.Chat


import CustomChatAdapter
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.stalmate.user.R
import com.stalmate.user.base.App
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentVideoCallBinding
import com.stalmate.user.utilities.Constants
import com.stalmate.user.utilities.PrefManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas


class FragmentVideoCall(var receiver_id: String,var isIncomingCall:Boolean) : BaseFragment() {


    // Fill the App ID of your project generated on Agora Console.
    private val appId = "97981e463e674300816cbe0118a02dba"

    // Fill the channel name.
    private val channelName = "test"

    // Fill the temp token generated on Agora Console.
    private val token = "007eJxTYPiWPedc2BGVHnbGMgPVwLAdTrbeDu/tFruvsnM4Mv/ojjoFBktzSwvDVBMz41QzcxNjAwMLQ7PkpFQDQ0OLRAOjlKRE3UX7kxsCGRmur+dhYmSAQBCfhaEktbiEgQEAwgcdyg=="

    // An integer that identifies the local user.
    private val uid = 0
    private var isJoined = false

    private var agoraEngine: RtcEngine? = null

    //SurfaceView to render local video in a Container.
    private var localSurfaceView: SurfaceView? = null

    //SurfaceView to render Remote video in a Container.
    private var remoteSurfaceView: SurfaceView? = null


    private fun setupVideoSDKEngine() {

        val config = RtcEngineConfig()
        config.mContext = App.getInstance()
        config.mAppId = appId
        config.mEventHandler = mRtcEventHandler
        agoraEngine = RtcEngine.create(config)
        // By default, the video module is disabled, call enableVideo to enable it.
        agoraEngine!!.enableVideo()
   /*     try {
            val config = RtcEngineConfig()
            config.mContext = App.getInstance()
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }*/
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            // Set the remote video view
          requireActivity().runOnUiThread {
              setupRemoteVideo(
                  uid
              )
          }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")

            requireActivity().runOnUiThread {
                remoteSurfaceView!!.visibility = View.GONE
            }
        }
    }

    private fun showMessage(s: String) {

    }

    private fun setupRemoteVideo(uid: Int) {
        remoteSurfaceView = SurfaceView(  App.getInstance().baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        // Display RemoteSurfaceView.
        remoteSurfaceView!!.setVisibility(View.VISIBLE)
    }

    private fun setupLocalVideo() {

        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = SurfaceView(  App.getInstance().baseContext)
        binding.localVideoViewContainer.addView(localSurfaceView)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
    }

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            requireContext(),
            REQUESTED_PERMISSIONS[0]
        ) !== PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    REQUESTED_PERMISSIONS[1]
                ) !== PackageManager.PERMISSION_GRANTED)
    }

    fun joinChannel(view: View?) {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()

            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            // Start local preview.
            agoraEngine!!.startPreview()
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine!!.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(
                App.getInstance().baseContext,
                "Permissions was not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun leaveChannel(view: View?) {
        if (!isJoined) {
            showMessage("Join a channel first")
        } else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
            // Stop remote video rendering.
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            // Stop local video rendering.
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
        }
    }










    var sender_id = ""
    lateinit var binding: FragmentVideoCallBinding
    lateinit var chatAdapter: CustomChatAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v = inflater.inflate(R.layout.fragment_video_call, null, false)
        binding = DataBindingUtil.bind<FragmentVideoCallBinding>(v)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVideoSDKEngine();
        sender_id = PrefManager.getInstance(requireContext())?.userDetail?.results?._id.toString()
        chatAdapter = CustomChatAdapter(requireContext())
        getRoomId()

    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        // Destroy the engine in a sub-thread to avoid congestion
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }





    var roomId=""
    private fun getRoomId() {
        var hashmap = HashMap<String, String>()
        hashmap.put("receiver_id", receiver_id)
        networkViewModel.createroomId(hashmap)
        networkViewModel.createRoomIdLiveData.observe(viewLifecycleOwner) {
            if (it!!.status){
             /*   listeningSocket(it.Room_id)
                roomId=it.Room_id*/
            }
        }
    }


}