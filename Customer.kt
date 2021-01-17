/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

import java.util.*
import kotlin.properties.Delegates

class Customer {

    lateinit var name: String
    var customerNum:Int = 0
    var orderTimeInit by Delegates.notNull<Long>()
    var orderTimeEnd by Delegates.notNull<Long>()
    lateinit var coffeeOrder: CoffeeOrder
    lateinit var id: UUID
    var isServed: Boolean = false

    init {
        setCustomerName()
        setCoffeeOrder()
        setUUID()
        orderTimeInit = System.currentTimeMillis() // TODO I am not currently setting this in setCustomerName()?
        customerNum = CafeStatus.customerNum++
    }

    private fun setCustomerName() {
        this.name = getRndCustomerName()
    }

    fun setOrderTimes(action:CustomerConstants) {
        when(action){
            CustomerConstants.BEGIN -> this.orderTimeInit = System.currentTimeMillis()
            CustomerConstants.END -> this.orderTimeEnd = System.currentTimeMillis()
        }

    }

    private fun setUUID() {
        this.id = UUID.randomUUID()
    }

    private fun setCoffeeOrder() {
        val menuItem: Menu = Menu.values()[(Menu.values().indices).random()]
        val coffeeItem: CoffeeItem = menuItem.item
        val rndSize = coffeeItem.price.keys.toList()[(coffeeItem.price.keys.toList().indices).random()]
        val rndPrice: Double = coffeeItem.price[rndSize] ?: error("Double was not found")
        val coffeeType: String = coffeeItem::class.simpleName.toString()
        this.coffeeOrder = CoffeeOrder(coffeeType, coffeeItem.name, rndSize, rndPrice, coffeeItem.brewTime)
    }

    private fun getRndCustomerName(): String {
        return if ((0..9).random() > 4) {
            RandomMaleNames.values()[(RandomMaleNames.values().indices).random()].name
        } else {
            RandomFemaleNames.values()[(RandomFemaleNames.values().indices).random()].name
        }
    }
}

enum class CustomerConstants{
    BEGIN,
    END
}