```asciidoc
 ____            _____            ____        _ _     _          
|  _ \ ___  __ _| ____|_  ___ __ | __ ) _   _(_) | __| | ___ _ __
| |_) / _ \/ _` |  _| \ \/ / '_ \|  _ \| | | | | |/ _` |/ _ \ '__
|  _ <  __/ (_| | |___ >  <| |_) | |_) | |_| | | | (_| |  __/ |  
|_| \_\___|\__, |_____/_/\_\ .__/|____/ \__,_|_|_|\__,_|\___|_|  
           |___/           |_|                                   
```
## human-readable regular expressions for Jvm
[![release](https://img.shields.io/github/v/release/senocak/regexpbuilderkotlin.svg?style=flat-square)](https://img.shields.io/github/v/release/senocak/regexpbuilderkotlin/releases)
[![phpunit](https://img.shields.io/github/actions/workflow/status/senocak/regexpbuilderkotlin/main.yml?style=flat-square&color=lightgreen)](https://github.com/senocak/regexpbuilderphp/actions)
[![release](https://img.shields.io/badge/coverage-100%25-lightgreen?style=flat-square)](https://github.com/senocak/regexpbuilderkotlin/releases)
![release](https://img.shields.io/badge/much-wow-lightgreen?style=flat-square)

> RegExpBuilder integrates regular expressions into the programming language, thereby making them easy to read and maintain. Regular Expressions are created by using chained methods and variables such as arrays or strings.

## Installation

```bash
composer req senocak/regexpbuilderkotlin
```

Or download [the appropriate release](https://github.com/senocak/regexpbuilderkotlin/releases/latest)
and require [RegExpBuilder.kt](lib/src/main/kotlin/com/github/senocak/regexb/RegExpBuilder.kt) and [RegExp.kt](lib/src/main/kotlin/com/github/senocak/regexb/RegExp.kt)


## Examples

### Validation

```kotlin
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
            println(it.matches(inputString0 = "2020_10_hund.jpg"))
            println(it.matches(inputString0 = "2030_11_katze.png"))
            println(it.matches(inputString0 = "4000_99_maus.gif"))
            // False
            println(it.matches(inputString0 = "123_00_nein.gif"))
            println(it.matches(inputString0 = "4000_0_nein.pdf"))
            println(it.matches(inputString0 = "201505_nein.jpg"))
        }
```

### Search

```kotlin
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
        println(matches[0] == "SomeFile.pdf")
        println(matches[1] == "doc_04.pdf")
        println(matches[2] == "File.doc")
    }
```

### Replace

```kotlin
val regExp3: RegExp = RegExpBuilder()
    .min(n = 1)
    .max(n = 10)
    .digits()
    .getRegExp()
    .also { it: RegExp ->
        var text = "98 bottles of beer on the wall"
        text = it.replace(inputString0 = text) { match: String -> (match.toInt() + 1).toString() }
        println("99 bottles of beer on the wall" == text)
```

### Validation with multiple patterns

```kotlin
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
```

Take a look at the [tests](lib/src/test/kotlin/com/github/senocak/regexb/LibraryTest.kt) for more examples

# ALL Thanks to https://github.com/gherkins/regexpbuilderphp