/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.timerTask

class CustomerGenerator {

    companion object{
        @RequiresApi(Build.VERSION_CODES.O)
        fun startCustomersWalkingInTheDoor() {
            val kotlinTimer = Timer()
            kotlinTimer.scheduleAtFixedRate(timerTask {
                if(CafeStatus.currentCafeStatus == CafeStatus.CLOSED){
                    kotlinTimer.cancel()
                }
                val rnd = (0..9).random()
                if(rnd == 8){ // a random 'blast' of customers
                    CustomerQueue.addSingleCustomer()
                    CustomerQueue.addSingleCustomer()
                    CustomerQueue.addSingleCustomer()
                    println("+++")
                }
                else if (rnd >= 7) {
                    CustomerQueue.addSingleCustomer()
                    println("+")
                }

            }, 5000, 5000)
        }
    }
}