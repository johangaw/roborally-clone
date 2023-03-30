package ui

enum class PlayerNumber(val number: Int) {
    Zero(0),
    One(1),
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
}

fun playerNumber(number: Int) = PlayerNumber.values().first {it.number == number}
