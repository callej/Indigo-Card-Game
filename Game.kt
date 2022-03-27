package indigo

private const val SCORE_RANKS = "A 10 J Q K"
private const val RANKS = "A 2 3 4 5 6 7 8 9 10 J Q K"
private const val SUITS = "♦ ♥ ♠ ♣"

data class InitGame(val turn: Int, val d: Deck, val p1: Player, val p2: Player, val table: Player, val go: Boolean)

data class State(var gameOn: Boolean, val lastWon: Int, val exit: Boolean)

data class Card(val rank: String, val suit: String) {
    fun display() = "$rank$suit"
}

class Player {
    private var hand = Deck()
    private var cardsWon = Deck()
    private var score = 0

    fun outOfCards() = hand.noCards()

    fun addCards(cards: List<Card>) = hand.addCards(cards)

    private fun addCard(card: Card) = addCards(listOf(card))

    fun showCards() = hand.showCards()

    fun showCard(pos: Int) = hand.showCard(pos)

    fun putCard(cardPos: Int, receiver: Player) = receiver.addCard(hand.giveCard(cardPos))

    private fun givAllCards(): MutableList<Card> {
        val cards = hand.allCards()
        hand = Deck()
        return cards
    }

    fun notEmpty() = hand.numberOfCardsInDeck() != 0

    fun topCard() = hand.topCard()

    fun numberOfCards() = hand.numberOfCardsInDeck()

    fun listCards() = hand.listCards()

    fun score() = score

    fun addScore(points: Int) {
        score += points
    }

    fun wonCards() = cardsWon.numberOfCardsInDeck()

    fun wonTheCards(table: Player) {
        val cards = table.givAllCards()
        cardsWon.addCards(cards)
        for (card in cards) {
            if (card.rank in SCORE_RANKS.split(" ")) score++
        }
    }

    private fun findCandidates(table: Player): MutableList<Card> {
        val candidates = emptyList<Card>().toMutableList()
        if (table.numberOfCards() > 0) {
            for (card in hand.allCards()) {
                if (card.rank == table.topCard().rank || card.suit == table.topCard().suit) {
                    candidates.add(card)
                }
            }
        }
        return candidates
    }

    fun bestCardToWaste(): Int {
        val suitMap = emptyMap<String, MutableList<Card>>().toMutableMap()
        val rankMap = emptyMap<String, MutableList<Card>>().toMutableMap()
        for (card in hand.allCards()) {
            if (card.suit in suitMap) suitMap[card.suit]!!.add(card) else suitMap[card.suit] = mutableListOf(card)
            if (card.rank in rankMap) rankMap[card.rank]!!.add(card) else rankMap[card.rank] = mutableListOf(card)
        }
        for (suit in suitMap.keys) {
            suitMap[suit]!!.sortBy { it.rank }
        }
        var suitList = emptyList<String>().toMutableList()
        var sNumOfCards = 0
        for (suit in suitMap.keys) {
            if (suitMap[suit]!!.size > sNumOfCards) {
                suitList = mutableListOf(suit)
                sNumOfCards = suitMap[suit]!!.size
            } else if (suitMap[suit]!!.size == sNumOfCards) {
                suitList.add(suit)
            }
        }
        if (sNumOfCards > 1) {
            var bestSuit = ""
            var lowestRank = ""
            for (suit in suitList) {
                if (lowestRank == "") {
                    lowestRank = suitMap[suit]!!.first().rank
                    bestSuit = suit
                } else if (suitMap[suit]!!.first().rank < lowestRank) {
                    lowestRank = suitMap[suit]!!.first().rank
                    bestSuit = suit
                }
            }
            return hand.allCards().indexOf(suitMap[bestSuit]!!.first())
        }
        var bestRank = ""
        var rNumOfCards = 0
        for (rank in rankMap.keys) {
            if (rankMap[rank]!!.size > rNumOfCards || (rankMap[rank]!!.size == rNumOfCards) && rank < bestRank) {
                bestRank = rank
                rNumOfCards = rankMap[rank]!!.size
            }
        }
        return hand.allCards().indexOf(rankMap[bestRank]!!.first())
    }

    private fun bestCatch(candidateCards: MutableList<Card>, table: Player): Int {
        val suitMap = emptyMap<String, MutableList<Card>>().toMutableMap()
        val rankMap = emptyMap<String, MutableList<Card>>().toMutableMap()
        for (card in candidateCards) {
            if (card.suit in suitMap) suitMap[card.suit]!!.add(card) else suitMap[card.suit] = mutableListOf(card)
            if (card.rank in rankMap) rankMap[card.rank]!!.add(card) else rankMap[card.rank] = mutableListOf(card)
        }
        for (suit in suitMap.keys) {
            suitMap[suit]!!.sortBy { it.rank }
        }
        var suitList = emptyList<String>().toMutableList()
        var sNumOfCards = 0
        for (suit in suitMap.keys) {
            if (suitMap[suit]!!.size > sNumOfCards) {
                suitList = mutableListOf(suit)
                sNumOfCards = suitMap[suit]!!.size
            } else if (suitMap[suit]!!.size == sNumOfCards) {
                suitList.add(suit)
            }
        }
        if (sNumOfCards > 1) {
            var bestSuit = ""
            var highestRank = ""
            for (suit in suitList) {
                if (highestRank == "") {
                    highestRank = suitMap[suit]!!.last().rank
                    bestSuit = suit
                } else if (suitMap[suit]!!.last().rank > highestRank) {
                    highestRank = suitMap[suit]!!.last().rank
                    bestSuit = suit
                }
            }
            return hand.allCards().indexOf(suitMap[bestSuit]!!.last())
        }
        var bestRank = ""
        var rNumOfCards = 0
        for (rank in rankMap.keys) {
            if (rankMap[rank]!!.size > rNumOfCards || (rankMap[rank]!!.size == rNumOfCards) && rank > bestRank) {
                bestRank = rank
                rNumOfCards = rankMap[rank]!!.size
            }
        }
        return hand.allCards().indexOf(rankMap[bestRank]!!.first())
    }

    fun ai(table: Player): Int {
        val candidateCards = findCandidates(table)
        when {
            numberOfCards() == 1 -> return 0
            table.numberOfCards() == 0 || candidateCards.isEmpty() -> return bestCardToWaste()
            candidateCards.size == 1 -> return hand.allCards().indexOf(candidateCards[0])
            else -> return bestCatch(candidateCards, table)
        }
    }
}

class Deck(private val command: String = "") {
    private var cardDeck = emptyList<Card>().toMutableList()

    init {
        if (command == "init") {
            reset()
            shuffle()
        }
    }

    private fun reset() {
        cardDeck = emptyList<Card>().toMutableList()
        for (suit in SUITS.split(" ")) {
            for (rank in RANKS.split(" ")) {
                cardDeck.add(Card(rank, suit))
            }
        }
    }

    private fun shuffle() {
        cardDeck.shuffle()
    }

    fun allCards() = cardDeck

    fun topCard() = cardDeck.last()

    fun showCard(pos: Int) = cardDeck[pos]

    fun showCards(): String {
        var cards = ""
        for (card in cardDeck) {
            cards += "${card.display()} "
        }
        return cards.dropLast(1)
    }

    fun noCards() = cardDeck.isEmpty()

    fun addCards(cards: List<Card>) {
        cardDeck.addAll(cards)
    }

    fun giveCards(numOfCards: Int, player: Player): Boolean {
        return if (numOfCards <= cardDeck.size) {
            player.addCards(cardDeck.take(numOfCards))
            cardDeck = cardDeck.drop(numOfCards).toMutableList()
            true
        } else {
            false
        }
    }

    fun giveCard(pos: Int) = cardDeck.removeAt(pos)

    fun numberOfCardsInDeck() = cardDeck.size

    fun listCards(): String {
        var cards = ""
        for (index in cardDeck.indices) {
            cards += "${index + 1})${cardDeck[index].display()} "
        }
        return cards.dropLast(1)
    }
}