package ua.POE.Task_abon.presentation.model

data class Technical(
    var zoneCount: String,
    var lastDate: String,
    var lastCount: String,
    var averageUsage: String,
    var type: String,
    var capacity: String,
    var checkDate: String,
    var inspector: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}
