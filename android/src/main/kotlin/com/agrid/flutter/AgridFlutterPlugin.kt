package com.agrid.flutter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.pm.PackageManager
import com.posthog.PersonProfiles
import com.posthog.PostHog
import com.posthog.PostHogConfig
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.Date

/** AgridFlutterPlugin */
class AgridFlutterPlugin :
    FlutterPlugin,
    MethodCallHandler {
    // / The MethodChannel that will be the communication between Flutter and native Android
    // /
    // / This local reference serves to register the plugin with the Flutter Engine and unregister it
    // / when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var applicationContext: Context

    private val snapshotSender = SnapshotSender()

    // Surveys disabled on Android in this fork (use native PostHog SDK without custom surveys delegate)

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "agrid_flutter")

        this.applicationContext = flutterPluginBinding.applicationContext
        initPlugin()

        channel.setMethodCallHandler(this)
    }

    private fun initPlugin() {
        try {
            val ai = applicationContext.packageManager.getApplicationInfo(
                applicationContext.packageName,
                PackageManager.GET_META_DATA
            )
            val bundle = ai.metaData ?: Bundle()
            val autoInit = bundle.getBoolean("com.agrid.agrid.AUTO_INIT", true)

            if (!autoInit) {
                Log.i("Agrid", "com.agrid.agrid.AUTO_INIT is disabled!")
                return
            }

            val apiKey = bundle.getString("com.agrid.agrid.API_KEY")

            if (apiKey.isNullOrEmpty()) {
                Log.e("Agrid", "com.agrid.agrid.API_KEY is missing!")
                return
            }

            val host = bundle.getString("com.agrid.agrid.AGRID_HOST", PostHogConfig.DEFAULT_HOST)
            val captureApplicationLifecycleEvents = bundle.getBoolean("com.agrid.agrid.TRACK_APPLICATION_LIFECYCLE_EVENTS", false)
            val debug = bundle.getBoolean("com.agrid.agrid.DEBUG", false)

            val agridConfig = mutableMapOf<String, Any>()
            agridConfig["apiKey"] = apiKey
            agridConfig["host"] = host
            agridConfig["captureApplicationLifecycleEvents"] = captureApplicationLifecycleEvents
            agridConfig["debug"] = debug

            setupAgrid(agridConfig)
        } catch (e: Throwable) {
            Log.e("Agrid", "initPlugin error: $e")
        }
    }

    override fun onMethodCall(
        call: MethodCall,
        result: Result,
    ) {
        when (call.method) {
            "setup" -> {
                setup(call, result)
            }
            "identify" -> {
                identify(call, result)
            }

            "capture" -> {
                capture(call, result)
            }

            "screen" -> {
                screen(call, result)
            }

            "alias" -> {
                alias(call, result)
            }

            "distinctId" -> {
                distinctId(result)
            }

            "reset" -> {
                reset(result)
            }

            "disable" -> {
                disable(result)
            }

            "enable" -> {
                enable(result)
            }

            "isOptOut" -> {
                isOptOut(result)
            }

            "isFeatureEnabled" -> {
                isFeatureEnabled(call, result)
            }

            "reloadFeatureFlags" -> {
                reloadFeatureFlags(result)
            }

            "group" -> {
                group(call, result)
            }

            "getFeatureFlag" -> {
                getFeatureFlag(call, result)
            }

            "getFeatureFlagPayload" -> {
                getFeatureFlagPayload(call, result)
            }

            "register" -> {
                register(call, result)
            }
            "unregister" -> {
                unregister(call, result)
            }
            "debug" -> {
                debug(call, result)
            }
            "flush" -> {
                flush(result)
            }
            "captureException" -> {
                captureException(call, result)
            }
            "close" -> {
                close(result)
            }
            "sendMetaEvent" -> {
                handleMetaEvent(call, result)
            }
            "sendFullSnapshot" -> {
                handleSendFullSnapshot(call, result)
            }
            "isSessionReplayActive" -> {
                result.success(isSessionReplayActive())
            }
            "getSessionId" -> {
                getSessionId(result)
            }
            "openUrl" -> {
                openUrl(call, result)
            }
            "surveyAction" -> {
                // Surveys not supported on Android in this fork
                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun isSessionReplayActive(): Boolean = PostHog.isSessionReplayActive()

    private fun handleMetaEvent(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val width = call.argument<Int>("width") ?: 0
            val height = call.argument<Int>("height") ?: 0
            val screen = call.argument<String>("screen") ?: ""

            if (width == 0 || height == 0) {
                result.error("INVALID_ARGUMENT", "Width or height is 0", null)
                return
            }

            snapshotSender.sendMetaEvent(width, height, screen)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun setup(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val args = call.arguments() as Map<String, Any>? ?: mapOf<String, Any>()
            if (args.isEmpty()) {
                result.error("AgridFlutterException", "Arguments is null or empty", null)
                return
            }

            setupAgrid(args)

            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun setupAgrid(agridConfig: Map<String, Any>) {
        val apiKey = agridConfig["apiKey"] as String?
        if (apiKey.isNullOrEmpty()) {
            Log.e("Agrid", "apiKey is missing!")
            return
        }

        val host = agridConfig["host"] as String? ?: PostHogConfig.DEFAULT_HOST

        val config =
            PostHogAndroidConfig(apiKey, host).apply {
                captureScreenViews = false
                captureDeepLinks = false
                agridConfig.getIfNotNull<Boolean>("captureApplicationLifecycleEvents") {
                    captureApplicationLifecycleEvents = it
                }
                agridConfig.getIfNotNull<Boolean>("debug") {
                    debug = it
                }
                agridConfig.getIfNotNull<Int>("flushAt") {
                    flushAt = it
                }
                agridConfig.getIfNotNull<Int>("maxQueueSize") {
                    maxQueueSize = it
                }
                agridConfig.getIfNotNull<Int>("maxBatchSize") {
                    maxBatchSize = it
                }
                agridConfig.getIfNotNull<Int>("flushInterval") {
                    flushIntervalSeconds = it
                }
                agridConfig.getIfNotNull<Boolean>("sendFeatureFlagEvents") {
                    sendFeatureFlagEvent = it
                }
                agridConfig.getIfNotNull<Boolean>("preloadFeatureFlags") {
                    preloadFeatureFlags = it
                }
                agridConfig.getIfNotNull<Boolean>("optOut") {
                    optOut = it
                }
                agridConfig.getIfNotNull<String>("personProfiles") {
                    when (it) {
                        "never" -> personProfiles = PersonProfiles.NEVER
                        "always" -> personProfiles = PersonProfiles.ALWAYS
                        "identifiedOnly" -> personProfiles = PersonProfiles.IDENTIFIED_ONLY
                    }
                }
                agridConfig.getIfNotNull<Boolean>("sessionReplay") {
                    sessionReplay = it
                }

                this.sessionReplayConfig.captureLogcat = false

                // Surveys not supported in this fork on Android (no delegate wired)

                // Configure error tracking autocapture
                agridConfig.getIfNotNull<Map<String, Any>>("errorTrackingConfig") { errorConfig ->
                    errorConfig.getIfNotNull<Boolean>("captureNativeExceptions") {
                        errorTrackingConfig.autoCapture = it
                    }
                    errorConfig.getIfNotNull<List<String>>("inAppIncludes") { includes ->
                        errorTrackingConfig.inAppIncludes.addAll(includes)
                    }
                }

                sdkName = "agrid-flutter"
                sdkVersion = agridVersion
            }
        PostHogAndroid.setup(applicationContext, config)
    }

    // Helper function to merge $lib property into event properties
    private fun mergeLibProperty(properties: Map<String, Any>?): Map<String, Any> {
        val mergedProperties = properties?.toMutableMap() ?: mutableMapOf()
        mergedProperties["\$lib"] = "agrid-flutter"
        return mergedProperties
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun handleSendFullSnapshot(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val imageBytes = call.argument<ByteArray>("imageBytes")
            val id = call.argument<Int>("id") ?: 1
            val x = call.argument<Int>("x") ?: 0
            val y = call.argument<Int>("y") ?: 0
            if (imageBytes != null) {
                snapshotSender.sendFullSnapshot(imageBytes, id, x, y)
                result.success(null)
            } else {
                result.error("INVALID_ARGUMENT", "Image bytes are null", null)
            }
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun getFeatureFlag(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val featureFlagKey: String = call.argument("key")!!
            val flag = PostHog.getFeatureFlag(featureFlagKey)
            result.success(flag)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun getFeatureFlagPayload(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val featureFlagKey: String = call.argument("key")!!
            val flag = PostHog.getFeatureFlagPayload(featureFlagKey)
            result.success(flag)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun identify(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val userId: String = call.argument("userId")!!
            val userProperties: Map<String, Any>? = call.argument("userProperties")
            val userPropertiesSetOnce: Map<String, Any>? = call.argument("userPropertiesSetOnce")
            PostHog.identify(userId, userProperties, userPropertiesSetOnce)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun capture(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val eventName: String = call.argument("eventName")!!
            val properties: Map<String, Any>? = call.argument("properties")
            //PostHog.capture(eventName, properties = mergeLibProperty(properties))
            PostHog.capture(eventName, properties = properties)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun screen(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val screenName: String = call.argument("screenName")!!
            val properties: Map<String, Any>? = call.argument("properties")
            //PostHog.screen(screenName, properties = mergeLibProperty(properties))
            PostHog.screen(screenName, properties = properties)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun alias(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val alias: String = call.argument("alias")!!
            PostHog.alias(alias)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun distinctId(result: Result) {
        try {
            val distinctId: String = PostHog.distinctId()
            result.success(distinctId)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun reset(result: Result) {
        try {
            PostHog.reset()
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun enable(result: Result) {
        try {
            PostHog.optIn()
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun debug(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val debug: Boolean = call.argument("debug")!!
            PostHog.debug(debug)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun disable(result: Result) {
        try {
            PostHog.optOut()
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun isOptOut(result: Result) {
        try {
            val isOptedOut = PostHog.isOptOut()
            result.success(isOptedOut)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun isFeatureEnabled(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val key: String = call.argument("key")!!
            val isEnabled = PostHog.isFeatureEnabled(key)
            result.success(isEnabled)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun reloadFeatureFlags(result: Result) {
        try {
            PostHog.reloadFeatureFlags()
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun group(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val groupType: String = call.argument("groupType")!!
            val groupKey: String = call.argument("groupKey")!!
            val groupProperties: Map<String, Any>? = call.argument("groupProperties")
            PostHog.group(groupType, groupKey, groupProperties)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun register(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val key: String = call.argument("key")!!
            val value: Any = call.argument("value")!!
            PostHog.register(key, value)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun unregister(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val key: String = call.argument("key")!!
            PostHog.unregister(key)
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun flush(result: Result) {
        try {
            PostHog.flush()
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun captureException(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val arguments =
                call.arguments as? Map<String, Any> ?: run {
                    result.error("INVALID_ARGUMENTS", "Invalid arguments for captureException", null)
                    return
                }

            val properties = arguments["properties"] as? Map<String, Any>
            val timestampMs = arguments["timestamp"] as? Long

            // Extract timestamp from Flutter
            val timestamp: Date? =
                timestampMs?.let {
                    // timestampMs already in UTC milliseconds epoch
                    Date(timestampMs)
                }

            //PostHog.capture("\$exception", properties = mergeLibProperty(properties), timestamp = timestamp)
            PostHog.capture("\$exception", properties = properties, timestamp = timestamp)
            result.success(null)
        } catch (e: Throwable) {
            result.error("CAPTURE_EXCEPTION_ERROR", "Failed to capture exception: ${e.message}", null)
        }
    }

    private fun close(result: Result) {
        try {
            PostHog.close()
            result.success(null)
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun getSessionId(result: Result) {
        try {
            val sessionId = PostHog.getSessionId()
            result.success(sessionId?.toString())
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    // Call the `completion` closure if cast to map value with `key` and type `T` is successful.
    @Suppress("UNCHECKED_CAST")
    private fun <T> Map<String, Any>.getIfNotNull(
        key: String,
        callback: (T) -> Unit,
    ) {
        (get(key) as? T)?.let {
            callback(it)
        }
    }

    private fun openUrl(
        call: MethodCall,
        result: Result,
    ) {
        try {
            val raw = (call.arguments as? String)?.trim()
            if (raw.isNullOrEmpty()) {
                result.error("InvalidArguments", "URL is null or empty", null)
                return
            }

            var uri =
                try {
                    Uri.parse(raw)
                } catch (e: Throwable) {
                    result.error("InvalidArguments", "Malformed URL: $raw", null)
                    return
                }

            // If no scheme provided (e.g., "example.com"), default to https://
            if (uri.scheme.isNullOrEmpty()) {
                uri = Uri.parse("https://$raw")
            }

            val intent =
                Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            try {
                applicationContext.startActivity(intent)
                result.success(null)
            } catch (e: ActivityNotFoundException) {
                result.error("ActivityNotFound", "No application can handle ACTION_VIEW for the given URL", null)
            }
        } catch (e: Throwable) {
            result.error("AgridFlutterException", e.localizedMessage, null)
        }
    }

    private fun invokeFlutterMethod(
        method: String,
        arguments: Any? = null,
    ) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            channel.invokeMethod(method, arguments)
        } else {
            Handler(Looper.getMainLooper()).post {
                channel.invokeMethod(method, arguments)
            }
        }
    }

    // Surveys disabled: no handling here
}
