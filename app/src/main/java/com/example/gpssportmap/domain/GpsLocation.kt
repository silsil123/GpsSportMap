package com.example.gpssportmap.domain

class GpsLocation {
    var id: Int = 0
    var sportMapId: String = ""
    var recordedAt: String = ""
    var latitude: String = ""
    var longitude: String = ""
    var accuracy: String = ""
    var altitude: String = ""
    var verticalAccuracy: String = ""
    var userId: String = ""
    var sessionId: String = ""
    var typeId: String = ""

    constructor(sportMapId: String, recordedAt: String, latitude: String, longitude: String,
                accuracy: String, altitude: String, verticalAccuracy: String, userId: String,
                sessionId: String, typeId: String):
            this(0, sportMapId, recordedAt, latitude, longitude, accuracy, altitude, verticalAccuracy,
                userId, sessionId, typeId)

    constructor(id: Int, sportMapId: String, recordedAt: String, latitude: String, longitude: String,
                accuracy: String, altitude: String, verticalAccuracy: String, userId: String,
                sessionId: String, typeId: String) {
        this.id = id
        this.sportMapId = sportMapId
        this.recordedAt = recordedAt
        this.latitude = latitude
        this.longitude = longitude
        this.accuracy = accuracy
        this.altitude = altitude
        this.verticalAccuracy = verticalAccuracy
        this.userId = userId
        this.sessionId = sessionId
        this.typeId = typeId
    }
}