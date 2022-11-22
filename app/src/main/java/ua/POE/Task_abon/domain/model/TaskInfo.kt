package ua.POE.Task_abon.domain.model


data class TaskInfo(
    val id: Int = -1,
    val name: String = "",
    val date: String= "",
    val count: String= "",
    val filial: String= "",
    val fileName: String= "",
    val isJur: String? = "0"
)
