package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import gamemodel.*
import kotlin.math.*

class ProgramArea(
    cellSize: Double,
    checkpointIds: List<CheckpointId>,
    val playerId: PlayerId,
    val robotId: RobotId,
    val bitmapCache: BitmapCache,
) : Container() {

    private val programingSlotWidth = cellSize
    private val programingSlotHeight = cellSize * 1.5
    private val programingSlotPadding = 10.0

    private val cardWidth = programingSlotWidth / 2
    private val cardHeight = programingSlotHeight / 2
    private val cardPadding = programingSlotPadding / 2

    private val checkpointSize = cardWidth
    private val checkpointPadding = cardPadding

    private var cards = listOf<Card>()
    private lateinit var programmingSlots: Map<Int, RoundRect>
    private val selectedCards = arrayOf<Card?>(null, null, null, null, null)

    private lateinit var checkpoints: Map<CheckpointId, Image>
    private var lockedSlots = emptySet<Int>()

    init {
        roundRect(800.0, 200.0, 0.0) {
            programmingSlots = (0..4).associateWith {
                roundRect(programingSlotWidth, programingSlotHeight, 3.0) {
                    alignBottomToBottomOf(parent!!)
                    alignLeftToLeftOf(parent!!, programingSlotPadding)
                    fill = Colors.LIGHTGRAY
                }
            }

            programmingSlots.values
                .windowed(2, 1)
                .forEach { (left, right) ->
                    right.alignLeftToRightOf(left, programingSlotPadding)
                }

            container {
                checkpoints = checkpointIds.associateWith {
                    image(bitmapCache.checkpoint) {
                        size(checkpointSize, checkpointSize)
                        alignLeftToLeftOf(parent!!)
                        alignTopToTopOf(parent!!)
                    }
                }
                checkpoints.values
                    .windowed(2, 1)
                    .forEach { (left, right) ->
                        right.alignLeftToRightOf(left, checkpointPadding)
                    }

                centerOn(parent!!)
                alignTopToTopOf(parent!!, checkpointPadding)
            }
        }
    }

    fun clearCards() {
        cards.forEach {
            it.removeFromParent()
        }
        cards = emptyList()
    }

    fun dealCards(newCards: List<ActionCard>) {
        clearCards()

        cards = newCards.map { cardModel ->
            card(cardModel, cardWidth, cardHeight, bitmapCache) {
                alignTopToTopOf(parent!!, programingSlotPadding)
                alignRightToRightOf(parent!!, programingSlotPadding)

                onDrop {
                    programmingSlots.entries
                        .firstOrNull { (slotIndex, slot) ->
                            collidesWith(slot) && slotIndex !in lockedSlots
                        }
                        ?.let { (slotIndex, slot) ->
                            scale = 2.0
                            centerOn(slot)

                            selectedCards[slotIndex]?.useOriginalPos()
                            selectedCards.remove(this)
                            selectedCards[slotIndex] = this
                        } ?: run {
                        useOriginalPos()
                        selectedCards.remove(this)
                    }
                }
            }
        }

        val (upperRow, lowerRow) = cards.chunked(ceil(cards.size / 2.0).toInt())

        upperRow
            .windowed(2, 1)
            .forEach { (first, second) ->
                second.alignRightToLeftOf(first, cardPadding)
            }

        lowerRow
            .first()
            .apply {
                alignTopToBottomOf(upperRow.first(), cardPadding)
            }
        lowerRow
            .windowed(2, 1)
            .forEach { (first, second) ->
                second.alignRightToLeftOf(first, cardPadding)
                second.alignTopToBottomOf(upperRow.first(), cardPadding)
            }

        cards.forEach { it.storeOriginalPos() }
    }

    fun getSelectedCards(): List<ActionCard> =
        selectedCards
            .map { it?.actionCard }
            .filterNotNull()

    fun markCheckpoint(id: CheckpointId, taken: Boolean = true) {
        val newBitmap = if (taken) bitmapCache.checkpointTaken else bitmapCache.checkpoint
        checkpoints[id]?.bitmap = newBitmap.slice()
    }

    fun lockRegister(registerIndex: Int, card: ActionCard) {
        val slot = programmingSlots.getValue(registerIndex)
        val cardView = cards.first { it.actionCard == card }
        selectedCards.remove(cardView)
        selectedCards[registerIndex] = cardView
        lockedSlots += registerIndex
        cardView.apply {
            draggable = false
            scale(2.0)
            centerOn(slot)
            colorMul = RGBA(0xFF, 0xFF, 0xFF, 0x88)
        }
    }
}

fun Container.programArea(
    cellSize: Double,
    checkpointIds: List<CheckpointId>,
    playerId: PlayerId,
    robotId: RobotId,
    bmCache: BitmapCache,
    callback: @ViewDslMarker() (ProgramArea.() -> Unit) = {},
) =
    ProgramArea(cellSize, checkpointIds, playerId, robotId, bmCache).addTo(this, callback)

fun <T> Array<T?>.remove(item: T) {
    if (contains(item)) {
        set(indexOf(item), null)
    }
}
