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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class KotlinCallbacks {
    var customerQueue = CustomerQueue()

    init {

        var activeBarista =  BaristasOnDuty.baristaList[0]

        fun serveCustomers(){
            var currentCustomer = customerQueue.customers[0]

            activeBarista.currentCustomer = currentCustomer
            activeBarista.acceptOrder(AsyncCoffeeOrder.getCoffee())

            when (activeBarista.status) {
                Status.AVAILABLE.status -> activeBarista.doBrewCoffee {
                    customerQueue.customers.remove(currentCustomer)
                    println("ready for next customer")
                    var customer = Customer()
                    customerQueue.customers.add(10,customer)
                    serveCustomers() // <- recursive
                }
            }
        }

        serveCustomers()

    }
}

class CoffeeTypeQueue{
    companion object{
        var coffeeQueue: MutableList<CoffeeType> = mutableListOf(
            CoffeeType.AMERICANO,
            CoffeeType.LATTE,
            CoffeeType.CAPPUCCINO,
            CoffeeType.DRIP,
            CoffeeType.ESPRESSO,
        )
    }
}


class AsyncCoffeeOrder{
    companion object{
        fun getCoffee():CoffeeType{
            val rnd: Int = (0..4).random()
            return CoffeeTypeQueue.coffeeQueue[rnd]
        }
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

/**
 * CoffeeNewMaker
 */
class CoffeeNewMaker {

    fun brewCoffee(type: CoffeeType, callback: OnCoffeeBrewedListener) {
        //TODO add a delay then callback to the BaristaMan to tell him that the coffee is brewed
    }
}

/**
 * The baristas that are available to work at the café
 */
class BaristasOnDuty{

    companion object{

        var baristaList:MutableList<Barista> = mutableListOf(
            Barista("Jan"),
            Barista("Wally"),
            Barista("Mike"),
            Barista("Richard"),
            Barista("Kevin")
        )
    }
}//

/**
 * Barista
 * Every barista makes at least $15 at Mike's café
 */
class Barista(val name: String, val hrlyRate: Double = 15.00) : OnCoffeeBrewedListener {
    var status: String = Status.AVAILABLE.status
    var coffeeType: CoffeeType? = null
    lateinit var currentCustomer:Customer
    private val coffeeMaker = CoffeeNewMaker()

    fun acceptOrder(coffeeType: CoffeeType) {
        this.coffeeType = coffeeType
    }

    fun doBrewCoffee(callback: () -> Unit) {
        var brewTimeSimulator = BrewTimeSimulator()

        println("|-------- [barista] ${name} ------- has started brewing ${this.coffeeType?.name} for ${currentCustomer.name}")
        status = Status.BUSY.status
        GlobalScope.async {
            coffeeType?.let {
                brewTimeSimulator.brewCoffee(it.brewTime) { // this is a lambda function
                    onCoffeeBrewed(coffeeType!!)
                    callback() // back to the calling function to set up next order
                }
            }
        }
    }

    override fun onCoffeeBrewed(coffeeType: CoffeeType) {
        status = Status.AVAILABLE.status
        println("|--------[barista] ${name}------finished brewing ${coffeeType.name} for ${currentCustomer.name}")

    }
} // end barista class

/* ***************************************************************************************************
 *                                         utility classes
 * ***************************************************************************************************/

interface OnCoffeeBrewedListener {
    fun onCoffeeBrewed(coffeeType: CoffeeType)
}

/**
 * TODO maybe this should be a singleton?
 */
@RequiresApi(Build.VERSION_CODES.O)
class CustomerQueue{
    var customers:MutableList<Customer> = mutableListOf()

    init{
        setCustomers()
    }

    private fun setCustomers(){
        for(n in 0..10){
            customers.add(Customer())
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
class Customer{
    lateinit var name:String
    lateinit var  orderTime:LocalDate
    lateinit var coffeeType:CoffeeType
    lateinit var id:UUID

    init{
        setCustomerName()
        setOrderTime()
        setCoffeeType()
        setUUID()
    }

    private fun setCustomerName(){
        this.name = getRndCustomerName()
    }

    private fun setOrderTime(){
        this.orderTime = LocalDate.now()
    }

    private fun setCoffeeType(){
        val coffeeType:CoffeeType = CoffeeType.values()[(0..4).random()]
        this.coffeeType = coffeeType
    }

    private fun setUUID(){
        this.id = UUID.randomUUID()
    }

    private fun getRndCustomerName():String{
        return if((0..9).random() > 4){
            RandomMaleNames.values()[(RandomMaleNames.values().indices).random()].name
        }else{
            RandomFemaleNames.values()[(RandomFemaleNames.values().indices).random()].name
        }
    }
}


data class Coffee(val type: CoffeeType)

enum class CoffeeType(val brewTime: Long, val coffeeName: String) {
    AMERICANO(6750L, "Americano"),
    CAPPUCCINO(9500L, "Cappuccino"),
    DRIP(9000L, "Drip"),
    ESPRESSO(8000L, "Espresso"),
    LATTE(10000L, "Latte")
}

enum class Status(val status: String) {
    AVAILABLE("available"),
    BUSY("busy")
}

enum class CoffeeRoast {
    LIGHT,
    MEDIUM,
    DARK
}