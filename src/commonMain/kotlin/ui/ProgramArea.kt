package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import gamemodel.*

class ProgramArea(
    cellSize: Double,
    checkpointIds: List<CheckpointId>,
    val playerId: PlayerId,
    val robotId: RobotId,
    private val bitmapCache: BitmapCache,
) : Container() {

    private val borderPadding = 10.0

    private val programingSlotWidth = cellSize
    private val programingSlotHeight = cellSize * 1.5
    private val programingSlotMargin = 10.0

    private val cardWidth = programingSlotWidth / 2
    private val cardHeight = programingSlotHeight / 2
    private val cardMargin = programingSlotMargin / 2

    private val checkpointSize = cardWidth
    private val checkpointMargin = cardMargin

    private val heartSize = checkpointSize
    private val heartMargin = checkpointMargin

    private var cards = listOf<Card>()
    private lateinit var programmingSlots: Map<Int, RoundRect>
    private val selectedCards = arrayOf<Card?>(null, null, null, null, null)

    private lateinit var checkpoints: Map<CheckpointId, Image>
    private lateinit var hearts: List<Image>
    private var lockedSlots = emptySet<Int>()

    init {
        roundRect(650.0, 150.0, 0.0) {
            programmingSlots = (0..4)
                .associateWith {
                    roundRect(programingSlotWidth, programingSlotHeight, 3.0, fill = Colors.LIGHTGRAY)
                }
                .apply {
                    values.alignOnRow(programingSlotMargin) {
                        alignBottomToBottomOf(parent!!, borderPadding)
                        alignLeftToLeftOf(parent!!, borderPadding)
                    }
                }

            val hearts = container {
                hearts = (0..9)
                    .map {
                        image(bitmapCache.heart) {
                            size(heartSize, heartSize)
                        }
                    }
                    .apply {
                        alignOnRow(heartMargin)
                    }
                alignTopToTopOf(parent!!, borderPadding)
                alignRightToRightOf(parent!!, borderPadding)
            }

            container {
                checkpoints = checkpointIds.associateWith {
                    image(bitmapCache.checkpoint) {
                        size(checkpointSize, checkpointSize)
                    }
                }.apply {
                    values.alignOnRow(borderPadding)
                }
                alignTopToBottomOf(hearts, borderPadding)
                alignRightToRightOf(parent!!, borderPadding)
            }
        }
    }

    fun clearCards() {
        cards.forEach {
            it.removeFromParent()
        }
        cards = emptyList()
        selectedCards.fill(null)
    }

    fun dealCards(newCards: List<ActionCard>) {
        clearCards()

        cards = newCards.map { cardModel ->
            card(cardModel, cardWidth, cardHeight, bitmapCache) {
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
        cards.alignOnRow(cardMargin) {
            alignTopToTopOf(parent!!, borderPadding)
            alignLeftToLeftOf(parent!!, borderPadding)
        }
        cards.forEach { it.storeOriginalPos() }
    }

    fun getSelectedCards(): List<ActionCard> =
        selectedCards.mapNotNull { it?.actionCard }

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

    fun setHealth(health: Int) {
        hearts.reversed().withIndex().forEach {(index, image) ->
            image.colorMul = if(health < index + 1) RGBA(0x00, 0x00, 0x00, 0x88) else Colors.WHITE
        }
    }
}

fun <T : View>Collection<T>.alignOnRow(margin: Double = 0.0, alignFirst: T.() -> Unit = {}) = apply {
    if(isEmpty()) return@apply
    first().alignFirst()
    zipWithNext {item0, item1 ->
        item1.alignTopToTopOf(item0)
        item1.alignLeftToRightOf(item0, margin)
    }
}

fun Container.programArea(
    cellSize: Double,
    checkpointIds: List<CheckpointId>,
    playerId: PlayerId,
    robotId: RobotId,
    bmCache: BitmapCache,
    callback: ProgramArea.() -> Unit = {},
) =
    ProgramArea(cellSize, checkpointIds, playerId, robotId, bmCache).addTo(this, callback)

fun <T> Array<T?>.remove(item: T) {
    if (contains(item)) {
        set(indexOf(item), null)
    }
}
