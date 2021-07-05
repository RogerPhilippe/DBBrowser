package br.com.phs.dbcore

import java.io.Serializable

data class ResultObject(
    var columns: Array<String> = arrayOf(),
    var result: Array<Array<Any>> = arrayOf(),
    var hasError: Boolean = false,
    var errorMsg: String = "",
    var resultMsg: String = "",
    var resultType: ResultTypeEnum = ResultTypeEnum.UNKNOWN
):Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResultObject

        if (!columns.contentEquals(other.columns)) return false
        if (!result.contentDeepEquals(other.result)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = columns.contentHashCode()
        result1 = 31 * result1 + result.contentDeepHashCode()
        return result1
    }
}