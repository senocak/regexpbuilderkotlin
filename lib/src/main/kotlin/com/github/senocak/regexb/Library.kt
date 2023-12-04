/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.github.senocak.regexb

fun main() {
    validation()
    search()
    replace()
    validationMultiple()
}

private fun validation() {
    val regExp1: RegExp = RegExpBuilder()
        .startOfInput()
        .startOfInput()
        .exactly(n = 4).digits()
        .then(s = "_")
        .exactly(n = 2).digits()
        .then(s = "_")
        .min(n = 3).max(n = 10).letters()
        .then(s = ".")
        .anyOf(r = listOf("png", "jpg", "gif"))
        .endOfInput()
        .getRegExp()
        .also { it: RegExp ->
            // True
            println("-----------------regExp1-----------------")
            println(it.matches(inputString0 = "2020_10_hund.jpg"))
            println(it.matches(inputString0 = "2030_11_katze.png"))
            println(it.matches(inputString0 = "4000_99_maus.gif"))
            // False
            println(it.matches(inputString0 = "123_00_nein.gif"))
            println(it.matches(inputString0 = "4000_0_nein.pdf"))
            println(it.matches(inputString0 = "201505_nein.jpg"))
        }
}
private fun search() {
    val regExp2: RegExp = RegExpBuilder()
        .multiLine()
        .globalMatch()
        .min(n = 1).max(n = 10).anythingBut(s = " ")
        .anyOf(r = listOf(".pdf", ".doc"))
        .getRegExp()
        .also { it: RegExp ->
            val text = """Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
sed diam nonumy SomeFile.pdf eirmod tempor invidunt ut labore et dolore
magna aliquyam erat, sed diam voluptua. At vero eos et accusam lorem
et justo duo dolores et ea rebum. doc_04.pdf Stet clita kasd File.doc."""
            val matches: List<String> = it.findIn(inputString0 = text)
            println("-----------------regExp2-----------------")
            println(matches[0] == "SomeFile.pdf")
            println(matches[1] == "doc_04.pdf")
            println(matches[2] == "File.doc")
        }
}
private fun replace() {
    val regExp3: RegExp = RegExpBuilder()
        .min(n = 1)
        .max(n = 10)
        .digits()
        .getRegExp()
        .also { it: RegExp ->
            var text = "98 bottles of beer on the wall"
            text = it.replace(inputString0 = text) { match: String -> (match.toInt() + 1).toString() }
            println("-----------------regExp3-----------------")
            println("99 bottles of beer on the wall" == text)
        }
}
private fun validationMultiple() {
    val regExp41: RegExpBuilder = RegExpBuilder()
        .startOfInput()
        .exactly(n = 3).digits()
        .anyOf(r = listOf(".pdf", ".doc"))
        .endOfInput()

    val regExp42: RegExpBuilder = RegExpBuilder()
        .getNew()
        .startOfInput()
        .exactly(n = 4).letters()
        .then(s = ".jpg")
        .endOfInput()

    val regExp43: RegExp = RegExpBuilder()
        .getNew()
        .eitherFind(r = regExp41)
        .orFind(r = regExp42)
        .getRegExp()

    println("-----------------regExp4-----------------")
    //true
    println(regExp43.matches(inputString0 = "123.pdf"))
    println(regExp43.matches(inputString0 = "456.doc"))
    println(regExp43.matches(inputString0 = "bbbb.jpg"))
    println(regExp43.matches(inputString0 = "aaaa.jpg"))

    //false
    println(regExp43.matches(inputString0 = "1234.pdf"))
    println(regExp43.matches(inputString0 = "123.gif"))
    println(regExp43.matches(inputString0 = "aaaaa.jpg"))
    println(regExp43.matches(inputString0 = "456.docx"))
}
