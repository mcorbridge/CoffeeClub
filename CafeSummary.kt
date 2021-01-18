/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.DecimalFormat

class CafeSummary {



    companion object{
        var shiftGross:Double = 0.0
        val dec = DecimalFormat("$#,###.00")
        var numCustomers:Int = 0
        var customerStats:MutableMap<String,Long> = mutableMapOf()

        private fun getShiftGross():String{
            return dec.format(shiftGross)
        }

        fun getShiftDuration():String{
            return CafeTimer.getAcceleratedTime(CafeTimer.endTime - CafeTimer.startTime)
        }


        @RequiresApi(Build.VERSION_CODES.N)
        fun doSummary(){
            println("----------------------------------------------- SUMMARY -----------------------------------------")
            println("TOTAL Customers: $numCustomers")
            println("TOTAL Shift duration: ${getShiftDuration()}")
            println("Shift Receipts: ${getShiftGross()}")
            BaristaStatus.baristasIdle.forEach{
                println("${it.name} IDLE TIME: ${ CafeTimer.getAcceleratedTime(it.totalIdleTime) }")
            }

            /*for(it in customerStats){
                println("${it.key}  ${it.value}")
            }*/

            println("MAX WAIT TIME: ${customerStats.maxOf { CafeTimer.getAcceleratedTime(it.value) }}")

            println("MIN WAIT TIME: ${customerStats.minOf { CafeTimer.getAcceleratedTime(it.value) }}")

            val values: List<Long> = customerStats.values.toList()

            println("AVERAGE WAIT TIME: ${CafeTimer.getAcceleratedTime(values.average().toLong())}")

            //values.forEach { println(it) }
        }
    }
}