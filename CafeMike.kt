/*
 * Copyright (c) 2020. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

/**
 * 54,000,000 msec per 15 hr shift (15 hr x 60 min x 60 sec x 1000 msec)
 * 3,600,000 msec per hr
 * --------------------------------
 * 3,600 msec = 1 min
 * 216,000 msec = 1 hr
 * 3,240,000 msec = 15hr
 *
 * Starbucks they are bringing a daily average of around 476 customers per store which leads to over 600+ cups of coffee per day.
 */
@RequiresApi(Build.VERSION_CODES.O)
class CafeMike {

    var customers = CustomerQueue.customers
    val dec = DecimalFormat("$#,###.00")

    init {
        CafeStatus.currentCafeStatus = CafeStatusConstants.OPEN
        CafeStatus.numBaristasOnDuty = 3
        getStartTime()
        BaristaStatus.addBaristasIdle()
        BaristaStatus.addBaristasOnDuty()
        CustomerGenerator.busyCustomerDay()
        CustomerQueue.startServingCustomers()

    } // end init

    private fun getStartTime() {
        CafeTimer.startTime = System.currentTimeMillis()
    }
}

/**
 * BrewTimeSimulator
 */
class BrewTimeSimulator() {
    suspend fun brewCoffee(delay: Long, callback: () -> Unit) {
// Add a delay then invoke the callback
        delay(delay)
        callback()
    }
}



class CoffeeOrder(val name: String, val type: String, val size: String, val price: Double, val brewTime: Long) {

}





