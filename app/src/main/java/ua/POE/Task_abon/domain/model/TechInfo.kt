package ua.POE.Task_abon.domain.model

data class TechInfo(
    val zoneCount: String,
    val lastDate: String,
    val lastCount: String,
    val averageUsage: String,
    val type: String,
    val capacity: String,
    val checkDate: String,
    val inspector: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}
