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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class KotlinCallbacks {
    var customerQueue = CustomerQueue()

    var ndx = 0

    init {

        var jan = BaristasOnDuty.baristaList[0]
        var wally = BaristasOnDuty.baristaList[1]
        var mike = BaristasOnDuty.baristaList[2]
        var richard = BaristasOnDuty.baristaList[3]
        var kevin = BaristasOnDuty.baristaList[4]


        fun serveCustomers(barista: Barista) {

            println(">>>>>>>>>>>>>>>> total customers served: ${ndx++}")

            var currentCustomer = customerQueue.getAvailableCustomer()

            if (currentCustomer != null) {
                currentCustomer.isServed = true
                barista.currentCustomer = currentCustomer
            }else{
                println("|--------------- There are no available customers!")
            }

            barista.doBrewCoffee { // lambda callback with time delay
                if (currentCustomer != null) {
                    customerQueue.removeCustomer(currentCustomer)
                }

                customerQueue.addSingleCustomer()
                println("|---------------- ${barista.name} is ready for next customer")

                var serveOffset = when(barista.name){
                    "Jan" -> 1000L
                    "Wally" -> 2000L
                    "Mike" -> 3000L
                    "Richard" -> 4000L
                    "Kevin" -> 5000L
                    else -> 500L
                }

                GlobalScope.launch {
                    delay(serveOffset)
                    serveCustomers(barista)// <- recursive
                }
            }
        }

        GlobalScope.launch {
            delay(5000L)
            serveCustomers(jan)
        }

        GlobalScope.launch {
            delay(5500L)
            serveCustomers(wally)
        }

        GlobalScope.launch {
            BaristaOffset.introduceDelay(6000L) {
                serveCustomers(mike)
            }
        }

        GlobalScope.launch {
            BaristaOffset.introduceDelay(6500L) {
                serveCustomers(richard)
            }
        }

        GlobalScope.launch {
            BaristaOffset.introduceDelay(7000L) {
                serveCustomers(kevin)
            }
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

class BaristaOffset {
    companion object {
        suspend fun introduceDelay(delay: Long, callback: () -> Unit) {
            delay(delay)
            callback()
        }
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
class BaristasOnDuty {

    companion object {

        var baristaList: MutableList<Barista> = mutableListOf(
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
    lateinit var currentCustomer: Customer
    private val coffeeMaker = CoffeeNewMaker() // TODO <- not sure what this is?

    fun doBrewCoffee(callback: () -> Unit) {
        val brewTimeSimulator = BrewTimeSimulator()
        this.coffeeType = currentCustomer.coffeeType

        println("|-------- [barista] ${name} ------- has started brewing ${this.coffeeType?.name} for ${currentCustomer.name}")

        status = Status.BUSY.status

        GlobalScope.launch {
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

/**
 * Customer for Mike's Café
 */
@RequiresApi(Build.VERSION_CODES.O)
class Customer {
    lateinit var name: String
    lateinit var orderTime: LocalDate
    lateinit var coffeeType: CoffeeType
    lateinit var id: UUID
    var isServed:Boolean = false

    init {
        setCustomerName()
        setOrderTime()
        setCoffeeType()
        setUUID()
    }

    private fun setCustomerName() {
        this.name = getRndCustomerName()
    }

    private fun setOrderTime() {
        this.orderTime = LocalDate.now()
    }

    private fun setCoffeeType() {
        val coffeeType: CoffeeType = CoffeeType.values()[(0..4).random()]
        this.coffeeType = coffeeType
    }

    private fun setUUID() {
        this.id = UUID.randomUUID()
    }

    private fun getRndCustomerName(): String {
        return if ((0..9).random() > 4) {
            RandomMaleNames.values()[(RandomMaleNames.values().indices).random()].name
        } else {
            RandomFemaleNames.values()[(RandomFemaleNames.values().indices).random()].name
        }
    }
}

/* ***************************************************************************************************
 *                                         utility classes
 * ***************************************************************************************************/

interface OnCoffeeBrewedListener {
    fun onCoffeeBrewed(coffeeType: CoffeeType)
}

/**
 * TODO maybe this should be a singleton? (or static - companion object - at least)
 */
@RequiresApi(Build.VERSION_CODES.O)
class CustomerQueue {
    var customers: MutableList<Customer> = mutableListOf()

    init {
        setCustomers()
    }

    private fun setCustomers() {
        for (n in 0..10) {
            customers.add(Customer())
        }
    }

    fun addSingleCustomer(){
        customers.add(10, Customer())
    }

    fun removeCustomer(customer: Customer){
        customers.remove(customer)
    }

    fun getAvailableCustomer():Customer?{
        var availableCustomer: Customer? = null
        for(customer in customers){
              if(!customer.isServed){
                  availableCustomer = customer
                  availableCustomer.isServed = true
                  break
            }
        }
        if (availableCustomer != null) {
            println("|--------availableCustomer.name--------- ${availableCustomer.name} [${customers.size}]")
        }
        return availableCustomer
    }

}



enum class CoffeeType(val brewTime: Long, val coffeeName: String) {
    AMERICANO(10000L, "Americano"),
    CAPPUCCINO(30000L, "Cappuccino"),
    DRIP(5000L, "Drip"),
    ESPRESSO(25000L, "Espresso"),
    LATTE(40000L, "Latte")
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