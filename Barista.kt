/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Barista(val name: String, val hrlyRate: Double = 15.00)  {

    var currentCustomer: Customer? = null
    var serverCallback: () -> Unit = {} // keeping this as example of storing function as variable
    var idleTimeStart:Long = 0
    var idleTimeEnd:Long = 0
    var totalIdleTime:Long = 0

    fun doBrewCoffee() {

        val brewTimeSimulator = BrewTimeSimulator()
        val currentCustomer = this.currentCustomer

        GlobalScope.launch {
            if (currentCustomer != null) {
                brewTimeSimulator.brewCoffee(currentCustomer.coffeeOrder.brewTime) {
                    currentCustomer.setOrderTimes(CustomerConstants.END)
                    val processTime = getProcessTime(currentCustomer)
                    println("     [ORDER COMPLETE] $name <- ${currentCustomer.name} (${currentCustomer.customerNum}) ${processTime}")
                    setBaristaIdle() // scope issues, so call outside function (?? <- works)
                    println("customers in queue: [${CustomerQueue.customers.size}]")
                    CafeSummary.customerStats[currentCustomer.name] = getProcessRawMillisec(currentCustomer)
                }
            }
        }
    }

    private fun setBaristaIdle() {
        BaristaStatus.setBaristaStatus(this, BaristaConstants.IDLE)
        idleTimeStart = System.currentTimeMillis()
        currentCustomer = null
    }

    private fun getProcessTime(customer: Customer):String{
        return CafeTimer.getAcceleratedTime(customer.orderTimeEnd - customer.orderTimeInit)
    }

    fun getProcessRawMillisec(customer: Customer):Long{
        return customer.orderTimeEnd - customer.orderTimeInit
    }

} // end barista class

