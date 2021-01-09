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
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.properties.Delegates

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
    var cafeReceipts: Double = 0.0
    private var ndx = 0
    val dec = DecimalFormat("$#,###.00")
    private var startTime: Long = 0L

    init {

        val jan = BaristasOnDuty.baristaList[0]
        val wally = BaristasOnDuty.baristaList[1]
        val mike = BaristasOnDuty.baristaList[2]
        var richard = BaristasOnDuty.baristaList[3]
        var kevin = BaristasOnDuty.baristaList[4]

        fun getStartTime() {
            startTime = System.currentTimeMillis()
        }

        fun serveCustomers(barista: Barista) {

            var currentCustomer = CustomerQueue.getAvailableCustomer()

            // might not need this, but this is the set of baristas currently 'working' out of the available 'pool'
            BaristaStatus.addBaristaOnDuty(barista)

            // prevent all available baristas rushing to get the next available customer which would cause 'collisions'
            val serveOffset = when (barista.name) {
                "Jan" -> 1000L
                "Wally" -> 2000L
                "Mike" -> 3000L
                "Richard" -> 4000L
                "Kevin" -> 5000L
                else -> 500L
            }

            // offset baristas being added to the IDLE loop so they are not activated at the same time when a customer is added
            val loopOffset = when (barista.name) {
                "Jan" -> 1000L
                "Wally" -> 5000L
                "Mike" -> 3000L
                "Richard" -> 4000L
                "Kevin" -> 5000L
                else -> 500L
            }

            if (currentCustomer != null) {
                println("|-----------serveCustomers-----------------> ${barista.name} -> ${currentCustomer.name} [${CustomerQueue.customers.size}] ")
            }

            // send the barista into an IDLE loop, waiting for customer to be added to queue
            if(CustomerQueue.customers.size == 0){
                println("---------------- send ${barista.name} back into Idle Loop")
                barista.doIdleLoop(loopOffset) {
                    GlobalScope.launch {
                        delay(serveOffset)
                        serveCustomers(barista)// <- recursive
                    }
                }
            }

            if (currentCustomer != null) {
                currentCustomer.isServed = true
                barista.currentCustomer = currentCustomer
                CustomerQueue.removeCustomer(currentCustomer)
            }

            barista.doBrewCoffee { // lambda callback with time delay

                val interimTime = System.currentTimeMillis()
                val elapsedTime = (interimTime - startTime)

                if (currentCustomer != null) {
                    cafeReceipts += currentCustomer!!.coffeeOrder.price
                    println("${ndx++} customers served: ${getAcceleratedTime(elapsedTime)}")
                    currentCustomer!!.orderTime = elapsedTime
                    currentCustomer = null
                }

                if (CustomerQueue.customers.size == 0) {
                    barista.doIdleLoop(loopOffset) {
                        GlobalScope.launch {
                            delay(serveOffset)
                            serveCustomers(barista)// <- recursive
                        }
                    }
                } else {
                    GlobalScope.launch {
                        delay(serveOffset)
                        serveCustomers(barista)// <- recursive
                    }
                }

            }

        } // end serveCustomers

        // baristas are activated/deactivated by uncommenting/commenting their thread

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

        /*GlobalScope.launch {
            BaristaOffset.introduceDelay(6500L) {
                serveCustomers(richard)
            }
        }*/

        /*GlobalScope.launch {
            BaristaOffset.introduceDelay(7000L) {
                serveCustomers(kevin)
            }
        }*/

        // TODO maybe move this out into it's own class?
        fun startCustomersWalkingInTheDoor() {
            val kotlinTimer = Timer()
            kotlinTimer.scheduleAtFixedRate(timerTask {
                val rnd = (0..9).random()
                if(rnd == 8){ // a random 'blast' of customers
                    CustomerQueue.addSingleCustomer()
                    CustomerQueue.addSingleCustomer()
                    CustomerQueue.addSingleCustomer()
                    println("+++")
                }
                else if (rnd >= 7) {
                    CustomerQueue.addSingleCustomer()
                    println("+")
                }

            }, 5000, 5000)


        }

        getStartTime()
        startCustomersWalkingInTheDoor()

    } // end init

    private fun getAcceleratedTime(elapsedTimeMSEC: Long): String {

        var accelTime: Long = elapsedTimeMSEC * 36L

        var strElapsedTime = String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(accelTime),
            TimeUnit.MILLISECONDS.toMinutes(accelTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(accelTime)), // The change is in this line
            TimeUnit.MILLISECONDS.toSeconds(accelTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(accelTime)));

        return strElapsedTime
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
 *
 */
class BaristaOffset {
    companion object {
        suspend fun introduceDelay(delay: Long, callback: () -> Unit) {
            delay(delay)
            callback()
        }
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
    var currentCustomer: Customer? = null
    var currentlyOnDuty:Boolean = false
    var isIdle:Boolean = false

    /**
     *
     */
    fun doBrewCoffee(callback: () -> Unit) {

        val brewTimeSimulator = BrewTimeSimulator()
        val currentCustomer = this.currentCustomer

        status = Status.BUSY

        GlobalScope.launch {
            if (currentCustomer != null) {
                brewTimeSimulator.brewCoffee(currentCustomer.coffeeOrder.brewTime) { // this is a lambda function
                    onCoffeeBrewed(currentCustomer.coffeeOrder)
                    callback() // back to the calling function to set up next order
                }
            }
        }
    }

    /**
     *
     */
    fun doIdleLoop(offset:Long, callback: () -> Unit) {
        isIdle = true
        var barista = this
        BaristaStatus.baristaIdle(this,"ADD")
        currentCustomer = null
        var kounter = 0
        val kotlinTimer = Timer()
        kotlinTimer.scheduleAtFixedRate(timerTask {
            if (CustomerQueue.customers.size > 0) {
                BaristaStatus.baristaIdle(barista,"REMOVE")
                kotlinTimer.cancel()
                callback()
            }
        }, offset, 5000)
    }

    override fun onCoffeeBrewed(coffeeOrder: CoffeeOrder) {
        status = Status.AVAILABLE
    }
} // end barista class

/**
 * Customer for Mike's Café
 */
@RequiresApi(Build.VERSION_CODES.O)
class Customer {
    lateinit var name: String
    var orderTime by Delegates.notNull<Long>()
    lateinit var coffeeOrder: CoffeeOrder
    lateinit var id: UUID
    var isServed: Boolean = false

    init {
        setCustomerName()
        setOrderTime()
        setCoffeeOrder()
        setUUID()
    }

    private fun setCustomerName() {
        this.name = getRndCustomerName()
    }

    private fun setOrderTime() {
        this.orderTime = System.currentTimeMillis()
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

class CoffeeOrder(val name: String, val type: String, val size: String, val price: Double, val brewTime: Long) {

}

/* ***************************************************************************************************
 *                                         utility classes
 * ***************************************************************************************************/

interface OnCoffeeBrewedListener {
    fun onCoffeeBrewed(coffeeOrder: CoffeeOrder)
}

/**
 * TODO maybe this should be a singleton? (or static - companion object - at least)
 * TODO add to this queue 'organically' by that I mean the customers being added to the queue
 * TODO is independent of the baristas.
 */


enum class Status() {
    AVAILABLE,
    BUSY
}
