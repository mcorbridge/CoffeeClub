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

    //var status = BaristaConstants.AVAILABLE
    var currentCustomer: Customer? = null
    var serverCallback: () -> Unit = {}

    fun doBrewCoffee() {

        val brewTimeSimulator = BrewTimeSimulator()
        val currentCustomer = this.currentCustomer

        GlobalScope.launch {
            if (currentCustomer != null) {
                brewTimeSimulator.brewCoffee(currentCustomer.coffeeOrder.brewTime) {
                    println("     [ORDER COMPLETE] $name <- ${currentCustomer.name}")
                    println("customers in queue: [${CustomerQueue.customers.size}]")
                    setBaristaIdle() // scope issues, so call outside function (?? <- works)
                }
            }
        }
    }

    fun setBaristaIdle() {
        BaristaStatus.setBaristaStatus(this, BaristaConstants.IDLE)
        currentCustomer = null
    }

} // end barista class

