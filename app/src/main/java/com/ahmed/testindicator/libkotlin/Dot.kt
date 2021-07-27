package com.ahmed.testindicator.libkotlin

class Dot {
    enum class State {
        SMALL, MEDIUM, INACTIVE, ACTIVE
    }

    fun setCurrentState(state: State){
        this.state = state
    }

    fun getCurrentState(): State{
        return state
    }

    var state: State = State.MEDIUM
}