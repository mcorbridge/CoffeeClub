/*
 * Copyright (c) 2021. Michael D. Corbridge
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Michael D. Corbridge. The intellectual and technical concepts contained herein are proprietary to Michael D. Corbridge and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained from Michael D. Corbridge.
 */

package com.mcorbridge.kotlinfirebase.callbacks

class LambdaPlay {


    init{
        val a:Double = (0..500).random().toDouble()
        val b:Double = (0..500).random().toDouble()
        val c:Double = (0..500).random().toDouble()
        var gnu = Gnu()
        val sum = gnu.sum(a, b, c)
        println("|------------------- $sum")
    }
}


class Gnu{
    //variable as a lambda
    var sum: (Double,Double, Double) -> Double = {a,b,c -> ((a+b)*(a*b)/(a-b)*c)}
}