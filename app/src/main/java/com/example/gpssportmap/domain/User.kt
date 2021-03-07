package com.example.gpssportmap.domain

class User {
    var id: Int = 0;
    var sportMapId: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var userName: String = ""

    constructor(sportMapId: String, firstName: String, lastName: String, userName: String):
            this (0, sportMapId, firstName, lastName, userName)

    constructor(id: Int, sportMapId: String, firstName: String, lastName: String, userName: String) {
        this.id = id
        this.sportMapId  = sportMapId
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
    }
}