package com.example.gpssportmap.domain

class GpsLocationType {
    var id: Int = 0
    var name: String = ""
    var description: String = ""

    constructor(name:String, description: String): this(0, name, description)

    constructor(id: Int, name: String, description: String) {
        this.id = id
        this.name = name
        this.description = description
    }
}