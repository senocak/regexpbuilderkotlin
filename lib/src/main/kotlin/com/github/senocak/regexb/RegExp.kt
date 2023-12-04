package com.github.senocak.regexb

import java.util.regex.Matcher
import java.util.regex.Pattern

class RegExp(expr: String, flags: String, pregMatchFlags: Int = 0) {
    private val _expr: String = expr
    private val _flags: String = flags
    private val _pregMatchFlags: Int = pregMatchFlags
    private var _method: String = "matches"

    init {
        if ("g" in _flags) {
            _method = "findIn"
            _method = _method.replace(oldValue = "g", newValue = "")
        }
    }

    override fun toString(): String = "RegExp(_expr='$_expr', _flags='$_flags', _pregMatchFlags=$_pregMatchFlags, _method='$_method')"

    val expression: String
        get() = _expr

    val regexFlags: String
        get() = _flags

    //fun matches(inputString0: String): Boolean = Pattern.matches(_expr, inputString0)

    fun matches(inputString0: String): Boolean =
        Pattern.compile(_expr).matcher(inputString0).find()

    fun findIn(inputString0: String): List<String> {
        val matcher: Matcher = Pattern.compile(_expr).matcher(inputString0)
        val result: MutableList<String> = mutableListOf()
        while (matcher.find()) {
            for (i: Int in 0 .. matcher.groupCount())
                result.add(element = matcher.group(i))
        }
        return result
    }

    fun replace(inputString0: String, callback: (String) -> Any?): String {
        return Pattern.compile(_expr).matcher(inputString0)
            .let { it: Matcher ->
                var result: String = inputString0
                while (it.find()) {
                    result = inputString0.replace(oldValue = it.group().toString(), newValue = callback(it.group()).toString(), ignoreCase = true)
                }
                result
            }
    }
}