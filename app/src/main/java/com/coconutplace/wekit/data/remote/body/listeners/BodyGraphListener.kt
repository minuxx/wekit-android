package com.coconutplace.wekit.data.remote.body.listeners

import com.coconutplace.wekit.data.entities.BodyGraph

interface BodyGraphListener {
    fun onBodyGraphStarted()
    fun onBodyGraphSuccess(bodyGraph: BodyGraph)
    fun onBodyGraphFailure(code: Int, message: String)
}