package com.example.gpssportmap.domain

class GpsSession {
    var id: Int = 0
    var sportMapId: String = ""
    var name: String = ""
    var recordedAt: String = ""
    var duration: String = ""
    var speed: String = ""
    var distance: String = ""
    var climb: String = ""
    var descent: String = ""
    var paceMin: String = ""
    var paceMax: String = ""
    var type: String = ""
    var locationCount: Int = 0
    var userId: String = ""

    constructor(sportMapId: String, name: String, recordedAt: String, duration: String,
                speed: String, distance: String, climb: String, descent: String,
                paceMin: String, paceMax: String, type: String, locationCount: Int, userId: String):
            this(0, sportMapId, name, recordedAt, duration, speed, distance, climb, descent,
                paceMin, paceMax, type, locationCount, userId)

    constructor(id: Int, sportMapId: String, name: String, recordedAt: String, duration: String,
                speed: String, distance: String, climb: String, descent: String,
                paceMin: String, paceMax: String, type: String, locationCount: Int, userId: String) {
        this.id = id
        this.sportMapId = sportMapId
        this.name = name
        this.recordedAt = recordedAt
        this.duration = duration
        this.speed = speed
        this.distance = distance
        this.climb = climb
        this.descent = descent
        this.paceMin = paceMin
        this.paceMax = paceMax
        this.type = type
        this.locationCount = locationCount
        this.userId = userId
    }
}