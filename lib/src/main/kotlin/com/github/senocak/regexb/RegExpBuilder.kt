package com.github.senocak.regexb

import java.util.regex.Pattern

class RegExpBuilder {
    private var _flags: String = ""
    private var _pregMatchFlags: Int? = null
    private val _literal = mutableListOf<String>()
    private var _groupsUsed: Int = 0
    private var _min: Int = -1
    private var _max: Int = -1
    private var _of: String = ""
    private var _ofAny: Boolean = false
    private var _ofGroup: Int = -1
    private var _from: String = ""
    private var _notFrom: String = ""
    private var _like: String? = null
    private var _either: String? = null
    private var _reluctant: Boolean = false
    private var _capture: Boolean = false
    private var _captureName: String? = null

    init {
        clear()
    }

    private fun clear() {
        _min = -1
        _max = -1
        _of = ""
        _ofAny = false
        _ofGroup = -1
        _from = ""
        _notFrom = ""
        _like = null
        _either = null
        _reluctant = false
        _capture = false
    }

    private fun flushState() {
        if (_of.isNotEmpty() || _ofAny || _ofGroup > 0 || _from.isNotEmpty() || _notFrom.isNotEmpty() || _like != null) {
            val captureLiteral: String = when {
                _capture -> _captureName?.let { "?<$it>" } ?: ""
                else -> "?:"
            }
            val quantityLiteral: String = getQuantityLiteral()
            val characterLiteral: String? = getCharacterLiteral()
            val reluctantLiteral: String = when {
                _reluctant -> "?"
                else -> ""
            }
            _literal.add(element = "($captureLiteral(?:$characterLiteral)$quantityLiteral$reluctantLiteral)")
            clear()
        }
    }

    private fun getQuantityLiteral(): String {
        return when {
            _min != -1 && _max != -1 -> "{$_min,$_max}"
            _min != -1 -> "{$_min,}"
            else -> "{0,$_max}"
        }
    }

    private fun getCharacterLiteral(): String? {
        return when {
            _of.isNotEmpty() -> _of
            _ofAny -> "."
            _ofGroup > 0 -> "\\$_ofGroup"
            _from.isNotEmpty() -> "[$_from]"
            _notFrom.isNotEmpty() -> "[^$_notFrom]"
            _like != null -> _like
            else -> null
        }
    }

    fun getLiteral(): String {
        flushState()
        return _literal.joinToString(separator = "")
    }

    private fun combineGroupNumberingAndGetLiteralral(r: RegExpBuilder): String {
        val literal: String = incrementGroupNumbering(literal = r.getLiteral(), increment = _groupsUsed)
        _groupsUsed += r._groupsUsed
        return literal
    }

    fun incrementGroupNumbering(literal: String, increment: Int): String =
        when {
            increment > 0 -> {
                Regex(pattern = "\\\\(\\d+)").replace(input = literal) { result: MatchResult ->
                    val groupNumber: Int = result.value.substring(startIndex = 1).toInt() + increment
                    "\\$groupNumber"
                }
            }
            else -> literal
        }

    fun getRegExp(): RegExp =
        flushState()
            .run {
                RegExp(
                    expr = _literal.joinToString(separator = ""),
                    flags = _flags,
                    pregMatchFlags = _pregMatchFlags ?: 0)
            }

    private fun addFlag(flag: String): RegExpBuilder {
        if (!_flags.contains(other = flag)) {
            _flags += flag
        }
        return this
    }

    fun ignoreCase(): RegExpBuilder = addFlag(flag = "i")

    fun multiLine(): RegExpBuilder = addFlag(flag = "m")

    fun globalMatch(): RegExpBuilder = addFlag(flag = "g")

    fun pregMatchFlags(flags: Int): RegExpBuilder {
        _pregMatchFlags = flags
        return this
    }

    fun startOfInput(): RegExpBuilder {
        _literal.add(element = "(?:^)")
        return this
    }

    fun startOfLine(): RegExpBuilder = multiLine().run { startOfInput() }

    fun endOfInput(): RegExpBuilder {
        flushState()
        _literal.add(element = "(?:\$)")
        return this
    }

    fun endOfLine(): RegExpBuilder = multiLine().run { endOfInput() }

    fun eitherFind(r: String): RegExpBuilder = setEither(r = getNew().exactly(n = 1).of(s = r))

    fun eitherFind(r: RegExpBuilder): RegExpBuilder = setEither(r = r)

    private fun setEither(r: RegExpBuilder): RegExpBuilder {
        flushState()
        _either = combineGroupNumberingAndGetLiteralral(r = r)
        return this
    }

    fun orFind(r: String): RegExpBuilder = setOr(r = getNew().exactly(n = 1).of(s = r))

    fun orFind(r: RegExpBuilder): RegExpBuilder = setOr(r = r)

    fun anyOf(r: List<String>): RegExpBuilder =
        when {
            r.isEmpty() -> this
            else -> {
                eitherFind(r = r.first())
                r.drop(n = 1).forEach { orFind(r = it) }
                this
            }
        }

    //fun anyOf(r: List<Any>): RegExpBuilder =
    //    when {
    //        r.isEmpty() -> this
    //        else -> {
    //            val first: Any = r.first()
    //            when (first) {
    //                is String -> r.first()
    //                is  RegExpBuilder -> r.first()
    //            }
    //            //eitherFind(r = first)
    //            r.drop(n = 1).forEach{ it: Any ->
    //                when (it) {
    //                    is String -> orFind(r = it)
    //                    is  RegExpBuilder -> orFind(r = it)
    //                }
    //            }
    //            this
    //        }
    //    }

    private fun setOr(r: RegExpBuilder): RegExpBuilder {
        val or: String = combineGroupNumberingAndGetLiteralral(r = r)
        if (_either.isNullOrEmpty()) {
            val lastOr: String = _literal.last()
            _literal[_literal.size - 1] = lastOr.substring(startIndex = 0, endIndex = lastOr.length - 1)
            _literal.add(element = "|(?:$or))")
        } else {
            _literal.add(element = "(?:(?:$_either)|(?:$or))")
        }
        clear()
        return this
    }

    fun neither(r: String): RegExpBuilder = notAhead(r = getNew().exactly(n = 1).of(s = r))

    fun neither(r: RegExpBuilder): RegExpBuilder {
        notAhead(r = r)
        return min(n = 0).ofAny()
    }

    fun nor(r: String): RegExpBuilder {
        if (_min == 0 && _ofAny) {
            _min = -1
            _ofAny = false
        }
        neither(r = r)
        return min(n = 0).ofAny()
    }

    fun nor(r: RegExpBuilder): RegExpBuilder {
        if (_min == 0 && _ofAny) {
            _min = -1
            _ofAny = false
        }
        neither(r = r)
        return min(n = 0).ofAny()
    }

    fun exactly(n: Int): RegExpBuilder {
        flushState()
        _min = n
        _max = n
        return this
    }

    fun min(n: Int): RegExpBuilder {
        flushState()
        _min = n
        return this
    }

    fun max(n: Int): RegExpBuilder {
        flushState()
        _max = n
        return this
    }

    fun of(s: String): RegExpBuilder {
        _of = sanitize(s)
        return this
    }

    fun ofAny(): RegExpBuilder {
        _ofAny = true
        return this
    }

    fun ofGroup(n: Int): RegExpBuilder {
        _ofGroup = n
        return this
    }

    fun from(s: List<String>): RegExpBuilder {
        _from = sanitize(s = s.joinToString(separator = ""))
        return this
    }

    fun notFrom(s: List<String>): RegExpBuilder {
        _notFrom = sanitize(s = s.joinToString(separator = ""))
        return this
    }

    fun like(r: RegExpBuilder): RegExpBuilder {
        _like = combineGroupNumberingAndGetLiteralral(r = r)
        return this
    }

    fun reluctantly(): RegExpBuilder {
        _reluctant = true
        return this
    }

    fun ahead(r: RegExpBuilder): RegExpBuilder {
        flushState()
        _literal.add(element = "(?=${combineGroupNumberingAndGetLiteralral(r = r)})")
        return this
    }

    fun notAhead(r: RegExpBuilder): RegExpBuilder {
        flushState()
        _literal.add(element = "(?!${combineGroupNumberingAndGetLiteralral(r = r)})")
        return this
    }

    fun asGroup(name: String? = null): RegExpBuilder {
        _capture = true
        _captureName = name
        _groupsUsed++
        return this
    }

    fun then(s: String): RegExpBuilder = exactly(n = 1).of(s = s)

    fun find(s: String): RegExpBuilder = then(s = s)

    fun some(s: List<String>): RegExpBuilder = min(n = 1).from(s = s)

    fun maybeSome(s: List<String>): RegExpBuilder = min(n = 0).from(s = s)

    fun maybe(s: String): RegExpBuilder = max(n = 1).of(s = s)

    fun anything(): RegExpBuilder = min(n = 0).ofAny()

    fun anythingBut(s: String): RegExpBuilder =
        when (s.length) {
            1 -> min(n = 1).notFrom(s = listOf(s))
            else -> {
                notAhead(r = getNew().exactly(n = 1).of(s = s))
                min(n = 0).ofAny()
            }
        }

    fun something(): RegExpBuilder = min(n = 1).ofAny()

    fun any(): RegExpBuilder = exactly(n = 1).ofAny()

    fun lineBreak(): RegExpBuilder {
        flushState()
        _literal.add(element = "(?:\\r\\n|\\r|\\n)")
        return this
    }

    fun lineBreaks(): RegExpBuilder = like(r = getNew().lineBreak())

    fun whitespace(): RegExpBuilder =
        when {
            _min == -1 && _max == -1 -> {
                flushState()
                _literal.add(element = "(?:\\s)")
                this
            }
            else -> {
                _like = "\\s"
                this
            }
        }

    fun notWhitespace(): RegExpBuilder =
        when {
            _min == -1 && _max == -1 -> {
                flushState()
                _literal.add(element = "(?:\\S)")
                this
            }
            else -> {
                _like = "\\S"
                this
            }
        }

    fun tab(): RegExpBuilder {
        flushState()
        _literal.add(element = "(?:\\t)")
        return this
    }

    fun tabs(): RegExpBuilder = like(r = getNew().tab())

    fun digit(): RegExpBuilder {
        flushState()
        _literal.add(element = "(?:\\d)")
        return this
    }

    fun notDigit(): RegExpBuilder {
        flushState()
        _literal.add(element = "(?:\\D)")
        return this
    }

    fun digits(): RegExpBuilder = like(r = getNew().digit())

    fun notDigits(): RegExpBuilder = like(r = getNew().notDigit())

    fun letter(): RegExpBuilder {
        exactly(n = 1)
        _from = "A-Za-z"
        return this
    }

    fun notLetter(): RegExpBuilder {
        exactly(n = 1)
        _notFrom = "A-Za-z"
        return this
    }

    fun letters(): RegExpBuilder {
        _from = "A-Za-z"
        return this
    }

    fun notLetters(): RegExpBuilder {
        _notFrom = "A-Za-z"
        return this
    }

    fun lowerCaseLetter(): RegExpBuilder {
        exactly(n = 1)
        _from = "a-z"
        return this
    }

    fun lowerCaseLetters(): RegExpBuilder {
        _from = "a-z"
        return this
    }

    fun upperCaseLetter(): RegExpBuilder {
        exactly(n = 1)
        _from = "A-Z"
        return this
    }

    fun upperCaseLetters(): RegExpBuilder {
        _from = "A-Z"
        return this
    }

    fun append(r: RegExpBuilder): RegExpBuilder {
        exactly(n = 1)
        _like = combineGroupNumberingAndGetLiteralral(r = r)
        return this
    }

    fun optional(r: RegExpBuilder): RegExpBuilder {
        max(n = 1)
        _like = combineGroupNumberingAndGetLiteralral(r = r)
        return this
    }

    private fun sanitize(s: String): String = Pattern.quote(s)

    fun getNew(): RegExpBuilder = this.javaClass.getDeclaredConstructor().newInstance() as RegExpBuilder
}