/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import android.os.Build
import androidx.annotation.RequiresApi

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

    fun removeCustomer(customer: Customer) {
        customers.remove(customer)
    }

    fun getAvailableCustomer(): Customer? {
        var availableCustomer: Customer? = null
        for (customer in customers) {
            if (!customer.isServed) {
                availableCustomer = customer
                availableCustomer.isServed = true
                break
            }
        }
        return availableCustomer
    }

}

