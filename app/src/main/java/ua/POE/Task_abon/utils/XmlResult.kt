package ua.POE.Task_abon.utils

sealed class XmlResult{
    data class Success(val message : String) : XmlResult()
    data class Fail(val error : String) : XmlResult()
}
