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
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.O)
object CustomerQueue {


    var customers: MutableList<Customer> = mutableListOf()

    init {
        setInitialCustomers()
    }

    private fun setInitialCustomers() {
        for (n in 0..9) {
            customers.add(Customer())
        }
    }

    fun addSingleCustomer() {
        val customer = Customer()
        customers.add(customers.size, customer)
    }

    private fun getAvailableCustomer(): Customer? {
        var availableCustomer: Customer? = null
        for (customer in customers) {
            if (!customer.isServed) {
                availableCustomer = customer
                availableCustomer.isServed = true
                customers.remove(availableCustomer)
                break
            }
        }
        return availableCustomer
    }

    fun inspectCustomer(customer: Customer) {
        println("Customer:")
        println("name: ${customer.name}")
        println("isServed: ${customer.isServed}")
        println("orderTime(ms): ${customer.orderTime}")
        println("id: ${customer.id}")
        println("Coffee Order:")
        println(
            "name: ${customer.coffeeOrder.name} " +
                    "size: ${customer.coffeeOrder.size} " +
                    "type: ${customer.coffeeOrder.type} " +
                    "price: ${customer.coffeeOrder.price} " +
                    "brewTime: ${customer.coffeeOrder.brewTime}"
        )

    }

    fun checkCurrentQueue() {

        val kotlinTimer = Timer()
        kotlinTimer.scheduleAtFixedRate(timerTask {
            println("[${CafeTimer.getAcceleratedTime(System.currentTimeMillis() - CafeTimer.startTime)}]")
            if(isClosingTime()){
                CafeStatus.currentCafeStatus = CafeStatusConstants.CLOSED
            }
            if (isClosingConditions()) {
                println("************************************ STORE IS NOW ${CafeStatusConstants.CLOSED} ************************************")
                CafeSummary.doSummary()
                exitProcess(-1)
            } else {
                if (customers.size > 0) {
                    if (BaristaStatus.baristasIdle.size != 0) {
                        var barista = BaristaStatus.baristasIdle[0]
                        BaristaStatus.setBaristaStatus(barista, BaristaConstants.ACTIVE)
                        barista.currentCustomer = getAvailableCustomer()
                        println("${barista.name} is serving ${barista.currentCustomer?.name}")
                        CafeSummary.shiftGross += barista.currentCustomer?.coffeeOrder?.price!!
                        CafeSummary.numCustomers++
                        barista.doBrewCoffee()
                        BaristaStatus.showIdleBaristas()
                    } else {
                        println("...There currently are no baristas available")
                    }
                } else {
                    println("...There currently are no customers")
                }
            }
        }, 10, 2000)
    }

    fun isClosingConditions(): Boolean {
        val isStoreHoursExceeded = CafeTimer.isClosingTime(System.currentTimeMillis() - CafeTimer.startTime)
        val isCustomers = (customers.size == 0)
        val isBaristasActive = (BaristaStatus.baristasIdle.size == BaristaStatus.baristasOnDuty.size)
        return (isStoreHoursExceeded && isCustomers && isBaristasActive)
    }

    private fun isClosingTime() :Boolean{
        return CafeTimer.isClosingTime(System.currentTimeMillis() - CafeTimer.startTime)
    }

}

