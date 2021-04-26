package com.munch.pre.lib.bluetooth

import androidx.annotation.IntDef
import com.munch.pre.lib.ATTENTION

/**
 * Create by munch1182 on 2021/4/26 14:53.
 */
interface BtScanListener {

    fun onStart()

    fun onScan(device: BtDevice)

    fun onEnd(devices: MutableList<BtDevice>)

    /**
     *
     * @see android.bluetooth.le.ScanCallback.SCAN_FAILED_ALREADY_STARTED
     * @param errorCode
     */
    fun onFail(@ScanFailReason errorCode: Int){}
}

@IntDef(
    ScanFailReason.SCAN_FAILED_NO_SCANNER,
    ScanFailReason.SCAN_FAILED_ALREADY_STARTED,
    ScanFailReason.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED,
    ScanFailReason.SCAN_FAILED_INTERNAL_ERROR,
    ScanFailReason.SCAN_FAILED_FEATURE_UNSUPPORTED,
    ScanFailReason.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES,
    ScanFailReason.SCAN_FAILED_SCANNING_TOO_FREQUENTLY,
)
@Retention(AnnotationRetention.SOURCE)
annotation class ScanFailReason {

    companion object {

        const val SCAN_FAILED_NO_SCANNER = 0

        /**
         * @see android.bluetooth.le.ScanCallback.SCAN_FAILED_ALREADY_STARTED
         *
         * Fails to start scan as BLE scan with the same settings is already started by the app.
         */
        const val SCAN_FAILED_ALREADY_STARTED = 1

        /**
         * Fails to start scan as app cannot be registered.
         */
        const val SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2

        /**
         * Fails to start scan due an internal error
         */
        const val SCAN_FAILED_INTERNAL_ERROR = 3

        /**
         * Fails to start power optimized scan as this feature is not supported.
         */
        const val SCAN_FAILED_FEATURE_UNSUPPORTED = 4

        /**
         * Fails to start scan as it is out of hardware resources.
         *
         * @hide
         */
        const val SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES = 5

        /**
         * Fails to start scan as application tries to scan too frequently.
         * @hide
         */
        const val SCAN_FAILED_SCANNING_TOO_FREQUENTLY = 6

        @ATTENTION
        @ScanFailReason
        fun fromErrorCode(errorCode: Int): Int {
            return errorCode
        }
    }
}


