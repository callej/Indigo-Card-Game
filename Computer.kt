package indigo

fun cInitializeGame(): InitGame {
    println("Indigo Card Game")
    var userPlayFirst = ""
    while (userPlayFirst.lowercase() != "yes" && userPlayFirst.lowercase() != "no") {
        println("Play first?")
        userPlayFirst = readln()
    }
    val deck = Deck("init")
    val user = Player()
    deck.giveCards(6, user)
    val computer = Player()
    deck.giveCards(6, computer)
    val table = Player()
    deck.giveCards(4, table)
    println("Initial cards on the table: ${table.showCards()}")
    return InitGame(if (userPlayFirst.lowercase() == "yes") 0 else 1, deck, user, computer, table, true)
}

fun cDisplayScore(user: Player, computer: Player) {
    println("Score: Player ${user.score()} - Computer ${computer.score()}")
    println("Cards: Player ${user.wonCards()} - Computer ${computer.wonCards()}")
}

fun cFinalScore(state: State, firstPlayer: Int, user: Player, computer: Player, table: Player) {
    if (table.numberOfCards() == 0) {
        println("\nNo cards on the table")
    } else {
        println("\n${table.numberOfCards()} cards on the table, and the top card is ${table.topCard().display()}")
    }
    if (state.lastWon == 0) {
        user.wonTheCards(table)
    } else {
        computer.wonTheCards(table)
    }
    if (user.wonCards() > computer.wonCards() || (user.wonCards() == computer.wonCards() && firstPlayer == 0)) {
        user.addScore(3)
    } else {
        computer.addScore(3)
    }
    cDisplayScore(user, computer)
}

fun cPlay(turn: Int, user: Player, computer: Player, table: Player, state: State): State {
    if (state.gameOn) {
        var won = state.lastWon
        if (table.numberOfCards() == 0) {
            println("\nNo cards on the table")
        } else {
            println("\n${table.numberOfCards()} cards on the table, and the top card is ${table.topCard().display()}")
        }
        if (turn == 0) {
            val userCardIndex = user.ai(table)
            println("Cards in hand: ${user.showCards()}")
            println("Player plays ${user.showCard(userCardIndex).display()}")
            if (table.notEmpty() && (user.showCard(userCardIndex).rank == table.topCard().rank || user.showCard(userCardIndex).suit == table.topCard().suit)) {
                user.putCard(userCardIndex, table)
                user.wonTheCards(table)
                println("Player wins cards")
                cDisplayScore(user, computer)
                won = 0
            } else {
                user.putCard(userCardIndex, table)
            }
            return State(true, won, false)
        } else {
            val cardIndex = computer.ai(table)
            println(computer.showCards())
            println("Computer plays ${computer.showCard(cardIndex).display()}")
            if (table.notEmpty() && (computer.showCard(cardIndex).rank == table.topCard().rank || computer.showCard(cardIndex).suit == table.topCard().suit)) {
                computer.putCard(cardIndex, table)
                computer.wonTheCards(table)
                println("Computer wins cards")
                cDisplayScore(user, computer)
                won = 1
            } else {
                computer.putCard(cardIndex, table)
            }
            return State(true, won, false)
        }
    } else {
        return State(false, state.lastWon, state.exit)
    }
}

fun main() {
    val (firstPlayer, deck, user, computer, table, gameOn) = cInitializeGame()
    var state = State(gameOn, firstPlayer, exit = false)
    while (state.gameOn) {
        state = cPlay(firstPlayer, user, computer, table, state)
        state = cPlay(1 - firstPlayer, user, computer, table, state)
        if (user.outOfCards()) {
            deck.giveCards(6, user)
            state.gameOn = state.gameOn && deck.giveCards(6, computer)
        }
        if (table.numberOfCards() == 52) {
            if (table.numberOfCards() == 0) {
                println("\nNo cards on the table")
            } else {
                println("\n${table.numberOfCards()} cards on the table, and the top card is ${table.topCard().display()}")
            }
        }
    }
    if (!state.exit) {
        cFinalScore(state, firstPlayer, user, computer, table)
    }
    println("Game Over")
}