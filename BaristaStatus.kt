/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

object BaristaStatus {

    var baristasOnDuty:MutableList<Barista> = mutableListOf()
    var baristasIdle:MutableList<Barista> = mutableListOf()

    fun addBaristasOnDuty(){
        baristasOnDuty.add(Barista("Jan"))
        baristasOnDuty.add(Barista("Wally"))
        baristasOnDuty.add(Barista("Mike"))
        baristasOnDuty.add(Barista("Richard"))
        baristasOnDuty.add(Barista("Kevin"))
    }

    fun addBaristasIdle(){
        baristasIdle.add(Barista("Jan"))
        baristasIdle.add(Barista("Wally"))
        baristasIdle.add(Barista("Mike"))
        baristasIdle.add(Barista("Richard"))
        baristasIdle.add(Barista("Kevin"))
    }
    
    fun setBaristaStatus(barista: Barista, action:BaristaConstants){
        if(action == BaristaConstants.IDLE){
            println("${barista.name} IS NOW ${BaristaConstants.IDLE}")
            if(!baristasIdle.contains(barista)){
                baristasIdle.add(barista)
                showIdleBaristas()
            }
        }else if(action == BaristaConstants.ACTIVE){
            println("${barista.name} IS NOW ${BaristaConstants.ACTIVE}")
            baristasIdle.remove(barista)
            showIdleBaristas()
        }
    }

    fun showIdleBaristas(){
        print("\n:: ")
        baristasIdle.forEach {
            print(" ${it.name} | ")
        }
        println("")
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

enum class BaristaConstants{
    IDLE,
    ACTIVE,
}
