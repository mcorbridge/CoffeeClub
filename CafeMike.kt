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
import kotlin.system.exitProcess

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

        CafeStatus.currentCafeStatus = CafeStatus.OPEN
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

            // Collision?? Then send the barista into an IDLE loop, waiting for a customer to be added to queue
            if(CustomerQueue.customers.size == 0){
                println("---------------- send ${barista.name} back into Idle Loop")
                barista.setBaristaIdle(loopOffset) {
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

                if( !((CustomerQueue.customers.size == 0) && (CafeStatus.currentCafeStatus == CafeStatus.CLOSED)) ){
                    if (CustomerQueue.customers.size == 0) {
                        barista.setBaristaIdle(loopOffset) {
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
                } else {
                    println("THAT'S IT FOLKS.  CALL IT A DAY.  WE'RE FINISHED")
                    // TODO do summary
                    exitProcess(-1)
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

        /*GlobalScope.launch {
            BaristaOffset.introduceDelay(6000L) {
                serveCustomers(mike)
            }
        }*/

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

        getStartTime()
        CustomerGenerator.startCustomersWalkingInTheDoor()
        CustomerQueue.checkCurrentQueue()

    } // end init

    // TODO investigate further increasing the time acceleration
    private fun getAcceleratedTime(elapsedTimeMSEC: Long): String {

        var durationOpen:Long = 1L * 60L * 60L * 1000L // currently set to virtual 1 (ONE) hour - 1L
        var accelTime: Long = elapsedTimeMSEC * 10L // <- the process is sped up factor of 10

        if(accelTime >= durationOpen){
            CafeStatus.currentCafeStatus = CafeStatus.CLOSED
            println("STORE IS NOW ${CafeStatus.CLOSED}")
        }

        var strElapsedTime = String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(accelTime),
            TimeUnit.MILLISECONDS.toMinutes(accelTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(accelTime)),
            TimeUnit.MILLISECONDS.toSeconds(accelTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(accelTime)));

        return "$strElapsedTime (HH:mm:ss)"
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

    var status: String = BaristaStatus.AVAILABLE
    var currentCustomer: Customer? = null
    var currentlyOnDuty:Boolean = false
    var isIdle:Boolean = false
    var serverCallback: () -> Unit = {}

    fun doBrewCoffee(callback: () -> Unit) {

        val brewTimeSimulator = BrewTimeSimulator()
        val currentCustomer = this.currentCustomer

        status = BaristaStatus.BUSY

        GlobalScope.launch {
            if (currentCustomer != null) {
                brewTimeSimulator.brewCoffee(currentCustomer.coffeeOrder.brewTime) { // this is a lambda function
                    onCoffeeBrewed(currentCustomer.coffeeOrder)
                    callback()

                }
            }
        }
    }

    // TODO offset required to prevent collisions?
    fun setBaristaActive(){
        BaristaStatus.baristaIdle(this,"REMOVE")
        serverCallback()
    }

    //TODO might be moving this logic to the CustomerQueue class?
    @RequiresApi(Build.VERSION_CODES.O)
    fun setBaristaIdle(offset:Long, callback: () -> Unit) {
        isIdle = true
        var barista = this
        BaristaStatus.baristaIdle(this,"ADD")
        currentCustomer = null
        serverCallback = callback

        // TODO what I want to do here is setup a callback from CustomerQueue so that if it
        // TODO sees that THIS barista is IDLE and customers are waiting (in the queue) then
        // TODO it will send THIS barista back into the fray
        /*CustomerQueue.baristaIdle(this) {
            //serverCallback()// <- this callback takes the barista back to the serve customers
            println("CustomerQueue.baristaIdle: $name")
        }*/

        // TODO this is going away?
        /*val kotlinTimer = Timer()
        kotlinTimer.scheduleAtFixedRate(timerTask {
            if (CustomerQueue.customers.size > 0) {
                BaristaStatus.baristaIdle(barista,"REMOVE")
                kotlinTimer.cancel()
                serverCallback() // <- this callback takes the barista back to the serve customers
            }
        }, offset, 5000)*/
    }

    override fun onCoffeeBrewed(coffeeOrder: CoffeeOrder) {
        status = BaristaStatus.AVAILABLE
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

/*enum class BaristaServeStatus() {
    AVAILABLE,
    BUSY
}*/

