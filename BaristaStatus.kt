/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

object BaristaStatus {


    var listBaristasOnDuty:MutableList<Barista> = mutableListOf()
    var listBaristasIdle:MutableList<Barista> = mutableListOf()

    fun addBaristaOnDuty(barista: Barista){
        if(!listBaristasOnDuty.contains(barista)){
            barista.currentlyOnDuty = true
            listBaristasOnDuty.add(barista)
        }
    }

    fun baristaIdle(barista: Barista, action:String){
        if(action == "ADD"){
            println("ADD ${barista.name} AS IDLE")
            listBaristasIdle.add(barista)
        }else if(action == "REMOVE"){
            println("REMOVE ${barista.name} AS IDLE")
            listBaristasIdle.remove(barista)
        }
    }

}