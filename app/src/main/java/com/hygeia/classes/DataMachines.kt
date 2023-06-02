package com.hygeia.classes

class DataMachines {
    var name: String = ""
    var location: String = ""

    // Default constructor
    constructor()

    // Additional constructor with fields
    constructor(name: String, location: String) {
        this.name = name
        this.location = location
    }
}