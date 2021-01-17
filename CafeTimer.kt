/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import java.util.concurrent.TimeUnit

class CafeTimer {

    companion object{

        var startTime:Long = 0L
        var durationOpen:Long = 1L * 60L * 60L * 1000L // currently set to virtual 1 (ONE) hour - 1L

        // TODO investigate further increasing the time acceleration
        fun getAcceleratedTime(elapsedTimeMSEC: Long): String {
            val accelTime: Long = elapsedTimeMSEC * 10L // <- the process is sped up factor of 10
            val strElapsedTime = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(accelTime),
                TimeUnit.MILLISECONDS.toMinutes(accelTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(accelTime)),
                TimeUnit.MILLISECONDS.toSeconds(accelTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(accelTime)));
            return "$strElapsedTime (HH:mm:ss)"
        }

        fun isClosingTime(elapsedTimeMSEC: Long):Boolean{
            val accelTime: Long = elapsedTimeMSEC * 10L // <- the process is sped up factor of 10
            /*if(accelTime >= durationOpen){
                CafeStatus.currentCafeStatus = CafeStatusConstants.CLOSED
                println("STORE IS NOW ${CafeStatusConstants.CLOSED}")
            }*/
            return (accelTime >= durationOpen)
        }

    }
}