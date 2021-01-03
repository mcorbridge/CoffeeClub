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
import java.time.LocalDate
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class CafeMike {
    var customerQueue = CustomerQueue()
    var cafeReceipts:Double = 0.0
    private var ndx = 0
    val dec = DecimalFormat("$#,###.00")
    private var startTime:Long = 0

    init {

        var jan = BaristasOnDuty.baristaList[0]
        var wally = BaristasOnDuty.baristaList[1]
        var mike = BaristasOnDuty.baristaList[2]
        var richard = BaristasOnDuty.baristaList[3]
        var kevin = BaristasOnDuty.baristaList[4]

        fun getStartTime(){
            startTime = System.currentTimeMillis()
        }

        fun serveCustomers(barista: Barista) {

            println(">>>>>>>>>>>>>>>> TOTAL customers served: ${ndx++}")

            var currentCustomer = customerQueue.getAvailableCustomer()

            if (currentCustomer != null) {
                currentCustomer.isServed = true
                barista.currentCustomer = currentCustomer
            }else{
                println("|--------------- There are no available customers!")
            }

            barista.doBrewCoffee { // lambda callback with time delay

                if (currentCustomer != null) {
                    cafeReceipts += currentCustomer.coffeePrice
                    customerQueue.removeCustomer(currentCustomer)
                }

                var interimTime = System.currentTimeMillis()
                var elapsedTime = (interimTime - startTime)

                customerQueue.addSingleCustomer()
                println("|---------------- ${barista.name} is ready for next customer [${dec.format(cafeReceipts)}] [${getElapsedTime(elapsedTime)}]")



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

        getStartTime()

        GlobalScope.launch {
            BaristaOffset.introduceDelay(5000L) {
                serveCustomers(jan)
            }
        }

        GlobalScope.launch {
            BaristaOffset.introduceDelay(5500L) {
                serveCustomers(wally)
            }
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

    private fun getElapsedTime(millisec:Long):String {
        var sec: Long = millisec / 1000
        var min: Long = (millisec / 1000) / 60
        var hour: Long = ((millisec / 1000) / 60) / 60
        println("sec: $sec min:$min hour:$hour")
        var strElapsed = if (hour >= 1) {
            "$hour hrs"
        } else if (min >= 1) {
            "$min mins"
        } else {
            "$sec secs"
        }
        return strElapsed
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
 * Every barista makes at least $15/hr at Mike's café
 */
class Barista(val name: String, val hrlyRate: Double = 15.00) : OnCoffeeBrewedListener {

    var status: Status = Status.AVAILABLE
    var coffeeType: CoffeeType? = null
    lateinit var currentCustomer: Customer

    fun doBrewCoffee(callback: () -> Unit) {

        val brewTimeSimulator = BrewTimeSimulator()
        this.coffeeType = currentCustomer.coffeeOrder.type

        println("|-------- [barista] ${name} ------- has started brewing ${currentCustomer.coffeeOrder.size} ${currentCustomer.coffeeOrder.roast} ${this.coffeeType?.name} for ${currentCustomer.name}")

        status = Status.BUSY

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
        status = Status.AVAILABLE
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
    private lateinit var coffeeType: CoffeeType
    private lateinit var coffeeSize: CoffeeSize
    private lateinit var coffeeRoast: CoffeeRoast
    lateinit var coffeeOrder:Coffee
    var coffeePrice:Double = 0.0
    lateinit var id: UUID
    var isServed:Boolean = false

    init {
        setCustomerName()
        setOrderTime()
        setCoffeeType()
        setCoffeeRoast()
        setCoffeeSize()
        setCoffeeOrder()
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
        this.coffeePrice += coffeeType.basePrice
    }

    private fun setCoffeeSize() {
        val coffeeSize: CoffeeSize = CoffeeSize.values()[(0..2).random()]
        this.coffeeSize = coffeeSize
        this.coffeePrice += coffeeSize.price
    }

    private fun setCoffeeRoast() {
        val coffeeRoast: CoffeeRoast = CoffeeRoast.values()[(0..2).random()]
        this.coffeeRoast = coffeeRoast
        this.coffeePrice += coffeeRoast.price
    }

    private fun setUUID() {
        this.id = UUID.randomUUID()
    }

    private fun setCoffeeOrder(){
        this.coffeeOrder = Coffee()
        this.coffeeOrder.type = this.coffeeType
        this.coffeeOrder.size = this.coffeeSize
        this.coffeeOrder.roast = this.coffeeRoast
    }

    private fun getRndCustomerName(): String {
        return if ((0..9).random() > 4) {
            RandomMaleNames.values()[(RandomMaleNames.values().indices).random()].name
        } else {
            RandomFemaleNames.values()[(RandomFemaleNames.values().indices).random()].name
        }
    }
}

/**
 *
 */
class Coffee{
    lateinit var type:CoffeeType
    lateinit var size:CoffeeSize
    lateinit var roast:CoffeeRoast
}

/* ***************************************************************************************************
 *                                         utility classes
 * ***************************************************************************************************/

interface OnCoffeeBrewedListener {
    fun onCoffeeBrewed(coffeeType: CoffeeType)
}

/**
 * TODO maybe this should be a singleton? (or static - companion object - at least)
 * TODO add to this queue 'organically' by that I mean the customers being added to the queue
 * TODO is independent of the baristas.
 */
@RequiresApi(Build.VERSION_CODES.O)
class CustomerQueue {
    var customers: MutableList<Customer> = mutableListOf()

    init {
        setInitialCustomers()
    }

    private fun setInitialCustomers() {
        for (n in 0..10) {
            customers.add(Customer())
        }
    }

    fun addSingleCustomer(){
        customers.add(9, Customer())
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

enum class CoffeeType(val brewTime: Long, val coffeeName: String, val basePrice:Double) {
    AMERICANO(10000L, "Americano", 2.60),
    CAPPUCCINO(30000L, "Cappuccino", 3.40),
    DRIP(5000L, "Drip", 1.60),
    ESPRESSO(25000L, "Espresso", 2.70),
    LATTE(40000L, "Latte", 3.60)
}

enum class Status() {
    AVAILABLE,
    BUSY
}

enum class CoffeeRoast(val price: Double) {
    LIGHT(0.25),
    MEDIUM(0.50),
    DARK(0.70)
}

enum class CoffeeSize(val price:Double) {
    SMALL(0.00),
    MEDIUM(0.50),
    LARGE(1.00)
}